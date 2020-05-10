package org.transitclock.core.dataCache;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.transitclock.core.dataCache.memcached.scheduled.TinyArrivalDeparture;
import org.transitclock.ipc.data.IpcArrivalDeparture;
public class TripEvents implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -510989387398784935L;
	
	
	public List <TinyArrivalDeparture> events = null;

	public List<IpcArrivalDeparture> getEvents() {
		return toIPC(events);
	}

	private List<IpcArrivalDeparture> toIPC(List<TinyArrivalDeparture> events) {
		return null;
	}

	public void setEvents(List<IpcArrivalDeparture> events) {
		this.events = tiny(events);
		Collections.sort(this.events, new TinyArrivalDepartureComparator());
	}

	private List<TinyArrivalDeparture> tiny(List<IpcArrivalDeparture> events) {
		List<TinyArrivalDeparture> tinyList = new ArrayList<>();
		for (IpcArrivalDeparture iad : events) {
			TinyArrivalDeparture tad = toTiny(iad);
			tinyList.add(tad);
		}
		return tinyList;
	}

	private TinyArrivalDeparture toTiny(IpcArrivalDeparture iad) {
		TinyArrivalDeparture tad = new TinyArrivalDeparture();
		return tad;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((events == null) ? 0 : events.hashCode());
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
		TripEvents other = (TripEvents) obj;
		if (events == null) {
			if (other.events != null)
				return false;
		} else if (!events.equals(other.events))
			return false;
		return true;
	}

	public TripEvents() {
		super();		
	}

	public TripEvents(List<IpcArrivalDeparture> events) {
		super();
		this.events = tiny(events);
		Collections.sort(this.events, new TinyArrivalDepartureComparator());
	}
	
	public void addEvent(IpcArrivalDeparture event)
	{
		if(this.events==null)
		{
			events=new ArrayList<TinyArrivalDeparture>();
		}
		events.add(toTiny(event));
		Collections.sort(this.events, new TinyArrivalDepartureComparator());
	}

}
