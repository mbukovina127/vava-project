package org.shippin.services;

import org.shippin.database.dao.PriceListDAO;
import org.shippin.database.dao.ShipmentDAO;
import org.shippin.database.dao.WarehouseDAO;
import org.shippin.domain.*;
import org.shippin.domain.enums.State;
import org.shippin.util.Range;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class ShipmentService {

    private final ShipmentDAO shipmentDAO = ShipmentDAO.getInstance();

    public ShipmentDAO getDao() { return shipmentDAO; }

    public List<ShipmentHistory> getShipmentHistory(int shipmentId) throws SQLException {
        return shipmentDAO.getShipmentHistoryByShipmentID(shipmentId);
    }
    public Shipment saveShipment(Shipment shipment, int userId) throws SQLException {
        shipmentDAO.insertShipment(shipment, shipment.getWarehouse().getId(), userId);
        updateShipmentState(shipment, shipment.getState());
        return shipment;
    }

    public Shipment createShipment(String name, Date deliveryDate, int destPostalCode,
                                   float fuelSurchargeCoefficient, float toll, float weight, float volume,
                                   int warehouseId, List<Integer> serviceIds) throws SQLException {

        WarehouseDAO warehouseDAO = WarehouseDAO.getInstance();

        Warehouse warehouse = warehouseDAO.getById(warehouseId);

        List<AdditionalService> allServices = shipmentDAO.getSAllServices();
        List<AdditionalService> selected = allServices.stream()
                .filter(s -> serviceIds.contains(s.getId())).toList();

        Shipment shipment = new Shipment();
        shipment.setServices(new ArrayList<>(selected));
        shipment.setWarehouse(new BriefWarehouse(
                warehouse.getId(), warehouse.getName(), warehouse.getRegionName()));
        shipment.setCreated_at(new Timestamp(deliveryDate.getTime()));
        shipment.setDest_region(destPostalCode);
        shipment.setWeight(weight);
        shipment.setVolume(volume);
        shipment.setToll(toll);
        shipment.setFuel_payment(fuelSurchargeCoefficient);
        shipment.setToll(toll);
        shipment.setState(State.NOT_READY);

        float baseCost = calculateBaseCost(shipment);
        System.out.println(baseCost);
        shipment.setTotalCost(calculateTotalCost(shipment, baseCost));

        return shipment;
    }

    // ── Cost calculations ─────────────────────────────────────────

    public static float calculateBaseCost(Shipment shipment) throws SQLException {
        WarehouseDAO warehouseDAO = WarehouseDAO.getInstance();
        Warehouse warehouse = warehouseDAO.getById(shipment.getWarehouse().getId());

        float weight = shipment.getWeight();
        float volume = shipment.getVolume();
        int destPostalCode = shipment.getDest_region();

        if (weight > 30) {
            String regionCode = findRegionForPostalCode(warehouse.getRegionTable(), destPostalCode);
            float costByWeight = findCostInPriceList(warehouse.getPriceList(), regionCode, weight, true);
            float costByVolume = findCostInPriceList(warehouse.getPriceList(), regionCode, volume, false);
            return Math.max(costByWeight, costByVolume);
        } else {
            PriceListDAO priceListDAO = PriceListDAO.getInstance();
            SmallPriceList smallPriceList = priceListDAO.getSmallPriceList();
            return findCostInSmallPriceList(smallPriceList, weight);
        }
    }

    // only help function for CostBreakdown (patrial sums in rows)
    public static float calculateFuelCost(Shipment shipment, float baseCost) {
        return baseCost * shipment.getFuel_payment();
    }

    // only help function for CostBreakdown (patrial sums in rows)
    public static float calculateTollCost(Shipment shipment, float baseCost) {
        return baseCost * shipment.getToll();
    }

    public static float calculateServiceCost(Shipment shipment, float baseCost) {
        List<AdditionalService> services = shipment.getServices();
        if (services == null || services.isEmpty()) return 0;
        float baseWithCoefficients = baseCost * (shipment.getFuel_payment() + shipment.getToll() + 1);
        float modifierSum = 0;
        float defaultCostSum = 0;
        for (AdditionalService s : services) {
            modifierSum += s.getCostModifier();
            defaultCostSum += s.getDefaultCost();
        }
        return baseWithCoefficients * modifierSum + defaultCostSum;
    }

    // only help function for CostBreakdown (patrial sums in rows)
    public static float calculateServiceCost(Shipment shipment,float baseCost,AdditionalService service)
    {
        float mainCost = baseCost * (shipment.getFuel_payment() + shipment.getToll() + 1);
        return mainCost * service.getCostModifier() + service.getDefaultCost();
    }

    public static float calculateTotalCost(Shipment shipment, float baseCost) {
        float baseWithCoefficients = baseCost * (shipment.getFuel_payment() + shipment.getToll() + 1);
        float modifierSum = 0;
        float defaultCostSum = 0;
        if (shipment.getServices() != null) {
            for (AdditionalService s : shipment.getServices()) {
                modifierSum += s.getCostModifier();
                defaultCostSum += s.getDefaultCost();
            }
        }
        return baseWithCoefficients * (modifierSum + 1) + defaultCostSum;
    }

    // ── Private helpers ───────────────────────────────────────────

    private static String findRegionForPostalCode(RegionTable regionTable, int postalCode) {
        for (RegionTableEntry entry : regionTable.getEntries()) {
            for (Range range : entry.getRanges()) {
                if (range.contains(postalCode)) {
                    return entry.getRegionCode();
                }
            }
        }
        throw new IllegalArgumentException("No region found for postal code: " + postalCode);
    }

    private static float findCostInSmallPriceList(SmallPriceList smallPriceList, float weight) {
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

    private static float findCostInPriceList(PriceList priceList, String regionCode,
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
            User currentUser = UserService.getUser();
            entry.setUser_id(currentUser != null ? currentUser.getId() : 0);
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

    public Map<LocalDate, Double> getDailySummaries(YearMonth ym) throws SQLException {

        ShipmentDAO shipmentDAO = ShipmentDAO.getInstance();

        Timestamp from = Timestamp.valueOf(ym.atDay(1).atStartOfDay());
        Timestamp to   = Timestamp.valueOf(ym.plusMonths(1).atDay(1).atStartOfDay());

        List<Shipment> shipments = shipmentDAO.getAllShipmentsByDate(from, to);

        Map<LocalDate, Double> result = new HashMap<>();

        for (Shipment s : shipments) {
            LocalDate date = s.getCreated_at()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime().toLocalDate();

            result.put(date,
                    result.getOrDefault(date, 0.0) + s.getTotalCost());
        }

        return result;
    }

    public List<Shipment> getShipmentsForDay(LocalDate date) throws SQLException {

        ShipmentDAO shipmentDAO = ShipmentDAO.getInstance();

        Timestamp from = Timestamp.valueOf(date.atStartOfDay());
        Timestamp to   = Timestamp.valueOf(date.plusDays(1).atStartOfDay());

        return shipmentDAO.getAllShipmentsByDate(from, to);
    }

    public List<Shipment> getShipmentsByUser(int userId) throws SQLException {
        return shipmentDAO.getShipmentByUserID(userId);
    }



}