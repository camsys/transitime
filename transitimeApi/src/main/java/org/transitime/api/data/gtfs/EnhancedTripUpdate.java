package org.transitime.api.data.gtfs;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class EnhancedTripUpdate {
    @XmlElement(name = "trip")
    private EnhancedTrip _trip;
    @XmlElement(name = "vehicle")
    private EnhancedVehicle _vehicle;
    @XmlElement(name = "stopTimeUpdates")
    private List<EnhancedStopTimeUpdate> _stopTimeUpdates = new ArrayList<>();

    public EnhancedTrip getTrip() { return _trip; }
    public void setTrip(EnhancedTrip trip) { _trip = trip; }

    public void setVehicle(EnhancedVehicle vehicle) {
        _vehicle = vehicle;
    }

    public List<EnhancedStopTimeUpdate> getStopTimeUpdates() {
        return _stopTimeUpdates;
    }

}
