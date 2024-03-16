package org.transitclock.db.dao;

import com.google.common.collect.Lists;
import org.hibernate.*;
import org.hibernate.collection.internal.PersistentList;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.SessionImpl;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.configData.AgencyConfig;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.Trip;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.logging.Markers;
import org.transitclock.utils.IntervalTimer;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.*;

public class BlockDAO {

  private static final Logger logger = LoggerFactory.getLogger(BlockDAO.class);
  private static BooleanConfigValue blockLoading =
          new BooleanConfigValue("transitclock.blockLoading.agressive", false, "Set true to eagerly fetch all blocks into memory on startup");
  private static IntegerConfigValue blockConcurrencyCount =
          new IntegerConfigValue("transitclock.blockLoading.count", 4, "number of concurrent calls for block loading");

  // For making sure only lazy load trips collection via one thread
  // at a time.
  private static final Object lazyLoadingSyncObject = new Object();

  /**
   * So can sync up loading of trip and trip pattern data when trips are all
   * read at once in another class as opposed to through Block.getTrips().
   *
   * @return
   */
  public static Object getLazyLoadingSyncObject() {
    return lazyLoadingSyncObject;
  }

  /**
   * Returns list of Block objects for the specified configRev
   *
   * @param session
   * @param configRev
   * @return List of Block objects
   * @throws HibernateException
   */
  @SuppressWarnings("unchecked")
  public static List<Block> getBlocks(Session session, int configRev)
          throws HibernateException {
    IntervalTimer blockTimer = new IntervalTimer();
    try {
      if (Boolean.TRUE.equals(blockLoading.getValue())) {
        logger.warn("caching blocks aggressively....");
        return getBlocksConcurrently(session, configRev);
      }
      logger.warn("caching blocks passively....");
      return getBlocksPassive(session, configRev);
    } finally {
      logger.warn("caching complete in {}", blockTimer.elapsedMsecStr());
    }
  }

  private static List<Block> getBlocksPassive(Session session, int configRev)
          throws HibernateException {
    String hql = "FROM Blocks b "
            + "WHERE b.configRev = :configRev";
    Query query = session.createQuery(hql);
    query.setInteger("configRev", configRev);
    return query.list();
  }

  private static List<Block> getBlocksConcurrently(Session session, int configRev) {
    IntervalTimer blockTimer = new IntervalTimer();
    logger.info("querying for block service ids");
    String serviceIdsSql = "SELECT DISTINCT serviceId from Blocks where configRev = " + configRev;
    logger.info("querying for serviceIds {}", serviceIdsSql);
    SQLQuery sqlQuery = session.createSQLQuery(serviceIdsSql);
    List serviceIds = sqlQuery.list();

    if (serviceIds == null || serviceIds.isEmpty()) {
      logger.error("no serviceIds present for configRev {}, exiting.", configRev);
      return new ArrayList<>();
    }
    if (serviceIds.size() / blockConcurrencyCount.getValue() < 1) {
      logger.error("only one bin requested, falling back on passive impl");
      return getBlocksPassive(session, configRev);
    }

    logger.info("concurrently loading all blocks for configRev {} and serviceIds {}", configRev, serviceIds);
    List partitions = Lists.partition(serviceIds,serviceIds.size() / blockConcurrencyCount.getValue());
    List<BlockLoader> loaders = new ArrayList();

    for (Object partition : partitions) {
      List objects = (List) partition;
      List<String> serviceIdSection = new ArrayList<>();
      for (Object obj : objects) {
        serviceIdSection.add((String) obj);
      }
      BlockLoader bl = new BlockLoader(serviceIdSection, configRev);
      loaders.add(bl);
      new Thread(bl).start();
    }

    Set<Block> allBlocks = new HashSet<>();
    int i = 0;
    for (BlockLoader bl : loaders) {
      while (!bl.finished) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      if (bl.blocks != null) {
        logger.info("BlockLoader {} of {} complete", i, loaders.size());
        allBlocks.addAll(bl.blocks);
      }
      i++;
    }
    logger.info("getBlocksConcurrently executed in {} with {} blocks", blockTimer.elapsedMsecStr(), allBlocks.size());
    return new ArrayList<>(allBlocks);
  }


  /**
   * Deletes rev from the Blocks, Trips, and Block_to_Trip_joinTable
   *
   * @param session
   * @param configRev
   * @return Number of rows deleted
   * @throws HibernateException
   */
  public static int deleteFromRev(Session session, int configRev)
          throws HibernateException {
    // In a perfect Hibernate world one would simply call on session.delete()
    // for each block and the block to trip join table and the associated
    // trips would be automatically deleted by using the magic of Hibernate.
    // But this means that would have to read in all the Blocks and sub-objects
    // first, which of course takes lots of time and memory, often causing
    // program to crash due to out of memory issue. And since reading in the
    // Trips is supposed to automatically read in associated travel times
    // we would be reading in data that isn't even needed for deletion since
    // don't want to delete travel times (want to reuse them!). Therefore
    // using the much, much faster solution of direct SQL calls. Can't use
    // HQL on the join table since it is not a regularly defined table.
    //
    // Note: Would be great to see if can actually use HQL and delete the
    // appropriate Blocks and have the join table and the trips table
    // be automatically updated. I doubt this would work but would be
    // interesting to try if had the time.
    int totalRowsUpdated = 0;

    // Delete configRev data from Block_to_Trip_joinTable
    int rowsUpdated = session.
            createSQLQuery("DELETE FROM Block_to_Trip_joinTable "
                    + "WHERE Blocks_configRev=" + configRev).
            executeUpdate();
    logger.info("Deleted {} rows from Block_to_Trip_joinTable for "
            + "configRev={}", rowsUpdated, configRev);
    totalRowsUpdated += rowsUpdated;

    // Delete configRev data from Trips
    rowsUpdated = session.
            createSQLQuery("DELETE FROM Trips WHERE configRev="
                    + configRev).
            executeUpdate();
    logger.info("Deleted {} rows from Trips for configRev={}",
            rowsUpdated, configRev);
    totalRowsUpdated += rowsUpdated;

    // Delete configRev data from Blocks
    rowsUpdated = session.
            createSQLQuery("DELETE FROM Blocks WHERE configRev="
                    + configRev).
            executeUpdate();
    logger.info("Deleted {} rows from Blocks for configRev={}",
            rowsUpdated, configRev);
    totalRowsUpdated += rowsUpdated;

    return totalRowsUpdated;
  }


  public static class BlockLoader implements Runnable {

    private List<String> serviceIds;
    private int configRev;
    private boolean finished = false;
    private List<Block> blocks;

    public BlockLoader(List<String> serviceIds, int configRev) {
      this.serviceIds = serviceIds;
      this.configRev = configRev;
    }

    @Override
    public void run() {
      try {
        // when in a seperate thread you need a distinct session
        Session session = HibernateUtils.getSession();
        String hql = "FROM Blocks b "
                + "WHERE b.configRev = :configRev and b.serviceId in (";
        for (String s : serviceIds) {
          hql += "'" + s + "', ";
        }
        // remove last trailing comma
        hql = hql.substring(0, hql.length()-2);
        hql += ")";
        logger.info("executing {}", hql);
        Query query = session.createQuery(hql);
        query.setInteger("configRev", configRev);

        blocks = query.list();

      } finally {
        finished = true;
      }
    }
  }

  public static void getTrips(Block block, List<Trip> trips) {

    // Trips not yet lazy loaded so do so now.
    // It appears that lazy initialization is problematic when have multiple
    // simultaneous threads. Get "org.hibernate.AssertionFailure: force
    // initialize loading collection". Therefore need to make sure that
    // only loading lazy sub-data serially. Since it is desirable to have
    // trips collection be lazy loaded so that app starts right away without
    // loading all the sub-data for every block assignment need to make
    // sure this is done in a serialized way. Having app not load all data
    // at startup is especially important when debugging.
    // trips not yet initialized so synchronize so only a single
    // thread can initialize at once and then access something
    // in trips that will cause it to be lazy loaded.
    synchronized (lazyLoadingSyncObject) {
      logger.debug("About to do lazy load for trips data for "
              + "blockId={} serviceId={}...", block.getId(), block.getServiceId());
      IntervalTimer timer = new IntervalTimer();

      // Access the collection so that it is lazy loaded.
      // Problems can be difficult to debug so log error along
      // with the SQL.
      try {
        // First see if the session associated with trips is different
        // from the current global session. This can happen if a new
        // global session was created when trips for another block was
        // loaded and it was found that the old session was no longer
        // valid, such as when the db is rebooted.
        if (trips instanceof PersistentList) {
          // Get the current session associated with the trips.
          // Can be null.
          PersistentList persistentListTrips = (PersistentList) trips;
          SessionImplementor session =
                  persistentListTrips.getSession();

          // If the session is different from the global
          // session then need to attach the new session to the
          // object.
          DbConfig dbConfig = Core.getInstance().getDbConfig();
          Session globalLazyLoadSession = dbConfig.getGlobalSession();
          if (session != globalLazyLoadSession) {
            // The persistent object is using an old session so
            // switch to new one
            logger.info("For blockId={} was using an old session "
                            + "(hash={}) instead of the current "
                            + "globalLazyLoadSession (hash={}). Therefore "
                            + "switching the Block to use the new "
                            + "globalLazyLoadSession.",
                    block.getId(), session == null ? null : session.hashCode(),
                    globalLazyLoadSession.hashCode());
            try {
              globalLazyLoadSession.update(block);
            } catch(Exception e){
              logger.warn("Unable to load lazy session", e);
            }
          }
        } else {
          logger.error("Blocks.trips member is not a PersistentList!?!?. ");
          // not exiting here....
        }

        // Actually lazy-load the trips
        trips.get(0);
      } catch (JDBCException e) {
        // TODO this is an anti-pattern
        // If root cause of exception is a SocketTimeoutException
        // then somehow lost connection to the database. Might have
        // been rebooted or such. For this situation need to attach
        // object to new session.
        Throwable rootCause = HibernateUtils.getRootCause(e);
        if (rootCause instanceof SocketTimeoutException || rootCause instanceof SocketException) {
          logger.error("Socket timeout in getTrips() for "
                          + "blockId={}. Database might have been "
                          + "rebooted. Creating a new session.",
                  block.getId(), e);

          if (!(rootCause instanceof SocketException
                  || rootCause instanceof SocketTimeoutException
                  || rootCause instanceof PSQLException)) {
            logger.error(Markers.email(),
                    "For agencyId={} in Blocks.getTrips() for "
                            + "blockId={} encountered exception whose root "
                            + "cause was not a SocketException, "
                            + "SocketTimeoutException, or PSQLException,"
                            + "which therefore is unexpected. Therefore should "
                            + "investigate. Root cause is {}.",
                    AgencyConfig.getAgencyId(), block.getId(),
                    rootCause, e);
          }

          // Even though there was a timeout meaning that the
          // session is no longer any good the Block object
          // might still be associated with the old session.
          // In order to attach the Block to a newly created
          // session need to first close the old session or else
          // system will complain that trying to add a object
          // to two live sessions. Tried using session.evict(this)
          // but still got exception "Illegal attempt to associate
          // a collection with two open sessions"
          PersistentList persistentListTrips = (PersistentList) trips;
          SessionImplementor sessionImpl =
                  persistentListTrips.getSession();
          SessionImpl session = (SessionImpl) sessionImpl;
          if (!session.isClosed()) {
            try {
              // Note: this causes a stack trace to be output
              // to stdout by Hibernate. Seems that this
              // cannot be avoided since need to close the
              // session.
              session.close();
            } catch (HibernateException e1) {
              logger.error("Exception occurred when trying "
                      + "to close session when lazy loading "
                      + "data after socket timeout occurred.", e1);
            }
          }

          // Get new session, update object to use it, and try again.
          // Note: before calling get(0) to load the data first made
          // sure that the session used for the Block.trips is the same
          // as the current session. Therefore if made it here then it
          // means that definitely need to create new session.
          DbConfig dbConfig = Core.getInstance().getDbConfig();
          logger.info("CREATING NEW SESSION");
          dbConfig.createNewGlobalSession();
          Session globalLazyLoadSession = dbConfig.getGlobalSession();
          globalLazyLoadSession.update(block);

          // Now that have attached a new session lazy load the trips
          // data
          trips.get(0);
        } else {
          // Not a socket timeout. Therefore don't know handle
          // to handle so just log and throw the exception
          logger.error("In Block.getTrips() got JDBCException. "
                  + "SQL=\"{}\" msg={}", e.getSQL(), e
                  .getSQLException().getMessage(), e);
          throw e;
        }

        // Actually lazy-load the trips
        trips.get(0);
      }

      logger.debug("Finished lazy load for trips data for "
                      + "blockId={} serviceId={}. Took {} msec", block.getId(),
              block.getServiceId(), timer.elapsedMsec());
    }

  }

}
