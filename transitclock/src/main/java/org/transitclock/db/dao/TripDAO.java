package org.transitclock.db.dao;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.collection.internal.PersistentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.configData.AgencyConfig;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.query.TripQuery;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.Trip;
import org.transitclock.db.structs.TripInterface;
import org.transitclock.utils.IntervalTimer;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TripDAO {

  private static final Logger logger =
          LoggerFactory.getLogger(TripDAO.class);


  public static synchronized void initialize(Trip trip) {
    initialize(HibernateUtils.getSessionForThread(AgencyConfig.getAgencyId()), trip);
  }
  /**
   * Force any lazy-loaded objects to be loaded now before moving to another thread.
   */
  public static synchronized Trip initialize(Session session, Trip trip) {
    if (trip.isInitialized()) return trip;
    if (((PersistentList)trip.getScheduleTimes()).getSession().isConnected()) {
      return initializeSingleTrip(session, trip);
    }
    try {
      if (!Hibernate.isInitialized(trip.getScheduleTimes())) {
        Hibernate.initialize(trip.getScheduleTimes());
      }
    } catch (Throwable t) {
      logger.error("unable to load schedule times for trip {}", trip, t);
    }
    try {
      if (!Hibernate.isInitialized(trip.getTripPattern())) {
        Hibernate.initialize(trip.getTripPattern());
      }
    } catch (Throwable t) {
      logger.error("unable to load trip pattern for trip {}", trip, t);
    }
    try {
      if (!Hibernate.isInitialized(trip.getTravelTimes())) {
        Hibernate.initialize(trip.getTravelTimes());
      }
    } catch (Throwable t) {
      logger.error("unable to load travel times for trip {}", trip, t);
    }
    try {
      if (trip.getTravelTimes() != null && !Hibernate.isInitialized(trip.getTravelTimes().getTravelTimesForStopPaths())) {
        Hibernate.initialize(trip.getTravelTimes().getTravelTimesForStopPaths());
      }
    } catch (Throwable t) {
      logger.error("unable to load stop path travel times for trip {}", trip, t);
    }
    trip.setInitialized();
    return trip;
  }

  // load (hydrate) this trip and all properties and collections.
  private static Trip initializeSingleTrip(Session session, Trip trip) {
    Trip returnTrip = (Trip) session.load(Trip.class, trip);
    if (returnTrip == null) {
      logger.error("trip not found {}", trip.getId());
      return null;
    }

    Hibernate.initialize(returnTrip.getScheduleTimes());
    Hibernate.initialize(returnTrip.getTripPattern());
    Hibernate.initialize(returnTrip.getTravelTimes());
    if (returnTrip.getTravelTimes() != null)
      Hibernate.initialize(returnTrip.getTravelTimes().getTravelTimesForStopPaths());
    returnTrip.setInitialized();

    return returnTrip;
  }


  // fully load trip/block on this thread
  public static synchronized List<TripInterface> refreshTrips(List<TripInterface> potentialTrips, Session sessionForThread) {
    List<TripInterface> loadedTrips = new ArrayList<>();
    for (TripInterface potentialTrip : potentialTrips) {
      if (potentialTrip.getBlock() != null) {
        Block potentialBlock = potentialTrip.getBlock().initialize(sessionForThread);
        Trip loadedTrip = potentialBlock.getTrip(potentialTrip.getId());
        if (loadedTrip != null) {
          loadedTrips.add(loadedTrip);
        } else {
          logger.error("missing trip {} for block {} with index", potentialTrip.getId(), potentialBlock.getId(), potentialTrip.getIndexInBlock());
        }
      }
    }
    return loadedTrips;
  }

  /**
   * Returns map of Trip objects for the specified configRev. The
   * map is keyed on the trip IDs.
   *
   * @param session
   * @param configRev
   * @return
   * @throws HibernateException
   */
  @SuppressWarnings("unchecked")
  public static Map<String, Trip> getTrips(Session session, int configRev)
          throws HibernateException {
    // Setup the query
    String hql = "FROM Trip " +
            "    WHERE configRev = :configRev";
    Query query = session.createQuery(hql);
    query.setInteger("configRev", configRev);

    // Actually perform the query
    List<Trip> tripsList = query.list();

    // Now put the Trips into a map and return it
    Map<String, Trip> tripsMap = new HashMap<String, Trip>();
    for (Trip trip : tripsList) {
      tripsMap.put(trip.getId(), trip);
    }
    return tripsMap;
  }

  /**
   * Returns specified Trip object for the specified configRev and tripId.
   *
   * @param session
   * @param configRev
   * @param tripId
   * @return
   * @throws HibernateException
   */
  public static Trip getTrip(Session session, int configRev, String tripId)
          throws HibernateException {
    // Setup the query
    String hql = "FROM Trip t " +
            " left join fetch t.scheduledTimesList " +
            " left join fetch t.travelTimes " +
            " WHERE t.configRev = :configRev" +
            " AND tripId = :tripId";
    Query query = session.createQuery(hql);
    query.setInteger("configRev", configRev);
    query.setString("tripId", tripId);

    // Actually perform the query
    Trip trip = (Trip) query.uniqueResult();

    return trip;
  }

  /**
   * Returns list of Trip objects for the specified configRev and
   * tripShortName. There can be multiple trips for a tripShortName since can
   * have multiple service IDs configured. Therefore a list must be returned.
   *
   * @param session
   * @param configRev
   * @param tripShortName
   * @return list of trips for specified configRev and tripShortName
   * @throws HibernateException
   */
  public static List<Trip> getTripByShortName(Session session, int configRev,
                                              String tripShortName) throws HibernateException {
    // Setup the query
    String hql = "FROM Trip t " +
            "   left join fetch t.scheduledTimesList " +
            "   left join fetch t.travelTimes " +
            "    WHERE t.configRev = :configRev" +
            "      AND t.tripShortName = :tripShortName";
    Query query = session.createQuery(hql);
    query.setInteger("configRev", configRev);
    query.setString("tripShortName", tripShortName);

    // Actually perform the query
    @SuppressWarnings("unchecked")
    List<Trip> trips = query.list();

    return trips;
  }

  /**
   * Deletes rev from the Trips table
   *
   * @param session
   * @param configRev
   * @return Number of rows deleted
   * @throws HibernateException
   */
  public static int deleteFromRev(Session session, int configRev)
          throws HibernateException {
    int rowsUpdated = 0;
    rowsUpdated += session.
            createSQLQuery("DELETE FROM Trips WHERE configRev="
                    + configRev).
            executeUpdate();
    return rowsUpdated;
  }

  /**
   * Query how many travel times for trips entries exist for a given
   * travelTimesRev.  Used for metrics.
   * @param session
   * @param travelTimesRev
   * @return
   */
  public static Long countTravelTimesForTrips(Session session,
                                              int travelTimesRev) {
    String sql = "Select count(*) from TravelTimesForTrips where travelTimesRev=:rev";

    Query query = session.createSQLQuery(sql);
    query.setInteger("rev", travelTimesRev);
    Long count = null;
    try {

      Integer bcount;
      if(query.uniqueResult() instanceof BigInteger)
      {
        bcount = ((BigInteger)query.uniqueResult()).intValue();
      }else
      {
        bcount = (Integer) query.uniqueResult();
      }

      if (bcount != null)
        count = bcount.longValue();
    } catch (HibernateException e) {
      Core.getLogger().error("exception querying for metrics", e);
    }
    return count;
  }

  public static List<Trip> getTripsFromDb(TripQuery tripQuery) {
    IntervalTimer timer = new IntervalTimer();

    // Get the database session. This is supposed to be pretty light weight
    Session session = HibernateUtils.getSession(tripQuery.isReadOnly());

    Map<String, Object> parameterNameAndValues = new HashMap<>();

    // Create the query. Table name is case sensitive and needs to be the
    // class name instead of the name of the db table.

    String hql = "SELECT DISTINCT t " +
            "FROM Trip t " +
            "LEFT JOIN fetch t.scheduledTimesList " +
            "WHERE t.routeShortName = :routeShortName " +
            "AND t.configRev IN (:configRevs) " +
            getHeadsignWhere(tripQuery, parameterNameAndValues) +
            getDirectionWhere(tripQuery, parameterNameAndValues) +
            getTripPatternWhere(tripQuery, parameterNameAndValues) +
            getStartTimeWhere(tripQuery, parameterNameAndValues) +
            "ORDER BY t.startTime";
    try {
      Query query = session.createQuery(hql);

      query.setParameter("routeShortName", tripQuery.getRouteShortName());
      query.setParameterList("configRevs", new ArrayList(tripQuery.getConfigRevs()));

      for (Map.Entry<String, Object> e : parameterNameAndValues.entrySet()) {
        query.setParameter(e.getKey(), e.getValue());
      }

      List<Trip> results = query.list();

      logger.debug("Getting trips from database took {} msec",
              timer.elapsedMsec());

      return results;

    } catch (Exception e) {
      // Log error to the Core logger
      Core.getLogger().error("Unable to retrieve trips", e);
      return null;
    } finally {
      // Clean things up. Not sure if this absolutely needed nor if
      // it might actually be detrimental and slow things down.
      session.close();
    }
  }

  private static String getHeadsignWhere(TripQuery tripQuery, Map<String, Object> parameterNameAndValues){
    if(StringUtils.isNotBlank(tripQuery.getHeadsign())){
      parameterNameAndValues.put("headsign", tripQuery.getHeadsign());
      return  "AND t.headsign = :headsign ";
    }
    return "";
  }

  private static String getDirectionWhere(TripQuery tripQuery, Map<String, Object> parameterNameAndValues){
    if(StringUtils.isNotBlank(tripQuery.getDirection())){
      parameterNameAndValues.put("directionId", tripQuery.getDirection());
      return "AND t.directionId = :directionId ";
    }
    return "";
  }

  private static String getTripPatternWhere(TripQuery tripQuery, Map<String, Object> parameterNameAndValues){
    if(StringUtils.isNotBlank(tripQuery.getDirection())){
      parameterNameAndValues.put("tripPatternId", tripQuery.getTripPatternId());
      return "AND t.tripPatternId = :tripPatternId ";
    }
    return "";
  }

  private static String getStartTimeWhere(TripQuery tripQuery, Map<String, Object> parameterNameAndValues){
    String startTime = "";
    if(tripQuery.getFirstStartTime() != null
            && (tripQuery.getLastStartTime() == null || tripQuery.getFirstStartTime() < tripQuery.getLastStartTime())){
      parameterNameAndValues.put("firstStartTime", tripQuery.getFirstStartTime());
      startTime += "AND t.startTime >= :firstStartTime ";
    }
    if(tripQuery.getLastStartTime() != null
            && (tripQuery.getFirstStartTime() == null || tripQuery.getFirstStartTime() < tripQuery.getLastStartTime())){
      parameterNameAndValues.put("lastStartTime", tripQuery.getLastStartTime());
      startTime += "AND t.startTime <= :lastStartTime ";
    }
    return startTime;
  }

}
