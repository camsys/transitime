package org.transitclock.db.dao;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.configData.DbSetupConfig;
import org.transitclock.core.ServiceType;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.query.ArrivalDepartureQuery;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.utils.IntervalTimer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ArrivalDepartureDAO {

  private static final Logger logger = LoggerFactory
          .getLogger(ArrivalDepartureDAO.class);

  private static DateTimeFormatter isoDateTimeFormat = DateTimeFormatter.ISO_DATE_TIME;


  /**
   * For querying large amount of data. With a Hibernate Iterator not
   * all the data is read in at once. This means that can iterate over
   * a large dataset without running out of memory. But this can be slow
   * because when using iterate() an initial query is done to get all of
   * Id column data and then a separate query is done when iterating
   * over each row. Doing an individual query per row is of course
   * quite time consuming. Better to use getArrivalsDeparturesFromDb()
   * with a fairly large batch size of ~50000.
   * <p>
   * Note that the session needs to be closed externally once done with
   * the Iterator.
   *
   * @param session
   * @param beginTime
   * @param endTime
   * @return
   * @throws HibernateException
   */
  public static Iterator<ArrivalDeparture> getArrivalsDeparturesDbIterator(
          Session session, Date beginTime, Date endTime)
          throws HibernateException {
    // Create the query. Table name is case sensitive and needs to be the
    // class name instead of the name of the db table.
    String hql = "FROM ArrivalDeparture " +
            "    WHERE time >= :beginDate " +
            "      AND time < :endDate";
    Query query = session.createQuery(hql);

    // Set the parameters
    query.setTimestamp("beginDate", beginTime);
    query.setTimestamp("endDate", endTime);

    @SuppressWarnings("unchecked")
    Iterator<ArrivalDeparture> iterator = query.iterate();
    return iterator;
  }

  /**
   * Read in arrivals and departures for a vehicle, over a time range.
   *
   * @param beginTime
   * @param endTime
   * @param vehicleId
   * @return
   */
  public static List<ArrivalDeparture> getArrivalsDeparturesFromDb(
          Date beginTime, Date endTime, String vehicleId) {
    // Call in standard getArrivalsDeparturesFromDb() but pass in
    // sql clause
    return getArrivalsDeparturesFromDb(
            null,  // Use db specified by transitclock.db.dbName
            beginTime, endTime,
            "AND vehicleId='" + vehicleId + "'",
            0, 0,  // Don't use batching
            null,  // Read both arrivals and departures
            false);
  }

  /**
   * Reads in arrivals and departures for a particular trip and service. Create session and uses it
   *
   * @param beginTime
   * @param endTime
   * @param tripId
   * @param serviceId
   * @return
   */
  public static List<ArrivalDeparture> getArrivalsDeparturesFromDb(Date beginTime, Date endTime, String tripId, String serviceId)
  {
    Session session = HibernateUtils.getSession();

    return ArrivalDepartureDAO.getArrivalsDeparturesFromDb(session, beginTime, endTime, tripId, serviceId);
  }

  /**
   * Reads in arrivals and departures for a particular trip and service. Uses session provided
   *
   * @paran session
   * @param beginTime
   * @param endTime
   * @param tripId
   * @param serviceId
   * @return
   */
  public static List<ArrivalDeparture> getArrivalsDeparturesFromDb(Session session, Date beginTime, Date endTime, String tripId, String serviceId)
  {
    Criteria criteria = session.createCriteria(ArrivalDeparture.class);

    criteria.add(Restrictions.eq( "tripId",tripId ));
    criteria.add(Restrictions.gt("time", beginTime));
    criteria.add(Restrictions.lt("time",endTime)).list();

    if(serviceId!=null)
      criteria.add(Restrictions.eq( "serviceId",serviceId ));

    @SuppressWarnings("unchecked")
    List<ArrivalDeparture> arrivalsDeparatures=criteria.list();
    return arrivalsDeparatures;

  }
  /**
   * Reads in arrivals and departures for a particular stopPathIndex of a trip between two dates. Uses session provided
   *
   * @paran session
   * @param beginTime
   * @param endTime
   * @param tripId
   * @param stopPathIndex
   * @return
   */
  public static List<ArrivalDeparture> getArrivalsDeparturesFromDb(Session session, Date beginTime, Date endTime, String tripId, Integer stopPathIndex)
  {
    Criteria criteria = session.createCriteria(ArrivalDeparture.class);

    if(tripId!=null)
    {
      criteria.add(Restrictions.eq( "tripId",tripId ));

      if(stopPathIndex!=null)
        criteria.add(Restrictions.eq( "stopPathIndex",stopPathIndex ));
    }

    criteria.add(Restrictions.gt("time", beginTime));
    criteria.add(Restrictions.lt("time",endTime)).list();



    @SuppressWarnings("unchecked")
    List<ArrivalDeparture> arrivalsDeparatures=criteria.list();
    return arrivalsDeparatures;

  }
  /**
   * Reads the arrivals/departures for the timespan specified. All of the
   * data is read in at once so could present memory issue if reading
   * in a very large amount of data. For that case probably best to instead
   * use getArrivalsDeparturesDb() where one specifies the firstResult and
   * maxResult parameters.
   *
   * @param projectId
   * @param beginTime
   * @param endTime
   * @return
   */
  public static List<ArrivalDeparture> getArrivalsDeparturesFromDb(
          String projectId, Date beginTime, Date endTime) {
    IntervalTimer timer = new IntervalTimer();

    // Get the database session. This is supposed to be pretty light weight
    Session session = HibernateUtils.getSession(projectId);

    // Create the query. Table name is case sensitive and needs to be the
    // class name instead of the name of the db table.
    String hql = "FROM ArrivalDeparture " +
            "    WHERE time >= :beginDate " +
            "      AND time < :endDate";
    Query query = session.createQuery(hql);

    // Set the parameters
    query.setTimestamp("beginDate", beginTime);
    query.setTimestamp("endDate", endTime);

    try {
      @SuppressWarnings("unchecked")
      List<ArrivalDeparture> arrivalsDeparatures = query.list();
      logger.debug("Getting arrival/departures from database took {} msec",
              timer.elapsedMsec());
      return arrivalsDeparatures;
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

  /**
   * Allows batch retrieval of data. This is likely the best way to read in
   * large amounts of data. Using getArrivalsDeparturesDbIterator() reads in
   * only data as needed so good with respect to memory usage but it does a
   * separate query for each row. Reading in list of all data is quick but can
   * cause memory problems if reading in a very large amount of data. This
   * method is a good compromise because it only reads in a batch of data at a
   * time so is not as memory intensive yet it is quite fast. With a batch
   * size of 50k found it to run in under 1/4 the time as with the iterator
   * method.
   *
   * @param dbName
   *            Name of the database to retrieve data from. If set to null
   *            then will use db name configured by Java property
   *            transitclock.db.dbName
   * @param beginTime
   * @param endTime
   * @param sqlClause
   *            The clause is added to the SQL for retrieving the
   *            arrival/departures. Useful for ordering the results. Can be
   *            null.
   * @param firstResult
   *            For when reading in batch of data at a time.
   * @param maxResults
   *            For when reading in batch of data at a time. If set to 0 then
   *            will read in all data at once.
   * @param arrivalOrDeparture
   *            Enumeration specifying whether to read in just arrivals or
   *            just departures. Set to null to read in both.
   * @return List<ArrivalDeparture> or null if there is an exception
   */
  public static List<ArrivalDeparture> getArrivalsDeparturesFromDb(
          String dbName, Date beginTime, Date endTime,
          String sqlClause,
          final Integer firstResult, final Integer maxResults,
          ArrivalDeparture.ArrivalsOrDepartures arrivalOrDeparture, boolean readOnly) {
    IntervalTimer timer = new IntervalTimer();

    // Get the database session. This is supposed to be pretty light weight
    Session session = dbName != null ? HibernateUtils.getSession(dbName, readOnly) : HibernateUtils.getSession(true);

    // Create the query. Table name is case sensitive and needs to be the
    // class name instead of the name of the db table.
    String hql = "FROM ArrivalDeparture " +
            "    WHERE time between :beginDate " +
            "      AND :endDate";
    if (arrivalOrDeparture != null) {
      if (arrivalOrDeparture == ArrivalDeparture.ArrivalsOrDepartures.ARRIVALS)
        hql += " AND isArrival = true";
      else
        hql += " AND isArrival = false";
    }
    if (sqlClause != null)
      hql += " " + sqlClause;
    Query query = session.createQuery(hql);

    // Set the parameters for the query
    query.setTimestamp("beginDate", beginTime);
    query.setTimestamp("endDate", endTime);

    // Only get a batch of data at a time if maxResults specified
    if (firstResult != null) {
      query.setFirstResult(firstResult);
    }
    if (maxResults != null && maxResults > 0) {
      query.setMaxResults(maxResults);
    }

    try {
      @SuppressWarnings("unchecked")
      List<ArrivalDeparture> arrivalsDeparatures = query.list();
      logger.debug("Getting arrival/departures from database took {} msec",
              timer.elapsedMsec());
      return arrivalsDeparatures;
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

  public static Long getArrivalsDeparturesCountFromDb(
          String dbName, Date beginTime, Date endTime,
          ArrivalDeparture.ArrivalsOrDepartures arrivalOrDeparture, boolean readOnly) {
    IntervalTimer timer = new IntervalTimer();
    Long count = null;
    // Get the database session. This is supposed to be pretty light weight
    Session session = dbName != null ? HibernateUtils.getSession(dbName, false) : HibernateUtils.getSession(true);

    // Create the query. Table name is case sensitive and needs to be the
    // class name instead of the name of the db table.
    String hql = "select count(*) FROM ArrivalDeparture " +
            "    WHERE time >= :beginDate " +
            "      AND time < :endDate";
    if (arrivalOrDeparture != null) {
      if (arrivalOrDeparture == ArrivalDeparture.ArrivalsOrDepartures.ARRIVALS)
        hql += " AND isArrival = true";
      else
        hql += " AND isArrival = false";
    }

    Query query = session.createQuery(hql);

    // Set the parameters for the query
    query.setTimestamp("beginDate", beginTime);
    query.setTimestamp("endDate", endTime);


    try {
      count = (Long) query.uniqueResult();
      logger.debug("Getting arrival/departures from database took {} msec",
              timer.elapsedMsec());
      return count;
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


  /**
   * Same as other getArrivalsDeparturesFromDb() but uses
   * -Dtransitclock.db.dbName Java property to specify the name of the database.
   *
   * @param beginTime
   * @param endTime
   * @param sqlClause
   * @param firstResult
   * @param maxResults
   * @param arrivalOrDeparture
   * @return List<ArrivalDeparture> or null if there is an exception
   */
  public static List<ArrivalDeparture> getArrivalsDeparturesFromDb(
          Date beginTime, Date endTime, String sqlClause,
          final int firstResult, final int maxResults,
          ArrivalDeparture.ArrivalsOrDepartures arrivalOrDeparture) {
    return getArrivalsDeparturesFromDb(DbSetupConfig.getDbName(), beginTime,
            endTime, sqlClause, firstResult, maxResults, arrivalOrDeparture, false);
  }

  public static List<ArrivalDeparture> getArrivalsDeparturesFromDb(LocalDate beginDate, LocalDate endDate,
                                                                   LocalTime beginTime, LocalTime endTime,
                                                                   String routeShortName, String headsign,
                                                                   ServiceType serviceType, boolean timePointsOnly,
                                                                   boolean scheduledTimesOnly, boolean dwellTimeOnly,
                                                                   boolean includeTrip, boolean includeStop,
                                                                   boolean includeStopPath, boolean readOnly) throws Exception {
    return getArrivalsDeparturesFromDb(beginDate, endDate, beginTime, endTime, routeShortName, headsign,
            null, null, serviceType, timePointsOnly, scheduledTimesOnly, dwellTimeOnly,
            includeTrip, includeStop, includeStopPath, readOnly);
  }


  /**
   * Reads the arrivals/departures for the timespan and routeId specified
   * Can specify whether you want to retrieve the data from a readOnly db
   *
   * @param beginTime
   * @param endTime
   * @param routeShortName
   * @param serviceType
   * @param timePointsOnly
   * @param readOnly
   * @return
   */
  public static List<ArrivalDeparture> getArrivalsDeparturesFromDb(LocalDate beginDate, LocalDate endDate,
                                                                   LocalTime beginTime, LocalTime endTime,
                                                                   String routeShortName, String headsign,
                                                                   String startStop, String endStop,
                                                                   ServiceType serviceType, boolean timePointsOnly,
                                                                   boolean scheduledTimesOnly, boolean dwellTimeOnly,
                                                                   boolean includeTrip, boolean includeStop,
                                                                   boolean includeStopPath, boolean readOnly) throws Exception {
    IntervalTimer timer = new IntervalTimer();

    // Get the database session. This is supposed to be pretty light weight
    Session session = HibernateUtils.getSession(readOnly);

    // Create the query. Table name is case sensitive and needs to be the
    // class name instead of the name of the db table.

    String hql = "SELECT " +
            "ad " +
            "FROM " +
            "ArrivalDeparture ad " +
            getTimePointsJoin(timePointsOnly) +
            getServiceTypeJoin(serviceType) +
            getStopsJoin(includeStop) +
            getTripsJoin(headsign, includeTrip) +
            getStopPathsJoin(includeStopPath) +
            "WHERE " +
            getArrivalDepartureTimeWhere(beginDate, endDate, beginTime, endTime) +
            getRouteWhere(routeShortName) +
            getTripPatternWhere(null) +
            getScheduledTimesWhere(scheduledTimesOnly) +
            getTimePointsWhere(timePointsOnly) +
            getServiceTypeWhere(serviceType) +
            getTripsWhere(headsign, includeTrip) +
            getStopsWhere(includeStop) +
            getStopPathsWhere(includeStopPath) +
            getDwellTimesWhere(dwellTimeOnly) +
            "ORDER BY ad.time, ad.stopPathIndex, ad.isArrival DESC";

    try {
      Query query = session.createQuery(hql);

      List<ArrivalDeparture> results = query.list();

      logger.debug("Getting arrival/departures from database took {} msec",
              timer.elapsedMsec());

      return results;

    } catch (HibernateException e) {
      // Log error to the Core logger
      Core.getLogger().error("Unable to retrieve arrival departures", e);
      return null;
    } finally {
      // Clean things up. Not sure if this absolutely needed nor if
      // it might actually be detrimental and slow things down.
      session.close();
    }
  }


  /**
   * Reads the arrivals/departures for the timespan and routeId specified
   * Can specify whether you want to retrieve the data from a readOnly db
   *
   * @param adQuery {@link org.transitclock.db.query.ArrivalDepartureQuery}
   * @return List<ArrivalDeparture>
   */
  public static List<ArrivalDeparture> getArrivalsDeparturesFromDb(ArrivalDepartureQuery adQuery) throws Exception {
    IntervalTimer timer = new IntervalTimer();

    // Get the database session. This is supposed to be pretty light weight
    Session session = HibernateUtils.getSession(adQuery.isReadOnly());

    // Create the query. Table name is case sensitive and needs to be the
    // class name instead of the name of the db table.

    String hql = "SELECT " +
            "ad " +
            "FROM " +
            "ArrivalDeparture ad " +
            getServiceTypeJoin(adQuery.getServiceType()) +
            getStopsJoin(adQuery.isIncludeStop()) +
            getTripsJoin(adQuery.getHeadsign(), adQuery.isIncludeTrip()) +
            getStopPathsJoin(adQuery.isIncludeStopPath()) +
            "WHERE " +
            getArrivalDepartureTimeWhere(adQuery.getBeginDate(), adQuery.getEndDate(), adQuery.getBeginTime(), adQuery.getEndTime()) +
            getRouteWhere(adQuery.getRouteShortName()) +
            getTripPatternWhere(adQuery.getTripPatternId()) +
            getTripIdsWhere(adQuery.getTripIds()) +
            getScheduledTimesWhere(adQuery.isScheduledTimesOnly()) +
            getTimePointsWhere(adQuery.isTimePointsOnly()) +
            getServiceTypeWhere(adQuery.getServiceType()) +
            getTripsWhere(adQuery.getHeadsign(), adQuery.isIncludeTrip()) +
            getStopsWhere(adQuery.isIncludeStop()) +
            getStopPathsWhere(adQuery.isIncludeStopPath()) +
            getDwellTimesWhere(adQuery.isDwellTimeOnly()) +
            "ORDER BY ad.time, ad.stopPathIndex, ad.isArrival DESC";

    try {
      Query query = session.createQuery(hql);
      if(adQuery.getTripIds() != null) {
        query.setParameterList("tripIds", adQuery.getTripIds());
      }

      List<ArrivalDeparture> results = query.list();

      logger.debug("Getting arrival/departures from database took {} msec",
              timer.elapsedMsec());

      return results;

    } catch (HibernateException e) {
      // Log error to the Core logger
      Core.getLogger().error("Unable to retrieve arrival departures", e);
      return null;
    } finally {
      // Clean things up. Not sure if this absolutely needed nor if
      // it might actually be detrimental and slow things down.
      session.close();
    }
  }

  /**
   * Helper HQL methods for getArrivalsDeparturesFromDb
   * Broken down into JOIN methods and WHERE methods
   * /

   /*
   * JOIN HQL STATEMENTS
   */
  private static String getTimePointsJoin(boolean timePointsOnly){
    if(!timePointsOnly){
      return "";
    }
    return ", StopPath sp ";
  }

  private static String getServiceTypeJoin(ServiceType serviceType){
    if(serviceType == null){
      return "";
    }
    return ", Calendar c ";
  }

  private static String getStopsJoin(boolean includeStop){
    if(includeStop){
      return "JOIN FETCH ad.stop s ";
    }
    return "";
  }

  private static String getTripsJoin(String headsign, boolean includeTrip){
    if(includeTrip){
      return " JOIN FETCH ad.trip t ";
    }else if(StringUtils.isNotBlank(headsign)){
      return ", Trip t ";
    }

    return "";
  }

  private static String getStopPathsJoin(boolean includeStopPath) {
    if(includeStopPath){
      return "JOIN FETCH ad.stopPath sp ";
    }
    return "";
  }

  /*
   * WHERE HQL STATEMENTS
   */
  public static String getArrivalDepartureTimeWhere(LocalDate beginDate, LocalDate endDate, LocalTime beginTime, LocalTime endTime) {
    String hql = "";

    if(beginTime != null && endTime != null) {
      List<LocalDate> dates = new ArrayList<>();
      while (!beginDate.isAfter(endDate)) {
        dates.add(beginDate);
        beginDate = beginDate.plusDays(1);
      }

      for (int i = 0; i < dates.size(); i++) {
        if (i == 0) {
          hql += " (";

        } else {
          hql += " OR ";
        }
        LocalDateTime startDateTime = LocalDateTime.of(dates.get(i), beginTime);
        LocalDateTime endDateTime = LocalDateTime.of(dates.get(i), endTime);
        hql += String.format(" ad.time between '%s' AND '%s' ",
                startDateTime.format(isoDateTimeFormat), endDateTime.format(isoDateTimeFormat));
        if (i == dates.size() - 1) {
          hql += ") ";
        }
      }
    }
    else if(!beginDate.isAfter(endDate)) {
      LocalDateTime startDateTime = beginDate.atStartOfDay();
      LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
      hql += String.format(" ad.time between '%s' AND '%s' ",
              startDateTime.format(isoDateTimeFormat), endDateTime.format(isoDateTimeFormat));
    }
    else{
      LocalDateTime startDateTime = beginDate.atStartOfDay();
      LocalDateTime endDateTime = beginDate.atTime(LocalTime.MAX);
      hql += String.format(" ad.time between '%s' AND '%s' ",
              startDateTime.format(isoDateTimeFormat), endDateTime.format(isoDateTimeFormat));
    }
    return hql;
  }

  private static String getRouteWhere(String routeShortName){
    if(StringUtils.isNotBlank(routeShortName) && routeShortName !=null) {
      return String.format("AND ad.routeShortName = '%s' ", routeShortName);
    }
    return "";
  }

  private static String getTripPatternWhere(String tripPatternId){
    if(StringUtils.isNotBlank(tripPatternId)) {
      return String.format("AND ad.tripPatternId = '%s' ", tripPatternId);
    }
    return "";
  }

  private static String getTripIdsWhere(Set<String> tripIds){
    if(tripIds != null) {
      return "AND ad.tripId IN (:tripIds)";
    }
    return "";
  }

  private static String getScheduledTimesWhere(boolean scheduledTimesOnly){
    if(scheduledTimesOnly){
      return "AND ad.scheduledTime IS NOT NULL ";
    }
    return "";
  }

  private static String getTimePointsWhere(boolean timePointsOnly){
    if(timePointsOnly){
      return "AND ad.scheduleAdherenceStop = true ";
    }
    return "";
  }

  private static String getServiceTypeWhere(ServiceType serviceType){
    if(serviceType != null) {
      String query = "AND ad.serviceId = c.serviceId AND ad.configRev = c.configRev ";
      if (serviceType.equals(ServiceType.WEEKDAY)) {
        query += "AND (c.monday = true OR c.tuesday = true OR c.wednesday = true OR c.thursday = true OR c.friday = true)";
      } else if (serviceType.equals(ServiceType.SATURDAY)) {
        query += "AND c.saturday = true ";
      } else if (serviceType.equals(ServiceType.SUNDAY)) {
        query += "AND c.sunday = true ";
      }
      return query;
    }
    return "";
  }

  private static String getTripsWhere(String headsign, boolean includeTrip){
    String tripsWhere = "";
    boolean includeHeadsign = StringUtils.isNotBlank(headsign);
    if(includeTrip || includeHeadsign) {
      tripsWhere = "AND ad.configRev = t.configRev AND ad.tripId = t.tripId ";
      if(includeHeadsign){
        tripsWhere += String.format("AND t.headsign = '%s' ", headsign);
      }
    }
    return tripsWhere;
  }

  private static String getStopsWhere(boolean includeStop){
    if(includeStop){
      return "AND ad.configRev = s.configRev AND ad.stopId = s.id ";
    }
    return "";
  }

  private static String getStopPathsWhere(boolean includeStopPaths){
    if(includeStopPaths){
      return "AND ad.configRev = sp.configRev AND ad.stopPathId = sp.stopPathId AND ad.tripPatternId = sp.tripPatternId ";
    }
    return "";
  }

  private static String getDwellTimesWhere(boolean dwellTimesOnly){
    if(dwellTimesOnly){
      return "AND ad.dwellTime != null ";
    }
    return "";
  }

}
