package org.shippin.domain;

import java.util.ArrayList;
import java.util.List;

public class RegionTableEntry implements EntityEntry {
    private int id;
    private ArrayList<Range> ranges;

    public String getRegionCode() {
        return regionCode;
    }

    private String regionCode;

    public RegionTableEntry(int id, ArrayList<Range> ranges, String regionCode) {
        this.id = id;
        this.ranges = ranges;
        this.regionCode = regionCode;
    }

    public List<Range> getRanges() {
        return new ArrayList<>(ranges);
    }

    public void setRanges(ArrayList<Range> ranges) {
        this.ranges = ranges;
    }

    public void addRange(Range range){
        this.ranges.add(range);
    }
}
