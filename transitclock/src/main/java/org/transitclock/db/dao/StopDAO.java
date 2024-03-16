package org.transitclock.db.dao;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.Stop;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StopDAO {
  /**
   * Deletes rev from the Stops table
   *
   * @param session
   * @param configRev
   * @return Number of rows deleted
   * @throws HibernateException
   */
  public static int deleteFromRev(Session session, int configRev)
          throws HibernateException {
    // Note that hql uses class name, not the table name
    String hql = "DELETE Stop WHERE configRev=" + configRev;
    int numUpdates = session.createQuery(hql).executeUpdate();
    return numUpdates;
  }

  /**
   * Returns List of Stop objects for the specified database revision.
   *
   * @param session
   * @param configRev
   * @return
   * @throws HibernateException
   */
  @SuppressWarnings("unchecked")
  public static List<Stop> getStops(Session session, int configRev)
          throws HibernateException {
    String hql = "FROM Stop " +
            "    WHERE configRev = :configRev";
    Query query = session.createQuery(hql);
    query.setInteger("configRev", configRev);
    return query.list();
  }

  public static Map<String, Stop> getStops(int configRev, Set<String> stopIds)
          throws HibernateException {
    String hql = "FROM Stop s "
            + " WHERE s.configRev = :configRev "
            + " AND s.id in (:stopIds) ";

    Session session = HibernateUtils.getReadOnlySession();

    Query query = session.createQuery(hql);
    query.setInteger("configRev", configRev);
    query.setParameterList("stopIds", stopIds);
    List<Stop> stops = query.list();

    session.close();

    return stops.stream().collect(Collectors.toMap(Stop::getId, Function.identity()));
  }

}
