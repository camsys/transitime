package org.transitclock.db.dao;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.PredictionForStopPath;

import java.util.Date;
import java.util.List;

public class PredictionForStopPathDAO {

  @SuppressWarnings("unchecked")
  public static List<PredictionForStopPath> getPredictionForStopPathFromDB (
          Date beginTime,
          Date endTime,
          String algorithm,
          String tripId,
          Integer stopPathIndex)
  {
    Session session = HibernateUtils.getSession();
    Criteria criteria = session.createCriteria(PredictionForStopPath.class);

    if(algorithm!=null&&algorithm.length()>0)
      criteria.add(Restrictions.eq("algorithm", algorithm));
    if(tripId!=null)
      criteria.add(Restrictions.eq("tripId", tripId));
    if(stopPathIndex!=null)
      criteria.add(Restrictions.eq("stopPathIndex", stopPathIndex));
    if(beginTime!=null)
      criteria.add(Restrictions.gt("creationTime", beginTime));
    if(endTime!=null)
      criteria.add(Restrictions.lt("creationTime", endTime));
    List<PredictionForStopPath> results=criteria.list();
    if(results.size()>0)
    {
      System.out.println("Got some results");
    }
    return results;
  }

}
