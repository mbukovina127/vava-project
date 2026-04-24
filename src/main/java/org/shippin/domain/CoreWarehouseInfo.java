package org.shippin.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class CoreWarehouseInfo {
    private int id;
    private String name; //SK 83104 Bratislava
    private String regionName; //ZBS-BA aka filename aka excel sheet name
}
