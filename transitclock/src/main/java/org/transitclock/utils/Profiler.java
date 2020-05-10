package org.transitclock.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performing timing of key operations and log the expensive ones.
 */
public class Profiler {

    private static final Logger logger = LoggerFactory.getLogger(Profiler.class);

    private String name;
    private Long startTime;
    private int maxTime;
    private Long endTime = null;

    public Profiler(String profileName) {
        this.name = profileName;
        this.startTime = System.currentTimeMillis();
        this.maxTime = 30;
    }
    public Profiler(String profileName, int maxTime) {
        this.name = profileName;
        this.startTime = System.currentTimeMillis();
        this.maxTime = maxTime;
    }

    public void end() {
        endTime = System.currentTimeMillis();
        long delta = endTime - startTime;
        if (delta < maxTime)
            return;
        logger.info(name + ": " + delta + ", " + prettyPrint(delta));
    }

    public String getPrettyPrintTime() {
        if (endTime != null)
            return prettyPrint(endTime - startTime);
        return prettyPrint(System.currentTimeMillis() - startTime);
    }

    public Long getTime() {
        if (endTime != null)
            return (endTime - startTime);
        return (System.currentTimeMillis() - startTime);
    }

    private String prettyPrint(long delta) {
        if ( delta > 2000 * 60 )
            return (delta / 1000 / 60) + " min";
        if ( delta > 2000)
            return (delta / 1000) + " s";
        return delta + " ms";
    }
}
