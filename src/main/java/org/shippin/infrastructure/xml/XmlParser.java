package org.shippin.infrastructure.xml;

import org.shippin.domain.Table;
import org.shippin.exception.ValidationException;
import org.shippin.domain.Row;

public interface XmlParser<R extends Row> {
    Table<R> parseFromXml(String text) throws ValidationException;
    String exportToXml(Table<R> table);
}
