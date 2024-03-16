package org.transitclock.db.dao;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.transitclock.applications.Core;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.AvlReport;

import java.util.Date;
import java.util.List;

public class AvlReportDAO {

  /**
   * Gets list of AvlReports from database for the time span specified.
   *
   * @param beginTime
   * @param endTime
   * @param vehicleId
   *            Optional. If not null then will only return results for that
   *            vehicle
   * @param clause
   * 				Optional. If not null then the clause, such as "ORDER BY time"
   * will be added to the hql statement.
   * @return List of AvlReports or null if an exception is thrown
   */
  public static List<AvlReport> getAvlReportsFromDb(
          Date beginTime,
          Date endTime,
          String vehicleId,
          String clause) {
    // Sessions are not threadsafe so need to create a new one each time.
    // They are supposed to be lightweight so this should be OK.
    Session session = HibernateUtils.getSession();

    // Create the query. Table name is case sensitive!
    String hql = "FROM AvlReport " +
            "    WHERE time >= :beginDate " +
            "      AND time < :endDate";
    if (vehicleId != null && !vehicleId.isEmpty())
      hql += " AND vehicleId=:vehicleId";
    if (clause != null)
      hql += " " + clause;
    Query query = session.createQuery(hql);

    // Set the parameters
    if (vehicleId != null && !vehicleId.isEmpty())
      query.setString("vehicleId", vehicleId);
    query.setTimestamp("beginDate", beginTime);
    query.setTimestamp("endDate", endTime);

    try {
      @SuppressWarnings("unchecked")
      List<AvlReport> avlReports = query.list();
      return avlReports;
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
