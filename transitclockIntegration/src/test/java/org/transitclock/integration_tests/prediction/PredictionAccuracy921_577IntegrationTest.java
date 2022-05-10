package org.transitclock.integration_tests.prediction;

import org.junit.Test;

public class PredictionAccuracy921_577IntegrationTest extends AbstractPredictionAccuracyIntegrationTest {

    public PredictionAccuracy921_577IntegrationTest() {
        super (createTraceConfig("921-577", "America/Chicago"));
    }

    @Test
    public void testPredictions() {
        super.testPredictions();
    }

}
