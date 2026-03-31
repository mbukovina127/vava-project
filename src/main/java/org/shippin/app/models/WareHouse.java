package org.shippin.app.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WareHouse {
    private int id;
    private String name;
    private Map<String, List<PriceListItem>> priceList;
    private ArrayList<Region> regions;


    public WareHouse(int id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, List<PriceListItem>> getPriceList() {
        return new HashMap<>(priceList);
    }

    public void setPriceList(Map<String, List<PriceListItem>> priceList) {
        this.priceList = priceList;
    }

    public ArrayList<Region> getRegions() {
        return new ArrayList<>(regions);
    }

    public void setRegions(ArrayList<Region> regions) {
        this.regions = regions;
    }
}
