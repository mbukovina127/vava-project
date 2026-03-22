package org.shippin.domain.formatted;

import org.shippin.domain.Row;
import org.shippin.domain.Table;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceListFormatted implements Table {
    private List<Row> rows = new ArrayList<>();

    public void addRow(PriceListRow row) {
        rows.add(row);
    }
}