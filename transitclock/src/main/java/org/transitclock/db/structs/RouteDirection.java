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

import com.google.common.base.Objects;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.gtfs.gtfsStructs.GtfsRouteDirection;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * Contains data from the transfers.txt GTFS file. This class is
 * for reading/writing that data to the db.
 *
 * @author carabalb
 *
 */
@Entity @DynamicUpdate @Table(name="RouteDirections")
public class RouteDirection implements Serializable {

	@Column
	@Id
	private final int configRev;

	@Column
	@Id
	private final String routeShortName;

	@Column(length= HibernateUtils.DEFAULT_ID_SIZE)
	@Id
	private final String directionId;

	@Column
	private final String directionName;

	// Logging
	public static final Logger logger = LoggerFactory.getLogger(RouteDirection.class);

	/********************** Member Functions **************************/

	/**
	 * @param configRev
	 * @param gtfsRouteDirection
	 */
	public RouteDirection(int configRev, GtfsRouteDirection gtfsRouteDirection) {
		this.configRev = configRev;
		this.routeShortName = gtfsRouteDirection.getRouteShortName();
		this.directionId = gtfsRouteDirection.getDirectionId();
		this.directionName = gtfsRouteDirection.getDirectionName();
	}

	/**
	 * Needed because no-arg constructor required by Hibernate
	 */
	@SuppressWarnings("unused")
	private RouteDirection() {
		this.configRev = -1;
		this.directionId=null;
		this.directionName=null;
		this.routeShortName =null;
	}

	public int getConfigRev() {
		return configRev;
	}

	public String getDirectionId() {
		return directionId;
	}

	public String getDirectionName() {
		return directionName;
	}

	public String getRouteShortName() {
		return routeShortName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RouteDirection that = (RouteDirection) o;
		return configRev == that.configRev &&
				Objects.equal(routeShortName, that.routeShortName) &&
				Objects.equal(directionId, that.directionId) &&
				Objects.equal(directionName, that.directionName);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(configRev, routeShortName, directionId, directionName);
	}

	@Override
	public String toString() {
		return "RouteDirection{" +
				"configRev=" + configRev +
				", routeShortName='" + routeShortName + '\'' +
				", directionId='" + directionId + '\'' +
				", directionName='" + directionName + '\'' +
				'}';
	}
}
