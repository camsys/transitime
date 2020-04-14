package org.transitime.api.data.gtfs;

import javax.xml.bind.annotation.XmlElement;

public class EnhancedStopTimeEvent {
    @XmlElement(name = "time")
    private long _time;

    public void setTime(long time) {
        _time = time;
    }
}
