package org.shippin.domain;

import java.util.List;

public class PriceList implements EntityTable<PriceListEntry> {

    @Override
    public List<PriceListEntry> getEntries() {
        return List.of();
    }

    @Override
    public boolean setEntries(List<PriceListEntry> entries) {
        return false;
    }
}
