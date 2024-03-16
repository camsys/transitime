package org.transitclock.db.dao;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.structs.TravelTimesForStopPath;

import java.util.List;

public class TravleTimesForStopPathDAO {

  private static final Logger logger =
          LoggerFactory.getLogger(TravleTimesForStopPathDAO.class);

  /**
   * Reads in all the travel times for the specified rev
   *
   * @param sessionFactory
   * @param configRev
   * @return
   */
  public static List<TravelTimesForStopPath> getTravelTimes(SessionFactory sessionFactory,
                                                            int configRev) {
    // Sessions are not threadsafe so need to create a new one each time.
    // They are supposed to be lightweight so this should be OK.
    Session session = sessionFactory.openSession();

    // Create the query. Table name is case sensitive!
    String hql = "FROM TravelTimesForStopPath " +
            "    WHERE configRev=:configRev ";
    Query query = session.createQuery(hql);

    // Set the parameters
    query.setInteger("configRev", configRev);

    try {
      @SuppressWarnings("unchecked")
      List<TravelTimesForStopPath> travelTimes = query.list();
      return travelTimes;
    } catch (HibernateException e) {
      // Log error to the Core logger
      logger.error(e.getMessage(), e);
      return null;
    } finally {
      // Clean things up. Not sure if this absolutely needed nor if
      // it might actually be detrimental and slow things down.
      session.close();
    }
  }

}
