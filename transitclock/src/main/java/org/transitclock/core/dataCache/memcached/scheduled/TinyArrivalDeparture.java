package org.transitclock.core.dataCache.memcached.scheduled;


/**
 * minimal struct to serialize Arrival / Departures
 */
public class TinyArrivalDeparture implements java.io.Serializable {
    public long time;
    public long avlTime;
    // trip_id is not stored; serviceId + blockId + tripIndex is
    public int tripIndex;
    public String blockId;
    public boolean isArrival;
    public String stopId;
    public int stopPathIndex;
    public String directionId;
    public String serviceId;

    @Override
    public int hashCode() {
        final int prime = 51;
        int result = prime;
        result += Long.hashCode(time);
        result += Long.hashCode(avlTime);
        result += tripIndex;
        result += Boolean.hashCode(isArrival);
        result += ((blockId == null) ? 0 : blockId.hashCode());
        result += ((stopId == null) ? 0 : stopId.hashCode());
        result += stopPathIndex;
        result += ((directionId == null) ? 0 : directionId.hashCode());
        result += ((serviceId == null) ? 0 : serviceId.hashCode());
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
        if (isArrival != other.isArrival)
            return false;
        if (stopId == null) {
            if (other.stopId != null)
                return false;
        } else if (!stopId.equals(other.stopId))
            return false;
        if (tripIndex != other.tripIndex)
            return false;
        if (blockId == null) {
            if (other.blockId != null)
                return false;
        } else if (!blockId.equals(other.blockId))
            return false;
        if (stopPathIndex != other.stopPathIndex)
            return false;
        if (directionId == null) {
            if (other.directionId != null)
                return false;
        } else if (!directionId.equals(other.directionId))
            return false;
        if (serviceId == null) {
            if (other.serviceId != null)
                return false;
        } else if (!serviceId.equals(other.serviceId))
            return false;

        return true;
    }
}
