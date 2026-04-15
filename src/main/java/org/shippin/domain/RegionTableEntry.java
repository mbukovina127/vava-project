package org.shippin.domain;

import lombok.Getter;
import lombok.Setter;
import org.shippin.util.Range;

import java.util.ArrayList;
import java.util.List;

public class RegionTableEntry implements EntityEntry {
    private int id;
    private ArrayList<Range> ranges;
    @Setter
    @Getter
    private String regionCode;

    public RegionTableEntry(int id, ArrayList<Range> ranges, String regionCode) {
        this.id = id;
        this.ranges = ranges;
        this.regionCode = regionCode;
    }

    public List<Range> getRanges() {
        return new ArrayList<>(ranges);
    }

    public void addRange(Range range){
        this.ranges.add(range);
    }

}
