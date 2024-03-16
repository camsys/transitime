package org.transitclock.db.dao;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.Calendar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarDAO {

  /**
   * Deletes rev from the Calendars table
   *
   * @param session
   * @param configRev
   * @return Number of rows deleted
   * @throws HibernateException
   */
  public static int deleteFromRev(Session session, int configRev)
          throws HibernateException {
    // Note that hql uses class name, not the table name
    String hql = "DELETE Calendar WHERE configRev=" + configRev;
    int numUpdates = session.createQuery(hql).executeUpdate();
    return numUpdates;
  }

  /**
   * Returns List of Calendar objects for the specified database revision.
   *
   * @param session
   * @param configRev
   * @return List of Calendar objects
   * @throws HibernateException
   */
  @SuppressWarnings("unchecked")
  public static List<Calendar> getCalendars(Session session, int configRev)
          throws HibernateException {

    if(session == null){
      session = HibernateUtils.getSession(true);
    }

    String hql = "FROM Calendar " +
            "    WHERE configRev = :configRev" +
            " ORDER BY serviceId";
    Query query = session.createQuery(hql);
    query.setInteger("configRev", configRev);
    return query.list();
  }

  /**
   * Returns List of Calendar objects for the specified database revision.
   *
   * @param session
   * @param configRev
   * @return List of Calendar objects
   * @throws HibernateException
   */
  @SuppressWarnings("unchecked")
  public static List<Calendar> getCalendar(Session session, int configRev, String serviceId)
          throws HibernateException {

    String hql = "FROM Calendar " +
            "    WHERE configRev = :configRev " +
            "    AND serviceId = :serviceId";
    Query query = session.createQuery(hql);
    query.setInteger("configRev", configRev);
    query.setParameter("serviceId", serviceId);
    return query.setMaxResults(1).list();
  }

  /**
   * Opens up a new db session and returns Map of Calendar objects for the
   * specified database revision. The map is keyed on the serviceId.
   *
   * @param dbName
   *            Specified name of database
   * @param configRev
   * @return Map of Calendar objects keyed on serviceId
   * @throws HibernateException
   */
  public static Map<String, Calendar> getCalendars(String dbName, int configRev)
          throws HibernateException {
    // Get the database session. This is supposed to be pretty light weight
    Session session = HibernateUtils.getSession(dbName);

    // Get list of calendars
    List<Calendar> calendarList = getCalendars(session, configRev);

    // Convert list to map and return result
    Map<String, Calendar> map = new HashMap<String, Calendar>();
    for (Calendar calendar : calendarList)
      map.put(calendar.getServiceId(), calendar);
    return map;
  }

}
