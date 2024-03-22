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

package org.transitclock.ipc.servers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.core.dataCache.VehicleDataCache;
import org.transitclock.db.model.AgencyInterface;
import org.transitclock.db.structs.*;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.ipc.data.*;
import org.transitclock.ipc.interfaces.ConfigInterface;
import org.transitclock.ipc.rmi.AbstractServer;
import org.transitclock.service.BackingStore;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Implements ConfigInterface to serve up configuration information to RMI
 * clients. 
 * 
 * @author SkiBu Smith
 * 
 */
public class ConfigServer extends AbstractServer implements ConfigInterface {

	// Should only be accessed as singleton class
	private static ConfigServer singleton;
	

	private static final Logger logger = 
			LoggerFactory.getLogger(ConfigServer.class);

	/********************** Member Functions **************************/

	/**
	 * Starts up the ConfigServer so that RMI calls can query for configuration
	 * data. This will automatically cause the object to continue to run and
	 * serve requests.
	 * 
	 * @param agencyId
	 * @return the singleton ConfigServer object. Usually does not need to
	 *         used since the server will be fully running.
	 */
	public static ConfigServer start(String agencyId) {
		if (singleton == null) {
			singleton = new ConfigServer(agencyId);
		}
		
		if (!singleton.getAgencyId().equals(agencyId)) {
			logger.error("Tried calling ConfigServer.start() for " +
					"agencyId={} but the singleton was created for agencyId={}", 
					agencyId, singleton.getAgencyId());
			return null;
		}
		
		return singleton;
	}

	/**
	 * Constructor. Made private so that can only be instantiated by
	 * get(). Doesn't actually do anything since all the work is done in
	 * the superclass constructor.
	 * 
	 * @param agencyId
	 *            for registering this object with the rmiregistry
	 */
	private ConfigServer(String agencyId) {
		super(agencyId, ConfigInterface.class.getSimpleName());
	}

	/**
	 * For getting route from routeIdOrShortName. Tries using
	 * routeIdOrShortName as first a route short name to see if there is such a
	 * route. If not, then uses routeIdOrShortName as a routeId.
	 * 
	 * @param routeIdOrShortName
	 * @return The Route, or null if no such route
	 */
	private RouteInterface getRoute(String routeIdOrShortName) {
		DbConfig dbConfig = Core.getInstance().getDbConfig();
		RouteInterface aRoute =
				dbConfig.getRouteByShortName(routeIdOrShortName);
		if (aRoute == null)
			aRoute = dbConfig.getRouteById(routeIdOrShortName);
		if (aRoute != null)
			return aRoute;
		else return null;
	}
	
	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.ConfigInterface#getRoutes()
	 */
	@Override
	public Collection<IpcRouteSummary> getRoutes() throws RemoteException {
		// Get the db route info
		BackingStore backingStore = Core.getInstance().getBackingStore();
		Collection<RouteInterface> routes = backingStore
				.getRoutes();
		
		// Convert the db routes into ipc routes
		Collection<IpcRouteSummary> ipcRoutes = 
				new ArrayList<IpcRouteSummary>(routes.size());
		for (org.transitclock.db.structs.RouteInterface aRoute : routes) {
			IpcRouteSummary ipcRoute = new IpcRouteSummary(aRoute);
			ipcRoutes.add(ipcRoute);
		}
		
		// Return the collection of ipc routes
		return ipcRoutes;
	}


	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.ConfigInterface#getRoute(java.lang.String)
	 */
	@Override
	public IpcRoute getRoute(String routeIdOrShortName, String directionId,
			String stopId, String tripPatternId) throws RemoteException {
		// Determine the route
		RouteInterface aRoute = getRoute(routeIdOrShortName);
		if (aRoute == null)
			return null;
		
		// Convert db route into an ipc route and return it
		IpcRoute ipcRoute =
				new IpcRoute(aRoute, directionId, stopId, tripPatternId);
		return ipcRoute;
	}

	/*
	 * (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.ConfigInterface#getRoutes(java.util.List)
	 */
	@Override
	public List<IpcRoute> getRoutes(List<String> routeIdsOrShortNames)
			throws RemoteException {
		List<IpcRoute> filteredRoutes = new ArrayList<IpcRoute>();

		// If no route specified then return data for all routes
		if (routeIdsOrShortNames == null || routeIdsOrShortNames.isEmpty()) {
			BackingStore backingStore = Core.getInstance().getBackingStore();
			List<RouteInterface> allRoutes =
					backingStore.getRoutes();
			for (RouteInterface aRoute : allRoutes) {
				IpcRoute ipcRoute = new IpcRoute(aRoute, null, null, null);
				filteredRoutes.add(ipcRoute);
			}
		} else {
			// Routes specified so return data for those routes
			for (String routeIdOrShortName : routeIdsOrShortNames) {
				// Determine the route
				RouteInterface aRoute = getRoute(routeIdOrShortName);
				if (aRoute == null)
					continue;

				IpcRoute ipcRoute = new IpcRoute(aRoute, null, null, null);
				filteredRoutes.add(ipcRoute);
			}
		}
		
		return filteredRoutes;
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.ConfigInterface#getRoutesForStop(java.lang.String)
	 */
	@Override
	public List<IpcRoute> getRoutesForStop(String stopId) throws RemoteException{
		List<IpcRoute> routes = new ArrayList<>();
		BackingStore backingStore = Core.getInstance().getBackingStore();
		Collection<RouteInterface> routesForStop = backingStore.getRoutesForStop(stopId);
		if(routesForStop != null){
			for(RouteInterface route : routesForStop){
				routes.add(new IpcRoute(route, null, null, null));
			}
		}
		return routes;
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.ConfigInterface#getStops(java.lang.String)
	 */
	@Override
	public IpcDirectionsForRoute getStops(String routeIdOrShortName)
			throws RemoteException {
		// Get the db route info 
		RouteInterface aRoute = getRoute(routeIdOrShortName);
		if (aRoute == null)
			return null;
		
		// Convert db route into an ipc route
		IpcDirectionsForRoute ipcStopsForRoute = new IpcDirectionsForRoute(aRoute);
		
		// Return the ipc route
		return ipcStopsForRoute;
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.ConfigInterface#getStops(java.util.List)
	 */
	@Override
	public List<IpcDirectionsForRoute> getStops(List<String> routeIdsOrShortNames)
			throws RemoteException{
		List<RouteInterface> routes = new ArrayList<>();

		// If no route specified then return data for all routes
		if (routeIdsOrShortNames == null || routeIdsOrShortNames.isEmpty()) {
			BackingStore backingStore = Core.getInstance().getBackingStore();
			List<RouteInterface> allRoutes =
							backingStore.getRoutes();
			for (RouteInterface aRoute : routes) {
				routes.add(aRoute);
			}
		} else {
			// Routes specified so return data for those routes
			for (String routeIdOrShortName : routeIdsOrShortNames) {
				// Determine the route
				RouteInterface aRoute = getRoute(routeIdOrShortName);
				if (aRoute == null)
					continue;
				routes.add(aRoute);
			}
		}

		List<IpcDirectionsForRoute> ipcStopsForRoutes = new ArrayList<>();

		// Convert db route into an ipc route
		for(RouteInterface aRoute : routes){
			ipcStopsForRoutes.add(new IpcDirectionsForRoute(aRoute));
		}

		// Return the ipc routes list
		return ipcStopsForRoutes;
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.ConfigInterface#getBlock(java.lang.String, java.lang.String)
	 */
	@Override
	public IpcBlock getBlock(String blockId, String serviceId)
			throws RemoteException {
		BlockInterface dbBlock =
				Core.getInstance().getDbConfig().getBlock(serviceId, blockId);
		
		// If no such block then return null since can't create a IpcBlock
		if (dbBlock == null)
			return null;
		
		return new IpcBlock(dbBlock);
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.ConfigInterface#getBlocks(java.lang.String)
	 */
	@Override
	public Collection<IpcBlock> getBlocks(String blockId)
			throws RemoteException {
		// For returning results
		Collection<IpcBlock> ipcBlocks = new ArrayList<IpcBlock>();
		
		// Get the blocks with specified ID
		Collection<BlockInterface> dbBlocks =
				Core.getInstance().getBackingStore().getBlocksForAllServiceIds(blockId);
		
		// Convert blocks from DB into IpcBlocks
		for (BlockInterface aBlock : dbBlocks) {
			ipcBlocks.add(new IpcBlock(aBlock));
		}
		
		// Return result
		return ipcBlocks;
	}
	
	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.ConfigInterface#getTrip(java.lang.String)
	 */
	@Override
	public IpcTrip getTrip(String tripId) throws RemoteException {
		Trip dbTrip = Core.getInstance().getDbConfig().getTrip(tripId);

		// If couldn't find a trip with the specified trip_id then see if a
		// a trip has the trip_short_name specified.
		if (dbTrip == null) {
			dbTrip =
					Core.getInstance().getDbConfig()
							.getTripUsingTripShortName(tripId);
		}
		
		// If no such trip then return null since can't create a IpcTrip
		if (dbTrip == null)
			return null;
		
		return new IpcTrip(dbTrip);
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.ConfigInterface#getTripPattern(java.lang.String)
	 */
	@Override
	public List<IpcTripPattern> getTripPatterns(String routeIdOrShortName)
			throws RemoteException {
		DbConfig dbConfig = Core.getInstance().getDbConfig();
		
		RouteInterface aRoute = getRoute(routeIdOrShortName);
		if (aRoute == null)
			return null;

		List<TripPattern> dbTripPatterns = 
				dbConfig.getTripPatternsForRoute(aRoute.getId());
		if (dbTripPatterns == null)
			return null;
		
		List<IpcTripPattern> tripPatterns = new ArrayList<IpcTripPattern>();
		for (TripPattern dbTripPattern : dbTripPatterns) {
			tripPatterns.add(new IpcTripPattern(dbTripPattern));
		}
		return tripPatterns;
	}

	@Override
	public List<IpcTripPattern> getTripPatterns(String routeIdOrShortName, String headSign, String directionId)
			throws RemoteException {
		DbConfig dbConfig = Core.getInstance().getDbConfig();
		RouteInterface aRoute = getRoute(routeIdOrShortName);
		if (aRoute == null)
			return null;

		List<TripPattern> dbTripPatterns =
				dbConfig.getTripPatternsForRouteAndHeadSign(aRoute.getId(), headSign);
		if (dbTripPatterns == null)
			return null;

		List<IpcTripPattern> tripPatterns = new ArrayList<IpcTripPattern>();
		if(directionId != null){
			for (TripPattern dbTripPattern : dbTripPatterns) {
				if(dbTripPattern.getDirectionId().equals(directionId)){
					tripPatterns.add(new IpcTripPattern(dbTripPattern));
				}
			}
		} else {
			for (TripPattern dbTripPattern : dbTripPatterns) {
				tripPatterns.add(new IpcTripPattern(dbTripPattern));
			}
		}
		return tripPatterns;
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.ConfigInterface#getAgencies()
	 */
	@Override
	public List<AgencyInterface> getAgencies() throws RemoteException {
		return Core.getInstance().getDbConfig().getAgencies();
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.ConfigInterface#getSchedules(java.lang.String)
	 */
	@Override
	public List<IpcSchedule> getSchedules(String routeIdOrShortName)
			throws RemoteException {
		// Determine the route
		RouteInterface aRoute = getRoute(routeIdOrShortName);
		if (aRoute == null)
			return null;

		// Determine the blocks for the route for all service IDs
		List<BlockInterface> blocksForRoute = Core.getInstance().getBackingStore()
				.getBlocksForRoute(aRoute.getId());
		
		// Convert blocks to list of IpcSchedule objects and return
		List<IpcSchedule> ipcSchedules = 
				IpcSchedule.createSchedules(aRoute, blocksForRoute);
		return ipcSchedules;
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.ConfigInterface#getSchedulesForTrip(java.lang.String)
	 */
	@Override
	public List<IpcSchedule> getSchedulesForTrip(String tripId)
			throws RemoteException {
		// Determine the trip
		Trip trip = Core.getInstance().getDbConfig()
				.getTrip(tripId);
		if (trip == null)
			return null;

		// Convert trip to list of IpcSchedule objects and return
		List<IpcSchedule> ipcSchedules =
				IpcSchedule.createScheduleForTrip(trip);
		return ipcSchedules;
	}
	
	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.ConfigInterface#getCurrentCalendars()
	 */
	@Override
	public List<IpcCalendar> getCurrentCalendars() {
		// Get list of currently active calendars
		List<CalendarInterface> calendarList =
				Core.getInstance().getDbConfig().getCurrentCalendars();

		// Convert Calendar list to IpcCalendar list
		List<IpcCalendar> ipcCalendarList = new ArrayList<IpcCalendar>();
		for (CalendarInterface calendar : calendarList) {
			ipcCalendarList.add(new IpcCalendar(calendar));
		}

		return ipcCalendarList;
	}
	
	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.ConfigInterface#getAllCalendars()
	 */
	@Override
	public List<IpcCalendar> getAllCalendars() {		
		// Get list of currently active calendars
		List<CalendarInterface> calendarList =
				Core.getInstance().getBackingStore().getCalendars();

		// Convert Calendar list to IpcCalendar list
		List<IpcCalendar> ipcCalendarList = new ArrayList<IpcCalendar>();
		for (CalendarInterface calendar : calendarList) {
			ipcCalendarList.add(new IpcCalendar(calendar));
		}

		return ipcCalendarList;
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.ConfigInterface#getVehicleIds()
	 */
	@Override
	public List<String> getVehicleIds() throws RemoteException {
		Collection<VehicleConfig> vehicleConfigs = VehicleDataCache.getInstance().getVehicleConfigs();
		List<String> vehicleIds = new ArrayList<String>(vehicleConfigs.size());
		for (VehicleConfig vehicleConfig : vehicleConfigs)
			vehicleIds.add(vehicleConfig.getId());
		return vehicleIds;
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.ConfigInterface#getServiceIds()
	 */
	@Override
	public List<String> getServiceIds() throws RemoteException {
		// Convert the Set from getServiceIds() to a List since need
		// to use a List for IPC due to serialization.
		return new ArrayList<String>(Core.getInstance().getDbConfig()
				.getServiceIds());
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.ConfigInterface#getCurrentServiceIds()
	 */
	@Override
	public List<String> getCurrentServiceIds() throws RemoteException {
		// Convert the Set from getCurrentServiceIds() to a List since need
		// to use a List for IPC due to serialization.
		return new ArrayList<String>(Core.getInstance().getDbConfig()
				.getCurrentServiceIds());
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.ConfigInterface#getTripIds()
	 */
	@Override
	public List<String> getTripIds() throws RemoteException {
		Collection<Trip> trips =
				Core.getInstance().getDbConfig().getTrips().values();
		List<String> tripIds = new ArrayList<String>(trips.size());
		for (Trip trip : trips)
			tripIds.add(trip.getId());
		return tripIds;
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.ConfigInterface#getBlockIds()
	 */
	@Override
	public List<String> getBlockIds() throws RemoteException {
		Collection<BlockInterface> blocks = Core.getInstance().getBackingStore().getBlocks();
		Collection<String> blockIds = new HashSet<String>(blocks.size());
		for (BlockInterface block : blocks)
			blockIds.add(block.getId());
		return new ArrayList<String>(blockIds);
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.ConfigInterface#getBlockIds()
	 */
	@Override
	public List<String> getBlockIds(String serviceId)
			throws RemoteException {
		// If serviceId not specified (is null) then return all block IDs
		if (serviceId == null)
			return getBlockIds();
		
		Collection<BlockInterface> blocks =
				Core.getInstance().getBackingStore().getBlocks(serviceId);
		List<String> blockIds = new ArrayList<String>(blocks.size());
		for (BlockInterface block : blocks)
			blockIds.add(block.getId());
		return blockIds;
	}

	public List<String> getServiceIdsForDay(Long day) {
		return Core.getInstance().getServiceUtils().getServiceIdsForDay(day);
	}

	@Override
	public boolean getServiceIdSuffix() throws RemoteException {
		return Core.getInstance().getDbConfig().getServiceIdSuffix();
	}
}
