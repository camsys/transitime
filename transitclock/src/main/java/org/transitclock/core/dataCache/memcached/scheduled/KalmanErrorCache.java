package org.transitclock.core.dataCache.memcached.scheduled;

import net.spy.memcached.MemcachedClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.core.Indices;
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
	private Map<String, Calendar> serviceIdToCalendarMap = new HashMap<>();
	private Map<String, ServiceType> serviceIdToServiceTypeMap = new HashMap<>();


	private static final String keystub = "k_";
	private Integer DEFAULT_EXPIRY_DURATION = Time.SEC_PER_DAY*4;
	

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
							getExpiryDuration(key.getTripId()),
							value);
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

	private int getExpiryDuration(String tripId) {
		Trip trip = getTripForId(tripId);
		String serviceId = trip.getServiceId();
		if (serviceId != null) {
			return getExpiryForServiceType(getServiceType(serviceId));
		}
		return DEFAULT_EXPIRY_DURATION;
	}

	private int getExpiryForServiceType(ServiceType serviceType) {
		switch (serviceType.valueOf()) {
			case 1:
				return DEFAULT_EXPIRY_DURATION;
			case 2:
			case 3:
			case 4:
				return 28 * Time.SEC_PER_DAY;
			default:
				return 28 * Time.SEC_PER_DAY;
		}
	}

	private ServiceType getServiceType(String serviceId) {
		if (serviceIdToServiceTypeMap.containsKey(serviceId))
			return serviceIdToServiceTypeMap.get(serviceId);

		Calendar serviceCalendar = getCalendarForServiceId(serviceId);
		if (serviceCalendar == null) return ServiceType.UNKNOWN;
		if (serviceCalendar.getMonday()
			&& serviceCalendar.getTuesday()
			&& serviceCalendar.getWednesday()
			&& serviceCalendar.getThursday()
			&& serviceCalendar.getFriday()
			&& !serviceCalendar.getSaturday()
			&& !serviceCalendar.getSunday()) {
			serviceIdToServiceTypeMap.put(serviceId, ServiceType.WEEKDAY);
			return ServiceType.WEEKDAY;
		}

		if (!serviceCalendar.getMonday()
				&& !serviceCalendar.getTuesday()
				&& !serviceCalendar.getWednesday()
				&& !serviceCalendar.getThursday()
				&& !serviceCalendar.getFriday()
				&& serviceCalendar.getSaturday()
				&& serviceCalendar.getSunday()) {
			serviceIdToServiceTypeMap.put(serviceId, ServiceType.WEEKEND);
			return ServiceType.WEEKEND;
		}

		if (!serviceCalendar.getMonday()
				&& !serviceCalendar.getTuesday()
				&& !serviceCalendar.getWednesday()
				&& !serviceCalendar.getThursday()
				&& !serviceCalendar.getFriday()
				&& serviceCalendar.getSaturday()
				&& !serviceCalendar.getSunday()) {
			serviceIdToServiceTypeMap.put(serviceId, ServiceType.SATURDAY);
			return ServiceType.SATURDAY;
		}

		if (!serviceCalendar.getMonday()
				&& !serviceCalendar.getTuesday()
				&& !serviceCalendar.getWednesday()
				&& !serviceCalendar.getThursday()
				&& !serviceCalendar.getFriday()
				&& !serviceCalendar.getSaturday()
				&& serviceCalendar.getSunday()) {
			serviceIdToServiceTypeMap.put(serviceId, ServiceType.SUNDAY);
			return ServiceType.SUNDAY;
		}

		// cache the failure
		serviceIdToServiceTypeMap.put(serviceId, ServiceType.UNKNOWN);
		return ServiceType.UNKNOWN;
	}

	public enum ServiceType {
		UNKNOWN (-1),
		WEEKDAY (0),
		WEEKEND (1),
		SATURDAY (2),
		SUNDAY (3);

		public static int WEEKDAY_VALUE = 0;
		public static int WEEKEND_VALUE = 1;
		public static int SATURDAY_VALUE = 2;
		public static int SUNDAY_VALUE = 3;
		private int type;

		ServiceType() { type = -1; }
		ServiceType(int type) { this.type = type; }
		public int valueOf() { return type; }
	}

	private Calendar getCalendarForServiceId(String serviceId) {
		if (serviceId == null) return null;
		if (serviceIdToCalendarMap.containsKey(serviceId))
			return serviceIdToCalendarMap.get(serviceId);

		for (Calendar calendar : Core.getInstance().getDbConfig().getCalendars()) {
			if (serviceId.equals(calendar.getServiceId())) {
				serviceIdToCalendarMap.put(serviceId, calendar);
				return calendar;
			}
		}
		return null;
	}

	private Trip getTripForId(String tripId) {
		return Core.getInstance().getDbConfig().getTrip(tripId);
	}

	private String intern(String s) {
		return TripIntern.getInstance().intern(s);
	}

}
