package org.transitclock.integration_tests.prediction;

import org.junit.Test;

public class PredictionAccuracy921_577IntegrationTest extends AbstractPredictionAccuracyIntegrationTest {

    private static final String GTFS = "classpath:gtfs/921-577";
    private static final String AVL = "classpath:avl/921-577.csv";
    private static final String PREDICTIONS_CSV = "classpath:pred/921-577.csv";
    private static final String HISTORY = "classpath:history/921-577.csv";
    private static final String OUTPUT_DIRECTORY = "/tmp/output/921-577";

    public PredictionAccuracy921_577IntegrationTest() {
        super ("921-577", OUTPUT_DIRECTORY, GTFS, AVL, PREDICTIONS_CSV, HISTORY, "America/Chicago");
    }

    @Test
    public void testPredictions() {
        super.testPredictions();
    }

}
