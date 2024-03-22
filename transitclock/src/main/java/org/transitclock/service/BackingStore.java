package org.transitclock.service;

import org.transitclock.db.structs.*;
import org.transitclock.gtfs.DbConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Combine Static and Dynamic instances into interface.
 */
public class BackingStore {

  private DbConfig config;
  public BackingStore(DbConfig config) {
    this.config = config;
  }
  public List<BlockInterface> getBlocksForRoute(String id) {
    List<BlockInterface> blocks = new ArrayList<>();
    List<Block> blocksForRoute = config.getBlocksForRoute(id);
    for (Block block : blocksForRoute) {
      blocks.add(block);
    }
    return blocks;
  }

  public List<BlockInterface> getBlocksForRoute(String serviceId, String routeId) {
    List<BlockInterface> blocks = new ArrayList<>();
    List<Block> blocksForRoute = config.getBlocksForRoute(serviceId, routeId);
    for (Block block : blocksForRoute) {
      blocks.add(block);
    }
    return blocks;
  }
  public List<BlockInterface> getBlocks() {
    List<BlockInterface> blocks = new ArrayList<>();
    for (Block block : config.getBlocks()) {
      blocks.add(block);
    }
    return blocks;
  }

  public Collection<BlockInterface> getBlocks(String serviceId) {
    List<BlockInterface> blocks = new ArrayList<>();
    Collection<Block> configBlocks = config.getBlocks(serviceId);
    for (Block configBlock : configBlocks) {
      blocks.add(configBlock);
    }
    return blocks;
  }

  public Collection<BlockInterface> getBlocksForAllServiceIds(String blockId) {
    List<BlockInterface> blocks = new ArrayList<>();
    for (Block block : config.getBlocksForAllServiceIds(blockId)) {
      blocks.add(block);
    }
    return blocks;
  }

  public BlockInterface getBlock(String serviceId, String blockId) {
    return config.getBlock(serviceId, blockId);
  }

  public List<CalendarInterface> getCalendars() {
    List<CalendarInterface> calendars = new ArrayList<>();
    for (Calendar calendar : config.getCalendars()) {
      calendars.add(calendar);
    }
    return calendars;
  }

  public CalendarInterface getCalendarByServiceId(String serviceId) {
    return config.getCalendarByServiceId(serviceId);
  }

  public List<CalendarDateInterface> getCalendarDates(Date epochTime) {
    List<CalendarDateInterface> list = new ArrayList<>();
    for (CalendarDate calendarDate : config.getCalendarDates(epochTime)) {
      list.add(calendarDate);
    }
    return list;
  }

  public List<RouteInterface> getRoutes() {
    List<RouteInterface> list = new ArrayList<>();
    for (Route route : config.getRoutes()) {
      list.add(route);
    }
    return list;
  }

  public RouteInterface getRouteById(String routeId) {
    return config.getRouteById(routeId);
  }

  public RouteInterface getRouteByShortName(String routeShortName) {
    return config.getRouteByShortName(routeShortName);
  }

  public Collection<RouteInterface> getRoutesForStop(String stopId) {
    List<RouteInterface> list = new ArrayList<>();
    for (Route route : config.getRoutesForStop(stopId)) {
      list.add(route);
    }
    return list;
  }
}
