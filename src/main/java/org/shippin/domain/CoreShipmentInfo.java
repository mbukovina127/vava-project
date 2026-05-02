package org.shippin.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.shippin.domain.enums.State;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class CoreShipmentInfo{
    private int shipment_id;
    private int user_ID;
    private Timestamp created_at;
    private int dest_region;
    private Coordinates startCoordinate;
    private float weight;
    private float volume;
    private float fuel_payment;
    private float toll;
    private float totalCost;
    private State state;
}
