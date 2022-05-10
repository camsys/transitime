package org.transitclock.integration_tests.playback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.integration_tests.prediction.TraceConfig;
import org.transitclock.utils.DateRange;

import java.util.*;

/**
 * Integration test services for replaying data.
 */
public class ReplayService {

    private static final Logger logger = LoggerFactory.getLogger(ReplayService.class);

    private String id;
    private ReplayAnalysis analysis;

    private ReplayLoader loader;

    public Collection<CombinedPredictionAccuracy> getCombinedPredictionAccuracy() {
        return loader.getCombinedPredictionAccuracy();
    }

    public ReplayService(String id, String outputDirectory) {
         analysis = new ReplayAnalysis();
         loader = new ReplayLoader(outputDirectory);
         this.id = id;
         System.setProperty("transitclock.integration_test.enabled", "true");
    }

    public void run(TraceConfig config) {
        // Run trace
        DateRange range = PlaybackModule.runTrace(config, null);
        loader.createCombinedPredictionAccuracyStructure(range, config.getArrivalDepartureCsv());

    }

    public void loadPastPredictions(String predictionsCsvFileName) {
        loader.loadPredictionsFromCSV(predictionsCsvFileName);
    }

    public void accumulate() {
        // Fill new predictions
        loader.accumulate(id);
    }

    public ReplayResults compare() {
        return analysis.compare(getCombinedPredictionAccuracy());
    }
}
