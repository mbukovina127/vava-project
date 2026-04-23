package org.shippin.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Shipment extends CoreShipmentInfo{
    private ArrayList<AdditionalService> services;
    private BriefWarehouse warehouse;

    public void estimateCost(int volume, int weight, float fuelSurchargeCoefficient, float toll, float baseCost) {
        float cost = baseCost * (fuelSurchargeCoefficient + toll + 1);

        float modifierSum = 0;
        float defaultCostSum = 0;
        if (services != null) {
            for (AdditionalService s : services) {
                modifierSum += s.getCostModifier();
                defaultCostSum += s.getDefaultCost();
            }
        }

        cost = cost * (modifierSum + 1);
        cost = cost + defaultCostSum;

        this.setTotalCost(cost);
    }
}
