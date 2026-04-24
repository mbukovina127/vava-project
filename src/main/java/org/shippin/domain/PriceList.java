package org.shippin.domain;

import java.util.ArrayList;
import java.util.List;

public class PriceList implements EntityTable<PriceListEntry> {

    private List<PriceListEntry> entries = new ArrayList<>();

    @Override
    public List<PriceListEntry> getEntries() {
        return entries;
    }

    @Override
    public boolean setEntries(List<PriceListEntry> entries) {
        this.entries = entries;
        return true;
    }
}