package org.transitclock.core.dataCache;

import org.transitclock.core.dataCache.memcached.scheduled.TinyArrivalDeparture;

import java.util.Comparator;

public class TinyArrivalDepartureComparator implements Comparator<TinyArrivalDeparture> {

    @Override
    public int compare(TinyArrivalDeparture ad1, TinyArrivalDeparture ad2) {
        if (ad1.time < ad2.time)
            return 1;
        else if (ad1.time > ad2.time)
            return -1;
        return 0;
    }
}
