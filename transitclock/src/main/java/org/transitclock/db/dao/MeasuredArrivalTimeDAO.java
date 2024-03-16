package org.transitclock.db.dao;

import org.transitclock.db.structs.MeasuredArrivalTime;

public class MeasuredArrivalTimeDAO {

  /**
   * Returns the SQL to save the object into database. Usually Hibernate is
   * used because such data is stored by the core system. But
   * MeasuredArrivalTime objects are written by the website, which doesn't use
   * Hibernate to write objects since it has to be able to talk with any db.
   *
   * @return SQL to store the object
   */
  public static String getUpdateSql(MeasuredArrivalTime mat) {
    return "INSERT INTO MeasuredArrivalTimes ("
            + "time, stopId, routeId, routeShortName, directionId, headsign) "
            + "VALUES('" + mat.getTime() + "', '"
            + mat.getStopId() + "', '"
            + mat.getRouteId() + "', '"
            + mat.getRouteShortName() + "', '"
            + mat.getDirectionId() + "', '"
            + mat.getHeadsign() + "'"
            + ");";
  }

}
