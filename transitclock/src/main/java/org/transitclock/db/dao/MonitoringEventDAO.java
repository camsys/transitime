package org.transitclock.db.dao;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.MonitoringEvent;
import org.transitclock.utils.IntervalTimer;
import org.transitclock.utils.Time;

import java.util.Date;
import java.util.List;

public class MonitoringEventDAO {

  private static final Logger logger =
          LoggerFactory.getLogger(MonitoringEventDAO.class);
  /**
   * Constructs a monitoring event and queues it to be stored in database.
   *
   * @param time
   * @param type
   * @param triggered
   * @param message
   * @param value
   * @return
   */
  public static MonitoringEvent create(Date time, String type, boolean triggered,
                                       String message, double value) {
    MonitoringEvent monitoringEvent = new MonitoringEvent(time, type,
            triggered, message, value);

    // Queue to write object to database
    Core.getInstance().getDbLogger().add(monitoringEvent);

    // Return new MonitoringEvent
    return monitoringEvent;
  }

  /**
   * Reads in all MonitoringEvents from the database that were between the
   * beginTime and endTime.
   *
   * @param agencyId
   *            Which project getting data for
   * @param beginTime
   *            Specifies time range for query
   * @param endTime
   *            Specifies time range for query
   * @param sqlClause
   *            Optional. Can specify an SQL clause to winnow down the data,
   *            such as "AND routeId='71'".
   * @return
   */
  public static List<MonitoringEvent> getMonitoringEvents(String agencyId,
                                                          Date beginTime, Date endTime, String sqlClause) {
    IntervalTimer timer = new IntervalTimer();

    // Get the database session. This is supposed to be pretty light weight
    Session session = HibernateUtils.getSession(agencyId);

    // Create the query. Table name is case sensitive and needs to be the
    // class name instead of the name of the db table.
    String hql = "FROM MonitorEvent " +
            "    WHERE time >= :beginDate " +
            "      AND time < :endDate";
    if (sqlClause != null)
      hql += " " + sqlClause;
    Query query = session.createQuery(hql);

    // Set the parameters
    query.setTimestamp("beginDate", beginTime);
    query.setTimestamp("endDate", endTime);

    try {
      @SuppressWarnings("unchecked")
      List<MonitoringEvent> monitorEvents = query.list();
      logger.debug("Getting MonitoringEvent from database "
              + "took {} msec", timer.elapsedMsec());
      return monitorEvents;
    } catch (HibernateException e) {
      logger.error(e.getMessage(), e);
      return null;
    } finally {
      // Clean things up. Not sure if this absolutely needed nor if
      // it might actually be detrimental and slow things down.
      session.close();
    }

  }

  /**
   * Checks the monitor object to see if state has changed currently
   * triggered. If state changes then sends out notification e-mail.
   *
   * @return True if monitor currently triggered
   */
  public boolean checkAndNotify(MonitoringEvent event) {
    // monitoring is now external
    return false;
  }


}
