package org.transitclock.db.structs;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface BlockInterface {

  String getId();

  int getConfigRev();

  String getServiceId();

  int getStartTime();

  int getEndTime();

  List<Trip> getTrips();

  boolean isNoSchedule();

  boolean hasSchedule();

  Trip getTrip(int tripIndex);

  Trip getTrip(String tripId);

  int getTripIndex(TripInterface trip);

  Set<String> getRouteIds();

  Collection<TripPattern> getTripPatterns();

  int getStopPathTravelTime(int tripIndex, int stopPathIndex);

  int getPathStopTime(int tripIndex, int stopPathIndex);

  Vector getSegmentVector(int tripIndex, int stopPathIndex,
                          int segmentIndex);

  int numSegments(int tripIndex, int stopPathIndex);

  int numStopPaths(int tripIndex);

  int numTrips();

  boolean isLayover(int tripIndex, int stopPathIndex);

  boolean isWaitStop(int tripIndex, int stopPathIndex);

  StopPath getStopPath(int tripIndex, int stopPathIndex);

  Location getStartLoc();

  StopPath getPreviousPath(int tripIndex, int stopPathIndex);

  ScheduleTime getScheduleTime(int tripIndex, int stopPathIndex);
  boolean isActive(Date date);
  boolean isActive(long epochTime, int allowableBeforeTimeSecs);
  boolean isActive(Date date, int allowableBeforeTimeSecs,
                   int allowableAfterStartTimeSecs);
  boolean isActive(long epochTime, int allowableBeforeTimeSecs,
                   int allowableAfterStartTimeSecs);
  boolean isBeforeStartTime(Date date, int allowableBeforeTimeSecs);
  boolean shouldBeExclusive();
  int activeTripIndex(int secondsIntoDay);
  int activeTripIndex(Date date, int allowableBeforeTimeSecs);
  boolean isInitialized();
  void setInitialized();
  String toShortString();

}
