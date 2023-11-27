package org.transitclock.core.barefoot;

import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.core.MapMatcher;
import org.transitclock.core.SpatialMatch;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.Location;

import com.bmwcarit.barefoot.matcher.Matcher;
import com.bmwcarit.barefoot.matcher.MatcherCandidate;
import com.bmwcarit.barefoot.matcher.MatcherKState;
import com.bmwcarit.barefoot.matcher.MatcherSample;
import com.bmwcarit.barefoot.roadmap.Road;
import com.bmwcarit.barefoot.roadmap.RoadMap;
import com.bmwcarit.barefoot.roadmap.RoadPoint;
import com.bmwcarit.barefoot.roadmap.TimePriority;
import com.bmwcarit.barefoot.spatial.Geography;
import com.bmwcarit.barefoot.spatial.SpatialOperator;
import com.bmwcarit.barefoot.topology.Dijkstra;
import com.esri.core.geometry.Point;
import org.transitclock.db.structs.Trip;
import org.transitclock.utils.Geo;

/**
 * Loads BaseRoad into BareFoot and returns the best spatial match.
 */
public class BareFootMapMatcher implements MapMatcher {

    // constructing a RoadReader is expensive so cache it
    private static Map<Trip, TransitClockRoadReader> cache = new PassiveExpiringMap<>(4 * 60 * 60 * 1000); // 4 hours
    private RoadMap barefootMap = null;

    private Matcher barefootMatcher = null;

    private MatcherKState barefootState = null;

    private Trip trip = null;
    private int tripIndex = -1;

    private static SpatialOperator spatialOperator = new Geography();
    private boolean isInitialized = false;

    private static final Logger logger = LoggerFactory.getLogger(BareFootMapMatcher.class);

    @Override
    public void intialize(Trip trip) {
        if (trip != null) {
            long start = System.currentTimeMillis();
            this.trip = trip;
            Block block = trip.getBlock();
            tripIndex = block.getTripIndex(trip);
            TransitClockRoadReader roadReader = cache.get(trip);
            if (roadReader == null) {
                roadReader = new TransitClockRoadReader(trip);
                barefootMap = RoadMap.Load(roadReader);
                logger.info("cache miss");
                cache.put(trip, roadReader);
            } else {
                logger.info("cache hit");
                // should we ensure roadReader is in a good state?
            }
            logger.debug("reader load in {}", (System.currentTimeMillis() - start));
            if (barefootMap != null){
                barefootMap.construct();
            }
            logger.debug("reader construct in {}", (System.currentTimeMillis() - start));
            barefootMatcher = new Matcher(barefootMap, new Dijkstra<Road, RoadPoint>(), new TimePriority(),
                    new Geography());
            barefootMatcher.shortenTurns(false);
            barefootState = new MatcherKState();
            isInitialized = true;
            logger.debug("init complete in {}", (System.currentTimeMillis() - start));
        } else {
            logger.debug("nothing to do");
        }
    }

    @Override
    public SpatialMatch getSpatialMatch(AvlReport avlReport) {
        if (barefootState != null) {
            Point point = new Point();
            point.setX(avlReport.getLon());
            point.setY(avlReport.getLat());
            MatcherSample sample = new MatcherSample(avlReport.getTime(), point);

            if (barefootState.vector() != null && barefootState.sample() != null){
                Set<MatcherCandidate> result = barefootMatcher.execute(barefootState.vector(), barefootState.sample(),
                        sample);
                barefootState.update(result, sample);
            }

            MatcherCandidate estimate = barefootState.estimate();

            logger.debug("Vehicle {} has {} samples.", avlReport.getVehicleId(), barefootState.samples().size());

            if (estimate != null) {

                Location location = new Location(estimate.point().geometry().getY(),
                        estimate.point().geometry().getX());

                ReferenceId refId = ReferenceId.deconstructRefId(estimate.point().edge().base().refid());

                logger.debug(
                        "Vehicle {} assigned to {} is {} metres from GPS coordinates on {}. Probability is {} and Sequence probability is {}.",
                        avlReport.getVehicleId(), avlReport.getAssignmentId(),
                        Geo.distance(location, avlReport.getLocation()), refId, estimate.filtprob(),
                        estimate.seqprob());

                return new SpatialMatch(avlReport.getTime(), trip.getBlock(), tripIndex, refId.getStopPathIndex(),
                        refId.getSegmentIndex(), 0, spatialOperator.intercept(estimate.point().edge().geometry(), point)
                        * spatialOperator.length(estimate.point().edge().geometry()), SpatialMatch.MatchType.BAREFOOT);

            }
        }
        return null;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public boolean isTrip(Trip trip) {
        return this.trip.equals(trip);
    }
}