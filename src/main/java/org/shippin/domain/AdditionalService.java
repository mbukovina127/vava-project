package org.shippin.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.shippin.domain.enums.ServiceType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdditionalService {
    private int id;
    private String name;
    private float defaultCost;
    private float costModifier;
    private ServiceType serviceType;
}
