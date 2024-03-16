package org.transitclock.reporting.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.avl.ArrivalDepartureDAO;
import org.transitclock.core.ServiceType;
import org.transitclock.db.query.ArrivalDepartureQuery;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.ipc.data.IpcArrivalDepartureScheduleAdherence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.transitclock.ipc.util.GtfsDbDataUtil.*;

public class OnTimePerformanceService {

    private static final Logger logger =
            LoggerFactory.getLogger(OnTimePerformanceService.class);

    public List<IpcArrivalDepartureScheduleAdherence> getArrivalsDeparturesForOtp(ArrivalDepartureQuery query) throws Exception {

        List<IpcArrivalDepartureScheduleAdherence> ipcArrivalDepartures = new ArrayList<>();

        List<ArrivalDeparture> arrivalDepartures = ArrivalDepartureDAO.getArrivalsDeparturesFromDb(query);

        for(ArrivalDeparture arrivalDeparture : arrivalDepartures){
            IpcArrivalDepartureScheduleAdherence ipcArrivalDeparture = new IpcArrivalDepartureScheduleAdherence(arrivalDeparture);
            ipcArrivalDepartures.add(ipcArrivalDeparture);
        }
        return ipcArrivalDepartures;
    }

    public List<ArrivalDeparture> getArrivalsDepartures(LocalDate beginDate,
                                                        LocalDate endDate,
                                                        LocalTime beginTime,
                                                        LocalTime endTime,
                                                        String routeIdOrShortName,
                                                        ServiceType serviceType,
                                                        boolean timePointsOnly,
                                                        String headsign,
                                                        String tripPatternId,
                                                        boolean readOnly) throws Exception {

        String routeShortName = getRouteShortName(routeIdOrShortName);

        ArrivalDepartureQuery.Builder adBuilder = new ArrivalDepartureQuery.Builder();
        ArrivalDepartureQuery adQuery = adBuilder
                .beginDate(beginDate)
                .endDate(endDate)
                .beginTime(beginTime)
                .endTime(endTime)
                .routeShortName(routeShortName)
                .serviceType(serviceType)
                .timePointsOnly(timePointsOnly)
                .tripPatternId(tripPatternId)
                .scheduledTimesOnly(true)
                .readOnly(readOnly)
                .build();

        List<ArrivalDeparture> arrivalDepartures = ArrivalDepartureDAO.getArrivalsDeparturesFromDb(adQuery);
        return arrivalDepartures;
    }
}
