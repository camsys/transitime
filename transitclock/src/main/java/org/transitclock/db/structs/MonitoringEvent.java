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

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import net.jcip.annotations.Immutable;

import org.hibernate.annotations.DynamicUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For storing monitoring events into database. By storing the monitoring
 * events one can look back and easily see how frequent events are, when
 * the start and stop, etc.
 *
 * @author SkiBu Smith
 *
 */
@Immutable // From jcip.annoations
@Entity @DynamicUpdate 
@Table(name="MonitoringEvents",
       indexes = { @Index(name="MonitoringEventsTimeIndex", 
                   columnList="time" ) } )
public class MonitoringEvent implements Serializable {
	// The time the event occurred
	@Id
	@Column	
	@Temporal(TemporalType.TIMESTAMP)
	private final Date time;

	// String describing type of event, such as "System CPU"
	@Id
	@Column(length=40)
	private final String type;
	
	// Whether monitoring is now triggered
	@Column
	private final boolean triggered;
	
	// The long message associated with the monitoring
	private final static int MAX_MESSAGE_LENGTH = 512;
	@Column(length=MAX_MESSAGE_LENGTH)
	private final String message;
	
	// The value that caused monitoring to be triggered or untriggered.
	// For some monitors they are triggered when this value is too
	// high. For others though it can be when the value is too low.
	@Column
	private final double value;
	
	// Hibernate requires class to be Serializable
	private static final long serialVersionUID = 9133751362365305772L;

	private static final Logger logger = 
			LoggerFactory.getLogger(MonitoringEvent.class);
	
	/********************** Member Functions **************************/

	/**
	 * Simple constructor. Declared private because should be only accessed by
	 * the create() method so that can make sure that do things like log each
	 * creation of a VehicleEvent.

	 * @param time
	 * @param type
	 * @param triggered
	 * @param message
	 * @param value
	 */
	public MonitoringEvent(Date time, String type, boolean triggered,
			String message, double value) {
		super();
		this.time = time;
		this.type = type;
		this.triggered = triggered;
		// Since message to be stored in db and don't know how long it might
		// be make sure it is not too long so that don't get db errors.
		this.message = message.length() <= MAX_MESSAGE_LENGTH ? 
				message : message.substring(0, MAX_MESSAGE_LENGTH);
		// Note: MySQL can't handle double values of Double.NaN. Get an exception
		// "java.sql.SQLException: 'NaN' is not a valid numeric or approximate numeric value".
		// So if value is a NaN use 0.0 instead. Works fine with Postgres though.
		this.value = Double.isNaN(value) ? 0.0 : value;
	}
	
	/**
	 * Hibernate requires a no-args constructor for reading data.
	 * So this is an experiment to see what can be done to satisfy
	 * Hibernate but still have an object be immutable. Since
	 * this constructor is only intended to be used by Hibernate
	 * is is declared protected, since that still works. That way
	 * others won't accidentally use this inappropriate constructor.
	 * And yes, it is peculiar that even though the members in this
	 * class are declared final that Hibernate can still create an
	 * object using this no-args constructor and then set the fields.
	 * Not quite as "final" as one might think. But at least it works.
	 */
	protected MonitoringEvent() {
		time = null;
		type = null;
		triggered = false;
		message = null;
		value = Double.NaN;
	}

	/**
	 * Because using a composite Id Hibernate wants this method.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((time == null) ? 0 : time.hashCode());
		result = prime * result + (triggered ? 1231 : 1237);
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		long temp;
		temp = Double.doubleToLongBits(value);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/**
	 * Because using a composite Id Hibernate wants this method.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MonitoringEvent other = (MonitoringEvent) obj;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (time == null) {
			if (other.time != null)
				return false;
		} else if (!time.equals(other.time))
			return false;
		if (triggered != other.triggered)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (Double.doubleToLongBits(value) != Double
				.doubleToLongBits(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MonitoringEvent [" 
				+ "time=" + time 
				+ ", type=" + type
				+ ", triggered=" + triggered 
				+ ", message=" + message
				+ ", value=" + value 
				+ "]";
	}

	public Date getTime() {
		return time;
	}

	public String getType() {
		return type;
	}

	public boolean isTriggered() {
		return triggered;
	}

	public String getMessage() {
		return message;
	}

	public double getValue() {
		return value;
	}

}
