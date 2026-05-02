package org.shippin.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.shippin.domain.enums.State;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShipmentHistory {

    private int history_id;
    private Timestamp timestamp;
    private State state;
    private int shipment_id;
    private int user_id;
    private String userName;

}