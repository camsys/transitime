package org.transitclock.db.dao;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.ActiveRevisions;

public class ActiveRevisionDAO {

  private static final Logger logger = LoggerFactory
          .getLogger(ActiveRevisionDAO.class);
  /**
   * Gets the ActiveRevisions object using the passed in database session.
   *
   * @param session
   * @return the ActiveRevisions
   * @throws HibernateException
   */
  public static ActiveRevisions get(Session session)
          throws HibernateException {
    // There should only be a single object so don't need a WHERE clause
    String hql = "FROM ActiveRevisions";
    Query query = session.createQuery(hql);
    ActiveRevisions activeRevisions = null;
    try {
      activeRevisions = (ActiveRevisions) query.uniqueResult();
    } catch (Exception e) {
      System.err.println("Exception when reading ActiveRevisions object " +
              "from database so will create it");
    } finally {
      // If couldn't read from db use default values and write the
      // object to the database.
      if (activeRevisions == null) {
        activeRevisions = new ActiveRevisions();
        session.persist(activeRevisions);
      }
    }

    // Return the object
    return activeRevisions;
  }

  /**
   * Reads revisions from database.
   *
   * @param agencyId
   * @return
   * @throws HibernateException
   */
  public static ActiveRevisions get(String agencyId)
          throws HibernateException {
    Session session = null;
    try {
      // Get from db
      session = HibernateUtils.getSession(agencyId);
      ActiveRevisions activeRevisions = get(session);

      // Return the object
      return activeRevisions;
    } catch (HibernateException e) {
      logger.error("Exception in ActiveRevisions.get(). {}",
              e.getMessage(), e);
    } finally {
      // Always make sure session gets closed
      if (session != null)
        session.close();
    }

    return null;
  }

  /**
   * Updates configRev member and calls saveOrUpdate(this) on the session.
   * Useful for when want to update the value but don't want to commit it
   * until all other data is also written out successfully.
   *
   * @param session
   * @param configRev
   */
  public void setConfigRev(Session session, ActiveRevisions ar, int configRev) {
    ar.setConfigRev(configRev);
    session.saveOrUpdate(this);
  }

  /**
   * Updates travelTimeRev member and calls saveOrUpdate(this) on the session.
   * Useful for when want to update the value but don't want to commit it
   * until all other data is also written out successfully.
   *
   * @param session
   * @param travelTimeRev
   */
  public void setTravelTimesRev(Session session, ActiveRevisions ar, int travelTimeRev) {
    ar.setTravelTimesRev(travelTimeRev);
    session.saveOrUpdate(this);
  }

}
