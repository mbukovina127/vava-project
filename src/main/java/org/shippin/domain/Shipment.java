package org.shippin.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter

public class Shipment implements CoreShipmentInfo{
    private ArrayList<AdditionalService> services;
    private BriefWarehouse warehouse;

    public float estimateCost() {
        return 0;
    }
}
