package org.transitclock.integration_tests.playback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.structs.Prediction;
import org.transitclock.utils.Time;
import org.transitclock.utils.csv.CsvWriterBase;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

/**
 * Write out combined prediction records.
 */
public class PredictionBucketAccuracyCsvWriter extends CsvWriterBase {


    private static final Logger logger =
            LoggerFactory.getLogger(PredictionBucketAccuracyCsvWriter.class);
    public PredictionBucketAccuracyCsvWriter(String fileName, String timezoneStr) {
        super(fileName, false);
    }

    @Override
    protected void writeHeader() throws IOException {
        StringBuilder header = new StringBuilder();
        header.append("horizon").append(",");
        header.append("old_ontime_count").append(",");
        header.append("old_early_count").append(",");
        header.append("old_late_count").append(",");
        header.append("old_ontime_percentage").append(",");
        header.append("old_early_percentage").append(",");
        header.append("old_late_percentage").append(",");
        header.append("new_ontime_count").append(",");
        header.append("new_early_count").append(",");
        header.append("new_late_count").append(",");
        header.append("new_ontime_percentage").append(",");
        header.append("new_early_percentage").append(",");
        header.append("new_late_percentage");
        appendLine(header.toString());
    }

    public void write(PredictionBucketAccuracy bucket) {
        try {
            writeUnsafe(bucket);
        } catch (IOException e) {
            logger.error("Error writing {}.", bucket, e);
        }
    }

    private void writeUnsafe(PredictionBucketAccuracy bucket) throws IOException {
        if(bucket != null){
            // horizon
            appendCol(bucket.getHorizonDescription());
            // old ontime count
            appendCol(bucket.getOldOntimeCount());
            // old early count
            appendCol(bucket.getOldEarlyCount());
            // old late count
            appendCol(bucket.getOldLateCount());
            // old ontime percentage
            appendCol(bucket.getOldOntimePercentage());
            // old early percentage
            appendCol(bucket.getOldEarlyPercentage());
            // old late percentage
            appendCol(bucket.getOldLatePercentage());
            // new ontime count
            appendCol(bucket.getNewOntimeCount());
            // new early count
            appendCol(bucket.getNewEarlyCount());
            // new late count
            appendCol(bucket.getNewLateCount());
            // new ontime percentage
            appendCol(bucket.getNewOntimePercentage());
            // new early percentage
            appendCol(bucket.getNewEarlyPercentage());
            // new late percentage
            appendLine(bucket.getNewLatePercentage());
        }
    }

}
