package org.transitclock.db.dao;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.transitclock.applications.Core;
import org.transitclock.db.structs.*;
import org.transitclock.db.structs.Vector;

import java.util.*;

public class RouteDAO {

  /**
   * Returns List of Route objects for the specified database revision.
   * Orders them based on the GTFS route_order extension or the
   * route short name if route_order not set.
   *
   * @param session
   * @param configRev
   * @return Map of routes keyed on routeId
   * @throws HibernateException
   */
  @SuppressWarnings("unchecked")
  public static List<Route> getRoutes(Session session, int configRev)
          throws HibernateException {
    // Get list of routes from database
    String hql = "FROM Route "
            + "    WHERE configRev = :configRev"
            + "    ORDER BY routeOrder, shortName";
    Query query = session.createQuery(hql);
    query.setInteger("configRev", configRev);
    List<Route> routesList = query.list();

    // Need to set the route order for each route so that can sort
    // predictions based on distance from stop and route order. For
    // the routes that didn't have route ordered configured in db
    // start with 1000 and count on up.
    int routeOrderForWhenNotConfigured = 1000;
    for (Route route: routesList) {
      if (!route.atBeginning() && !route.atEnd()) {
        route.setRouteOrder(routeOrderForWhenNotConfigured++);
      }
    }

    // Return the list of routes
    return routesList;
  }

  public static Collection<Stop> getStops(String id) {
    // Get the trip patterns for the route. Can't use the member
    // variable tripPatternsForRoute since it is only set when the
    // GTFS data is processed and stored in the db. Since this member
    // is transient it is not stored in the db and therefore not
    // available to this client application. But it can be obtained
    // from the DbConfig.
    List<TripPattern> tripPatternsForRoute =
            Core.getInstance().getDbConfig().getTripPatternsForRoute(id);

    // Stop list not yet determined so determine it now using
    // trip patterns.
    Map<String, Stop> stopMap = new HashMap<String, Stop>();
    for (TripPattern tripPattern : tripPatternsForRoute) {
      for (StopPath stopPath : tripPattern.getStopPaths()) {
        String stopId = stopPath.getStopId();

        // If already added this stop then continue to next one
        if (stopMap.containsKey(stopId))
          continue;

        Stop stop = Core.getInstance().getDbConfig().getStop(stopId);
        stopMap.put(stopId, stop);
      }
    }

    // Return the newly created collection of stops
    return stopMap.values();
  }

  public static Collection<Vector> getPathSegments(String id) {
    Collection<Vector> stopPaths = null;
    // Get the trip patterns for the route. Can't use the member
    // variable tripPatternsForRoute since it is only set when the
    // GTFS data is processed and stored in the db. Since this member
    // is transient it is not stored in the db and therefore not
    // available to this client application. But it can be obtained
    // from the DbConfig.
    List<TripPattern> tripPatternsForRoute =
            Core.getInstance().getDbConfig().getTripPatternsForRoute(id);

    Map<String, StopPath> stopPathMap = new HashMap<String, StopPath>();
    for (TripPattern tripPattern : tripPatternsForRoute) {
      for (StopPath stopPath : tripPattern.getStopPaths()) {
        String stopPathId = stopPath.getId();

        // If already added this stop then continue to next one
        if (stopPathMap.containsKey(stopPathId))
          continue;

        stopPathMap.put(stopPathId, stopPath);
      }
    }

    // For each of the unique stop paths add the vectors to the collection
    stopPaths = new ArrayList<Vector>(stopPathMap.values().size());
    for (StopPath stopPath : stopPathMap.values()) {
      for (Vector vector : stopPath.getSegmentVectors()) {
        stopPaths.add(vector);
      }
    }

    // Return the newly created collection of stop paths
    return stopPaths;
  }

  /**
   * Returns the specified trip pattern, or null if that trip pattern doesn't
   * exist for the route.
   *
   * @param tripPatternId
   * @return
   */
  public static TripPattern getTripPattern(String id, String tripPatternId) {
    List<TripPattern> tripPatternsForRoute = Core.getInstance()
            .getDbConfig().getTripPatternsForRoute(id);
    for (TripPattern tripPattern : tripPatternsForRoute) {
      if (tripPattern.getId().equals(tripPatternId))
        return tripPattern;
    }

    // Never found the specified trip pattern
    return null;
  }

  /**
   * Returns longest trip pattern for the directionId specified.
   * Note: gets trip patterns from Core, which means it works
   * in the core application, not just when processing GTFS data.
   *
   * @param directionId
   * @return
   */
  public static TripPattern getLongestTripPatternForDirection(String id, String directionId) {
    List<TripPattern> tripPatternsForRoute = Core.getInstance()
            .getDbConfig().getTripPatternsForRoute(id);
    TripPattern longestTripPatternForDir = null;
    for (TripPattern tripPattern : tripPatternsForRoute) {
      if (Objects.equals(tripPattern.getDirectionId(), directionId)) {
        if (longestTripPatternForDir == null
                || tripPattern.getNumberStopPaths() > longestTripPatternForDir
                .getNumberStopPaths())
          longestTripPatternForDir = tripPattern;
      }
    }

    return longestTripPatternForDir;
  }

  /**
   * Returns the longest trip pattern for each direction ID for the route.
   * Will typically be two trip patterns since there are usually two
   * directions per route.
   *
   * @return
   */
  public static List<TripPattern> getLongestTripPatternForEachDirection(RouteInterface r) {
    List<TripPattern> tripPatterns = new ArrayList<TripPattern>();

    List<String> directionIds = r.getDirectionIds();
    for (String directionId : directionIds)
      tripPatterns.add(getLongestTripPatternForDirection(r.getId(), directionId));

    return tripPatterns;
  }

  public static List<String> getDirectionIds(String id) {
    List<String> directionIds = new ArrayList<String>();
    List<TripPattern> tripPatternsForRoute = Core.getInstance()
            .getDbConfig().getTripPatternsForRoute(id);
    if (tripPatternsForRoute == null) return directionIds;
    for (TripPattern tripPattern : tripPatternsForRoute) {
      String directionId = tripPattern.getDirectionId();
      if (!directionIds.contains(directionId))
        directionIds.add(directionId);
    }
    return directionIds;
  }

  public static List<TripPattern> getTripPatterns(String id) {
    return 	Core.getInstance()
            .getDbConfig().getTripPatternsForRoute(id);
  }

  /**
   * Deletes rev from the Routes table
   *
   * @param session
   * @param configRev
   * @return Number of rows deleted
   * @throws HibernateException
   */
  public static int deleteFromRev(Session session, int configRev)
          throws HibernateException {
    // Note that hql uses class name, not the table name
    String hql = "DELETE Route WHERE configRev=" + configRev;
    int numUpdates = session.createQuery(hql).executeUpdate();
    return numUpdates;
  }


}
