package org.shippin.domain;

import java.util.List;

public interface Table<R extends Row> {
    List<R> getRows();
    void setRows(List<R> rows);

    default void addRow(R row) {
        getRows().add(row);
    }
}