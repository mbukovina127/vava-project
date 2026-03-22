package org.shippin.domain.formatted;

import org.shippin.domain.Row;
import org.shippin.domain.Table;
import java.util.ArrayList;
import java.util.List;


public class SmallPriceListFormatted implements Table {
    private List<Row> rows = new ArrayList<>();

    @Override
    public List<Row> getRows() {
        return rows;
    }

    @Override
    public void setRows(List<Row> rows) {
        this.rows = rows;
    }

    // Convenience method (template – you can expand)
    public void addRow(SmallPriceListRow row) {
        rows.add(row);
    }
}