/* 
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Transitime.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */
package org.transitclock.db.structs;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.hibernate.*;
import org.hibernate.annotations.*;
import org.hibernate.classic.Lifecycle;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.configData.AgencyConfig;
import org.transitclock.configData.DbSetupConfig;
import org.transitclock.core.ServiceType;
import org.transitclock.core.TemporalDifference;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.query.ArrivalDepartureQuery;
import org.transitclock.ipc.interfaces.ArrivalDepartureSpeed;
import org.transitclock.logging.Markers;
import org.transitclock.utils.Geo;
import org.transitclock.utils.IntervalTimer;
import org.transitclock.utils.Time;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * For persisting an Arrival or a Departure time. Should use Arrival or
 * Departure subclasses.
 * <p>
 * Implements Lifecycle so that can have the onLoad() callback be called when
 * reading in data so that can intern() member strings. In order to do this the
 * String members could not be declared as final since they are updated after
 * the constructor is called. By interning the member strings less than half
 * (about 40%) of the RAM is used. This is very important when reading in
 * large batches of ArrivalDeparture objects!
 * 
 * @author SkiBu Smith
 */

@Entity 
@DynamicUpdate
@Table(name="ArrivalsDepartures",
       indexes = { @Index(name="ArrivalsDeparturesTimeIndex", 
                      columnList="time" ),
                   @Index(name="ArrivalsDeparturesRouteTimeIndex", 
                      columnList="routeShortName, time" ),
			       @Index(name="ArrivalsDeparturesTripPatternIdIndex",
					   columnList="tripPatternId" ),
			       @Index(name="ArrivalsDeparturesScheduledTimeIndex",
					   columnList="scheduledTime" ),
				   @Index(name="ArrivalsDeparturesTimePointIndex",
					   columnList="scheduleAdherenceStop" )} )
public class ArrivalDeparture implements Lifecycle, Serializable, ArrivalDepartureSpeed {
	
	@Id 
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private String vehicleId;
	
	// Originally did not use msec precision (datetime(3)) specification
	// because arrival/departure times are only estimates and having such
	// precision is not generally appropriate. But found that then some
	// arrival and departures for a stop would have the same time and when
	// one would query for the arrivals/departures and order by time one
	// could get a departure before an arrival. To avoid this kind of
	// incorrect ordering using the additional precision. And this way
	// don't have to add an entire second to a departure time to make 
	// sure that it is after the arrival. Adding a second is an 
	// exaggeration because it implies the vehicle was stopped for a second
	// when most likely it zoomed by the stop. It looks better to add
	// only a msec to make the departure after the arrival.
	@Id 
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private final Date time;

	@Id 
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private String stopId;
	
	// From the GTFS stop_times.txt file for the trip. The gtfsStopSeq can
	// be different from stopPathIndex. The stopIndex is included here so that
	// it is easy to find the corresponding stop in the stop_times.txt file.
	// It needs to be part of the @Id because can have loops for a route
	// such that a stop is served twice on a trip. Otherwise would get a
	// constraint violation.
	@Id
	@Column
	private final int gtfsStopSeq;
	
	@Id 
	@Column
	private final boolean isArrival;

	@Id 
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private String tripId;
	
	// The revision of the configuration data that was being used
	@Column 
	final int configRev;
	
	// So can match the ArrivalDeparture time to the AvlReport that
	// generated it by using vehicleId and avlTime.
	@Column	
	@Temporal(TemporalType.TIMESTAMP)
	private final Date avlTime;
	
	// The schedule time will only be set if the schedule info was available
	// from the GTFS data and it is the proper type of arrival or departure 
	// stop (there is an arrival schedule time and this is the last stop for
	// a trip and and this is an arrival time OR there is a departure schedule
	// time and this is not the last stop for a trip and this is a departure 
	// time. Otherwise will be null.
	@Column	
	@Temporal(TemporalType.TIMESTAMP)
	private final Date scheduledTime;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private String blockId;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private String routeId;
	
	// routeShortName is included because for some agencies the
	// route_id changes when there are schedule updates. But the
	// routeShortName is more likely to stay consistent. Therefore
	// it is better for when querying for arrival/departure data
	// over a timespan.
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private String routeShortName;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private String serviceId;
		
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private String directionId;
	
	// The index of which trip this is within the block.
	@Column 
	private final int tripIndex;
	
	/* this is required for frequenecy based services */
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private final Date freqStartTime;
	
	// The index of which stop path this is within the trip.
	// Different from the GTFS gtfsStopSeq. The stopPathIndex starts
	// at 0 and increments by one for every stop. The GTFS gtfsStopSeq
	// on the other hand doesn't need to be sequential.
	@Column 
	private final int stopPathIndex;
	
	// The order of the stop for the direction of the route. This can
	// be useful for displaying data in proper stop order. The member
	// stopPathIndex is for the current trip, but since a route's
	// direction can have multiple trip patterns the stopPathIndex
	// is not sufficient for properly ordering data for a route/direction.
	// Declared an Integer instead of an int because might not always 
	// be set.
	@Column
	private final Integer stopOrder;
	
	// Sometimes want to look at travel times using arrival/departure times.
	// This would be complicated if had to get the path length by using
	// tripIndex to determine trip to determine trip pattern to determine
	// StopPath to determine length. So simply storing the stop path
	// length along with arrivals/departures so that it is easy to obtain
	// for post-processing.
	@Column
	private final float stopPathLength;
	
	// So can easily create copy constructor withUpdatedTime()
	@Transient
	private final BlockInterface block;

	// Record of dwell time for departures
	@Column
	private final Long dwellTime;

	@Column(length=TripPattern.TRIP_PATTERN_ID_LENGTH)
	private String tripPatternId;

	@Column(length=2*HibernateUtils.DEFAULT_ID_SIZE)
	private String stopPathId;

	@Column
	private final boolean scheduleAdherenceStop;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns(
		{
			@JoinColumn(updatable=false,insertable=false, name="stopId", referencedColumnName="id"),
			@JoinColumn(updatable=false,insertable=false, name="configRev", referencedColumnName="configRev")
		}
	)
	private Stop stop;

	// Fetches first Trip that matches tripId and configRev
	// Does NOT take frequencyTime into consideration
	// TODO - add trip startTime as seconds to ArrivalDeparture and join on that
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumnsOrFormulas({
		@JoinColumnOrFormula(column = @JoinColumn(updatable=false,insertable=false, name="tripId", referencedColumnName="tripId")),
		@JoinColumnOrFormula(column = @JoinColumn(updatable=false,insertable=false, name="configRev", referencedColumnName="configRev")),
		@JoinColumnOrFormula(formula = @JoinFormula(value="(SELECT t.startTime FROM Trips t WHERE t.tripId = tripId AND t.configRev = configRev LIMIT 1)", referencedColumnName="startTime"))
	})
	private Trip trip;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(updatable=false,insertable=false, name="stopPathId", referencedColumnName="stopPathId"),
		@JoinColumn(updatable=false,insertable=false, name="tripPatternId", referencedColumnName="tripPatternId"),
		@JoinColumn(updatable=false,insertable=false, name="configRev", referencedColumnName="configRev")
	})
	private StopPath stopPath;

	public enum ArrivalsOrDepartures {ARRIVALS, DEPARTURES};

	private static final Logger logger = 
			LoggerFactory.getLogger(ArrivalDeparture.class);

	// Needed because Hibernate objects must be serializable
	private static final long serialVersionUID = -2186334947521763886L;

	/********************** Member Functions **************************/

	/**
	 * Constructor called when creating an ArrivalDeparture object to be 
	 * stored in db.
	 * 
	 * @param vehicleId
	 * @param time
	 * @param avlTime
	 * @param block
	 * @param tripIndex
	 * @param stopPathIndex
	 * @param isArrival
	 */
	public ArrivalDeparture(int configRev, String vehicleId, Date time, Date avlTime, BlockInterface block,
							   int tripIndex, int stopPathIndex, boolean isArrival, Date freqStartTime, Long dwellTime,
							   String stopPathId, boolean scheduleAdherenceStop, Date scheduledTime) {
		this.vehicleId = vehicleId;
		this.time = time;
		this.avlTime = avlTime;
		this.block = block;
		this.tripIndex = tripIndex;
		this.stopPathIndex = stopPathIndex;
		this.isArrival = isArrival;
		this.configRev = configRev; 
		this.freqStartTime = freqStartTime;
		this.dwellTime = dwellTime;
		this.stopPathId = stopPathId;
		this.scheduleAdherenceStop = scheduleAdherenceStop;

		// Some useful convenience variables

		if(block!=null)
		{
			Trip trip = block.getTrip(tripIndex);
			if (trip == null) {
				throw new IllegalArgumentException("No trip retrieved for tripIndex " + tripIndex + " on block " + block);
			}
			StopPath stopPath = trip.getStopPath(stopPathIndex);
			if (stopPath == null) {
				throw new IllegalArgumentException("No stopPath retrieved for stopPathIndex " + stopPathIndex + " on trip " + trip);
			}
			this.tripPatternId = stopPath.getTripPatternId();
			String stopId = stopPath.getStopId();
			// Determine and store stop order
			this.stopOrder =
				trip.getRoute().getStopOrder(trip.getDirectionId(), stopId,
						stopPathIndex);

			// Determine the schedule time, which is a bit complicated.
			// Of course, only do this for schedule based assignments.
			// The schedule time will only be set if the schedule info was available
			// from the GTFS data and it is the proper type of arrival or departure 
			// stop (there is an arrival schedule time and this is the last stop for
			// a trip and and this is an arrival time OR there is a departure schedule
			// time and this is not the last stop for a trip and this is a departure 
			// time.
			Date scheduledEpochTime = null;
			if (!trip.isNoSchedule()) {
				ScheduleTime scheduleTime = trip.getScheduleTime(stopPathIndex);
				Integer scheduleTimeSec;
				if(!isArrival){
					scheduleTimeSec = scheduleTime.getTime();
				} else {
					scheduleTimeSec = scheduleTime.getArrivalOrDepartureTime();
				}
				long epochTime = Core.getInstance().getTime().getEpochTime(scheduleTimeSec, time);
				scheduledEpochTime = new Date(epochTime);

			}
			this.scheduledTime = scheduledEpochTime;
			
			this.blockId = block.getId();
			this.tripId = trip.getId();
			this.directionId = trip.getDirectionId();
			this.stopId = stopId;
			this.gtfsStopSeq = stopPath.getGtfsStopSeq();
			this.stopPathLength = (float) stopPath.getLength();
			this.routeId = trip.getRouteId();
			this.routeShortName = trip.getRouteShortName();
			this.serviceId = block.getServiceId();
		}else
		{
			/* have to do this as they are final */
			this.stopPathLength=0;
			this.gtfsStopSeq=0;
			this.scheduledTime=scheduledTime;
			this.tripId="";
			this.stopId="";
			this.serviceId = "";
			this.stopOrder=0;
			this.tripPatternId = null;
		}
	}

	public ArrivalDeparture(int configRev, String vehicleId, Date time, Date avlTime, BlockInterface block,
							int tripIndex, int stopPathIndex, boolean isArrival, Date freqStartTime, Long dwellTime,
							String stopPathId, boolean scheduleAdherenceStop) {
		this(configRev, vehicleId, time, avlTime, block, tripIndex, stopPathIndex,isArrival, freqStartTime,
				dwellTime, stopPathId, scheduleAdherenceStop, null);
	}

	protected ArrivalDeparture(String vehicleId, Date time, Date avlTime, BlockInterface block,
							   int tripIndex, int stopPathIndex, boolean isArrival, Date freqStartTime, Long dwellTime,
							   String stopPathId, boolean scheduleAdherenceStop) {

		this(Core.getInstance().getDbConfig().getConfigRev(),vehicleId, time, avlTime, block,
				tripIndex, stopPathIndex, isArrival, freqStartTime, dwellTime, stopPathId, scheduleAdherenceStop);
	}
	public Date getFreqStartTime() {
		return freqStartTime;
	}
	/**
	 * Hibernate requires a no-arg constructor for reading objects
	 * from database.
	 */
	protected ArrivalDeparture() {
		this.vehicleId = null;
		this.time = null;
		this.avlTime = null;
		this.block = null;
		this.directionId = null;
		this.tripIndex = -1;
		this.stopPathIndex = -1;
		this.stopOrder = null;
		this.isArrival = false;
		this.configRev = -1;
		this.scheduledTime = null;
		this.blockId = null;
		this.tripId = null;
		this.stopId = null;
		this.gtfsStopSeq = -1;
		this.stopPathLength = Float.NaN;
		this.routeId = null;
		this.routeShortName = null;
		this.serviceId = null;
		this.freqStartTime = null;
		this.dwellTime = null;
		this.tripPatternId = null;
		this.stopPathId = null;
		this.scheduleAdherenceStop = false;
	}

	/**
	 * for builder/unit test integration
	 * @param vehicleId
	 * @param time
	 * @param avlTime
	 * @param block which can be null
	 * @param directionId
	 * @param tripIndex
	 * @param stopPathIndex
	 * @param stopOrder
	 * @param isArrival
	 * @param configRev
	 * @param scheduledTime
	 * @param blockId
	 * @param tripId
	 * @param stopId
	 * @param gtfsStopSeq
	 * @param stopPathLength
	 * @param routeId
	 * @param routeShortName
	 * @param serviceId
	 * @param freqStartTime
	 * @param dwellTime
	 * @param tripPatternId
	 * @param stopPathId
	 */
	protected ArrivalDeparture(String vehicleId,
													 long time,
													 long avlTime,
													 Block block,
													 String directionId,
													 int tripIndex,
													 int stopPathIndex,
													 Integer stopOrder,
													 boolean isArrival,
													 int configRev,
													 long scheduledTime,
													 String blockId,
													 String tripId,
													 String stopId,
													 int gtfsStopSeq,
													 float stopPathLength,
													 String routeId,
													 String routeShortName,
													 String serviceId,
													 Long freqStartTime,
													 Long dwellTime,
													 String tripPatternId,
													 String stopPathId,
							 						 boolean scheduleAdherenceStop) {
		this.vehicleId = vehicleId;
		this.time = new Date(time);
		this.avlTime = new Date(avlTime);
		this.block = block;
		this.directionId = directionId;
		this.tripIndex = tripIndex;
		this.stopPathIndex = stopPathIndex;
		this.stopOrder = stopOrder;
		this.isArrival = isArrival;
		this.configRev = configRev;
		this.scheduledTime = new Date(scheduledTime);
		this.blockId = blockId;
		this.tripId = tripId;
		this.stopId = stopId;
		this.gtfsStopSeq = gtfsStopSeq;
		this.stopPathLength = stopPathLength;
		this.routeId = routeId;
		this.routeShortName = routeShortName;
		this.serviceId = serviceId;
		if (freqStartTime != null) {
			this.freqStartTime = new Date(freqStartTime);
		} else {
			this.freqStartTime = null;
		}
		this.dwellTime = dwellTime;
		this.tripPatternId = tripPatternId;
		this.stopPathId = stopPathId;
		this.scheduleAdherenceStop = scheduleAdherenceStop;
	}

	/**
	 * Callback due to implementing Lifecycle interface. Used to compact
	 * string members by interning them.
	 */
	@Override
	public void onLoad(Session s, Serializable id) throws CallbackException {
		if (vehicleId != null)
			vehicleId = vehicleId.intern();
		if (stopId != null)
			stopId = stopId.intern();
		if (tripId != null)
			tripId = tripId.intern();
		if (blockId != null)
			blockId = blockId.intern();
		if (routeId != null)
			routeId = routeId.intern();
		if (routeShortName != null)
			routeShortName = routeShortName.intern();
		if (serviceId != null)
			serviceId = serviceId.intern();
		if (directionId != null)
			directionId= directionId.intern();
		if (tripPatternId != null)
			tripPatternId= tripPatternId.intern();
		if (stopPathId != null)
			stopPathId= stopPathId.intern();
	}
	
	/**
	 * Implemented due to Lifecycle interface being implemented. Not actually
	 * used.
	 */
	@Override
	public boolean onSave(Session s) throws CallbackException {
		return Lifecycle.NO_VETO;
	}

	/**
	 * Implemented due to Lifecycle interface being implemented. Not actually
	 * used.
	 */
	@Override
	public boolean onUpdate(Session s) throws CallbackException {
		return Lifecycle.NO_VETO;
	}

	/**
	 * Implemented due to Lifecycle interface being implemented. Not actually
	 * used.
	 */
	@Override
	public boolean onDelete(Session s) throws CallbackException {
		return Lifecycle.NO_VETO;
	}
	
	/**
	 * For logging each creation of an ArrivalDeparture to the separate
	 * ArrivalsDepartures.log file.
	 */
	public void logCreation() {
		logger.info(this.toString());
	}

	/**
	 * Because using a composite Id Hibernate wants this member.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((avlTime == null) ? 0 : avlTime.hashCode());
		result = prime * result + ((block == null) ? 0 : block.hashCode());
		result = prime * result + ((blockId == null) ? 0 : blockId.hashCode());
		result = prime * result + configRev;
		result =
				prime * result
						+ ((directionId == null) ? 0 : directionId.hashCode());
		result = prime * result + gtfsStopSeq;
		result = prime * result + (isArrival ? 1231 : 1237);
		result = prime * result + ((routeId == null) ? 0 : routeId.hashCode());
		result =
				prime
						* result
						+ ((routeShortName == null) ? 0 : routeShortName
								.hashCode());
		result =
				prime
						* result
						+ ((scheduledTime == null) ? 0 : scheduledTime
								.hashCode());
		result =
				prime * result
						+ ((serviceId == null) ? 0 : serviceId.hashCode());
		result = prime * result + ((stopId == null) ? 0 : stopId.hashCode());
		result =
				prime * result
						+ ((stopOrder == null) ? 0 : stopOrder.hashCode());
		result = prime * result + stopPathIndex;
		result = prime * result + Float.floatToIntBits(stopPathLength);
		result = prime * result + ((time == null) ? 0 : time.hashCode());
		result = prime * result + ((tripId == null) ? 0 : tripId.hashCode());
		result = prime * result + tripIndex;
		result =
				prime * result
						+ ((vehicleId == null) ? 0 : vehicleId.hashCode());
		result =
				prime * result
						+ ((dwellTime == null) ? 0 : dwellTime.hashCode());
		result =
				prime * result
						+ ((tripPatternId == null) ? 0 : tripPatternId.hashCode());
		result =
				prime * result
						+ ((stopPathId == null) ? 0 : stopPathId.hashCode());
		result = prime * result + (scheduleAdherenceStop ? 1231 : 1237);
		return result;
	}

	/**
	 * Because using a composite Id Hibernate wants this member.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArrivalDeparture other = (ArrivalDeparture) obj;
		if (avlTime == null) {
			if (other.avlTime != null)
				return false;
		} else if (!avlTime.equals(other.avlTime))
			return false;
		if (block == null) {
			if (other.block != null)
				return false;
		} else if (!block.equals(other.block))
			return false;
		if (blockId == null) {
			if (other.blockId != null)
				return false;
		} else if (!blockId.equals(other.blockId))
			return false;
		if (configRev != other.configRev)
			return false;
		if (directionId == null) {
			if (other.directionId != null)
				return false;
		} else if (!directionId.equals(other.directionId))
			return false;
		if (gtfsStopSeq != other.gtfsStopSeq)
			return false;
		if (isArrival != other.isArrival)
			return false;
		if (routeId == null) {
			if (other.routeId != null)
				return false;
		} else if (!routeId.equals(other.routeId))
			return false;
		if (routeShortName == null) {
			if (other.routeShortName != null)
				return false;
		} else if (!routeShortName.equals(other.routeShortName))
			return false;
		if (scheduledTime == null) {
			if (other.scheduledTime != null)
				return false;
		} else if (!scheduledTime.equals(other.scheduledTime))
			return false;
		if (serviceId == null) {
			if (other.serviceId != null)
				return false;
		} else if (!serviceId.equals(other.serviceId))
			return false;
		if (stopId == null) {
			if (other.stopId != null)
				return false;
		} else if (!stopId.equals(other.stopId))
			return false;
		if (stopOrder == null) {
			if (other.stopOrder != null)
				return false;
		} else if (!stopOrder.equals(other.stopOrder))
			return false;
		if (stopPathIndex != other.stopPathIndex)
			return false;
		if (Float.floatToIntBits(stopPathLength) != Float
				.floatToIntBits(other.stopPathLength))
			return false;
		if (time == null) {
			if (other.time != null)
				return false;
		} else if (!time.equals(other.time))
			return false;
		if (tripId == null) {
			if (other.tripId != null)
				return false;
		} else if (!tripId.equals(other.tripId))
			return false;
		if (tripIndex != other.tripIndex)
			return false;
		if (vehicleId == null) {
			if (other.vehicleId != null)
				return false;
		} else if (!vehicleId.equals(other.vehicleId))
			return false;
		if (dwellTime == null) {
			if (other.dwellTime != null)
				return false;
		} else if (!dwellTime.equals(other.dwellTime))
			return false;
		if (tripPatternId == null) {
			if (other.tripPatternId != null)
				return false;
		} else if (!tripPatternId.equals(other.tripPatternId))
			return false;
		if (stopPathId == null) {
			if (other.stopPathId != null)
				return false;
		} else if (!stopPathId.equals(other.stopPathId))
			return false;
		if (scheduleAdherenceStop != other.scheduleAdherenceStop)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return (isArrival ? "Arrival  " : "Departure") + " ["
				+ "vehicleId=" + vehicleId
				+ ", time=" + Time.dateTimeStrMsec(time)
				+ ", route="	+ routeId
				+ ", rteName=" + routeShortName
				+ ", directionId=" + directionId
				+ ", stop=" + stopId
				+ ", gtfsStopSeq=" + gtfsStopSeq
				+ ", stopIdx=" + stopPathIndex
				+ ", stopPathId=" + stopPathId
				+ ", freqStartTime=" + freqStartTime
				+ ", stopOrder=" + stopOrder
				+  (avlTime != null ?
				 		", avlTime=" + Time.timeStrMsec(avlTime) : "")
				+ ", trip=" + tripId
				+ ", tripIdx=" + tripIndex
				+ ", tripPatternId=" + tripPatternId
				+ ", block=" + blockId
				+ ", srv=" + serviceId
				+ ", cfg=" + configRev
				+ (stopPathLength != Float.NaN ?
				 		", pathLnth=" + Geo.distanceFormat(stopPathLength) : "")
				+ (scheduledTime != null ?
						", schedTime=" + Time.timeStr(scheduledTime) : "")
				+ (scheduledTime != null ?
						", schedAdh=" + new TemporalDifference(
								scheduledTime.getTime() - time.getTime()) : "")
				+ (dwellTime != null ? ", dwellTime=" + dwellTime : "")
				+ ", isScheduleAdherenceStop=" + scheduleAdherenceStop
				+ "]";
	}
	

	public String getVehicleId() {
		return vehicleId;
	}

	public Date getDate() {
		return time;
	}

	public Date getAvlTime() {
		return avlTime;
	}
	
	public long getTime() {
		return time.getTime();
	}
	
	public String getStopId() {
		return stopId;
	}

	public boolean isArrival() {
		return isArrival;
	}

	/**
	 * Can be more clear than using !isArrival()
	 * @return
	 */
	public boolean isDeparture() {
		return !isArrival;
	}
	
	public String getTripId() {
		return tripId;
	}

	/**
	 * Returns the trip short name for the trip associated with the
	 * arrival/departure.
	 * 
	 * @return trip short name for the trip associated with the
	 * arrival/departure or null if there is a problem
	 */
	public String getTripShortName() {
		if (!Core.isCoreApplication()) {
			logger.error(Markers.email(), 
					"For agencyId={} alling ArrivalDeparture.getTripShortName() "
					+ "but it is not part of core application", 
					AgencyConfig.getAgencyId());
			return null;
		}
		
		Trip trip = Core.getInstance().getDbConfig().getTrip(tripId);
		if (trip != null)
			return trip.getShortName();
		else
			return null;
	}
	
	public String getBlockId() {
		return blockId;
	}

	public String getRouteId() {
		return routeId;
	}

	public String getServiceId() {
		return serviceId;
	}

	public String getDirectionId() {
		return directionId;
	}
	
	public int getConfigRev() {
		return configRev;
	}

	public int getTripIndex() {
		return tripIndex;
	}

	public int getStopPathIndex() {
		return stopPathIndex;
	}

	public float getStopPathLength() {
		return stopPathLength;
	}

	public Integer getStopOrder() {
		return stopOrder;
	}
	
	/**
	 * Note that the block is a transient element so will not be available if
	 * this object was read from the database. In that case it will be null.
	 * 
	 * @return
	 */
	public BlockInterface getBlock() {
		return block;
	}

	/**
	 * The schedule time will only be set if the schedule info was available
	 * from the GTFS data and it is the proper type of arrival or departure
	 * stop (there is an arrival schedule time and this is the last stop for
	 * a trip and and this is an arrival time OR there is a departure schedule
	 * time and this is not the last stop for a trip and this is a departure
	 * time. Otherwise will be null.
	 * 
	 * @return
	 */
	public Date getScheduledDate() {
		return scheduledTime;
	}

	/**
	 * Same as getScheduledDate() but returns long epoch time.
	 * @return
	 */
	public long getScheduledTime() {
		return scheduledTime.getTime();
	}

	/**
	 * Returns the schedule adherence for the stop if there was a schedule
	 * time. Otherwise returns null.
	 * 
	 * @return
	 */
	public TemporalDifference getScheduleAdherence() {
		// If there is no schedule time for this stop then there
		// is no schedule adherence information.
		if (scheduledTime == null)
			return null;
		
		// Return the schedule adherence
		return new TemporalDifference(scheduledTime.getTime() - time.getTime());
	}
	
	/**
	 * Returns the Stop object associated with the arrival/departure. Will only
	 * be valid for the Core system where the configuration has been read in.
	 * 
	 * @return The Stop associated with the arrival/departure
	 */
	public Stop getStop() {
		return Core.getInstance().getDbConfig().getStop(stopId);
	}
	
	/**
	 * @return the gtfsStopSequence associated with the arrival/departure
	 */
	public int getGtfsStopSequence() {
		return gtfsStopSeq;
	}

	public Long getDwellTime() {
		return dwellTime;
	}

	public String getStopPathId() {
		return stopPathId;
	}

	public boolean isScheduleAdherenceStop() { return scheduleAdherenceStop; }

	public String getTripPatternId() {
		return tripPatternId;
	}

	public Stop getStopFromDb() {
		return stop;
	}

	public Trip getTripFromDb() { return trip; }

	public StopPath getStopPathFromDb() { return stopPath; }

	/**
	 * Builder pattern allows for non-hibernate creation of this instance
	 * while still guaranteeing internal consistency.  Intended for unit tests only.
	 */
	public static class Builder {
		String vehicleId;
		long time;
		long avlTime;
		Block block;
		String directionId;
		int tripIndex;
		int stopPathIndex;
		Integer stopOrder;
		boolean isArrival;
		int configRev;
		long scheduledTime;
		String blockId;
		String tripId;
		String stopId;
		int gtfsStopSeq;
		float stopPathLength;
		String routeId;
		String routeShortName;
		String serviceId;
		Long freqStartTime;
		Long dwellTime;
		String tripPatternId;
		String stopPathId;
		boolean scheduleAdherenceStop;
		public Builder(String vehicleId,
						long time,
						long avlTime,
						Block block,
						String directionId,
						int tripIndex,
						int stopPathIndex,
						Integer stopOrder,
					  boolean isArrival,
						int configRev,
						long scheduledTime,
						String blockId,
						String tripId,
						String stopId,
						int gtfsStopSeq,
						float stopPathLength,
						String routeId,
						String routeShortName,
						String serviceId,
						Long freqStartTime,
						Long dwellTime,
						String tripPatternId,
						String stopPathId,
					    boolean scheduleAdherenceStop
		) {
			this.vehicleId = vehicleId;
			this.time = time;
			this.avlTime = avlTime;
			this.block = block;
			this.directionId = directionId;
			this.tripIndex = tripIndex;
			this.stopPathIndex = stopPathIndex;
			this.stopOrder = stopOrder;
			this.isArrival = isArrival;
			this.configRev = configRev;
			this.scheduledTime = scheduledTime;
			this.blockId = blockId;
			this.tripId = tripId;
			this.stopId = stopId;
			this.gtfsStopSeq = gtfsStopSeq;
			this.stopPathLength = stopPathLength;
			this.routeId = routeId;
			this.routeShortName = routeShortName;
			this.serviceId = serviceId;
			this.freqStartTime = freqStartTime;
			this.dwellTime = dwellTime;
			this.tripPatternId = tripPatternId;
			this.stopPathId = stopPathId;
			this.scheduleAdherenceStop = scheduleAdherenceStop;
		}

		/**
		 * create an instance of ArrivalDeparture outside of Hibernate.  Intended
		 * for unit tests only!
		 * @return
		 */
		public ArrivalDeparture create() {
			ArrivalDeparture ad
							= new ArrivalDeparture(
			vehicleId,
			time,
			avlTime,
			block,
			directionId,
			tripIndex,
			stopPathIndex,
			stopOrder,
			isArrival,
			configRev,
			scheduledTime,
			blockId,
			tripId,
			stopId,
			gtfsStopSeq,
			stopPathLength,
			routeId,
			routeShortName,
			serviceId,
			freqStartTime,
			dwellTime,
			tripPatternId,
			stopPathId,
			scheduleAdherenceStop
			);
			return ad;
		}
	}
}
