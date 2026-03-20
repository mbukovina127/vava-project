package org.shippin.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class CoreWarehouseInfo {
    private int id;
    private String name;
    private String regionName;
    private PriceList priceList;
    private RegionTable regionTable;
}
