package org.transitclock.core.dataCache;

import org.transitclock.core.Indices;
/**
 * @author Sean Óg Crudden
 * 
 */
public class StopPathCacheKey implements java.io.Serializable {
		
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9119654046491298859L;
	private String tripId;
	private int stopPathIndex;
	
	/* this is only set for frequency based trips otherwise null. This is seconds from midnight */
	private Integer startTimeFromMidnight = null  ;
	
	private boolean travelTime=true;
	
	public boolean isTravelTime() {
		return travelTime;
	}

	public StopPathCacheKey(String tripId, Integer stopPathIndex)
	{
		super();
		
		this.tripId = tripId;
		this.stopPathIndex = stopPathIndex;	
		this.travelTime=true;
		this.startTimeFromMidnight =null;
	}
	
	public StopPathCacheKey(String tripId, Integer stopPathIndex, boolean travelTime) {
		super();
		
		this.tripId = tripId;
		this.stopPathIndex = stopPathIndex;	
		this.travelTime=travelTime;
		this.startTimeFromMidnight = null;
	}
	public StopPathCacheKey(String tripId, Integer stopPathIndex, boolean travelTime, Integer startTime) {
		super();
		
		this.tripId = tripId;
		this.stopPathIndex = stopPathIndex;	
		this.travelTime=travelTime;
		this.startTimeFromMidnight = startTime;
	}
	
	public StopPathCacheKey(StopPathCacheKey key) {
		
		this.stopPathIndex=new Integer(key.getStopPathIndex());
				
		this.tripId=new String(key.getTripId());
		
		this.travelTime=key.travelTime;	
						
		this.startTimeFromMidnight = key.getStartTime();
	}
	

	public StopPathCacheKey(Indices indices) {
		super();
				
		this.stopPathIndex=indices.getStopPathIndex();
		
		int tripIndex = indices.getTripIndex();
		
		this.tripId=indices.getBlock().getTrip(tripIndex).getId();
		
		this.travelTime=true;		
	}

	public String getTripId() {
		return tripId;
	}

	public Integer getStartTime() {
		return startTimeFromMidnight;
	}

	public void setTripId(String tripId) {
		this.tripId = tripId;
	}


	public Integer getStopPathIndex() {
		return stopPathIndex;
	}


	public void setStopPathIndex(Integer stopPathIndex) {
		this.stopPathIndex = stopPathIndex;
	}

	@Override
	public String toString() {
		return "StopPathCacheKey [tripId=" + tripId + ", stopPathIndex=" + stopPathIndex + ", startTime=" + startTimeFromMidnight
				+ ", travelTime=" + travelTime + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((startTimeFromMidnight == null) ? 0 : startTimeFromMidnight.hashCode());
		result = prime * result + stopPathIndex;
		result = prime * result + (travelTime ? 1231 : 1237);
		result = prime * result + ((tripId == null) ? 0 : tripId.hashCode());
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
		StopPathCacheKey other = (StopPathCacheKey) obj;
		if (startTimeFromMidnight == null) {
			if (other.startTimeFromMidnight != null)
				return false;
		} else if (!startTimeFromMidnight.equals(other.startTimeFromMidnight))
			return false;
		if (stopPathIndex != ((StopPathCacheKey) obj).stopPathIndex) {
			return false;
		}
		if (travelTime != other.travelTime)
			return false;
		if (tripId == null) {
			if (other.tripId != null)
				return false;
		} else if (!tripId.equals(other.tripId))
			return false;
		return true;
	}


}




