package org.shippin.controller.utils;

import java.util.List;

public record CostEstimationInput(
        String date,
        String from,
        String destination,
        double weight,
        double volume,
        double fuelSurcharge,
        double toll,
        List<ExtraOption> options
) {}

