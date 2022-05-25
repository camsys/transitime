package org.transitclock.integration_tests.prediction;

import org.junit.Test;

/**
 * Test of Trace with APC data.  APCStopTimeGenerator will not succeed until we add real-time headway data.
 */
public class PredictionAccuracy921_2391IntegrationTest extends AbstractPredictionAccuracyIntegrationTest {
    public PredictionAccuracy921_2391IntegrationTest() {
        super(createApcTraceConfig("921-2391", "America/Chicago", false, true));
    }
    @Test
    public void testPredictions() { super.testPredictions(); }

}
