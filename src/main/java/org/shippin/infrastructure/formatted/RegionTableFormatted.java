package org.shippin.infrastructure.formatted;

import org.shippin.infrastructure.table.Row;
import org.shippin.infrastructure.table.Table;
import java.util.ArrayList;
import java.util.List;


public class RegionTableFormatted implements Table {
    private List<Row> rows = new ArrayList<>();

    @Override
    public List<Row> getRows() {
        return rows;
    }

    @Override
    public void setRows(List<Row> rows) {
        this.rows = rows;
    }

    // Convenience method (template)
    public void addRow(RegionTableRow row) {
        rows.add(row);
    }
}