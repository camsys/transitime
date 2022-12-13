package org.transitclock.reporting;

import java.util.List;

public class StopPathStatistics {
    private final String tripId;
    private final String stopPathId;
    private final String stopName;
    private final int stopPathIndex;
    private final boolean isTimePoint;
    private final boolean isLastStop;
    private final boolean isFirstStop;

    DoubleStatistics dwellTimeStats = new DoubleStatistics();
    DoubleStatistics runTimeStats = new DoubleStatistics();
    DoubleStatistics scheduledRunTimeStats = new DoubleStatistics();

    public StopPathStatistics(String tripId, String stopPathId, int stopPathIndex, String stopName,
                              boolean isTimePoint, boolean isLastStop) {
        this.tripId = tripId;
        this.stopPathId = stopPathId;
        this.stopPathIndex = stopPathIndex;
        this.stopName =  stopName;
        this.isTimePoint = isTimePoint;
        this.isLastStop = isLastStop;
        this.isFirstStop = stopPathIndex == 0;
    }

    public DoubleStatistics getDwellTimeStats() {
        return dwellTimeStats;
    }

    public DoubleStatistics getRunTimeStats() {
        return runTimeStats;
    }

    public DoubleStatistics getScheduledRunTimeStats() {
        return scheduledRunTimeStats;
    }

    public Double getAverageRunTime() {
        return runTimeStats.getAverage();
    }

    public Double getMedianRunTime() {
        return runTimeStats.getMedian();
    }

    public Double getMinRunTime() {
        return runTimeStats.getMin();
    }

    public Double getAverageDwellTime() {
        return dwellTimeStats.getAverage();
    }

    public Double getMedianDwellTime() {
        if(isLastStop){
            return 0.0;
        }
        return dwellTimeStats.getMedian();
    }

    public Double getAverageScheduledRunTime() { return scheduledRunTimeStats.getAverage(); }

    public Double getMedianScheduledRunTime() {
        return scheduledRunTimeStats.getMedian();
    }

    public List<Double> getAllRunTimes(){
        return runTimeStats.getAllValues();
    }

    public List<Double> getAllDwellTimes(){
        return dwellTimeStats.getAllValues();
    }

    public List<Double> getAllScheduledRunTimes() { return scheduledRunTimeStats.getAllValues(); }

    public long getCount() { return runTimeStats.getCount(); }

    public boolean isTimePoint() { return isTimePoint; }

    public boolean isLastStop() {
        return isLastStop;
    }

    public boolean isFirstStop(){
        return isFirstStop;
    }

    public String getStopName() {
        return stopName;
    }
}