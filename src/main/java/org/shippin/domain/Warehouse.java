package org.shippin.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Warehouse extends CoreWarehouseInfo{
    private PriceList priceList;
    private RegionTable regionTable;
    
    public Warehouse(String name, String regionName, PriceList priceList, RegionTable regionTable, int postalCode, Coordinates coordinates) {
    	super(-1, name, regionName, postalCode, coordinates);
    }
  
    public Warehouse(String name, String regionName, PriceList priceList, RegionTable regionTable) {
      super(-1, name, regionName, 0);
    	this.priceList = priceList;
    	this.regionTable = regionTable;
    }

    public Warehouse(String name, String regionName, int postalCode, PriceList priceList, RegionTable regionTable) {
      super(-1, name, regionName, postalCode);
    	this.priceList = priceList;
    	this.regionTable = regionTable;
    }
}
