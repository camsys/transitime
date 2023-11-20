package org.transitclock.core.barefoot;

import java.util.HashSet;

import com.bmwcarit.barefoot.road.BaseRoad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.core.Indices;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.Trip;
import org.transitclock.db.structs.VectorWithHeading;

import com.bmwcarit.barefoot.road.RoadReader;
import com.bmwcarit.barefoot.spatial.Geography;
import com.bmwcarit.barefoot.spatial.SpatialOperator;
import com.bmwcarit.barefoot.util.SourceException;
import com.esri.core.geometry.Line;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;

/**
 * Translates a GTFS shape into a Barefoot IndiciesRoad, a BaseRoad subclass
 */
public class TransitClockRoadReader implements RoadReader {

    // while this could be configurable, we set it to a logical maximum
    private static float MAX_SPEED = 60F; // or 134 mph
    private static short ROAD_TYPE = 1; // we don't distinguish road types, they are all shapes to us
    private static float PRIORITY = 1F; // we don't distinquish priority

    private final Indices indices;

    private long segmentCounter = 0;

    private static final Logger logger =
            LoggerFactory.getLogger(TransitClockRoadReader.class);

    private static SpatialOperator spatialOperator = new Geography();

    public TransitClockRoadReader(Trip trip) {
        Block block = trip.getBlock();
        int tripIndex = block.getTripIndex(trip);;
        indices = new Indices(block, tripIndex, 0, 0);
    }

    @Override
    public BaseRoad next() throws SourceException {

        if (indices.atEndOfTrip())
            return null;

        Polyline polyLine = new Polyline();

        Point startPoint = null;
        Point endPoint = null;
        Line polyLineSegment = new Line();

        indices.increment(Core.getInstance().getSystemTime());

        VectorWithHeading segment = indices.getSegment();

        startPoint = new Point();
        endPoint = new Point();
        startPoint.setXY(segment.getL1().getLon(), segment.getL1().getLat());
        endPoint.setXY(segment.getL2().getLon(), segment.getL2().getLat());
        polyLineSegment.setStart(startPoint);
        polyLineSegment.setEnd(endPoint);

        polyLine.addSegment(polyLineSegment, false);

        ReferenceId refId = new ReferenceId(indices.getStopPathIndex(), indices.getSegmentIndex());

        return new BaseRoad(indices.hashCode(), segmentCounter, segmentCounter++, refId.getRefId(), true,
                ROAD_TYPE, PRIORITY, MAX_SPEED, MAX_SPEED, (float)spatialOperator.length(polyLine),
                polyLine);
    }

    @Override
    public boolean isOpen() {
        // force the process to start over so we don't maintain the internal state
        return false;
    }

    @Override
    public void open() throws SourceException {
        // this is a no-op as we have the block in internal state
    }

    @Override
    public void open(Polygon polygon, HashSet<Short> exclusion) throws SourceException {
        // this is a no-op with the note that polygon/exclusions are not supported
    }

    @Override
    public void close() throws SourceException {
        // a no-op, nothing to cleanup
    }

}