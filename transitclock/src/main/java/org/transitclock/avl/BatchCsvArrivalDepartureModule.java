package org.transitclock.avl;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.StringConfigValue;
import org.transitclock.db.structs.ArrivalDeparture;

import java.util.List;

/**
 * Process historical Arrival Departures
 */
public class BatchCsvArrivalDepartureModule {

    private static final Logger logger = LoggerFactory
            .getLogger(BatchCsvArrivalDepartureModule.class);
    private String agencyId;
    private int configRev;

    private Session session;

    private static StringConfigValue csvArrivalDepartureFileName =
            new StringConfigValue("transitclock.avl.csvArrivalDepartureFeedFileName",
                    null,
                    "The name of the CSV file containing historical " +
                            "ArrivalDepartures to process.");

    private List<ArrivalDeparture> arrivalDepartures;

    public List<ArrivalDeparture> getArrivalDepartures() {
        return arrivalDepartures;
    }
    public BatchCsvArrivalDepartureModule(String agencyId, int configRev, Session session) {
        this.agencyId = agencyId;
        this.configRev = configRev;
        this.session = session;
    }
    public void run() {
        String fileName = getCsvArrivalDepartureFileName();
        ArrivalDepartureCsvReader arrivalDepartureCsvReader
                = new ArrivalDepartureCsvReader(fileName, configRev);
        this.arrivalDepartures =
                arrivalDepartureCsvReader.get();
        for (ArrivalDeparture arrivalDeparture : arrivalDepartures) {
            logger.info("Processing arrivalDeparture={}", arrivalDeparture);
            session.save(arrivalDeparture);
        }
        session.flush();
    }

    private String getCsvArrivalDepartureFileName() {
        return csvArrivalDepartureFileName.getValue();
    }
}
