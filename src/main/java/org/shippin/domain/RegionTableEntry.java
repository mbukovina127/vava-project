package org.shippin.domain;

import java.util.ArrayList;

public class RegionTableEntry implements EntityEntry {
    private int id;
    private ArrayList<Range> ranges;
    private String regionCode;
}
