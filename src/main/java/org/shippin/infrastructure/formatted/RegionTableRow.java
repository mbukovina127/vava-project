package org.shippin.infrastructure.formatted;

import org.shippin.infrastructure.table.Row;
import java.util.ArrayList;
import java.util.List;

public class RegionTableRow implements Row {
    private String regionCode;
    private List<Range> ranges = new ArrayList<>();

    public RegionTableRow() {}

    public RegionTableRow(String regionCode) {
        this.regionCode = regionCode;
    }

    public String getRegionCode() { return regionCode; }
    public void setRegionCode(String regionCode) { this.regionCode = regionCode; }
    public List<Range> getRanges() { return ranges; }
    public void setRanges(List<Range> ranges) { this.ranges = ranges; }

    public void addRange(Range range) {
        if (range != null) {
            ranges.add(range);
        }
    }
}