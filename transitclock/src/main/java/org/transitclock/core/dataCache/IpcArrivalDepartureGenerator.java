package org.transitclock.core.dataCache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.ipc.data.IpcArrivalDeparture;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Instantiate IpcArrivalDepartures efficiently populating internal structures
 * including forward/backward pointer forming linked list.
 */
public class IpcArrivalDepartureGenerator {

  private static final Logger logger =
          LoggerFactory.getLogger(IpcArrivalDepartureGenerator.class);
  public static final IntegerConfigValue MAX_ARRIVALS_DEPARTURES_PER_TRIP
          = new IntegerConfigValue("transitclock.core.cache.max_ad_per_trip",
          1000,
          "Max arrivals and departures that are supported in a single trip to " +
                  "prevent infinite recursion");
  public static final IntegerConfigValue ARRIVALS_DEPARTURES_CACHE_SIZE
          = new IntegerConfigValue("transticlock.core.cache.ad_cache_size",
          10000,
          "size of Arrival/Departure cache for generation");

  private static IpcArrivalDepartureGenerator INSTANCE;
  private LRUCache<String, IpcArrivalDeparture> linkedCache;
  private LRUCache<String, IpcArrivalDeparture> unlinkedCache;
  private long requestCount = 0;
  private long hitCount = 0;

  private IpcArrivalDepartureGenerator() {
    linkedCache = new LRUCache<>(ARRIVALS_DEPARTURES_CACHE_SIZE.getValue());
    unlinkedCache = new LRUCache<>(ARRIVALS_DEPARTURES_CACHE_SIZE.getValue());
  }

  public static IpcArrivalDepartureGenerator getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new IpcArrivalDepartureGenerator();
    }
    return INSTANCE;
  }

  private int getMaxDepth() {
    return MAX_ARRIVALS_DEPARTURES_PER_TRIP.getValue();
  }

  public IpcArrivalDeparture generate(ArrivalDeparture arrivalDeparture, boolean linkEntries) throws Exception {
    return generateRecursively(arrivalDeparture, linkEntries, 0);
  }

  public IpcArrivalDeparture update(ArrivalDeparture arrivalDeparture) throws Exception {
    // todo:  determine how to detect changes and update instead of delete/add
    synchronized (linkedCache) {
      remove(linkedCache.get(hash(arrivalDeparture)), 0);
      if (arrivalDeparture.getPrevious() != null)
        remove(linkedCache.get(hash(arrivalDeparture.getPrevious())), 0);
      if (arrivalDeparture.getNext() != null)
        remove(linkedCache.get(hash(arrivalDeparture.getNext())), 0);
      return generate(arrivalDeparture, true);
    }
  }

  public IpcArrivalDeparture remove(IpcArrivalDeparture ipc, int depth) throws Exception {
    if (ipc == null) return null;
    ipc = linkedCache.remove(hash(ipc));
    if (ipc == null) return null; // not in cache, nothing to do
    if (ipc.getPrevious() != null && depth < getMaxDepth()) {
      remove(ipc.getPrevious(), depth + 1);
    }
    if (ipc.getNext() != null && depth < getMaxDepth()) {
      remove(ipc.getNext(), depth + 1);
    }
    return ipc;
  }



  private IpcArrivalDeparture generateRecursively(ArrivalDeparture arrivalDeparture, boolean linkEntries, int count) throws Exception {
    if (arrivalDeparture == null) return null; // we are done
    String hash = hash(arrivalDeparture);
    IpcArrivalDeparture ad = linkedCache.get(hash);
    if (ad == null) {
      synchronized (linkedCache) {
        ad = linkedCache.get(hash);
        if (ad == null) {
          logMiss();
          ad = new IpcArrivalDeparture(arrivalDeparture);
          linkedCache.put(hash, ad);
          if (count < getMaxDepth()) {
            ad.setNext(generateRecursively(arrivalDeparture.getNext(), true, count + 1));
            ad.setPrevious(generateRecursively(arrivalDeparture.getPrevious(), true, count + 1));
          } else {
            logger.error("hit max recursion depth for A/D {}", arrivalDeparture);
          }
        }
      }
    } else {
      logHit();
    }

    if (!linkEntries) {
      IpcArrivalDeparture unlinked = unlinkedCache.get(hash);
      if (unlinked == null) {
        synchronized (unlinkedCache) {
          unlinked = unlinkedCache.get(hash);
          if (unlinked == null) {
            unlinked = ad.copyUnlinked();
            unlinkedCache.putIfAbsent(hash, unlinked);
          }
        }
      }
      return unlinked;
    }

    return ad;
  }


  private void logMiss() {
    requestCount++;
  }

  private void logHit() {
    requestCount++;
    hitCount++;
    if (requestCount % 1000 == 0) {
      logger.info("IpcADGenerator status {} hits of {}, {}%", hitCount, requestCount, (int)((double)hitCount/requestCount*100));
    }
  }

  public String hash(ArrivalDeparture ad) {
    return ad.getVehicleId()
            + "." + ad.getTime()
            + "." + ad.getStopId()
            + "." + ad.getStopPathIndex()
            + "." + ad.isArrival()
            + "." + ad.getTripId();
  }

  public String hash(IpcArrivalDeparture ipc) {
    return ipc.getVehicleId()
            + "." + ipc.getTime().getTime()
            + "." + ipc.getStopId()
            + "." + ipc.getStopPathIndex()
            + "." + ipc.isArrival()
            + "." + ipc.getTripId();
  }

  public IpcArrivalDeparture refresh(IpcArrivalDeparture currentArrivalDeparture) {
    IpcArrivalDeparture refreshedDeparture = linkedCache.get(hash(currentArrivalDeparture));
    if (refreshedDeparture != null)
      return refreshedDeparture;
    return currentArrivalDeparture;
  }

  public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private int cacheSize;

  public LRUCache(int cacheSize) {
      super(ARRIVALS_DEPARTURES_CACHE_SIZE.getValue(), 0.75f, true);
      this.cacheSize = cacheSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
      return size() >= cacheSize;
    }
  }
}
