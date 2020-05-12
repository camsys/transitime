/*
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Transitime.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transitclock.core.predictiongenerator;

import org.transitclock.applications.Core;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.core.Indices;
import org.transitclock.core.ServiceUtils;
import org.transitclock.core.TravelTimeDetails;
import org.transitclock.core.VehicleState;
import org.transitclock.core.dataCache.*;
import org.transitclock.core.predictiongenerator.datafilter.TravelTimeDataFilter;
import org.transitclock.core.predictiongenerator.datafilter.TravelTimeFilterFactory;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.PredictionEvent;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.utils.Time;

import java.util.*;

/**
 * Commonly-used methods for PredictionGenerators that use historical cached data.
 */
public class HistoricalPredictionLibrary {

	private static final IntegerConfigValue closestVehicleStopsAhead = new IntegerConfigValue(
			"transitclock.prediction.closestvehiclestopsahead", new Integer(2),
			"Num stops ahead a vehicle must be to be considers in the closest vehicle calculation");

	private static ServiceUtils serviceUtils = null;
	private static CacheDuration cacheDuration = null;


	public static TravelTimeDetails getLastVehicleTravelTime(VehicleState currentVehicleState, Indices indices) throws Exception {

		StopArrivalDepartureCacheKey nextStopKey = new StopArrivalDepartureCacheKey(
				indices.getStopPath().getStopId(),
				new Date(currentVehicleState.getMatch().getAvlTime()));

		/* TODO how do we handle the the first stop path. Where do we get the first stop id. */
		if(!indices.atBeginningOfTrip())
		{
			String currentStopId = indices.getPreviousStopPath().getStopId();

			StopArrivalDepartureCacheKey currentStopKey = new StopArrivalDepartureCacheKey(currentStopId,
					new Date(currentVehicleState.getMatch().getAvlTime()));

			List<IpcArrivalDeparture> currentStopList = StopArrivalDepartureCacheFactory.getInstance().getStopHistory(currentStopKey);

			List<IpcArrivalDeparture> nextStopList = StopArrivalDepartureCacheFactory.getInstance().getStopHistory(nextStopKey);

			if (currentStopList != null && nextStopList != null) {
				// lists are already sorted when put into cache.
				for (IpcArrivalDeparture currentArrivalDeparture : currentStopList) {

					if(currentArrivalDeparture.isDeparture()
							&& (currentArrivalDeparture.getVehicleId() != null && !currentArrivalDeparture.getVehicleId().equals(currentVehicleState.getVehicleId()))
							&& (currentVehicleState.getTrip().getDirectionId()==null || currentVehicleState.getTrip().getDirectionId().equals(currentArrivalDeparture.getDirectionId())))
					{
						IpcArrivalDeparture found;

						if ((found = findMatchInList(nextStopList, currentArrivalDeparture)) != null) {
							TravelTimeDetails travelTimeDetails=new TravelTimeDetails(currentArrivalDeparture, found);
							if(travelTimeDetails.getTravelTime()>0)
							{
								return travelTimeDetails;

							}else
							{
								String description=found + " : " + currentArrivalDeparture;
								PredictionEvent.create(currentVehicleState.getAvlReport(), currentVehicleState.getMatch(), PredictionEvent.TRAVELTIME_EXCEPTION,
										description,
										travelTimeDetails.getArrival().getStopId(),
										travelTimeDetails.getDeparture().getStopId(),
										travelTimeDetails.getArrival().getVehicleId(),
										travelTimeDetails.getArrival().getTime(),
										travelTimeDetails.getDeparture().getTime()
								);
								return null;
							}
						}else
						{
							return null;
						}
					}
				}
			}
		}
		return null;
	}

	public static Indices getLastVehicleIndices(VehicleState currentVehicleState, Indices indices) {

		StopArrivalDepartureCacheKey nextStopKey = new StopArrivalDepartureCacheKey(
				indices.getStopPath().getStopId(),
				new Date(currentVehicleState.getMatch().getAvlTime()));

		/* TODO how do we handle the the first stop path. Where do we get the first stop id. */
		if(!indices.atBeginningOfTrip())
		{
			String currentStopId = indices.getPreviousStopPath().getStopId();

			StopArrivalDepartureCacheKey currentStopKey = new StopArrivalDepartureCacheKey(currentStopId,
					new Date(currentVehicleState.getMatch().getAvlTime()));

			List<IpcArrivalDeparture> currentStopList = StopArrivalDepartureCacheFactory.getInstance().getStopHistory(currentStopKey);

			List<IpcArrivalDeparture> nextStopList = StopArrivalDepartureCacheFactory.getInstance().getStopHistory(nextStopKey);

			if (currentStopList != null && nextStopList != null) {
				// lists are already sorted when put into cache.
				for (IpcArrivalDeparture currentArrivalDeparture : currentStopList) {

					if(currentArrivalDeparture.isDeparture() && !currentArrivalDeparture.getVehicleId().equals(currentVehicleState.getVehicleId())
							&& (currentVehicleState.getTrip().getDirectionId()==null || currentVehicleState.getTrip().getDirectionId().equals(currentArrivalDeparture.getDirectionId())))
					{
						IpcArrivalDeparture found;

						if ((found = findMatchInList(nextStopList, currentArrivalDeparture)) != null) {
							if(found.getTime().getTime() - currentArrivalDeparture.getTime().getTime()>0)
							{
								Block currentBlock=null;
								/* block is transient in arrival departure so when read from database need to get from dbconfig. */

								DbConfig dbConfig = Core.getInstance().getDbConfig();

								currentBlock=dbConfig.getBlock(currentArrivalDeparture.getServiceId(), currentArrivalDeparture.getBlockId());

								if(currentBlock!=null)
									return new Indices(currentBlock, currentArrivalDeparture.getTripIndex(), found.getStopPathIndex(), 0);
							}else
							{
								// must be going backwards
								return null;
							}
						}else
						{
							return null;
						}
					}
				}
			}
		}
		return null;
	}
	/* TODO could also make it a requirement that it is on the same route as the one we are generating prediction for */
	private static IpcArrivalDeparture findMatchInList(List<IpcArrivalDeparture> nextStopList,
													   IpcArrivalDeparture currentArrivalDeparture) {
		for (IpcArrivalDeparture nextStopArrivalDeparture : nextStopList) {
			if (currentArrivalDeparture.getVehicleId().equals(nextStopArrivalDeparture.getVehicleId())
					&& currentArrivalDeparture.getTripId().equals(nextStopArrivalDeparture.getTripId())
					&&  currentArrivalDeparture.isDeparture() && nextStopArrivalDeparture.isArrival() ) {
				return nextStopArrivalDeparture;
			}
		}
		return null;
	}

	private static VehicleState getClosetVechicle(List<VehicleState> vehiclesOnRoute, Indices indices,
												  VehicleState currentVehicleState) {

		Map<String, List<String>> stopsByDirection = currentVehicleState.getTrip().getRoute()
				.getOrderedStopsByDirection();

		List<String> routeStops = stopsByDirection.get(currentVehicleState.getTrip().getDirectionId());

		Integer closest = 100;

		VehicleState result = null;

		for (VehicleState vehicle : vehiclesOnRoute) {

			Integer numAfter = numAfter(routeStops, vehicle.getMatch().getStopPath().getStopId(),
					currentVehicleState.getMatch().getStopPath().getStopId());
			if (numAfter != null && numAfter > closestVehicleStopsAhead.getValue() && numAfter < closest) {
				closest = numAfter;
				result = vehicle;
			}
		}
		return result;
	}

	private static boolean isAfter(List<String> stops, String stop1, String stop2) {
		if (stops != null && stop1 != null && stop2 != null) {
			if (stops.contains(stop1) && stops.contains(stop2)) {
				if (stops.indexOf(stop1) > stops.indexOf(stop2))
					return true;
				else
					return false;
			}
		}
		return false;
	}

	private static Integer numAfter(List<String> stops, String stop1, String stop2) {
		if (stops != null && stop1 != null && stop2 != null)
			if (stops.contains(stop1) && stops.contains(stop2))
				return stops.indexOf(stop1) - stops.indexOf(stop2);

		return null;
	}

    public static List<TravelTimeDetails> lastDaysTimes(TripDataHistoryCacheInterface cache, String serviceId,
														String tripId,String direction, int stopPathIndex, Date startDate,
														Integer startTime, int numDaysLookBack, int numDays) {

		List<TravelTimeDetails> times = new ArrayList<TravelTimeDetails>();
		List<IpcArrivalDeparture> results = null;
		int num_found = 0;
		int delta = 0;
		ServiceUtils.ServiceType serviceType = getServiceUtils().getServiceType(serviceId);
		int serviceTypeNumDaysLookBack = getCacheDuration().getExpiryForServiceType(serviceType);

		while (delta < serviceTypeNumDaysLookBack && num_found < numDays) {

			Date nearestDay = getServiceUtils().getNthDayPerCalendar(serviceType, startDate, (delta+1)*-1);
			delta = getDeltaDays(startDate, nearestDay) + 1;
			TripKey tripKey = new TripKey(tripId, nearestDay, startTime);

			results = cache.getTripHistory(tripKey);

			if (results != null) {

				IpcArrivalDeparture arrival = getArrival(stopPathIndex, results);

				if(arrival!=null)
				{
					IpcArrivalDeparture departure = TripDataHistoryCacheFactory.getInstance().findPreviousDepartureEvent(results, arrival);

					if (arrival != null && departure != null) {

						TravelTimeDetails travelTimeDetails=new TravelTimeDetails(departure, arrival);

						if(travelTimeDetails.getTravelTime()!=-1)
						{
							TravelTimeDataFilter travelTimefilter = TravelTimeFilterFactory.getInstance();
							if(!travelTimefilter.filter(travelTimeDetails.getDeparture(),travelTimeDetails.getArrival()))
							{
								times.add(travelTimeDetails);
								num_found++;
							}
						}
					}
				}
			}
		}
		return times;
    }

	private static CacheDuration getCacheDuration() {
		if (cacheDuration == null)
			cacheDuration = new CacheDuration();
		return cacheDuration;
	}

	private static int getDeltaDays(Date startDate, Date endDate) {
		long delta = endDate.getTime() - startDate.getTime();
		return new Double(delta / Time.MS_PER_YEAR).intValue();
	}


	private static IpcArrivalDeparture getArrival(int stopPathIndex, List<IpcArrivalDeparture> results)
	{
		for(IpcArrivalDeparture result:results)
		{
			if(result.isArrival()&&result.getStopPathIndex()==stopPathIndex)
			{
				return result;
			}
		}
		return null;
	}

	private static long timeBetweenStops(ArrivalDeparture ad1, ArrivalDeparture ad2) {
		
		return Math.abs(ad2.getTime() - ad1.getTime());		
	}

	public static <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
		return iterable == null ? Collections.<T> emptyList() : iterable;
	}

	private static ServiceUtils getServiceUtils() {
		if (serviceUtils == null)
			serviceUtils = new ServiceUtils(Core.getInstance().getDbConfig());
		return serviceUtils;
	}
}
