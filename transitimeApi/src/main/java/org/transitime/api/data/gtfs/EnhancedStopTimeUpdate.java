package org.transitime.api.data.gtfs;

import javax.xml.bind.annotation.XmlElement;

public class EnhancedStopTimeUpdate {
    @XmlElement(name = "stop_id")
    private String _stopId = null;
    @XmlElement(name = "stop_sequence")
    private int _stopSequence = -1;

    @XmlElement(name = "arrival")
    private EnhancedStopTimeEvent _arrival;
    @XmlElement(name = "departure")
    private EnhancedStopTimeEvent _departure;

    public void setStopId(String id) {
        _stopId = id;
    }

    public void setArrival(EnhancedStopTimeEvent arrival) {
        _arrival = arrival;
    }

    public void setDeparture(EnhancedStopTimeEvent departure) {
        _departure = departure;
    }

    public void setStopSequence(int gtfsStopSeq) {
        _stopSequence = gtfsStopSeq;
    }
}
