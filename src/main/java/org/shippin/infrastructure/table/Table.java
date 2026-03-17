package org.shippin.infrastructure.table;
import java.util.List;

public interface Table {
    List<Row> getRows();
    void setRows(List<Row> rows);
}