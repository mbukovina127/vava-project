package org.shippin.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PriceList implements EntityTable<PriceListEntry> {

    private List<PriceListEntry> entries = new ArrayList<>();

    public List<String> getRegions() {
        return entries.stream()
            .map(PriceListEntry::getZone)
            .distinct()
            .collect(Collectors.toList());
    }
    
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