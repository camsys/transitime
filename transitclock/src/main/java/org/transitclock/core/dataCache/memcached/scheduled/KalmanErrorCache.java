package org.transitclock.core.dataCache.memcached.scheduled;

import net.spy.memcached.MemcachedClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.core.Indices;
import org.transitclock.core.ServiceUtils;
import org.transitclock.core.dataCache.CacheDuration;
import org.transitclock.core.dataCache.ErrorCache;
import org.transitclock.core.dataCache.KalmanError;
import org.transitclock.core.dataCache.KalmanErrorCacheKey;
import org.transitclock.db.structs.Calendar;
import org.transitclock.db.structs.Trip;
import org.transitclock.db.structs.TripIntern;
import org.transitclock.utils.Time;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KalmanErrorCache implements ErrorCache {

	private static final Logger logger = LoggerFactory
			.getLogger(KalmanErrorCache.class);

	private static StringConfigValue memcachedHost = new StringConfigValue("transitclock.cache.memcached.host", "127.0.0.1",
			"Specifies the host machine that memcache is running on.");

	private static IntegerConfigValue memcachedPort = new IntegerConfigValue("transitclock.cache.memcached.port", 11211,
			"Specifies the port that memcache is running on.");

	private MemcachedClient memcachedClient = null;
	private CacheDuration cacheDuration = null;

	private static final String keystub = "k_";

	

	public KalmanErrorCache() throws IOException {
		memcachedClient = new MemcachedClient(
				new InetSocketAddress(memcachedHost.getValue(), memcachedPort.getValue().intValue()));
	}

	@Override
	public KalmanError getErrorValue(Indices indices) {
		return getErrorValue(getKey(indices.getTrip().getId(), indices.getStopPathIndex()));
	}

	@Override
	public KalmanError getErrorValue(KalmanErrorCacheKey key) {
		KalmanError value = (KalmanError) memcachedClient.get(createKey(key));
		return value;
	}

	@Override
	public void putErrorValue(Indices indices, Double value) {
		KalmanErrorCacheKey key= getKey(indices.getTrip().getId(),
										indices.getStopPathIndex());

		putErrorValue(key, value);
	}

	@Override
	public void putErrorValue(KalmanErrorCacheKey key, Double value) {
		memcachedClient.set(createKey(key),
							getCacheDuration().getExpiryDuration(key.getTripId()),
							value);
	}

	private CacheDuration getCacheDuration() {
		if (cacheDuration == null)
			cacheDuration = new CacheDuration();
		return cacheDuration;
	}

	@Override
	public List<KalmanErrorCacheKey> getKeys() {
		logger.info("Not implemented for memecached.");
		return null;
	}

	private KalmanErrorCacheKey getKey(String tripId, int stopPathIndex) {
		KalmanErrorCacheKey key=new KalmanErrorCacheKey(tripId, stopPathIndex);
		return key;
	}


	private String createKey(KalmanErrorCacheKey key) {
		return keystub + intern(key.getTripId()) + "_" + key.getStopPathIndex();

	}

	private String intern(String s) {
		return TripIntern.getInstance().intern(s);
	}

}
