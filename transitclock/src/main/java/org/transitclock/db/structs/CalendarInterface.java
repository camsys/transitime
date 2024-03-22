package org.transitclock.db.structs;

import java.util.Date;

public interface CalendarInterface {
  int getConfigRev();
  String getServiceId();
  boolean isOnWeekDay();
  boolean getMonday();
  boolean getTuesday();
  boolean getWednesday();
  boolean getThursday();
  boolean getFriday();
  boolean getSaturday();
  boolean getSunday();
  Date getStartDate();
  String getStartDateStr();
  Date getEndDate();
  String getEndDateStr();
}
