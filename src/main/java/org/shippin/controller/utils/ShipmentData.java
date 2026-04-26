package org.shippin.controller.utils;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ShipmentData
{
    private CostEstimationInput data;
    private String estimationName;
    private Integer shipmentID;

    public ShipmentData() {}

    public ShipmentData(CostEstimationInput data, String estimationName, Integer shipmentID)
    {
        this.data           = data;
        this.estimationName = estimationName;
        this.shipmentID     = shipmentID;
    }

}