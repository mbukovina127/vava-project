package org.shippin.domain;

import java.util.ArrayList;
import java.util.List;

public class RegionTable implements EntityTable<RegionTableEntry>{

    private List<RegionTableEntry> entries = new ArrayList<>();

    @Override
    public List<RegionTableEntry> getEntries() {
        return entries;
    }

    @Override
    public boolean setEntries(List<RegionTableEntry> entries) {
        this.entries = entries;
        return true;
    }
}