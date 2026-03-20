package org.shippin.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BriefWarehouse {
    private int id;
    private String name;
    private String regionName;
//    private Coordinates coordinates;
}
