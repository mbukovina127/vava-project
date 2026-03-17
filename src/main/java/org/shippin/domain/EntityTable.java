package org.shippin.domain;

import java.util.List;

public interface EntityTable<T extends EntityEntry> {
    public List<T> getEntries();
    public boolean setEntries(List<T> entries);
}
