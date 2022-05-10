package org.transitclock.integration_tests.prediction;

import org.junit.Test;

/**
 * Bad predictions on route 921.
 */
public class PredictionAccuracy921_497IntegrationTest extends AbstractPredictionAccuracyIntegrationTest {
    private static final String GTFS = "classpath:gtfs/921-497";
    private static final String AVL = "classpath:avl/921-497.csv";
    private static final String PREDICTIONS_CSV = "classpath:pred/921-497.csv";
    private static final String HISTORY = "classpath:history/921-497.csv";
    private static final String OUTPUT_DIRECTORY = "/tmp/output/921-497";

    public PredictionAccuracy921_497IntegrationTest() {
        super ("921-497", OUTPUT_DIRECTORY, GTFS, AVL, PREDICTIONS_CSV, HISTORY);
    }

    @Test
    public void testPredictions() {
        super.testPredictions();
    }
}
