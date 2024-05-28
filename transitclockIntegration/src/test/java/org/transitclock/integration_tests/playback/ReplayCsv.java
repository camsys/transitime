package org.transitclock.integration_tests.playback;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.core.predictiongenerator.PredictionCsvWriter;
import org.transitclock.db.structs.Prediction;
import org.transitclock.utils.Time;

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Delegate CSV operations to its own class.
 */
public class ReplayCsv {

    private static final Logger logger = LoggerFactory.getLogger(ReplayCsv.class);

    private String outputDirectory;
    public ReplayCsv(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public List<Prediction> loadPredictions(String predictionsCsvFileName) {
        ArrayList<Prediction> list = new ArrayList<>();
        InputStream inputStream = null;
        try {

            // Try removing classpath prefix if it exists
            String filename = predictionsCsvFileName.replaceFirst("classpath:", "");

            inputStream = this.getClass().getClassLoader().getResourceAsStream(filename);

            // Try removing src/test/resources prefix if it exists
            if (inputStream == null) {
                // some sort of classloader issue -- try again with truncated path
                filename = filename.replaceFirst("src/test/resources/", "");
                inputStream = this.getClass().getClassLoader().getResourceAsStream(filename);
            }

            // Try loading the file from absolute path
            if(inputStream == null){
                File file = new File(filename);
                inputStream = new FileInputStream(file);
            }

            if (inputStream == null) throw new FileNotFoundException("'" + filename + "' not found!");

            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(new InputStreamReader(inputStream));

            for (CSVRecord r : records) {
                Prediction p = createPredictionFromCsvRecord(r);
                if (p != null)
                    list.add(p);
            }
        } catch (IOException e) {
            logger.error("failed to load predictions from file " + predictionsCsvFileName , e);
            // don't continue if we were configured to load this file but couldn't
            throw new RuntimeException(e);
        } finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error("Error closing input stream", e);
                }
            }
        }
        logger.info("Loading {} predictions from CSV file {}", list.size(), predictionsCsvFileName);
        return list;
    }

    private static Prediction createPredictionFromCsvRecord(CSVRecord r) {
        try {
            long predictionTime = Time.parse(r.get("predictionTime")).getTime();
            long avlTime = Time.parse(r.get("avlTime")).getTime();
            long creationTime= Time.parse(r.get("creationTime")).getTime();
            String vehicleId = r.get("vehicleId");
            String stopId = r.get("stopId");
            String tripId = r.get("tripId");
            String routeId = r.get("routeId");
            boolean affectedByWaitStop = false;
            try {
                affectedByWaitStop = Integer.parseInt(r.get("affectedByWaitStop")) > 0;
            } catch (NumberFormatException nfe) {
                // support int or boolean
                affectedByWaitStop = "true".equalsIgnoreCase(r.get("affectedByWaitStop"));
            }
            boolean isArrival;
            try {
                isArrival = Integer.parseInt(r.get("isArrival")) > 0;
            } catch (NumberFormatException nfe) {
                isArrival = "true".equalsIgnoreCase(r.get("isArrival"));
            }
            boolean schedBasedPred;
            try {
                schedBasedPred = Integer.parseInt(r.get("schedBasedPred")) > 0;
            } catch (NumberFormatException nfe) {
                schedBasedPred = "true".equalsIgnoreCase(r.get("schedBasedPred"));
            }
            int stopSeq = Integer.parseInt(r.get("gtfsStopSeq"));
            return new Prediction(predictionTime, avlTime, creationTime, vehicleId,
                    stopId, tripId, routeId, affectedByWaitStop, isArrival, schedBasedPred,
                    stopSeq);
        } catch (ParseException ex) {
            logger.error(ex.getMessage());
            return null;
        }

    }

    public String write(List<Prediction> predictions, String fileType, String id) {
        String fileName = generateOutputFileName(fileType, id);
        if (predictions == null) {
            logger.error("no predictions to write out to disk");
            return null;
        }
        logger.info("writing {} predictions to {}", predictions.size(), fileName);
        PredictionCsvWriter writer = new PredictionCsvWriter(fileName, null);
        for (Prediction prediction : predictions) {
            writer.write(prediction);
        }
        writer.close();
        return fileName;
    }

    private String generateOutputFileName(String fileType, String id) {
        File outputDir = new File(outputDirectory);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        return outputDirectory + File.separator + fileType + "_" + id + ".csv";
    }


    public String write(ArrayList<CombinedPredictionAccuracy> combinedPredictionAccuracy, String fileType, String id) {
        String fileName = generateOutputFileName(fileType, id);
        if (combinedPredictionAccuracy == null) {
            logger.error("no combined predictions to write out to disk");
            return null;
        }

        CombinedPredictionCsvWriter writer = null;
        try {
            writer = new CombinedPredictionCsvWriter(fileName, null);
            for (CombinedPredictionAccuracy predictionAccuracy : combinedPredictionAccuracy) {
                writer.write(predictionAccuracy);
            }
        }
        finally {
            if(writer != null){
                writer.close();
            }
        }

        return fileName;
    }

    public String writeBucket(List<PredictionBucketAccuracy> predictionBucketAccuracies, String fileType, String id) {
        String fileName = generateOutputFileName(fileType, id);
        if (predictionBucketAccuracies == null) {
            logger.error("no bucket predictions to write out to disk");
            return null;
        }

        PredictionBucketAccuracyCsvWriter writer = null;
        try {
            writer = new PredictionBucketAccuracyCsvWriter(fileName, null);
            for (PredictionBucketAccuracy predictionBucketAccuracy : predictionBucketAccuracies) {
                writer.write(predictionBucketAccuracy);
            }
        } finally {
            if(writer != null){
                writer.close();
            }
        }

        return fileName;
    }
}
