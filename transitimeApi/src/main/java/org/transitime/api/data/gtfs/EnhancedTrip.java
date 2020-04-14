package org.transitime.api.data.gtfs;

import javax.xml.bind.annotation.XmlElement;

public class EnhancedTrip {

    @XmlElement(name = "trip_id")
    private String _tripId = null;

    public void setTripId(String tripId) {
        _tripId = tripId;
    }
    public String getTripId() { return _tripId; }
}
