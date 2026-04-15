package org.shippin.domain;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
public class Warehouse extends CoreWarehouseInfo{
    private PriceList priceList;
    private RegionTable regionTable;
}
