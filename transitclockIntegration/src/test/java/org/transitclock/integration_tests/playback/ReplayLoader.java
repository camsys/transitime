package org.transitclock.integration_tests.playback;

import org.apache.commons.collections.comparators.ComparatorChain;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.transitclock.utils.Time.sleep;

/**
 * Delegate data loading operations to its own class.
 */
public class ReplayLoader {

    private static final Logger logger = LoggerFactory.getLogger(ReplayLoader.class);
    private Session session;

    private ReplayCsv csv;

    private Collection<CombinedPredictionAccuracy> combinedPredictionAccuracies;

    public Collection<CombinedPredictionAccuracy> getCombinedPredictionAccuracies() {
        return combinedPredictionAccuracies;
    }

    private Map<PredictionKey, CombinedPredictionAccuracy> predsByStopAndAvlTime
            = new HashMap<PredictionKey, CombinedPredictionAccuracy>();

    private TreeMap<Long, PredictionBucketAccuracy> predictionBucketAccuracies;

    private static final long PREDICTION_BUCKET_MAX_HORIZON = 1800000;

    public ReplayLoader(String outputDirectory) {
        this.csv = new ReplayCsv(outputDirectory);
        setup();
    }

    private void setup() {
        setupPredictionBucketAccuracies();
    }

    private void setupPredictionBucketAccuracies(){
        predictionBucketAccuracies = new TreeMap<>();
        PredictionBucketAccuracy bucket01 = new PredictionBucketAccuracy("0-3 minutes", 0, 180000, -1 * TimeUnit.SECONDS.toMillis(30), TimeUnit.SECONDS.toMillis(60));
        PredictionBucketAccuracy bucket02 = new PredictionBucketAccuracy("3-6 minutes", 180001, 360000, -1 * TimeUnit.SECONDS.toMillis(60), TimeUnit.SECONDS.toMillis(180));
        PredictionBucketAccuracy bucket03 = new PredictionBucketAccuracy("6-12 minutes", 360001, 720000, -1 * TimeUnit.SECONDS.toMillis(60), TimeUnit.SECONDS.toMillis(240));
        PredictionBucketAccuracy bucket04 = new PredictionBucketAccuracy("12-20 minutes", 720001, 1200000, -1 * TimeUnit.SECONDS.toMillis(60), TimeUnit.SECONDS.toMillis(360));
        PredictionBucketAccuracy bucket05 = new PredictionBucketAccuracy("20-30 minutes",1200001, 1800000, -1 * TimeUnit.SECONDS.toMillis(60), TimeUnit.SECONDS.toMillis(360));

        predictionBucketAccuracies.put(bucket01.getHorizonStart(), bucket01);
        predictionBucketAccuracies.put(bucket02.getHorizonStart(), bucket02);
        predictionBucketAccuracies.put(bucket03.getHorizonStart(), bucket03);
        predictionBucketAccuracies.put(bucket04.getHorizonStart(), bucket04);
        predictionBucketAccuracies.put(bucket05.getHorizonStart(), bucket05);
    }


    public List<ArrivalDeparture> queryArrivalDepartures(DateRange avlRange, String arrivalDepartureFileName) {
        // Fill CombinedPredictionAccuracy objects with stop information
        waitForQueuesToDrain();
        logger.info("loading A/Ds for {}", avlRange);
        List<ArrivalDeparture> ads = getSession()
                .createCriteria(ArrivalDeparture.class)
                .add(Restrictions.between("time", avlRange.getStart(), avlRange.getEnd()))
                .addOrder(Order.asc("time"))
                .list();

        if (ads == null || ads.isEmpty())
            throw new RuntimeException("no ArrivalDepartures found, cannot prime data store");

        return ads;
    }

    private void waitForQueuesToDrain() {
        final int MAX_COUNT = 20;
        sleep(5000);
        int i = 0;
        while (Core.getInstance().getDbLogger().queueSize() > 0 && i < MAX_COUNT) {
            i++;
            logger.info("waiting on queues to drain with remaining size {}",
                    Core.getInstance().getDbLogger().queueSize());
            sleep(1000);
        }
        if (i >= MAX_COUNT) {
            logger.warn("DbLogger did not empty in allotted time.");
        }
    }

    public void loadPredictionsFromCSV(String predictionsCsvFileName) {

        List<Prediction> predictions = csv.loadPredictions(predictionsCsvFileName);
        for (Prediction p : predictions) {
            // Fill old predictions
            PredictionKey key = createKeyFromPrediction(p);
            CombinedPredictionAccuracy pred = getOrCreatePred(predsByStopAndAvlTime, key);
            pred.setOldPrediction(p);
        }

    }

    private CombinedPredictionAccuracy getOrCreatePred(
                    Map<PredictionKey, CombinedPredictionAccuracy>
                    predsByStopAndCreationTime,
                    PredictionKey key) {
        CombinedPredictionAccuracy pred = predsByStopAndCreationTime.get(key);
        if (pred == null) {
            // This prediction does not have an associated arrival departure. Cannot gauge accuracy.
            pred = new CombinedPredictionAccuracy(key.getTripId(), key.getStopSequence(),
                    key.getArrivalOrDeparture(), key.getAvlTime());
            predsByStopAndCreationTime.put(key, pred);
        }
        return pred;
    }

    public List<String> accumulate(String id, List<ArrivalDeparture> arrivalDepartures) {
        ArrayList<String> ouptputfilenames = new ArrayList<>();
        if (arrivalDepartures == null) {
            logger.info("accumulating error, no A/Ds");
        } else {
            logger.info("accumulating with {} A/Ds", arrivalDepartures.size());
        }
        List<Prediction> newPreds = getSession().createCriteria(Prediction.class).list();
        ouptputfilenames.add(csv.write(newPreds,"prediction", id));

        for (Prediction p : newPreds) {
            PredictionKey key = createKeyFromPrediction(p);
            CombinedPredictionAccuracy pred = getOrCreatePred(predsByStopAndAvlTime, key);
            pred.setNewPrediction(p);
        }

        combinedPredictionAccuracies = predsByStopAndAvlTime.values();

        // match the A/Ds to the predictions for accuracy comparison
        for (CombinedPredictionAccuracy combined : combinedPredictionAccuracies) {
            for (ArrivalDeparture ad : arrivalDepartures) {
                // match on trip / stop / direction
                if (match(combined, ad)) {
                    combined.actualADTime = ad.getTime();
                    combined.predLength = combined.actualADTime - combined.avlTime;

                    // Prediction Bucket Accuracy Report
                    if(combined.predLength >= 0 && combined.predLength <= PREDICTION_BUCKET_MAX_HORIZON){
                        Long horizonKey = predictionBucketAccuracies.floorKey(combined.predLength);
                        PredictionBucketAccuracy bucket = predictionBucketAccuracies.get(horizonKey);
                        bucket.processPrediction(combined);
                    }
                }
            }
        }

        ArrayList<CombinedPredictionAccuracy> sortedList = filter(combinedPredictionAccuracies);

        ComparatorChain chain = new ComparatorChain();

        chain.addComparator(new CombinedPredictionAccuracyTripIdComparator());
        chain.addComparator(new CombinedPredictionAccuracyAvlTimeComparator());
        chain.addComparator(new CombinedPredictionAccuracyStopSequenceComparator());

        Collections.sort(sortedList, chain);
        logger.info("writing {} preds to combined_prediction.csv", sortedList.size());
        ouptputfilenames.add(csv.write(sortedList, "combined_prediction", id));


        List<PredictionBucketAccuracy> sortedPredictionBucketAccuracy =
                predictionBucketAccuracies.values().stream().sorted().collect(Collectors.toList());
        logger.info("writing {} bucket predictions to bucket_prediction.csv", sortedPredictionBucketAccuracy.size());
        ouptputfilenames.add(csv.writeBucket(sortedPredictionBucketAccuracy, "bucket_prediction", id));

        getSession().close();
        return ouptputfilenames;
    }

    private boolean match(CombinedPredictionAccuracy combined, ArrivalDeparture ad) {
        // match on trip / stop / direction
        if (combined.tripId.equals(ad.getTripId())
        && combined.stopSeq == ad.getGtfsStopSequence()
        && combined.which.equals(CombinedPredictionAccuracy.ArrivalOrDeparture.ARRIVAL) == ad.isArrival()) {
            return true;
        }
        return false;
    }

    // remove nonsensical accuracy objects
    private ArrayList<CombinedPredictionAccuracy> filter(Collection<CombinedPredictionAccuracy> combinedPredictionAccuracy) {
        ArrayList<CombinedPredictionAccuracy> filtered = new ArrayList<>();
        for (CombinedPredictionAccuracy c : combinedPredictionAccuracy) {
            if (c.oldPredTime > -1 || c.newPredTime > -1)
                filtered.add(c);
            if (c.oldPredTime == -1 && c.newPredTime == -1) {
                logger.error("unmatched A/D {}", c);
            }
        }
        return filtered;
    }


    private static PredictionKey createKeyFromPrediction(Prediction p) {
        return new PredictionKey(p.getTripId(), p.getGtfsStopSeq(),
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
