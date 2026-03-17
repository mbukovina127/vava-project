package org.shippin.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.shippin.domain.enums.State;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BriefShippment implements CoreShipmentInfo {
    private int id;
    private String name;
    private Date deliveryDate; // TODO by malo mat iny nazov
//    private String sourcePostalCode;
    private String destinationPostalCode;
    private Coordinates startCoordinate;
    private float fuelCost;
    private float totalCost;
    private State state;
}
