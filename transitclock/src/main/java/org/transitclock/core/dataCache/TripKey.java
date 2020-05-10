package org.transitclock.core.dataCache;

import java.util.Calendar;
import java.util.Date;
/**
 * @author Sean Og Crudden
 * 
 */
public class TripKey implements java.io.Serializable {
	/**
	 * Needs to be serializable to add to cache
	 */
	private static final long serialVersionUID = 5029823633051153716L;
	private String tripId;
	
	private int dayOfYear;
	private Integer startTime;

	
	/**
	 * @return the tripId
	 */
	public String getTripId() {
		return tripId;
	}
	
	/**
	 * @return the tripStartDate
	 */
	public Date getTripStartDate() {
		return toDate(dayOfYear);
	}
	/**
	 * @return the startTime
	 */
	public Integer getStartTime() {
		return startTime;
	}
	public TripKey(String tripId,  Date tripStartDate,
			Integer startTime) {
		super();
		this.tripId = tripId;	
		
		this.dayOfYear = getDayOfYear(tripStartDate);
		this.startTime = startTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
		result = prime * result + ((tripId == null) ? 0 : tripId.hashCode());
		result = prime * result + dayOfYear;
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
		TripKey other = (TripKey) obj;
		if (startTime == null) {
			if (other.startTime != null)
				return false;
		} else if (!startTime.equals(other.startTime))
			return false;
		if (tripId == null) {
			if (other.tripId != null)
				return false;
		} else if (!tripId.equals(other.tripId))
			return false;
		if (dayOfYear != other.dayOfYear) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "TripKey [tripId=" + tripId + ", dayOfYear=" + dayOfYear + ", startTime=" + startTime + "]";
	}

	public void setStartTime(Integer time) {
		// TODO Auto-generated method stub
		this.startTime=time;
		
	}

	private int getDayOfYear(Date epochDate) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(epochDate.getTime());
		return calendar.get(Calendar.DAY_OF_YEAR);
	}

	private Date toDate(int dayOfYear) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_YEAR, dayOfYear);
		return calendar.getTime();
	}

}
