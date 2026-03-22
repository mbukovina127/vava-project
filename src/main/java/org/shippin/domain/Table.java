package org.shippin.domain;

import java.util.List;

public interface Table {
    List<Row> getRows();
    void setRows(List<Row> rows);
}