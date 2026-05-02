package org.shippin.services;

import org.shippin.database.dao.PriceListDAO;
import org.shippin.database.dao.ShipmentDAO;
import org.shippin.database.dao.WarehouseDAO;
import org.shippin.domain.*;
import org.shippin.domain.enums.State;
import org.shippin.util.Range;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ShipmentService {

    private final ShipmentDAO shipmentDAO = ShipmentDAO.getInstance();

    public ShipmentDAO getDao() { return shipmentDAO; }

    public List<ShipmentHistory> getShipmentHistory(int shipmentId) throws SQLException {
        return shipmentDAO.getShipmentHistoryByShipmentID(shipmentId);
    }
    public Shipment saveShipment(Shipment shipment, int userId) throws SQLException {
        shipmentDAO.insertShipment(shipment, shipment.getWarehouse().getId(), userId);
        return shipment;
    }

    public Shipment createShipment(String name, Date deliveryDate, int destPostalCode,
                                   float fuelSurchargeCoefficient, float toll, float weight, float volume,
                                   int warehouseId, List<Integer> serviceIds) throws SQLException {



        WarehouseDAO warehouseDAO = WarehouseDAO.getInstance();

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
            PriceListDAO priceListDAO = PriceListDAO.getInstance();
            SmallPriceList smallPriceList = priceListDAO.getSmallPriceList();
            baseCost = findCostInSmallPriceList(smallPriceList, weight);
        }

        Shipment shipment = new Shipment();
        shipment.setServices(new ArrayList<>(selected));
        shipment.setWarehouse(new BriefWarehouse(
                warehouse.getId(), warehouse.getName(), warehouse.getRegionName()));
        shipment.setCreated_at(new Timestamp(deliveryDate.getTime()));
        shipment.setDest_region(destPostalCode);
        shipment.setWeight(weight);
        shipment.setVolume(volume);
        shipment.setFuel_payment(fuelSurchargeCoefficient);
        shipment.setState(State.NOT_READY);

        estimateCost(shipment, fuelSurchargeCoefficient, toll, baseCost);

        return shipment;
    }

    private void estimateCost(Shipment shipment, float fuelSurchargeCoefficient, float toll, float baseCost) {
        float cost = baseCost * (fuelSurchargeCoefficient + toll + 1);

        float modifierSum = 0;
        float defaultCostSum = 0;
        if (shipment.getServices() != null) {
            for (AdditionalService s : shipment.getServices()) {
                modifierSum += s.getCostModifier();
                defaultCostSum += s.getDefaultCost();
            }
        }

        cost = cost * (modifierSum + 1);
        cost = cost + defaultCostSum;

        shipment.setTotalCost(cost);
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

    public Shipment updateShipmentState(Shipment shipment, State newState) throws SQLException {
        shipmentDAO.setAutoCommit(false);
        try {
            shipment.setState(newState);
            shipmentDAO.updateShipmentStatus(shipment.getShipment_id(), newState);

            ShipmentHistory entry = new ShipmentHistory();
            entry.setShipment_id(shipment.getShipment_id());
            entry.setState(newState);
            entry.setTimestamp(new Timestamp(System.currentTimeMillis()));
            shipmentDAO.addShipmentHistory(entry);

            shipmentDAO.commit();
            return shipment;
        } catch (SQLException e) {
            shipmentDAO.rollback();
            throw e;
        } finally {
            shipmentDAO.setAutoCommit(true);
        }
    }
}
