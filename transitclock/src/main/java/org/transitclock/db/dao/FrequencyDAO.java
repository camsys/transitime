package org.transitclock.db.dao;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.transitclock.db.structs.Frequency;

import java.util.List;

public class FrequencyDAO {
  /**
   * Deletes rev from the Frequencies table
   *
   * @param session
   * @param configRev
   * @return Number of rows deleted
   * @throws HibernateException
   */
  public static int deleteFromRev(Session session, int configRev)
          throws HibernateException {
    // Note that hql uses class name, not the table name
    String hql = "DELETE Frequency WHERE configRev=" + configRev;
    int numUpdates = session.createQuery(hql).executeUpdate();
    return numUpdates;
  }

  /**
   * Returns List of Frequency objects for the specified database revision.
   *
   * @param session
   * @param configRev
   * @return
   * @throws HibernateException
   */
  @SuppressWarnings("unchecked")
  public static List<Frequency> getFrequencies(Session session, int configRev)
          throws HibernateException {
    String hql = "FROM Frequency " +
            "    WHERE configRev = :configRev";
    Query query = session.createQuery(hql);
    query.setInteger("configRev", configRev);
    return query.list();
  }

}
