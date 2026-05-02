package org.shippin.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.shippin.domain.enums.State;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class CoreShipmentInfo{
    private int shipment_id;
    private int user_ID;
    // private String name;
    private Date created_at;
    // private String sourcePostalCode;
    private int dest_region;
    private Coordinates startCoordinate;
    private float fuel_payment;
    private float totalCost;
    private State state;
}
