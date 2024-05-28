package org.transitclock.integration_tests.playback;

import com.google.common.util.concurrent.AtomicDouble;
import java.text.DecimalFormat;

public class PredictionBucketAccuracy implements Comparable<PredictionBucketAccuracy> {
    private static final String ZERO_PERCENT = "0.00%";
    private final long horizonStart;

    private final long horizonEnd;
    private final String horizonDescription;
    private final long earlyThreshold;
    private final long lateThreshold;
    private AtomicDouble earlyOldPredictionsCount = new AtomicDouble(0.0);
    private AtomicDouble ontimeOldPredictionsCount = new AtomicDouble(0.0);
    private AtomicDouble lateOldPredictionsCount = new AtomicDouble(0.0);
    private AtomicDouble earlyNewPredictionsCount = new AtomicDouble(0.0);
    private AtomicDouble ontimeNewPredictionsCount = new AtomicDouble(0.0);
    private AtomicDouble lateNewPredictionsCount = new AtomicDouble(0.0);

    public PredictionBucketAccuracy(String horizonDescription, long horizonStart, long horizonEnd, long earlyThreshold, long lateThreshold){
        this.horizonDescription = horizonDescription;
        this.horizonStart = horizonStart;
        this.horizonEnd = horizonEnd;
        this.earlyThreshold = earlyThreshold;
        this.lateThreshold = lateThreshold;
    }

    public long getHorizonStart() {
        return horizonStart;
    }

    public long getHorizonEnd() {
        return horizonEnd;
    }


    public String getHorizonDescription() {
        return horizonDescription;
    }

    public long getEarlyThreshold() {
        return earlyThreshold;
    }

    public long getLateThreshold() {
        return lateThreshold;
    }

    public void processPrediction(CombinedPredictionAccuracy combinedPrediction){
        if(combinedPrediction.actualADTime > -1) {
            if (combinedPrediction.newPrediction != null && combinedPrediction.newPredTime > -1) {
                long newPredError = combinedPrediction.actualADTime - combinedPrediction.newPredTime;
                if (newPredError > lateThreshold) {
                    incrementLateNewPrediction();
                }
                if (newPredError < earlyThreshold) {
                    incrementEarlyNewPrediction();
                }
                incrementOntimeNewPrediction();
            }
            if (combinedPrediction.oldPrediction != null && combinedPrediction.oldPredTime > -1) {
                long oldPredError = combinedPrediction.actualADTime - combinedPrediction.oldPredTime;
                if (oldPredError > lateThreshold) {
                    incrementLateOldPrediction();
                }
                if (oldPredError < earlyThreshold) {
                    incrementEarlyOldPrediction();
                }
                incrementOntimeOldPrediction();
            }
        }

    }


    private void incrementEarlyOldPrediction(){
        earlyOldPredictionsCount.getAndAdd(1.0);
    }

    private void incrementOntimeOldPrediction(){
        ontimeOldPredictionsCount.getAndAdd(1.0);
    }

    private void incrementLateOldPrediction(){
        lateOldPredictionsCount.getAndAdd(1.0);
    }

    private void incrementEarlyNewPrediction(){
        earlyNewPredictionsCount.getAndAdd(1.0);
    }

    private void incrementOntimeNewPrediction(){
        ontimeNewPredictionsCount.getAndAdd(1.0);
    }

    private void incrementLateNewPrediction(){
        lateNewPredictionsCount.getAndAdd(1.0);
    }

    private double getTotalOldPredictionsCount(){
        return earlyOldPredictionsCount.get() + ontimeOldPredictionsCount.get() + lateOldPredictionsCount.get();
    }

    private double getTotalNewPredictionsCount(){
        return earlyNewPredictionsCount.get() + ontimeNewPredictionsCount.get() + lateNewPredictionsCount.get();
    }

    public String getOldOntimePercentage(){
        if(ontimeOldPredictionsCount.get() == 0){
            return ZERO_PERCENT;
        }
        double totalPredictionsCount = getTotalOldPredictionsCount();
        DecimalFormat df = new DecimalFormat("#.##");
        String ontimePercentage = df.format((ontimeOldPredictionsCount.get()/totalPredictionsCount) * 100) + "%";
        return ontimePercentage;
    }

    public String getOldOntimeCount(){
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(ontimeOldPredictionsCount.get());
    }

    public synchronized String getOldEarlyPercentage(){
        if(earlyOldPredictionsCount.get() == 0){
            return ZERO_PERCENT;
        }
        double totalPredictionsCount = getTotalOldPredictionsCount();
        DecimalFormat df = new DecimalFormat("#.##");
        String earlyPercentage = df.format((earlyOldPredictionsCount.get()/totalPredictionsCount)*100) + "%";
        return earlyPercentage;
    }

    public String getOldEarlyCount(){
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(earlyOldPredictionsCount.get());
    }

    public String getOldLatePercentage(){
        if(lateOldPredictionsCount.get() == 0){
            return ZERO_PERCENT;
        }
        double totalPredictionsCount = getTotalOldPredictionsCount();
        DecimalFormat df = new DecimalFormat("#.##");
        String latePercentage = df.format((lateOldPredictionsCount.get()/totalPredictionsCount) * 100) + "%";
        return latePercentage;
    }

    public String getOldLateCount(){
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(lateOldPredictionsCount.get());
    }


    public String getNewOntimePercentage(){
        if(ontimeNewPredictionsCount.get() == 0){
            return ZERO_PERCENT;
        }
        double totalPredictionsCount = getTotalNewPredictionsCount();
        DecimalFormat df = new DecimalFormat("#.##");
        String ontimePercentage = df.format((ontimeNewPredictionsCount.get()/totalPredictionsCount) * 100) + "%";
        return ontimePercentage;
    }

    public String getNewOntimeCount(){
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(ontimeNewPredictionsCount.get());
    }

    public String getNewEarlyPercentage(){
        if(earlyNewPredictionsCount.get() == 0){
            return ZERO_PERCENT;
        }
        double totalPredictionsCount = getTotalNewPredictionsCount();
        DecimalFormat df = new DecimalFormat("#.##");
        String earlyPercentage = df.format((earlyNewPredictionsCount.get()/totalPredictionsCount)*100) + "%";
        return earlyPercentage;
    }

    public String getNewEarlyCount(){
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(earlyNewPredictionsCount.get());
    }

    public String getNewLatePercentage(){
        if(lateNewPredictionsCount.get() == 0){
            return ZERO_PERCENT;
        }
        double totalPredictionsCount = getTotalNewPredictionsCount();
        DecimalFormat df = new DecimalFormat("#.##");
        String latePercentage = df.format((lateNewPredictionsCount.get()/totalPredictionsCount) * 100) + "%";
        return latePercentage;
    }

    public String getNewLateCount(){
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(lateNewPredictionsCount.get());
    }

    @Override
    public String toString() {
        return "PredictionBucketAccuracy{" +
                "horizonDescription='" + horizonDescription + '\'' +
                ", horizon=" + horizonStart +
                ", earlyOldPredictionsCount=" + earlyOldPredictionsCount +
                ", ontimeOldPredictionsCount=" + ontimeOldPredictionsCount +
                ", lateOldPredictionsCount=" + lateOldPredictionsCount +
                ", earlyNewPredictionsCount=" + earlyNewPredictionsCount +
                ", ontimeNewPredictionsCount=" + ontimeNewPredictionsCount +
                ", lateNewPredictionsCount=" + lateNewPredictionsCount +
                '}';
    }

    @Override
    public int compareTo(PredictionBucketAccuracy o) {
        return Long.compare(this.horizonStart,o.horizonStart);
    }
}
