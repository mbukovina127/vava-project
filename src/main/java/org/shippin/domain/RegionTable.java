package org.shippin.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RegionTable implements EntityTable<RegionTableEntry>{

    private List<RegionTableEntry> entries = new ArrayList<>();

    @Override
    public List<RegionTableEntry> getEntries() {
        return entries;
    }
    
    public List<String> getRegions() {
    	return entries.stream()
    		    .map(RegionTableEntry::getRegionCode)
    		    .collect(Collectors.toList());
    }

    @Override
    public boolean setEntries(List<RegionTableEntry> entries) {
        this.entries = entries;
        return true;
    }
}