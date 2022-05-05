package org.transitclock.integration_tests.prediction;

import junit.framework.TestCase;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.tuple.Triple;
import org.hibernate.Session;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.core.predictiongenerator.PredictionCsvWriter;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Prediction;
import org.transitclock.playback.CombinedPredictionAccuracy;
import org.transitclock.playback.CombinedPredictionAccuracyComparator;
import org.transitclock.playback.CombinedPredictionCsvWriter;
import org.transitclock.playback.PlaybackModule;
import org.transitclock.utils.DateUtils;
import org.transitclock.utils.Time;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.net.URL;
import java.text.ParseException;
import java.util.*;

/**
 * This integration test buids an entirely new transitime DB from GTFS files, prepares the DB for the app to run
 * imports a CSV file of avl test data, waits for predictions to be generated, then checks the output against
 * a csv file of expected prediction values. 
 * 
 * For the test to succeed, prediction quality must improve overall. Up to 5% of individual predictions 
 * (by stop and AVL time of creation) can be worse.
 *
 */
public class PredictionAccuracyIntegrationTest extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(PredictionAccuracyIntegrationTest.class);

    private static final String GTFS = "classpath:gtfs/S2";
	private static final String AVL = "classpath:avl/S2_2113.csv";
    private static final String PREDICTIONS_CSV = "classpath:pred/S2_2113.csv";

	private static final String OUTPUT_DIRECTORY = "/tmp/output";

    Collection<CombinedPredictionAccuracy> combinedPredictionAccuracy;
    
    @Override
    public void setUp() {
    	
    	// Run trace
    	PlaybackModule.runTrace(GTFS, AVL);
    	
    	Map<Triple<Integer, CombinedPredictionAccuracy.ArrivalOrDeparture, Long>, CombinedPredictionAccuracy> predsByStopAndCreationTime
    		= new HashMap<Triple<Integer, CombinedPredictionAccuracy.ArrivalOrDeparture, Long>, CombinedPredictionAccuracy>();
    	
    	// Fill CombinedPredictionAccuracy objects with stop information
    	Session session = HibernateUtils.getSession();
    	List<ArrivalDeparture> ads = session.createCriteria(ArrivalDeparture.class).list();
    	for (ArrivalDeparture ad : ads) {
    		CombinedPredictionAccuracy o = new CombinedPredictionAccuracy(ad);
    		predsByStopAndCreationTime.put(o.getKey(), o);
    	}
    	
    	// Fill old predictions
		try {
			URL predictionsCsv = PredictionAccuracyIntegrationTest.class.getClassLoader()
					.getResource(PREDICTIONS_CSV.substring("classpath:".length()));
			Reader in = new FileReader(predictionsCsv.getFile());
			Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(in);
			
			for (CSVRecord r : records) {
				long prediction = Time.parse(r.get("predictionTime")).getTime();
				Triple<Integer, CombinedPredictionAccuracy.ArrivalOrDeparture, Long> key = createKeyFromCsvRecord(r);
				CombinedPredictionAccuracy pred = getOrCreatePred(predsByStopAndCreationTime, key);
				pred.oldPredTime = prediction;
				pred.oldPrediction = createPredictionFromCsvRecord(r);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Fill new predictions
		List<Prediction> newPreds = session.createCriteria(Prediction.class).list();
		writeOutPredictions(newPreds, generateOutputFileName("prediction", "S2_113"));


		for (Prediction p : newPreds) {
			long prediction = p.getPredictionTime().getTime();
			Triple<Integer, CombinedPredictionAccuracy.ArrivalOrDeparture, Long> key = createKeyFromPrediction(p);
			CombinedPredictionAccuracy pred = getOrCreatePred(predsByStopAndCreationTime, key);
			pred.newPredTime = prediction;
			pred.newPrediction = p;
		}

		combinedPredictionAccuracy = predsByStopAndCreationTime.values();
		ArrayList<CombinedPredictionAccuracy> sortedList = new ArrayList<>(combinedPredictionAccuracy);
		Collections.sort(sortedList, new CombinedPredictionAccuracyComparator());
		writeOutCombinedPredictions(sortedList, generateOutputFileName("combined_prediction", "S2_113"));
		session.close();
    }

	private void writeOutCombinedPredictions(Collection<CombinedPredictionAccuracy> combinedPredictionAccuracy, String fileName) {
		if (combinedPredictionAccuracy == null) {
			logger.error("no combined predictions to write out to disk");
			return;
		}
		CombinedPredictionCsvWriter writer = new CombinedPredictionCsvWriter(fileName, null);
		for (CombinedPredictionAccuracy predictionAccuracy : combinedPredictionAccuracy) {
			writer.write(predictionAccuracy);
		}
		writer.close();
	}

	private String generateOutputFileName(String fileType, String id) {
		File outputDir = new File(OUTPUT_DIRECTORY);
		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}
		return OUTPUT_DIRECTORY + File.separator + fileType + "_" + id + ".csv";
	}

	private void writeOutPredictions(List<Prediction> predictions, String fileName) {
		if (predictions == null) {
			logger.error("no predictions to write out to disk");
			return;
		}
		logger.info("writing {} predictions to {}", predictions.size(), fileName);
		PredictionCsvWriter writer = new PredictionCsvWriter(fileName, null);
		for (Prediction prediction : predictions) {
			writer.write(prediction);
		}
		writer.close();
	}

	@Test
    public void testPredictions() {
    	
    	int oldTotalPreds = 0, newTotalPreds = 0, bothTotalPreds = 0;
    	
    	double oldTotalError = 0, newTotalError = 0;
    	
    	int oldBetter = 0, newBetter = 0;
    	
    	int oldPredsForUnobservedStop = 0, newPredsForUnobservedStop = 0;
    	
    	// For each avltime/stopid/type, check if better or worse, etc
    	for (CombinedPredictionAccuracy pred : combinedPredictionAccuracy) {
    		
    		double oldError = 0, newError = 0;
    		
    		if (pred.oldPredTime > 0) {
    			if (pred.actualADTime > 0) {
    				oldTotalPreds++;
    				oldError = (double) (pred.oldPredTime - pred.actualADTime) / pred.predLength;
    				oldTotalError += oldError;
    			}
    			else
    				oldPredsForUnobservedStop++;
    		}
    		
    		if (pred.newPredTime > 0) {
    			if (pred.actualADTime > 0) {
    				newTotalPreds++;
    				newError = (double) (pred.newPredTime - pred.actualADTime) / pred.predLength;
    				newTotalError += newError;
    			}
    			else
    				newPredsForUnobservedStop++;
    		}
    		
    		if (pred.oldPredTime > 0 && pred.newPredTime > 0 && pred.actualADTime > 0) {
    			bothTotalPreds++;
				logger.info("matched prediction {}, {}, {}, {}",
						pred.stopSeq, pred.actualADTime, pred.oldPredTime, pred.newPredTime);

				if (oldError < newError)
    				oldBetter++;
    			else if (newError < oldError)
    				newBetter++;
    		}
    	}
    	
    	oldTotalError /= oldTotalPreds;
    	newTotalError /= newTotalPreds;
    	
       	logger.info("Old total predictions: {}, old total error: {}, old predictions for unobserved stops: {}",
    			oldTotalPreds, oldTotalError, oldPredsForUnobservedStop);
    	logger.info("New total predictions: {}, new total error: {}, new predictions for unobserved stops: {}",
    			newTotalPreds, newTotalError, newPredsForUnobservedStop);
    	logger.info("Predictions for both: {}, old better: {}, new better: {}",
    			bothTotalPreds, oldBetter, newBetter);
    	
    	// New method is bad if...
    	
    	// there are fewer new predictions than old predictions
    	assertTrue(oldTotalPreds <= newTotalPreds);
    	
    	// total scaled error did not improve
    	assertTrue(newTotalError <= oldTotalError);
    	
    	// old is more accurate in over 5% of cases
    	assertTrue(((double) oldBetter/bothTotalPreds) <= 0.5);
    	
    }
    
    private static Triple<Integer, ArrivalOrDeparture, Long> createKeyFromCsvRecord(CSVRecord r) {
    	try {
	    	int stopSeq = Integer.parseInt(r.get("gtfsStopSeq"));
	    	boolean isArrival = Integer.parseInt(r.get("isArrival")) > 0;
	    	CombinedPredictionAccuracy.ArrivalOrDeparture ad = isArrival ? CombinedPredictionAccuracy.ArrivalOrDeparture.ARRIVAL : CombinedPredictionAccuracy.ArrivalOrDeparture.DEPARTURE;
	    	long avlTime = Time.parse(r.get("avlTime")).getTime();
	    	
	    	return Triple.of(stopSeq, ad, avlTime);
    	}
    	catch(ParseException ex) {
    		logger.error(ex.getMessage());
    		return null;
    	}
    }

	private static Prediction createPredictionFromCsvRecord(CSVRecord r) {
		try {
			long predictionTime = Time.parse(r.get("predictionTime")).getTime();
			long avlTime = Time.parse(r.get("avlTime")).getTime();
			long creationTime= Time.parse(r.get("creationTime")).getTime();
			String vehicleId = r.get("vehicleId");
			String stopId = r.get("stopId");
			String tripId = r.get("tripId");
			String routeId = r.get("routeId");
			boolean affectedByWaitStop = Integer.parseInt(r.get("affectedByWaitStop")) > 0;
			boolean isArrival = Integer.parseInt(r.get("isArrival")) > 0;
			boolean schedBasedPred = Integer.parseInt(r.get("schedBasedPred")) > 0;
			int stopSeq = Integer.parseInt(r.get("gtfsStopSeq"));
			return new Prediction(predictionTime, avlTime, creationTime, vehicleId,
					stopId, tripId, routeId, affectedByWaitStop, isArrival, schedBasedPred,
					stopSeq);
		} catch (ParseException ex) {
			logger.error(ex.getMessage());
			return null;
		}

	}
    
    private static Triple<Integer, CombinedPredictionAccuracy.ArrivalOrDeparture, Long> createKeyFromPrediction(Prediction p) {
    	return Triple.of(p.getGtfsStopSeq(), 
    			p.isArrival() ? CombinedPredictionAccuracy.ArrivalOrDeparture.ARRIVAL : CombinedPredictionAccuracy.ArrivalOrDeparture.DEPARTURE,
    			p.getAvlTime().getTime());
    }


	private CombinedPredictionAccuracy getOrCreatePred(
    		Map<Triple<Integer, CombinedPredictionAccuracy.ArrivalOrDeparture, Long>, CombinedPredictionAccuracy>
    			predsByStopAndCreationTime,
        	Triple<Integer, CombinedPredictionAccuracy.ArrivalOrDeparture, Long> key) {
    	CombinedPredictionAccuracy pred = predsByStopAndCreationTime.get(key);
		if (pred == null) {
			// This prediction does not have an associated arrival departure. Cannot gauge accuracy.
			pred = new CombinedPredictionAccuracy(key.getLeft(), key.getMiddle(), key.getRight());
			predsByStopAndCreationTime.put(key, pred);
		}
		return pred;
    }
}
