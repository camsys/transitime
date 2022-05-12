package org.transitclock.integration_tests.playback;

import org.apache.commons.lang3.tuple.Triple;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Prediction;
import org.transitclock.utils.DateRange;

import java.util.*;

/**
 * Delegate data loading operations to its own class.
 */
public class ReplayLoader {

    private static final Logger logger = LoggerFactory.getLogger(ReplayLoader.class);
    private Session session;


    private ReplayCsv csv;

    private Collection<CombinedPredictionAccuracy> combinedPredictionAccuracy;
    public Collection<CombinedPredictionAccuracy> getCombinedPredictionAccuracy() {
        return combinedPredictionAccuracy;
    }

    private Map<Triple<Integer, CombinedPredictionAccuracy.ArrivalOrDeparture, Long>, CombinedPredictionAccuracy> predsByStopAndCreationTime
            = new HashMap<Triple<Integer, CombinedPredictionAccuracy.ArrivalOrDeparture, Long>, CombinedPredictionAccuracy>();


    public ReplayLoader(String outputDirectory) {
        this.csv = new ReplayCsv(outputDirectory);
    }


    public void createCombinedPredictionAccuracyStructure(DateRange avlRange, String arrivalDepartureFileName) {
        // Fill CombinedPredictionAccuracy objects with stop information
        waitForQueuesToDrain();
        logger.info("loading A/Ds for {}", avlRange);
        List<ArrivalDeparture> ads = getSession()
                .createCriteria(ArrivalDeparture.class)
                .add(Restrictions.between("time", avlRange.getStart(), avlRange.getEnd()))
                .addOrder(Order.asc("time"))
                .list();
        boolean found = false;
        for (ArrivalDeparture ad : ads) {
            found = true;
            CombinedPredictionAccuracy o = new CombinedPredictionAccuracy(ad);
            logger.info("placeholder for {} ({}) and stopId={}",
                    o.getKey(), new Date(o.getKey().getRight()), ad.getStopId());
            predsByStopAndCreationTime.put(o.getKey(), o);
        }
        if (!found) {
            if (arrivalDepartureFileName != null)
                throw new RuntimeException("no ArrivalDepartures found, cannot prime data store");
        }
    }

    private void waitForQueuesToDrain() {
        final int MAX_COUNT = 20;
        try {
            int i = 0;
            while (Core.getInstance().getDbLogger().queueSize() > 0 && i < MAX_COUNT) {
                i++;
                logger.info("waiting on queues to drain with remaining size {}",
                        Core.getInstance().getDbLogger().queueSize());
                Thread.sleep(1 * 1000);
            }
            if (i >= MAX_COUNT) {
                logger.warn("DbLogger did not empty in allotted time.");
            }
        } catch (InterruptedException ie) {
            return;
        }
    }

    public void loadPredictionsFromCSV(String predictionsCsvFileName) {

        List<Prediction> predictions = csv.loadPredictions(predictionsCsvFileName);

        for (Prediction p : predictions) {
            // Fill old predictions
            Triple<Integer, CombinedPredictionAccuracy.ArrivalOrDeparture, Long> key = createKeyFromPrediction(p);
            CombinedPredictionAccuracy pred = getOrCreatePred(predsByStopAndCreationTime, key);
            pred.setOldPrediction(p);
        }

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

    public void accumulate(String id) {
        List<Prediction> newPreds = getSession().createCriteria(Prediction.class).list();
        // TODO these are hard coded, need to fix ID
        csv.write(newPreds,"prediction", id);


        for (Prediction p : newPreds) {
            long prediction = p.getPredictionTime().getTime();
            Triple<Integer, CombinedPredictionAccuracy.ArrivalOrDeparture, Long> key = createKeyFromPrediction(p);
            CombinedPredictionAccuracy pred = getOrCreatePred(predsByStopAndCreationTime, key);
            pred.setNewPrediction(p);
        }

        combinedPredictionAccuracy = predsByStopAndCreationTime.values();
        ArrayList<CombinedPredictionAccuracy> sortedList = new ArrayList<>(combinedPredictionAccuracy);
        Collections.sort(sortedList, new CombinedPredictionAccuracyComparator());
        csv.write(sortedList, "combined_prediction", id);

        getSession().close();

    }


    private static Triple<Integer, CombinedPredictionAccuracy.ArrivalOrDeparture, Long> createKeyFromPrediction(Prediction p) {
        return Triple.of(p.getGtfsStopSeq(),
                p.isArrival() ? CombinedPredictionAccuracy.ArrivalOrDeparture.ARRIVAL : CombinedPredictionAccuracy.ArrivalOrDeparture.DEPARTURE,
                p.getAvlTime().getTime());
    }

    // lazy load the session so config can happen first
    private Session getSession() {
        if (session == null) {
            session = HibernateUtils.getSession();
        }
        return session;
    }

}
