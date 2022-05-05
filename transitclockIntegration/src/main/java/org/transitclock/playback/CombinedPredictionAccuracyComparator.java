package org.transitclock.playback;

import java.util.Comparator;

/**
 * sort combined predictions in order.
 */
public class CombinedPredictionAccuracyComparator implements Comparator<CombinedPredictionAccuracy> {

    @Override
    public int compare(CombinedPredictionAccuracy o1, CombinedPredictionAccuracy o2) {
        int diff = compare(o1.avlTime, o2.avlTime);
        if (diff == 0)
            return compare(o1.stopSeq, o2.stopSeq);
        return diff;
    }

    private int compare(long a1, long a2) {
        if (a1 > a2) return 1;
        if (a1 < a2) return -1;
        return 0;
    }
    private int compare(int a1, int a2) {
        if (a1 > a2) return 1;
        if (a1 < a2) return -1;
        return 0;
    }
}
