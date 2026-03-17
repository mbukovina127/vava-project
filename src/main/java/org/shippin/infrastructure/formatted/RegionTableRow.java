package org.shippin.infrastructure.formatted;

import org.shippin.infrastructure.table.Row;
import java.util.ArrayList;

public class RegionTableRow implements Row {
    private String regionCode;
    private ArrayList<Range> ranges = new ArrayList<>();

    public RegionTableRow() {}

    public RegionTableRow(String regionCode) {
        this.regionCode = regionCode;
    }

    public String getRegionCode() { return regionCode; }
    public void setRegionCode(String regionCode) { this.regionCode = regionCode; }
    public ArrayList<Range> getRanges() { return ranges; }
    public void setRanges(ArrayList<Range> ranges) { this.ranges = ranges; }
}