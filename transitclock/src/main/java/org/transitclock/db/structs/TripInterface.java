package org.transitclock.db.structs;

import java.util.List;

public interface TripInterface {

  String toLongString();
  String toShortString();
  Integer getStartTime();
  Integer getEndTime();
  int getConfigRev();
  String getId();
  String getShortName();
  String getRouteId();
  String getRouteShortName();
  Route getRoute();
  String getRouteName();
  String getDirectionId();
  boolean isNoSchedule();
  boolean isExactTimesHeadway();
  String getServiceId();
  String getHeadsign();
  String getBlockId();
  Block getBlock();
  int getIndexInBlock();
  String getShapeId();
  TripPattern getTripPattern();
  ScheduleTime getScheduleTime(int stopPathIndex);
  List<ScheduleTime> getScheduleTimes();
  TravelTimesForTrip getTravelTimes();
  TravelTimesForStopPath getTravelTimesForStopPath(int stopPathIndex);
  double getLength();
  String getLastStopId();
  List<StopPath> getStopPaths();
  StopPath getStopPath(int stopPathIndex);
  StopPath getStopPath(String stopId);
  int getNumberStopPaths();
  String getTripPatternId();
  Integer getBoardingType();
}
