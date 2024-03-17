package org.transitclock.db.model;

import org.transitclock.db.structs.Extent;
import org.transitclock.utils.Time;

import java.util.TimeZone;

public interface AgencyInterface {
  Time getTime();

  int getConfigRev();

  String getId();

  String getName();

  String getUrl();

  String getTimeZoneStr();

  String getLang();

  String getPhone();

  String getFareUrl();

  Extent getExtent();

  TimeZone getTimeZone();
}
