package org.transitclock.service;

import org.transitclock.core.SpatialMatch;
import org.transitclock.db.structs.BlockInterface;

public class BlockService {

  /**
   * Returns true if on last trip of block and within the specified distance
   * of the end of that last trip.
   *
   * @param match
   * @param distance
   * @return True if within distance of end of block
   */
  public static boolean nearEndOfBlock(BlockInterface block, SpatialMatch match, double distance) {
    // If not last trip of block then not considered near end
    // so return false.
    if (match.getTripIndex() != block.getTrips().size()-1)
      return false;

    return match.withinDistanceOfEndOfTrip(distance);
  }

}
