package org.shippin.services;

import org.shippin.database.dao.PriceListDAO;
import org.shippin.database.dao.ShipmentDAO;
import org.shippin.database.dao.WarehouseDAO;
import org.shippin.domain.*;
import org.shippin.domain.enums.State;
import org.shippin.util.Range;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ShipmentService {

    public ShipmentService() {
    }

    public Shipment createShipment(String name, Date deliveryDate, int destPostalCode,
                                   float fuelSurchargeCoefficient, float toll, int weight, int volume,
                                   int warehouseId, List<Integer> serviceIds) throws SQLException {



        WarehouseDAO warehouseDAO = WarehouseDAO.getInstance();
        ShipmentDAO shipmentDAO = ShipmentDAO.getInstance();

        Warehouse warehouse = warehouseDAO.getById(warehouseId);

        List<AdditionalService> allServices = shipmentDAO.getSAllServices();
        List<AdditionalService> selected = allServices.stream().filter(s -> serviceIds.contains(s.getId())).toList();

        float baseCost;
        if (weight > 30) {
            String regionCode = findRegionForPostalCode(
                    warehouse.getRegionTable(), destPostalCode);

            float costByWeight = findCostInPriceList(
                    warehouse.getPriceList(), regionCode, weight, true);
            float costByVolume = findCostInPriceList(
                    warehouse.getPriceList(), regionCode, volume, false);
            baseCost = Math.max(costByWeight, costByVolume);
        } else {
            PriceListDAO priceListDAO = new PriceListDAO(connection);
            SmallPriceList smallPriceList = priceListDAO.getSmallPriceList();
            baseCost = findCostInSmallPriceList(smallPriceList, weight);
        }

        Shipment shipment = new Shipment();
        shipment.setServices(new ArrayList<>(selected));
        shipment.setWarehouse(new BriefWarehouse(
                warehouse.getId(), warehouse.getName(), warehouse.getRegionName()));
        shipment.setCreated_at(deliveryDate);
        shipment.setDest_region(destPostalCode);
        shipment.setState(State.NOT_READY);

        shipment.estimateCost(volume, weight, fuelSurchargeCoefficient, toll, baseCost);

        return shipment;
    }

    private String findRegionForPostalCode(RegionTable regionTable, int postalCode) {
        for (RegionTableEntry entry : regionTable.getEntries()) {
            for (Range range : entry.getRanges()) {
                if (range.contains(postalCode)) {
                    return entry.getRegionCode();
                }
            }
        }
        throw new IllegalArgumentException("No region found for postal code: " + postalCode);
    }

    private float findCostInSmallPriceList(SmallPriceList smallPriceList, float weight) {
        float bestCost = -1;
        float bestThreshold = Float.MAX_VALUE;

        for (SmallPriceListEntry entry : smallPriceList.getEntries()) {
            if (entry.getWeight() >= weight && entry.getWeight() < bestThreshold) {
                bestThreshold = entry.getWeight();
                bestCost = entry.getCost();
            }
        }

        if (bestCost < 0) {
            throw new IllegalArgumentException("No small price list entry found for weight: " + weight);
        }

        return bestCost;
    }

    private float findCostInPriceList(PriceList priceList, String regionCode,
                                      float value, boolean byWeight) {
        float bestCost = -1;
        float bestThreshold = Float.MAX_VALUE;

        for (PriceListEntry entry : priceList.getEntries()) {
            if (!entry.getZone().equals(regionCode)) continue;

            float threshold = byWeight ? entry.getWeight() : entry.getVolume();
            if (threshold >= value && threshold < bestThreshold) {
                bestThreshold = threshold;
                bestCost = entry.getCost();
            }
        }

        if (bestCost < 0) {
            throw new IllegalArgumentException(
                    "No price found for region " + regionCode
                            + (byWeight ? " weight " : " volume ") + value);
        }

        return bestCost;
    }
}
