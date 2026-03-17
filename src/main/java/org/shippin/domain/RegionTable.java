package org.shippin.domain;

import java.util.List;

public class RegionTable extends EntityTable<RegionTableEntry>{
    @Override
    public List<RegionTableEntry> getEntries() {
        return List.of();
    }

    @Override
    public boolean setEntries(List<RegionTableEntry> entries) {
        return false;
    }
}
