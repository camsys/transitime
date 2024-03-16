package org.transitclock.db.dao;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.transitclock.db.structs.CalendarDate;

import java.util.Date;
import java.util.List;

public class CalendarDateDAO {
  /**
   * Deletes rev from the CalendarDates table
   *
   * @param session
   * @param configRev
   * @return Number of rows deleted
   * @throws HibernateException
   */
  public static int deleteFromRev(Session session, int configRev)
          throws HibernateException {
    // Note that hql uses class name, not the table name
    String hql = "DELETE CalendarDate WHERE configRev=" + configRev;
    int numUpdates = session.createQuery(hql).executeUpdate();
    return numUpdates;
  }

  /**
   * Returns List of Agency objects for the specified database revision.
   *
   * @param session
   * @param configRev
   * @return
   * @throws HibernateException
   */
  @SuppressWarnings("unchecked")
  public static List<CalendarDate> getCalendarDates(Session session, int configRev)
          throws HibernateException {
    String hql = "FROM CalendarDate " +
            "    WHERE configRev = :configRev";
    Query query = session.createQuery(hql);
    query.setInteger("configRev", configRev);
    return query.list();
  }

  /**
   * Returns List of Agency objects for the specified database revision.
   *
   * @param session
   * @param configRev
   * @return
   * @throws HibernateException
   */
  @SuppressWarnings("unchecked")
  public static List<CalendarDate> getCalendarDates(Session session, int configRev, Date date)
          throws HibernateException {
    String hql = "FROM CalendarDate " +
            "    WHERE configRev = :configRev " +
            "    AND date = :date";
    Query query = session.createQuery(hql);
    query.setInteger("configRev", configRev);
    query.setDate("date", date);
    return query.list();
  }

}
