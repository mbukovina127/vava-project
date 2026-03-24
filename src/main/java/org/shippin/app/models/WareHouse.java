package org.shippin.app.models;

import java.util.List;
import java.util.Map;

public class WareHouse {
    private int warehouseRegionName;
    private Map<String, List<PriceListItem>> priceList;

    public WareHouse(int warehouseRegionName) {
        this.warehouseRegionName = warehouseRegionName;

    }
}
