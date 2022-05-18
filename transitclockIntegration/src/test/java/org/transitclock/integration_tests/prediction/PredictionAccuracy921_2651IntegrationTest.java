package org.transitclock.integration_tests.prediction;

import org.junit.Test;

/**
 * A Route 921 trace without APC data
 */
public class PredictionAccuracy921_2651IntegrationTest extends AbstractPredictionAccuracyIntegrationTest  {
    public PredictionAccuracy921_2651IntegrationTest() {
        super(createTraceConfig("921-2651", "America/Chicago", false, false));
    }

    @Test
    public void testPredictions() { super.testPredictions(); }

}
