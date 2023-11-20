package org.transitclock.core;

import org.transitclock.db.structs.AvlReport;
import org.transitclock.db.structs.Trip;

/**
 * Interface abstracting the concept of spatial matching a vehicle position to a shape (bus route).
 * Default and Barefoot implementation exist.
 */
public interface MapMatcher {
    void intialize(Trip trip);
    SpatialMatch getSpatialMatch(AvlReport avlReport);
    boolean isInitialized();

    boolean isTrip(Trip trip);
}