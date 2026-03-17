package org.shippin.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Warehouse {
    private int id;
    private String name;
    private String regionName;
    private PriceList priceList;
    private RegionTable regionTable;
}
