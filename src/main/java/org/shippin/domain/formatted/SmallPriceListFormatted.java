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
public class SmallPriceListFormatted implements Table<SmallPriceListRow> {
    private List<SmallPriceListRow> rows = new ArrayList<>();

    public void addRow(SmallPriceListRow row) {
        rows.add(row);
    }
}