package org.transitclock.playback;

import org.apache.commons.lang3.tuple.Triple;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Prediction;

/**
 * CombinedPredictionAccuracy: keep track of stop, old prediction, new prediction.
 * Arrival/Departure Key: key by gtfsStopSeq & whether arrival or departure
 */
public class CombinedPredictionAccuracy {
    public enum ArrivalOrDeparture {ARRIVAL, DEPARTURE};

    public int stopSeq;
    public ArrivalOrDeparture which;
    public long avlTime;

    public long actualADTime = -1;
    public long predLength = -1; // actualADTime - avlTime

    public long oldPredTime = -1;
    public Prediction oldPrediction = null;
    public long newPredTime = -1;
    public Prediction newPrediction = null;

    public CombinedPredictionAccuracy(int stopSeq, ArrivalOrDeparture which, long avlTime) {
        this.stopSeq = stopSeq;
        this.which = which;
        this.avlTime = avlTime;
    }

    public CombinedPredictionAccuracy(ArrivalDeparture ad) {
        this(ad.getGtfsStopSequence(),
                ad.isArrival() ? ArrivalOrDeparture.ARRIVAL : ArrivalOrDeparture.DEPARTURE,
                ad.getAvlTime().getTime());
        this.actualADTime = ad.getTime();
        this.predLength = actualADTime - avlTime;
    }

    public Triple<Integer, ArrivalOrDeparture, Long> getKey() {
        return Triple.of(stopSeq, which, avlTime);
    }
}
