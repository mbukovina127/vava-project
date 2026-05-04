package org.shippin.infrastructure.csv;

import org.shippin.domain.Table;
import org.shippin.exception.ValidationException;
import org.shippin.domain.Row;

public interface CsvParser<R extends Row> {
    Table<R> parseFromCsv(String text) throws ValidationException;
    String exportToCsv(Table<R> table);
}