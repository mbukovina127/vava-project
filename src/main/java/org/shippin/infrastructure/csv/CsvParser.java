package org.shippin.infrastructure.csv;

import org.shippin.infrastructure.table.Table;

public interface CsvParser {
    Table parseFromCsv(String text);
    String exportToCsv(Table table);
}