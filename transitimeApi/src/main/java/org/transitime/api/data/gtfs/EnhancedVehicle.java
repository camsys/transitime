package org.transitime.api.data.gtfs;

import javax.xml.bind.annotation.XmlElement;

public class EnhancedVehicle {
    @XmlElement(name = "vehicle_id")
    private String _vehicleId =  null;
    @XmlElement(name = "operator")
    private EnhancedOperator _operator = null;
    public void setVehicleId(String id) {_vehicleId = id; }
    public String getVehicleId() { return _vehicleId; }
    public void setOperator(EnhancedOperator op) {
        _operator = op;
    }
    public EnhancedOperator getEnhancedOperator() {
        return _operator;
    }

}
