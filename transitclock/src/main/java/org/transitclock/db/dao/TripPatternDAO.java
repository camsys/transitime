package org.transitclock.db.dao;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.TripPattern;

import java.util.List;

public class TripPatternDAO {
  /**
   * Deletes rev from the TripPattern_to_Path_joinTable, StopPaths,
   * and TripPatterns tables.
   *
   * @param session
   * @param configRev
   * @return Number of rows deleted
   * @throws HibernateException
   */
  public static int deleteFromRev(Session session, int configRev)
          throws HibernateException {
    // In a perfect Hibernate world one would simply call on session.delete()
    // for each trip pattern and the join table and the associated trip pattern
    // elements would be automatically deleted by using the magic of Hibernate.
    // But this means that would have to read in all the objects and sub-objects
    // first, which of course takes lots of time and memory, often causing
    // program to crash due to out of memory issue. Therefore
    // using the much, much faster solution of direct SQL calls. Can't use
    // HQL on the join table since it is not a regularly defined table.
    //
    // Would be great to see if can actually use HQL and delete the
    // appropriate TripPatterns and have the join table and the trip pattern
    // elements table be automatically updated. I doubt this would work but
    // would be interesting to try if had the time.
    //
    // Delete from TripPattern_to_Path_joinTable first since it has a foreign
    // key to the StopPath table,
    int rowsUpdated = 0;
    rowsUpdated += session.
            createSQLQuery("DELETE FROM TripPattern_to_Path_joinTable "
                    + "WHERE TripPatterns_configRev=" + configRev).
            executeUpdate();
    rowsUpdated += session.
            createSQLQuery("DELETE FROM StopPaths WHERE configRev="
                    + configRev).
            executeUpdate();
    rowsUpdated += session.
            createSQLQuery("DELETE FROM TripPatterns WHERE configRev="
                    + configRev).
            executeUpdate();
    return rowsUpdated;

//		// Because TripPattern uses a List of Paths things are
//		// complicated because there are multiple tables with foreign keys.
//		// And the join table is not a regular Hibernate table so I don't
//		// believe can use hql to empty it out. Therefore it is best to
//		// read in the objects and then delete them and let Hibernate make
//		// sure it is all done correctly.
//		// NOTE: Unfortunately this is quite slow since have to read in
//		// all the objects first. Might just want to use regular SQL to
//		// delete the items in the TripPattern_to_Path_joinTable
//		List<TripPattern> tripPatternsFromDb = getTripPatterns(session, 0);
//		for (TripPattern tp : tripPatternsFromDb)
//			session.delete(tp);
//		// Need to flush. Otherwise when writing new TripPatterns will get
//		// a uniqueness violation even though already told the session to
//		// delete those objects.
//		session.flush();
//		return tripPatternsFromDb.size();

//		int numUpdates = 0;
//		String hql;
//
//		// Need to first delete the list of Paths
//		hql = "DELETE StopPath WHERE configRev=0";
//		numUpdates += session.createQuery(hql).executeUpdate();
////		hql = "";
////		numUpdates += session.createQuery(hql).executeUpdate();
//
//		// Note that hql uses class name, not the table name
//		hql = "DELETE TripPattern WHERE configRev=0";
//		numUpdates += session.createQuery(hql).executeUpdate();
//		return numUpdates;
  }

  /**
   * Returns list of TripPattern objects for the specified configRev
   *
   * @param session
   * @param configRev
   * @return
   * @throws HibernateException
   */
  @SuppressWarnings("unchecked")
  public static List<TripPattern> getTripPatterns(Session session, int configRev)
          throws HibernateException {
    String hql = "FROM TripPattern " +
            "    WHERE configRev = :configRev";
    Query query = session.createQuery(hql);
    query.setInteger("configRev", configRev);
    return query.list();
  }

  /**
   * Returns list of TripPattern objects for the specified configRev
   *
   * @param configRev
   * @return
   * @throws HibernateException
   */
  @SuppressWarnings("unchecked")
  public static List<TripPattern> getTripPatternsForRoute(String routeShortName, int configRev, boolean readOnly)
          throws HibernateException {
    Session session = HibernateUtils.getSession(readOnly);
    String hql = "FROM TripPattern " +
            "WHERE configRev = :configRev " +
            "AND routeShortName = :routeShortName";
    Query query = session.createQuery(hql);
    query.setInteger("configRev", configRev);
    query.setString("routeShortName", routeShortName);
    return query.list();
  }

}
