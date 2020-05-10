package org.transitclock.core.dataCache.memcached.scheduled;


/**
 * minimal struct to serialize Arrival / Departures
 */
public class TinyArrivalDeparture implements java.io.Serializable {
    public long time;
    public long avlTime;
    public String tripIdIntern;
    public boolean isArrival;
    public String stopId;
    public int stopPathIndex;
    public String directionId;

    @Override
    public int hashCode() {
        final int prime = 51;
        int result = prime;
        result += Long.hashCode(time);
        result += Long.hashCode(avlTime);
        result += ((tripIdIntern == null) ? 0 : tripIdIntern.hashCode());
        result += Boolean.hashCode(isArrival);
        result += ((stopId == null) ? 0 : stopId.hashCode());
        result += stopPathIndex;
        result += ((directionId == null) ? 0 : directionId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TinyArrivalDeparture other = (TinyArrivalDeparture) obj;
        if (time != other.time)
            return false;
        if (avlTime != other.avlTime)
            return false;
        if (tripIdIntern == null) {
            if (other.tripIdIntern != null)
                return false;
        } else if (!tripIdIntern.equals(other.tripIdIntern))
            return false;
        if (isArrival != other.isArrival)
            return false;
        if (stopId == null) {
            if (other.stopId != null)
                return false;
        } else if (!stopId.equals(other.stopId))
            return false;
        if (stopPathIndex != other.stopPathIndex)
            return false;
        if (directionId == null) {
            if (other.directionId != null)
                return false;
        } else if (!directionId.equals(other.directionId))
            return false;

        return true;
    }
}
