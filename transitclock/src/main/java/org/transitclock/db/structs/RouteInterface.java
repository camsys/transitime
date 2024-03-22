package org.transitclock.db.structs;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface RouteInterface {

  String getId();
  int getConfigRev();
  String getName();
  String getShortName();
  String getLongName();
  String getColor();
  String getTextColor();
  Integer getRouteOrder();
  boolean isHidden();
  String getType();
  String getDescription();
  Extent getExtent();
  double getMaxAllowableDistanceFromSegment();


  boolean atBeginning();
  boolean atEnd();
  Collection<Stop> getStops();
  List<TripPattern> getTripPatterns(String directionId);
  List<TripPattern> getTripPatterns();
  List<String> getDirectionIds();
  Collection<Vector> getPathSegments();
  Map<String, List<String>> getUnorderedUniqueStopsByDirection();
  Map<String, List<String>> getOrderedStopsByDirection();
  int getStopOrder(String directionId, String stopId, int stopIndex);
}
