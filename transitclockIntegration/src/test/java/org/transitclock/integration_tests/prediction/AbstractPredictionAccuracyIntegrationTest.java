package org.transitclock.integration_tests.prediction;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.integration_tests.playback.ReplayResults;
import org.transitclock.integration_tests.playback.ReplayService;

/**
 * This integration test builds an entirely new transitime DB from GTFS files, prepares the DB for the app to run
 * imports a CSV file of avl test data, waits for predictions to be generated, then checks the output against
 * a csv file of expected prediction values. 
 * 
 * For the test to succeed, prediction quality must improve overall. Up to 5% of individual predictions 
 * (by stop and AVL time of creation) can be worse.
 *
 */
public abstract class AbstractPredictionAccuracyIntegrationTest extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(AbstractPredictionAccuracyIntegrationTest.class);


    private ReplayService rs;
	private String id, outputDirectory, gtfs, avl, predictionCsv;
	public AbstractPredictionAccuracyIntegrationTest(String id, String outputDirectory, String gtfs, String avl, String predictionsCsv) {
		this.id = id;
		this.outputDirectory = outputDirectory;
		this.gtfs = gtfs;
		this.avl = avl;
		this.predictionCsv = predictionsCsv;
	}

	@Override
    public void setUp() {
		rs = new ReplayService(id, outputDirectory);
		rs.run(gtfs, avl);

		rs.loadPastPredictions(predictionCsv);
		rs.accumulate();

    }

    public void testPredictions() {
    	ReplayResults results = rs.compare();
		// New method is bad if...

		// there are fewer new predictions than old predictions
		assertTrue(results.getOldTotalPreds() <= results.getNewTotalPreds());

		// total scaled error did not improve
		assertTrue(results.getNewTotalError() <= results.getOldTotalError());

		// old is more accurate in over 5% of cases
		assertTrue(((double) results.getOldBetter()/results.getBothTotalPreds()) <= 0.5);

	}

}
