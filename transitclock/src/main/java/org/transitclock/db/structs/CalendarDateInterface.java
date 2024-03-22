package org.transitclock.db.structs;

import java.util.Date;

public interface CalendarDateInterface {

  int getConfigRev();
  String getServiceId();
  Date getDate();
  long getTime();
  String getExceptionType();
  boolean addService();
}
