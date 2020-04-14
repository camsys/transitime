package org.transitime.api.data.gtfs;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class EnhancedEntity {
    @XmlElement(name = "tripUpdates")
    private List<EnhancedTripUpdate> tripUpdates = new ArrayList<>();

    public List<EnhancedTripUpdate> getTripUpdates() {
        return tripUpdates;
    }

}
