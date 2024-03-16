package org.transitclock.db.dao;

import org.hibernate.Query;
import org.hibernate.Session;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.ApcReport;

import java.util.Date;
import java.util.List;

public class ApcReportDAO {

  public static List<ApcReport> getApcReportsFromDb(String projectId, Date beginTime, Date endTime) {
    Session session = HibernateUtils.getSession(projectId);
    String hql = "FROM ApcReport " +
            " WHERE time >= :beginDate " +
            " AND time < :endDate";
    Query query = session.createQuery(hql);
    query.setTimestamp("beginDate", beginTime);
    query.setTimestamp("endDate", endTime);

    try {
      return query.list();
    } finally {
      session.close();
    }
  }

}
