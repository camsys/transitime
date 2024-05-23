package org.transitclock.monitoring;

public interface MonitoringService {
    void flush();

    void sumMetric(String metricName);

    void averageMetric(String metricName, double metricValue);

    void rateMetric(String metricName, boolean hit);
}
