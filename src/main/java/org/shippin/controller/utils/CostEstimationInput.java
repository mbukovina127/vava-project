package org.shippin.controller.utils;

import org.shippin.domain.Shipment;

import java.util.List;

public record CostEstimationInput(
        String date,
        String from,
        int warehouseId,
        String destination,
        double weight,
        double volume,
        double fuelSurcharge,
        double toll,
        String delivery_opt,
        List<ExtraOption> options,
        Shipment computedShipment
) {}

