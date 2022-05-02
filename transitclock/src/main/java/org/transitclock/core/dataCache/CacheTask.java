package org.transitclock.core.dataCache;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.avl.ApcModule;
import org.transitclock.core.dataCache.frequency.FrequencyBasedHistoricalAverageCache;
import org.transitclock.core.dataCache.scheduled.ScheduleBasedHistoricalAverageCache;
import org.transitclock.core.predictiongenerator.scheduled.traveltime.kalman.TrafficManager;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.ArrivalDeparture;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

import static org.transitclock.gtfs.DbConfig.MAX_PREVIOUS_CONFIG_REV_LOAD;

/**
 * A task populating the cache on startup.  Designed to be
 * run in parallel.
 */
public class CacheTask implements ParallelTask {

    private static final Logger logger =
            LoggerFactory.getLogger(CacheTask.class);

    /**
     * type of cache we are dealing with
     */
    public enum Type {
        TripDataHistoryCacheFactory,
        StopArrivalDepartureCacheFactory,
        FrequencyBasedHistoricalAverageCache,
        ScheduleBasedHistoricalAverageCache,
        DwellTimeModelCacheFactory,
        TrafficDataHistoryCache,
        ApcCache
    }

    private Date startDate;
    private Date endDate;
    private Type type;
    private Future<?> futureResults;

    public CacheTask(Date startDate, Date endDate, Type type, Future<?> futureInput) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
        this.futureResults = futureInput;
    }

    @Override
    public String toString() {
        return type.name();
    }

    @Override
    public void run() throws Exception {
        logger.info("in run for task with results=" + futureResults);
        Session session = null;
        List<ArrivalDeparture> results = null;
        if (futureResults != null) {
            // block here until we have input ready
            logger.info("async retrieval of {} to {}", startDate, endDate);
            try {
                results = (List<ArrivalDeparture>) futureResults.get();
            } catch (Throwable t) {
                // this will catch any hydration issues in the future query
                logger.error("futureResult retrieval failed with {}", t, t);
            }
            if (results == null) {
                logger.info("async retrieval of {} to {} failed!", startDate, endDate);
            } else {
                logger.info("async retrieval of {} to {} finished with {} results", startDate, endDate, results.size());
            }
        }
        try {
            if (this.futureResults == null) {
                session = HibernateUtils.getSession();
                Criteria criteria = session.createCriteria(ArrivalDeparture.class);
                logger.info("async / future results null, performing manual retrieval now");
                results = criteria.add(Restrictions.between("time", startDate, endDate)).list();
            }

            logger.info("Populating {} cache for period {} to {}", type, startDate, endDate);
            switch (type) {
                case TripDataHistoryCacheFactory:
                    if (results != null) {
                        logger.info("populating TripDataHistoryCache with " + results.size() + " records");
                    } else {
                        logger.info("populating TripDataHistoryCache with NuLl records");
                    }
                    loadMissingTrips(results);
                    TripDataHistoryCacheFactory.getInstance().populateCacheFromDb(results);
                    break;
                case StopArrivalDepartureCacheFactory:
                    StopArrivalDepartureCacheFactory.getInstance().populateCacheFromDb(results);
                    break;
                case FrequencyBasedHistoricalAverageCache:
                    FrequencyBasedHistoricalAverageCache.getInstance().populateCacheFromDb(results);
                    break;
                case ScheduleBasedHistoricalAverageCache:
                    ScheduleBasedHistoricalAverageCache.getInstance().populateCacheFromDb(results);
                    break;
                case DwellTimeModelCacheFactory:
                    DwellTimeModelCacheFactory.getInstance().populateCacheFromDb(results);
                    break;
                case TrafficDataHistoryCache:
                    TrafficManager.getInstance().populateCacheFromDb(session, startDate, endDate);
                    break;
                case ApcCache:
                    ApcModule.getInstance().populateFromDb(results);
                    break;
                default:
                    throw new IllegalArgumentException("unknown type=" + type);
            }
        } catch (Throwable t) {
            logger.error("Error Populating {} cache for period {} to {}, {}", type, startDate, endDate, t, t);
        } finally {
            logger.info("Finished Populating {} cache for period {} to {}", type, startDate, endDate);
            if (session != null) {
                // this session is in a separate thread and needs to be reclaimed
                // as it counts against the connection pool
                session.close();
            }
        }
    }

    private static void loadMissingTrips(List<ArrivalDeparture> results) {
        try {

            int i = 0;
            while (i < MAX_PREVIOUS_CONFIG_REV_LOAD) {
                // check if the set of trips is known about
                HashSet<String> tripsToLoad = new HashSet<>();
                for (ArrivalDeparture arrivalDeparture : results) {
                    if (!Core.getInstance().getDbConfig().isKnownTrip(arrivalDeparture.getTripId())) {
                        logger.info("found unknown trip {}", arrivalDeparture.getTripId());
                        tripsToLoad.add(arrivalDeparture.getTripId());
                    }
                }
                Core.getInstance().getDbConfig().loadTrips(tripsToLoad, i);
            }
            // for trips that aren't know, load them in bulk
            // iteratively per configRev
        } catch (Throwable t) {
            logger.error("loadMissingTrips caught {}", t, t);
            return;
        }
    }

}
