package org.shippin.domain.formatted;

import org.shippin.util.Range;
import org.shippin.domain.Row;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionTableRow implements Row {
    private String regionCode;
    private List<Range> ranges = new ArrayList<>();

    public RegionTableRow(String regionCode) {
        this.regionCode = regionCode;
    }

    public void addRange(Range range) {
        if (range != null) {
            ranges.add(range);
        }
    }
}