package org.transitclock.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalMonitoringServiceImpl implements MonitoringService{

    private static final Logger logger = LoggerFactory.getLogger(LocalMonitoringServiceImpl.class);

    @Override
    public void flush() {
        logger.debug("flush() method called, doing nothing");
    }

    @Override
    public void sumMetric(String metricName) {
        logger.debug("sumMetric({}) method called, doing nothing", metricName);
    }

    @Override
    public void averageMetric(String metricName, double metricValue) {
        logger.debug("averageMetric({},{}) method called, doing nothing", metricName, metricValue);
    }

    @Override
    public void rateMetric(String metricName, boolean hit) {
        logger.debug("rateMetric({},{}) method called, doing nothing", metricName, hit);
    }
}
