package org.transitclock.db.dao;

import org.hibernate.Query;
import org.hibernate.Session;
import org.transitclock.db.structs.TrafficSensorData;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class TrafficSensorDataDAO {
  /**
   * do bulk load of data.
   * @param session
   * @param startDate
   * @param endDate
   * @return
   */
  public static Iterator<TrafficSensorData> getTrafficSensorDataIteratorFromDb(Session session, Date startDate, Date endDate) {
    String hql = "FROM TrafficSensorData " +
            " WHERE time >= :beginDate " +
            " AND time < :endDate";
    Query query = session.createQuery(hql);
    query.setTimestamp("beginDate", startDate);
    query.setTimestamp("endDate", endDate);
    //iterator performance on mysql is poor!
    return query.iterate();
  }

  public static List<TrafficSensorData> getTrafficSensorDataFromDb(Session session, Date startDate, Date endDate) {
    String hql = "FROM TrafficSensorData " +
            " WHERE time >= :beginDate " +
            " AND time < :endDate";
    Query query = session.createQuery(hql);
    query.setTimestamp("beginDate", startDate);
    query.setTimestamp("endDate", endDate);

    return query.list();
  }

}
