package org.transitclock.db.structs;

public interface StopInterface {

  int getConfigRev();
  String getId();
  Integer getCode();
  String getName();
  Location getLoc();
  boolean isTimepointStop();
  Boolean isLayoverStop();
  Boolean isWaitStop();
  boolean isHidden();
}
