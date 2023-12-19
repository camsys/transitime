package org.transitclock.core.dwell;

import org.transitclock.applications.Core;
import org.transitclock.config.LongConfigValue;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.Trip;
import org.transitclock.utils.Time;

import java.util.Date;

public class DwellTimeUtil {

    /**
     * Specify minimum allowable time in msec when calculating dwell time for departures.
     * Defaults to 1 millisecond, can be overwritten in properties
     * @return
     */
    private static long getMinAllowableDwellTime() {
        return minAllowableDwellTime.getValue();
    }
    private static LongConfigValue minAllowableDwellTime =
            new LongConfigValue(
                    "transitclock.arrivalsDepartures.minAllowableDwellTimeMsec",
                    1l * Time.MS_PER_SEC,
                    "Specify minimum allowable time in msec when calculating dwell time for departures.");

    /**
     * Specifying the Max allowable time when calculating dwell time for departures.
     * Defaults to 60 seconds, can be overwritten in properties
     * @return
     */
    private static long getMaxAllowableDwellTime() {
        return maxAllowableDwellTime.getValue();
    }
    private static LongConfigValue maxAllowableDwellTime =
            new LongConfigValue(
                    "transitclock.arrivalsDepartures.maxAllowableDwellTimeMsec",
                    60l * Time.MS_PER_MIN,
                    "Specify maximum allowable time in msec when calculating dwell time for departures.");


    public static Long getDwellTime(Long arrivalTime, Long departureTime,Block block,int tripIndex,
                              int departureStopPathIndex, Integer arrivalStopPathIndex){

        //Logic for when it is the first stop
        if (departureStopPathIndex == 0 && departureTime != null) {
            Trip trip = block.getTrip(tripIndex);
            if (trip != null) {
                return calculateFirstStopDwellTime(trip.getStartTime(), departureTime);
            }
        //Logic for all other stops except for the last stop (no dwell for last stop)
        } else if (departureStopPathIndex != block.numStopPaths(tripIndex) - 1) {
            if (arrivalTime != null && departureTime != null &&
                    arrivalStopPathIndex != null && departureStopPathIndex == arrivalStopPathIndex) {
                //get the dwell time by doing some simple subtraction :)
                return recalculateDwellTimeUsingThresholds(departureTime - arrivalTime);
            }
        } else {
            return 0l;
        }

        return null;
    }

    public static Long calculateFirstStopDwellTime(Integer scheduledStartTime, Long departureTime){
            long tripStartTimeMsecs = scheduledStartTime * 1000;
            Time time = Core.getInstance().getTime();
            long msecsIntoDay = time.getMsecsIntoDay(new Date(departureTime), tripStartTimeMsecs);
            // Dwell for 0 seconds if the departure time is earlier than the trip start time
            // could be an issue in the data given or the bus could be late
            if (msecsIntoDay < tripStartTimeMsecs) {
                return 0l;
            } else {
            //get the dwell time by doing some simple subtraction :)
                return recalculateDwellTimeUsingThresholds(msecsIntoDay - tripStartTimeMsecs);
            }
    }

    private static Long recalculateDwellTimeUsingThresholds(Long dwellTime){
        //Should we return the min dwell time if the dwell time is null?
        if(dwellTime != null){
            if(dwellTime < getMinAllowableDwellTime()){
                return getMinAllowableDwellTime();
            }
            //If dwell time is less than max, return dwell time.
            //Should we return max time if the dwell time is over max?
            else if(dwellTime <= getMaxAllowableDwellTime()){
                return dwellTime;
            }
        }
        return null;
    }
}
