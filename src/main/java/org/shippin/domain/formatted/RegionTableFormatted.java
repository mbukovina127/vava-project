package org.shippin.domain.formatted;

import org.shippin.domain.Table;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionTableFormatted implements Table<RegionTableRow> {
    private List<RegionTableRow> rows = new ArrayList<>();

    public void addRow(RegionTableRow row) {
        rows.add(row);
    }
}