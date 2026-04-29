package org.shippin.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmallPriceListEntry implements EntityEntry {
    private int id;
    private float weight;
    private float cost;
}
