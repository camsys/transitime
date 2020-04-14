package org.transitime.api.data.gtfs;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class EnhancedFeedMessage {
    @XmlElement(name = "entity")
    private List<EnhancedTripUpdate> _entities = new ArrayList<>();
    public List<EnhancedTripUpdate> getEntities() { return _entities; }
}
