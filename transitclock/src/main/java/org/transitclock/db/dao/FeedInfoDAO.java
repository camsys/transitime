package org.transitclock.db.dao;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.transitclock.db.structs.FeedInfo;

import java.util.List;

public class FeedInfoDAO {

  /**
   * Deletes rev 0 from the Transfers table
   *
   * @param session
   * @param configRev
   * @return Number of rows deleted
   * @throws HibernateException
   */
  public static int deleteFromRev(Session session, int configRev)
          throws HibernateException {
    // Note that hql uses class name, not the table name
    String hql = "DELETE FeedInfo WHERE configRev=" + configRev;
    int numUpdates = session.createQuery(hql).executeUpdate();
    return numUpdates;
  }

  /**
   * Returns List of Transfer objects for the specified database revision.
   *
   * @param session
   * @param configRev
   * @return
   * @throws HibernateException
   */
  @SuppressWarnings("unchecked")
  public static List<FeedInfo> getFeedInfo(Session session, int configRev)
          throws HibernateException {
    String hql = "FROM FeedInfo " +
            "    WHERE configRev = :configRev";
    Query query = session.createQuery(hql);
    query.setInteger("configRev", configRev);
    return query.list();
  }

  public static List<FeedInfo> getFeedInfos(Session session) throws HibernateException {
    String hql = "From FeedInfo f ORDER by feedStartDate, feedEndDate";
    Query query = session.createQuery(hql);
    return query.list();
  }

}
