package org.transitime.api.data.gtfs;

import javax.xml.bind.annotation.XmlElement;

public class EnhancedOperator {
    @XmlElement(name = "id")
    private String _id = null;
    @XmlElement(name = "name")
    private String _name = null;

    public void setId(String id) {
        _id = id;
    }

    public void setName(String name) {
        _name = name;
    }
}
