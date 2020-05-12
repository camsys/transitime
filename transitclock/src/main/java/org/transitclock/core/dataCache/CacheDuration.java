package org.transitclock.core.dataCache;

import org.transitclock.applications.Core;
import org.transitclock.core.ServiceUtils;
import org.transitclock.db.structs.Trip;
import org.transitclock.utils.Time;

/**
 * Convenience methods to return duration of caches based on serviceId / ServiceType
 */
public class CacheDuration {

    public final int DEFAULT_EXPIRY_DURATION = Time.SEC_PER_DAY*4;
    private final int ONE_MONTH_IN_DAYS = 28;
    private ServiceUtils serviceUtils = null;


    private Trip getTripForId(String tripId) {
        return Core.getInstance().getDbConfig().getTrip(tripId);
    }

    public int getExpiryDuration(String tripId) {
        Trip trip = getTripForId(tripId);
        String serviceId = trip.getServiceId();
        if (serviceId != null) {
            return getExpiryForServiceType(getServiceUtils().getServiceType(serviceId));
        }
        return DEFAULT_EXPIRY_DURATION;
    }

    public int getExpiryForServiceType(ServiceUtils.ServiceType serviceType) {
        switch (serviceType.valueOf()) {
            case ServiceUtils.ServiceType.WEEKDAY_VALUE:
                return DEFAULT_EXPIRY_DURATION;
            case ServiceUtils.ServiceType.WEEKEND_VALUE:
            case ServiceUtils.ServiceType.SATURDAY_VALUE:
            case ServiceUtils.ServiceType.SUNDAY_VALUE:
                return ONE_MONTH_IN_DAYS * Time.SEC_PER_DAY;
            default:
                return ONE_MONTH_IN_DAYS * Time.SEC_PER_DAY;
        }
    }


    private ServiceUtils getServiceUtils() {
        if (serviceUtils == null)
            serviceUtils = new ServiceUtils(Core.getInstance().getDbConfig());
        return serviceUtils;
    }


}
