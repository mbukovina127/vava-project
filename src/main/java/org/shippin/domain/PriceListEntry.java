package org.shippin.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceListEntry implements EntityEntry {
    private int id;
    private float weight;
    private float volume;
    private float cost;
    private String zone;


}
