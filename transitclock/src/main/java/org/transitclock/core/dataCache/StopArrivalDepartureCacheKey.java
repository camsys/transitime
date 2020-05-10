package org.transitclock.core.dataCache;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
/**
 * @author Sean Og Crudden
 * 
 */
public class StopArrivalDepartureCacheKey implements Serializable {
	
	
	private static final long serialVersionUID = 2466653739981305006L;
	private String stopid;
	private int dayOfYear;
	public StopArrivalDepartureCacheKey(String stopid, Date date) {
		super();
		setDate(date);
		this.stopid=stopid;
	}
	public String getStopid() {
		return stopid;
	}

	public Date getDate() {
		return toDate(dayOfYear);
	}
	public void setDate(Date date) {
		this.dayOfYear = getDayOfYear(date);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dayOfYear;
		result = prime * result + ((stopid == null) ? 0 : stopid.hashCode());
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
		StopArrivalDepartureCacheKey other = (StopArrivalDepartureCacheKey) obj;
		if (((StopArrivalDepartureCacheKey) obj).dayOfYear != dayOfYear)
			return false;
		if (stopid == null) {
			if (other.stopid != null)
				return false;
		} else if (!stopid.equals(other.stopid))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "StopArrivalDepartureCacheKey [stopid=" + stopid + ", dayOfYear=" + dayOfYear + "]";
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
