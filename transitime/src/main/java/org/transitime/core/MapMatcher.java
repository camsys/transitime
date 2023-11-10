package org.transitime.core;

import java.util.Date;

import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.Block;
import org.transitime.db.structs.Location;

public interface MapMatcher {
    void setMatcher(Block block,  Date assignmentTime);
    SpatialMatch getSpatialMatch(AvlReport avlReport);
}