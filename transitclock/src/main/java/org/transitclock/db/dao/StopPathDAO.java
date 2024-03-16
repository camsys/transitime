package org.transitclock.db.dao;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.transitclock.db.structs.StopPath;

import java.util.List;

public class StopPathDAO {
  /**
   * Returns List of StopPath objects for the specified database revision.
   *
   * @param session
   * @param configRev
   * @return
   * @throws HibernateException
   */
  @SuppressWarnings("unchecked")
  public static List<StopPath> getPaths(Session session, int configRev)
          throws HibernateException {
    String hql = "FROM StopPath " +
            "    WHERE configRev = :configRev";
    Query query = session.createQuery(hql);
    query.setInteger("configRev", configRev);
    return query.list();
  }

  /**
   * For consistently naming the path Id. It is based on the current
   * stop ID and the previous stop Id. If previousStopId is null
   * then will return "to_" + stopId. If not null will return
   * previousStopId + "_to_" + stopId.
   * @param previousStopId
   * @param stopId
   * @return
   */
  public static String determinePathId(String previousStopId, String stopId) {
    if (previousStopId == null) {
      return "to_" + stopId;
    } else {
      return previousStopId + "_to_" + stopId;
    }
  }


}
