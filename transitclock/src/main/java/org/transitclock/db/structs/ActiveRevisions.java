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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.hibernate.HibernateUtils;

/**
 * For keeping track of current revisions. This table should only have a single
 * row, one that specified the configRev and the travelTimesRev currently being
 * used.
 *
 * @author SkiBu Smith
 *
 */
@Entity @DynamicUpdate
public class ActiveRevisions {

	// Need a generated ID since Hibernate required some type
	// of ID. Both configRev and travelTimesRev
	// might get updated and with Hibernate you can't read in an
	// object, modify an ID and then write it out again. Therefore
	// configRev and travelTimesRev can't be an ID. This means
	// that need a separate ID. Yes, somewhat peculiar.
	@Id 
	@Column 
	@GeneratedValue 
	private Integer id;

	// For the configuration data for routes, stops, schedule, etc.
	@Column
	private int configRev;
	
	// For the travel time configuration data. Updated independently of
	// configRev.
	@Column
	private int travelTimesRev;


	// For the traffic configuration data.  Updated independently of
	// configRev and travelTimesRev.
	@Column
	private Integer trafficRev;

	private static final Logger logger =
			LoggerFactory.getLogger(ActiveRevisions.class);

	/********************** Member Functions **************************/

	/**
	 * Constructor. Sets the revisions to default values of -1.
	 */
	public ActiveRevisions() {
		configRev = -1;
		travelTimesRev = -1;
		trafficRev = null;
	}
	

	/**
	 * Sets the travel time rev. Doesn't write it to db though. To write to db
	 * should flush the session that the object was read in by.
	 * 
	 * @param travelTimeRev
	 */
	public void setTravelTimesRev(int travelTimeRev) {
		this.travelTimesRev = travelTimeRev;
	}
	
	/**
	 * Sets the config rev. Doesn't write it to db though. To write to db
	 * should flush the session that the object was read in by.
	 * 
	 * @param configRev
	 */
	public void setConfigRev(int configRev) {
		this.configRev = configRev;
	}

	public int getConfigRev() {
		return configRev;
	}

	public Integer getTrafficRev() { return trafficRev; }

	public void setTrafficRev(Integer trafficRev) {
		this.trafficRev = trafficRev;
	}

	public int getTravelTimesRev() {
		return travelTimesRev;
	}
	
	/**
	 * @return True if both the configRev and travelTimesRev are both valid.
	 */
	public boolean isValid() {
		return configRev >= 0 && travelTimesRev >= 0;
	}

	@Override
	public String toString() {
		return "ActiveRevisions [" 
				+ "configRev=" + configRev 
				+ ", travelTimesRev=" + travelTimesRev
				+ ", trafficRev=" + trafficRev
				+ "]";
	}

}
