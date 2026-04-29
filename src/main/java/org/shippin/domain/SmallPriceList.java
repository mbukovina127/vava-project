package org.shippin.domain;

import java.util.ArrayList;
import java.util.List;

public class SmallPriceList implements EntityTable<SmallPriceListEntry>{

    private List<SmallPriceListEntry> entries = new ArrayList<>();

    @Override
    public List<SmallPriceListEntry> getEntries() {
        return entries;
    }

    @Override
    public boolean setEntries(List<SmallPriceListEntry> entries) {
        this.entries = entries;
        return true;
    }
}
