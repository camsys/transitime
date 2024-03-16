package org.transitclock.db.dao;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TravelTimesForTripDAO {

  private static final Logger logger =
          LoggerFactory.getLogger(TravelTimesForTripDAO.class);
  /**
   * Deletes data from the TravelTimesForTrip and the
   * TravelTimesForTrip_to_TravelTimesForPath_jointable.
   *
   * @param session
   * @param configRev
   * @return
   * @throws HibernateException
   */
  public static int deleteFromRev(Session session, int configRev)
          throws HibernateException {
    int totalRowsUpdated = 0;

    // Delete configRev data from TravelTimesForTrip_to_TravelTimesForPath_jointable.
    // This needs to work with at least mySQL and PostgreSQL but they are different.
    // This means that cannot use an INNER JOIN as part of the delete since the
    // syntax for inner joins is different for the two databases. Therefore need to
    // use the IN statement with a SELECT clause.
    int rowsUpdated = session.
            createSQLQuery("DELETE "
                    + " FROM TravelTimesForTrip_to_TravelTimesForPath_joinTable "
                    + "WHERE TravelTimesForTrips_id IN "
                    + "  (SELECT id "
                    + "     FROM TravelTimesForTrips "
                    + "    WHERE configRev=" + configRev
                    + "  )" ).
            executeUpdate();
    logger.info("Deleted {} rows from "
            + "TravelTimesForTrip_to_TravelTimesForPath_joinTable for "
            + "configRev={}", rowsUpdated, configRev);
    totalRowsUpdated += rowsUpdated;

    // Delete configRev data from TravelTimesForStopPaths
    rowsUpdated = session.
            createSQLQuery("DELETE FROM TravelTimesForStopPaths WHERE configRev="
                    + configRev).
            executeUpdate();
    logger.info("Deleted {} rows from TravelTimesForStopPaths for "
            + "configRev={}", rowsUpdated, configRev);
    totalRowsUpdated += rowsUpdated;

    // Delete configRev data from TravelTimesForTrips
    rowsUpdated = session.
            createSQLQuery("DELETE FROM TravelTimesForTrips WHERE configRev="
                    + configRev).
            executeUpdate();
    logger.info("Deleted {} rows from TravelTimesForTrips for configRev={}",
            rowsUpdated, configRev);
    totalRowsUpdated += rowsUpdated;

    return totalRowsUpdated;
  }

  /**
   * Returns Map keyed by tripPatternId of Lists of TravelTimesForTrip. Since
   * there are usually multiple trips per trip pattern the Map contains a List
   * of TravelTimesForTrip instead of just a single one.
   *
   * @param session
   * @param travelTimesRev
   * @return Map keyed by tripPatternId of Lists of TripPatterns
   * @throws HibernateException
   */
  @SuppressWarnings("unchecked")
  public static Map<String, List<org.transitclock.db.structs.TravelTimesForTrip>> getTravelTimesForTrips(
          Session session, int travelTimesRev)
          throws HibernateException {
    logger.info("Reading TravelTimesForTrips for travelTimesRev={} ...",
            travelTimesRev);

    List<org.transitclock.db.structs.TravelTimesForTrip> allTravelTimes = session.createCriteria(org.transitclock.db.structs.TravelTimesForTrip.class)
            .add(Restrictions.eq("travelTimesRev", travelTimesRev))
            .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();

    logger.info("Putting travel times into map...");

    // Now create the map and return it
    Map<String, List<org.transitclock.db.structs.TravelTimesForTrip>> map =
            new HashMap<String, List<org.transitclock.db.structs.TravelTimesForTrip>>();
    for (org.transitclock.db.structs.TravelTimesForTrip travelTimes : allTravelTimes) {
      // Get the List to add the travelTimes to
      String tripPatternId = travelTimes.getTripPatternId();
      List<org.transitclock.db.structs.TravelTimesForTrip> listForTripPattern =
              map.get(tripPatternId);
      if (listForTripPattern == null) {
        listForTripPattern = new ArrayList<org.transitclock.db.structs.TravelTimesForTrip>();
        map.put(tripPatternId, listForTripPattern);
      }

      // Add the travelTimes to the List
      listForTripPattern.add(travelTimes);
    }

    logger.info("Done putting travel times into map.");

    // Return the map containing all the travel times
    return map;
  }

}
