package org.transitclock.db.dao;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.core.TemporalMatch;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.db.structs.Location;
import org.transitclock.db.structs.VehicleEvent;
import org.transitclock.utils.IntervalTimer;

import java.util.Date;
import java.util.List;

public class VehicleEventDAO {

  private static final Logger logger =
          LoggerFactory.getLogger(VehicleEventDAO.class);
  /**
   * Constructs a vehicle event and logs it and queues it to be stored in
   * database.
   *
   * @param time
   * @param avlTime
   * @param vehicleId
   * @param eventType
   * @param description
   * @param predictable
   * @param becameUnpredictable
   * @param supervisor
   * @param location
   * @param routeId
   * @param routeShortName
   * @param blockId
   * @param serviceId
   * @param tripId
   * @param stopId
   * @return The VehicleEvent constructed
   */
  public static VehicleEvent create(Date time, Date avlTime,
                                    String vehicleId, String eventType, String description,
                                    boolean predictable, boolean becameUnpredictable,
                                    String supervisor, Location location, String routeId,
                                    String routeShortName, String blockId, String serviceId,
                                    String tripId, String stopId) {
    VehicleEvent vehicleEvent =
            new VehicleEvent(time, avlTime, vehicleId, eventType,
                    description, predictable, becameUnpredictable,
                    supervisor, location, routeId, routeShortName, blockId,
                    serviceId, tripId, stopId);

    // Log VehicleEvent in log file
    logger.info(vehicleEvent.toString());

    // Queue to write object to database
    Core.getInstance().getDbLogger().add(vehicleEvent);

    // Return new VehicleEvent
    return vehicleEvent;
  }

  /**
   * A simpler way to create a VehicleEvent that gets a lot of its info from
   * the avlReport and match params. This also logs it and queues it to be
   * stored in database. The match param can be null.
   *
   * @param avlReport
   * @param match
   * @param eventType
   * @param description
   * @param predictable
   * @param becameUnpredictable
   * @param supervisor
   * @return The VehicleEvent constructed
   */
  public static VehicleEvent create(AvlReport avlReport, TemporalMatch match,
                                    String eventType, String description, boolean predictable,
                                    boolean becameUnpredictable, String supervisor) {
    // Get a log of the info from the possibly null match param
    String routeId = match==null ? null : match.getTrip().getRouteId();
    String routeShortName =
            match==null ? null : match.getTrip().getRouteShortName();
    String blockId = match==null ? null : match.getBlock().getId();
    String serviceId = match==null ? null : match.getBlock().getServiceId();
    String tripId = match==null ? null : match.getTrip().getId();
    String stopId = match==null ? null : match.getStopPath().getStopId();

    // Create and return the VehicleEvent
    return create(Core.getInstance().getSystemDate(), avlReport.getDate(),
            avlReport.getVehicleId(), eventType, description, predictable,
            becameUnpredictable, supervisor, avlReport.getLocation(),
            routeId, routeShortName, blockId, serviceId, tripId, stopId);
  }

  /**
   * Reads in all VehicleEvents from the database that were between the
   * beginTime and endTime.
   *
   * @param agencyId
   *            Which project getting data for
   * @param beginTime
   *            Specifies time range for query
   * @param endTime
   *            Specifies time range for query
   * @param sqlClause
   *            Optional. Can specify an SQL clause to winnow down the data,
   *            such as "AND routeId='71'".
   * @return
   */
  public static List<VehicleEvent> getVehicleEvents(String agencyId,
                                                    Date beginTime, Date endTime, String sqlClause) {
    IntervalTimer timer = new IntervalTimer();

    // Get the database session. This is supposed to be pretty light weight
    Session session = HibernateUtils.getSession(agencyId);

    // Create the query. Table name is case sensitive and needs to be the
    // class name instead of the name of the db table.
    String hql = "FROM VehicleEvent " +
            "    WHERE time >= :beginDate " +
            "      AND time < :endDate";
    if (sqlClause != null)
      hql += " " + sqlClause;
    Query query = session.createQuery(hql);

    // Set the parameters
    query.setTimestamp("beginDate", beginTime);
    query.setTimestamp("endDate", endTime);

    try {
      @SuppressWarnings("unchecked")
      List<VehicleEvent> vehicleEvents = query.list();
      logger.debug("Getting VehicleEvents from database took {} msec",
              timer.elapsedMsec());
      return vehicleEvents;
    } catch (HibernateException e) {
      logger.error(e.getMessage(), e);
      return null;
    } finally {
      // Clean things up. Not sure if this absolutely needed nor if
      // it might actually be detrimental and slow things down.
      session.close();
    }
  }

}
