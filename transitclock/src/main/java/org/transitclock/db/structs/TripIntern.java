package org.transitclock.db.structs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.StringConfigValue;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Take a very long trip_id and reduce it a reasonable size long
 */
public class TripIntern {

    private static final Logger logger = LoggerFactory
            .getLogger(TripIntern.class);

    public static final String JVM_INTERN_TYPE = "jvm";
    public static final String PREFIX_INTERN_TYPE = "prefix";
    public static final String REGEX_INTERN_TYPE = "regex";
    public static final String TABLE_INTERN_TYPE = "table";

    public static StringConfigValue internType = new StringConfigValue(
            "transitclock.tripIntern.strategy",
            "jvm",
            "Strategy to reduce the size in memory of trip id");

    public static StringConfigValue prefixDelimiter = new StringConfigValue(
            "transitclock.tripIntern.delimiter",
            "-",
            "Delimeter of prefix of trip id");

    public static StringConfigValue regex = new StringConfigValue(
            "transitclock.tripIntern.regex",
            "[0-9]+",
            "regex of tripId pattern");

    private static Map<String, String> internTripToTripMap = new HashMap();
    private static Map<String, String> tripToInternTrip = new HashMap();
    private static Pattern regexPattern = null;

    private static TripIntern singleton = new TripIntern();
    public static TripIntern getInstance() {
        return singleton;
    }

    /**
     * create/retrieve a smaller value of the trip string.  Set
     * transitclock.tripIntern.strategy to determine that behaviour.
     * @param tripId
     * @return
     */
    public synchronized String intern(String tripId) {
        switch (internType.getValue()) {
            case JVM_INTERN_TYPE:
                return tripId.intern();
            case PREFIX_INTERN_TYPE:
                return internByPrefix(tripId);
            case REGEX_INTERN_TYPE:
                return internByRegex(tripId);
            case TABLE_INTERN_TYPE:
                return internByTable(tripId);
            default:
                throw new UnsupportedOperationException("intern type of "
                        + internType.getValue() + "not understood");
        }
    }

    /*
     * this makes the assumption that the value was stored before it
     * was retrieved.
     */
    public String lookup(String tripIntern) {
        if (JVM_INTERN_TYPE.equals(internType.getValue())) {
            return tripIntern;
        }

        if (!internTripToTripMap.containsKey(tripIntern)) {
            logger.error("interned trip {} not present in cache", tripIntern);
        }
        return internTripToTripMap.get(tripIntern);
    }


    private String internByPrefix(String tripId) {
        if (tripToInternTrip.containsKey(tripId)) {
            return tripToInternTrip.get(tripId);
        }
        int end = tripId.indexOf(prefixDelimiter.getValue());
        if (end < 0) {
            logger.error("unexpected trip of " + tripId);
            // return it regardless so as to not break
            return tripId;
        }
        String tripIntern = tripId.substring(0, end-1);
        cache(tripId, tripIntern);
        return tripIntern;
    }

    private String internByRegex(String tripId) {
        if (tripToInternTrip.containsKey(tripId)) {
            return tripToInternTrip.get(tripId);
        }

        if (regexPattern == null) {
            regexPattern = Pattern.compile(regex.getValue());
        }
        Matcher matcher = regexPattern.matcher(tripId);
        if (!matcher.matches()) {
            // cache the failure to match
            return cache(tripId, tripId);
        }
        return cache(tripId, matcher.group(0));
    }

    private String internByTable(String tripId) {
        // TODO database lookup, return sequence or count of rows
        throw new UnsupportedOperationException("intern by table not supported");
    }

    private  static String cache(String tripId, String tripIntern) {
        if (tripToInternTrip.size() % 100 == 0) {
            logger.info("intern cache size {}", tripToInternTrip.size());
        }
        tripToInternTrip.put(tripId, tripIntern);
        internTripToTripMap.put(tripIntern, tripId);
        return tripIntern;
    }
}
