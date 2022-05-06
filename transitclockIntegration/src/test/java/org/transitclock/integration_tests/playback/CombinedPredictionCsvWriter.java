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
public class CombinedPredictionCsvWriter extends CsvWriterBase {

    private final Time timeUsingTimeZone;

    private static final Logger logger =
            LoggerFactory.getLogger(CombinedPredictionCsvWriter.class);
    public CombinedPredictionCsvWriter(String fileName, String timezoneStr) {
        super(fileName, false);
        timeUsingTimeZone = new Time(timezoneStr);
    }

    @Override
    protected void writeHeader() throws IOException {
        appendLine("id,affectedByWaitStop,avlTime,configRev,old_creationTime,gtfsStopSeq,isArrival," +
                "old_predictionTime,routeId,schedBasedPred,stopId,tripId,vehicleId,new_predictionTime");

    }

    public void write(CombinedPredictionAccuracy combined) {
        try {
            writeUnsafe(combined);
        } catch (IOException e) {
            logger.error("Error writing {}.", combined, e);
        }
    }

    private void writeUnsafe(CombinedPredictionAccuracy combined) throws IOException {
        if (combined.oldPrediction == null && combined.newPrediction == null) {
            // nothing to do
            logger.error("nothing to do for record avlTime={} and stopSeq={}",
                    combined.actualADTime, combined.stopSeq);
            return;
        }
        Prediction prediction = combined.oldPrediction;
        if (prediction != null) {
            // id
            appendCol(prediction.getId());
            // affectedByWaitStop
            appendCol(prediction.isAffectedByWaitStop());
            // avlTime
            appendCol(formatTime(prediction.getAvlTime()));
            //configRev
            appendCol(prediction.getConfigRev());
            // creationTime
            appendCol(formatTime(prediction.getCreationTime()));
            // gtfsStopSeq
            appendCol(prediction.getGtfsStopSeq());
            // isArrival
            appendCol(prediction.isArrival());
            // predictionTIme
            appendCol(formatTime(prediction.getPredictionTime()));
            // routeId
            appendCol(prediction.getRouteId());
            // schedBasedPred
            appendCol(prediction.isSchedBasedPred());
            // stopId
            appendCol(prediction.getStopId());
            // tripId
            appendCol(prediction.getTripId());
            // vehicleId
            appendCol(prediction.getVehicleId());
        } else if (combined.newPrediction != null) {
            prediction = combined.newPrediction;
            // id
            appendCol(-1);
            // affectedByWaitStop
            appendCol(prediction.isAffectedByWaitStop());
            // avlTime
            appendCol(formatTime(prediction.getAvlTime()));
            //configRev
            appendCol(prediction.getConfigRev());
            // creationTime
            appendCol(formatTime(prediction.getCreationTime()));
            // gtfsStopSeq
            appendCol(prediction.getGtfsStopSeq());
            // isArrival
            appendCol(prediction.isArrival());
            // predictionTIme
            appendCol(-1);
            // routeId
            appendCol(prediction.getRouteId());
            // schedBasedPred
            appendCol(prediction.isSchedBasedPred());
            // stopId
            appendCol(prediction.getStopId());
            // tripId
            appendCol(prediction.getTripId());
            // vehicleId
            appendCol(prediction.getVehicleId());

        }
        // new prediction
        if (combined.newPrediction != null) {
            appendLine(formatTime(combined.newPrediction.getPredictionTime()));
        } else {
            appendLine("-1");
        }
    }

    private String formatTime(Date value) {
        DateFormat sdf = Time.getReadableDateFormat24NoTimeZoneNoMsec();
        return sdf.format(value);
    }
}
