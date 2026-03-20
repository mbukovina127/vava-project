package org.shippin.domain;

import java.util.List;

public class SmallPriceList implements EntityTable<SmallPriceListEntry>{
    @Override
    public List<SmallPriceListEntry> getEntries() {
        return List.of();
    }

    @Override
    public boolean setEntries(List<SmallPriceListEntry> entries) {
        return false;
    }
}
