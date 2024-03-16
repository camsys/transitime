package org.transitclock.db.dao;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.Match;
import org.transitclock.utils.IntervalTimer;

import java.util.Date;
import java.util.List;

public class MatchDAO {

  private static final Logger logger =
          LoggerFactory.getLogger(MatchDAO.class);
  /**
   * Allows batch retrieval of Match data from database. This is likely the
   * best way to read in large amounts of data.
   *
   * @param projectId
   * @param beginTime
   * @param endTime
   * @param sqlClause
   *            The clause is added to the SQL for retrieving the
   *            arrival/departures. Useful for ordering the results. Can be
   *            null.
   * @param firstResult
   * @param maxResults
   * @return
   */
  public static List<Match> getMatchesFromDb(
          String projectId, Date beginTime, Date endTime,
          String sqlClause,
          final Integer firstResult, final Integer maxResults) {
    IntervalTimer timer = new IntervalTimer();

    // Get the database session. This is supposed to be pretty light weight
    Session session = HibernateUtils.getSession(projectId, false);

    // Create the query. Table name is case sensitive and needs to be the
    // class name instead of the name of the db table.
    String hql = "FROM Match " +
            "    WHERE avlTime between :beginDate " +
            "      AND :endDate";
    if (sqlClause != null)
      hql += " " + sqlClause;
    Query query = session.createQuery(hql);

    // Set the parameters for the query
    query.setTimestamp("beginDate", beginTime);
    query.setTimestamp("endDate", endTime);

    if (firstResult != null) {
      // Only get a batch of data at a time
      query.setFirstResult(firstResult);
    }
    if (maxResults != null) {
      query.setMaxResults(maxResults);
    }

    try {
      @SuppressWarnings("unchecked")
      List<Match> matches = query.list();
      logger.debug("Getting matches from database took {} msec",
              timer.elapsedMsec());
      return matches;
    } catch (HibernateException e) {
      // Log error to the Core logger
      Core.getLogger().error(e.getMessage(), e);
      return null;
    } finally {
      // Clean things up. Not sure if this absolutely needed nor if
      // it might actually be detrimental and slow things down.
      session.close();
    }
  }

  public static Long getMatchesCountFromDb(
          String projectId, Date beginTime, Date endTime,
          String sqlClause) {
    IntervalTimer timer = new IntervalTimer();

    // Get the database session. This is supposed to be pretty light weight
    Session session = HibernateUtils.getSession(projectId, false);

    // Create the query. Table name is case sensitive and needs to be the
    // class name instead of the name of the db table.
    String hql = "Select count(*) FROM Match " +
            "    WHERE avlTime >= :beginDate " +
            "      AND avlTime < :endDate";
    if (sqlClause != null)
      hql += " " + sqlClause;
    Query query = session.createQuery(hql);

    // Set the parameters for the query
    query.setTimestamp("beginDate", beginTime);
    query.setTimestamp("endDate", endTime);

    Long count = null;

    try {
      count = (Long) query.uniqueResult();
      logger.debug("Getting matches from database took {} msec",
              timer.elapsedMsec());
      return count;
    } catch (HibernateException e) {
      // Log error to the Core logger
      Core.getLogger().error(e.getMessage(), e);
      return null;
    } finally {
      // Clean things up. Not sure if this absolutely needed nor if
      // it might actually be detrimental and slow things down.
      session.close();
    }
  }

}
