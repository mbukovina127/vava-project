package org.shippin.domain.formatted;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseFormatted {
    private String title;
    private String name;
    private PriceListFormatted priceList;
    private RegionTableFormatted regionTable;

    public WarehouseFormatted(String title, String name) {
        this.title = title;
        this.name = name;
    }

}