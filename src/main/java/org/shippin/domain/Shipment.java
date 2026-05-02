package org.shippin.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Shipment extends CoreShipmentInfo{
    private ArrayList<AdditionalService> services;
    private BriefWarehouse warehouse;
}
