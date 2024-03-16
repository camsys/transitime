package org.transitclock.db.dao;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.ConfigRevision;

import java.time.LocalDateTime;
import java.util.List;

public class ConfigRevisionDAO {

  public static final Logger logger =
          LoggerFactory.getLogger(ConfigRevisionDAO.class);
  /**
   * Stores this ConfigRevision into the database for the agencyId.
   *
   * @param agencyId
   */
  public void save(String agencyId) {
    Session session = HibernateUtils.getSession(agencyId);
    Transaction tx = session.beginTransaction();
    try {
      session.save(this);
      tx.commit();
    } catch (HibernateException e) {
      logger.error("Error saving ConfigRevision data to db. {}",
              this, e);
    } finally {
      session.close();
    }
  }

  public static List<ConfigRevision> getConfigRevisions(Session session, int configRev) throws HibernateException {
    String hql = "From ConfigRevision c ORDER by configRev";
    Query query = session.createQuery(hql);
    return query.list();
  }

  public static List<ConfigRevision> getConfigRevisionsForDateRange(LocalDateTime startTime,
                                                                    LocalDateTime endTime,
                                                                    boolean readOnly) throws HibernateException {


    String hql = "FROM ConfigRevision c " +
            "WHERE c.processedTime between " +
            "(" +
            "SELECT MAX(c2.procssedTime) " +
            "FROM ConfigRevision c2 " +
            "WHERE c2.processedTime < :start " +
            ") " +
            "AND :end " +
            "ORDER BY c.processedTime DESC";

    Session session = HibernateUtils.getSession(readOnly);
    Query query = session.createQuery(hql);
    query.setParameter("start", startTime);
    query.setParameter("end", endTime);
    return query.list();
  }

  public static List<ConfigRevision> getConfigRevisionsForMaxDate( LocalDateTime endTime,
                                                                   boolean readOnly) throws HibernateException {


    String hql = "FROM ConfigRevision c " +
            "WHERE c.processedTime < :end " +
            "ORDER BY c.processedTime DESC";

    Session session = HibernateUtils.getSession(readOnly);
    Query query = session.createQuery(hql);
    query.setParameter("end", java.sql.Timestamp.valueOf(endTime));
    query.setMaxResults(1);
    return query.list();
  }

}
