package org.shippin.infrastructure.csv;

import org.shippin.domain.Table;
import org.shippin.domain.formatted.PriceListFormatted;
import org.shippin.domain.Row;

public interface CsvParser<R extends Row> {
    Table<R> parseFromCsv(String text);
    String exportToCsv(Table<R> table);
}