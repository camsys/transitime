package org.transitclock.integration_tests.prediction;

import org.junit.Test;

public class PredictionAccuracy921_2641IntegrationTest extends AbstractPredictionAccuracyIntegrationTest {
    public PredictionAccuracy921_2641IntegrationTest() {
        super (createTraceConfig("921-2641", "America/Chicago"));
    }
    @Test
    public void testPredictions() {
        super.testPredictions();
    }
}
