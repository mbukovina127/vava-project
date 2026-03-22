package org.shippin.domain.formatted;

public class WarehouseFormatted {
    private String title;
    private String name;
    private PriceListFormatted priceList;
    private RegionTableFormatted regionTable;

    public WarehouseFormatted() {}

    public WarehouseFormatted(String title, String name) {
        this.title = title;
        this.name = name;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public PriceListFormatted getPriceList() { return priceList; }
    public void setPriceList(PriceListFormatted priceList) { this.priceList = priceList; }
    public RegionTableFormatted getRegionTable() { return regionTable; }
    public void setRegionTable(RegionTableFormatted regionTable) { this.regionTable = regionTable; }
}