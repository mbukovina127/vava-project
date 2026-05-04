package ServiceTest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shippin.database.DBConnector;
import org.shippin.domain.AdditionalService;
import org.shippin.domain.BriefWarehouse;
import org.shippin.domain.PriceList;
import org.shippin.domain.PriceListEntry;
import org.shippin.domain.RegionTable;
import org.shippin.domain.RegionTableEntry;
import org.shippin.domain.Shipment;
import org.shippin.domain.ShipmentHistory;
import org.shippin.domain.SmallPriceList;
import org.shippin.domain.SmallPriceListEntry;
import org.shippin.domain.User;
import org.shippin.domain.enums.Role;
import org.shippin.domain.enums.ServiceType;
import org.shippin.domain.enums.State;
import org.shippin.services.ShipmentService;
import org.shippin.services.UserService;
import org.shippin.util.Range;

public class ShipmentServiceTest {

    private final ShipmentService shipmentService = new ShipmentService();

    @BeforeEach
    void beginTransaction() throws SQLException {
        DBConnector.getInstance().getConnection().setAutoCommit(false);
    }

    @AfterEach
    void rollbackTransaction() throws SQLException {
        Connection connection = DBConnector.getInstance().getConnection();

        if (!connection.getAutoCommit()) {
            connection.rollback();
        }

        connection.setAutoCommit(true);
    }

    @Test
    void getDaoReturnsNonNullDaoInstance() {
        assertNotNull(shipmentService.getDao());
    }

    @Test
    void findRegionForPostalCodeReturnsMatchingRegion() throws Exception {
        RegionTable regionTable = createRegionTable();

        String region = invokeFindRegionForPostalCode(regionTable, 82101);

        assertEquals("BA", region);
    }

    @Test
    void findRegionForPostalCodeReturnsSecondMatchingRegion() throws Exception {
        RegionTable regionTable = createRegionTable();

        String region = invokeFindRegionForPostalCode(regionTable, 94901);

        assertEquals("NR", region);
    }

    @Test
    void findRegionForPostalCodeThrowsWhenNoRegionMatches() {
        RegionTable regionTable = createRegionTable();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> invokeFindRegionForPostalCode(regionTable, 11111)
        );

        assertTrue(exception.getMessage().contains("No region found"));
    }

    @Test
    void findCostInPriceListByWeightReturnsSmallestMatchingThreshold() throws Exception {
        PriceList priceList = createPriceList();

        float cost = invokeFindCostInPriceList(priceList, "BA", 12f, true);

        assertEquals(7f, cost, 0.001f);
    }

    @Test
    void findCostInPriceListByVolumeReturnsSmallestMatchingThreshold() throws Exception {
        PriceList priceList = createPriceList();

        float cost = invokeFindCostInPriceList(priceList, "BA", 150f, false);

        assertEquals(7f, cost, 0.001f);
    }

    @Test
    void findCostInPriceListIgnoresDifferentRegion() throws Exception {
        PriceList priceList = createPriceList();

        float cost = invokeFindCostInPriceList(priceList, "NR", 12f, true);

        assertEquals(9f, cost, 0.001f);
    }

    @Test
    void findCostInPriceListReturnsExactWeightThreshold() throws Exception {
        PriceList priceList = createPriceList();

        float cost = invokeFindCostInPriceList(priceList, "BA", 10f, true);

        assertEquals(5f, cost, 0.001f);
    }

    @Test
    void findCostInPriceListReturnsExactVolumeThreshold() throws Exception {
        PriceList priceList = createPriceList();

        float cost = invokeFindCostInPriceList(priceList, "BA", 100f, false);

        assertEquals(5f, cost, 0.001f);
    }

    @Test
    void findCostInPriceListThrowsWhenNoPriceMatches() {
        PriceList priceList = createPriceList();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> invokeFindCostInPriceList(priceList, "BA", 1000f, true)
        );

        assertTrue(exception.getMessage().contains("No price found"));
    }

    @Test
    void findCostInPriceListThrowsWhenRegionDoesNotExist() {
        PriceList priceList = createPriceList();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> invokeFindCostInPriceList(priceList, "XX", 10f, true)
        );

        assertTrue(exception.getMessage().contains("No price found"));
    }

    @Test
    void findCostInSmallPriceListReturnsSmallestMatchingThreshold() throws Exception {
        SmallPriceList smallPriceList = createSmallPriceList();

        float result = invokeFindCostInSmallPriceList(smallPriceList, 7f);

        assertEquals(4f, result, 0.001f);
    }

    @Test
    void findCostInSmallPriceListReturnsExactThreshold() throws Exception {
        SmallPriceList smallPriceList = createSmallPriceList();

        float result = invokeFindCostInSmallPriceList(smallPriceList, 5f);

        assertEquals(2.5f, result, 0.001f);
    }

    @Test
    void findCostInSmallPriceListThrowsWhenWeightIsTooHigh() {
        SmallPriceList smallPriceList = createSmallPriceList();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> invokeFindCostInSmallPriceList(smallPriceList, 50f)
        );

        assertTrue(exception.getMessage().contains("No small price list entry found"));
    }

    @Test
    void calculateFuelCostReturnsBaseCostMultipliedByFuelCoefficient() {
        Shipment shipment = new Shipment();
        shipment.setFuel_payment(0.15f);

        float result = ShipmentService.calculateFuelCost(shipment, 100f);

        assertEquals(15f, result, 0.001f);
    }

    @Test
    void calculateTollCostReturnsBaseCostMultipliedByTollCoefficient() {
        Shipment shipment = new Shipment();
        shipment.setToll(0.20f);

        float result = ShipmentService.calculateTollCost(shipment, 100f);

        assertEquals(20f, result, 0.001f);
    }

    @Test
    void calculateServiceCostReturnsZeroWhenServicesAreNull() {
        Shipment shipment = new Shipment();
        shipment.setServices(null);

        float result = ShipmentService.calculateServiceCost(shipment, 100f);

        assertEquals(0f, result, 0.001f);
    }

    @Test
    void calculateServiceCostReturnsZeroWhenServicesAreEmpty() {
        Shipment shipment = new Shipment();
        shipment.setServices(new ArrayList<>());

        float result = ShipmentService.calculateServiceCost(shipment, 100f);

        assertEquals(0f, result, 0.001f);
    }

    @Test
    void calculateServiceCostIncludesModifiersAndDefaultCosts() {
        Shipment shipment = createShipmentWithCoefficientsAndServices();

        float result = ShipmentService.calculateServiceCost(shipment, 100f);

        assertEquals(47f, result, 0.001f);
    }

    @Test
    void calculateSingleServiceCostIncludesModifierAndDefaultCost() {
        Shipment shipment = new Shipment();
        shipment.setFuel_payment(0.10f);
        shipment.setToll(0.20f);

        AdditionalService service = new AdditionalService(
                1,
                "Insurance",
                5f,
                0.10f,
                ServiceType.SERVICES,
                "Insurance service"
        );

        float result = ShipmentService.calculateServiceCost(shipment, 100f, service);

        assertEquals(18f, result, 0.001f);
    }

    @Test
    void calculateTotalCostWithoutServicesReturnsBaseWithCoefficients() {
        Shipment shipment = new Shipment();
        shipment.setFuel_payment(0.10f);
        shipment.setToll(0.20f);
        shipment.setServices(null);

        float result = ShipmentService.calculateTotalCost(shipment, 100f);

        assertEquals(130f, result, 0.001f);
    }

    @Test
    void calculateTotalCostWithEmptyServicesReturnsBaseWithCoefficients() {
        Shipment shipment = new Shipment();
        shipment.setFuel_payment(0.10f);
        shipment.setToll(0.20f);
        shipment.setServices(new ArrayList<>());

        float result = ShipmentService.calculateTotalCost(shipment, 100f);

        assertEquals(130f, result, 0.001f);
    }

    @Test
    void calculateTotalCostWithServicesIncludesModifiersAndDefaultCosts() {
        Shipment shipment = createShipmentWithCoefficientsAndServices();

        float result = ShipmentService.calculateTotalCost(shipment, 100f);

        assertEquals(177f, result, 0.001f);
    }

    @Test
    void calculateBaseCostForSmallPackageUsesSmallPriceList() throws SQLException {
        int warehouseId = insertWarehouseDirectly(uniqueName("Small Base Warehouse"));
        insertSmallPriceListEntryDirectly(0.01f, 123.45f);

        Shipment shipment = new Shipment();
        shipment.setWarehouse(new BriefWarehouse(warehouseId, "Small Base Warehouse", "small_base.xlsx"));
        shipment.setWeight(0.01f);
        shipment.setVolume(1f);
        shipment.setDest_region(82101);

        float result = ShipmentService.calculateBaseCost(shipment);

        assertTrue(result > 0f);
    }

    @Test
    void calculateBaseCostForLargePackageUsesMaximumOfWeightAndVolumeCost() throws SQLException {
        String warehouseName = uniqueName("Large Base Warehouse");
        int warehouseId = insertWarehouseDirectly(warehouseName);
        insertRegionWithPostalRangeAndPriceListDirectly(warehouseId, "BA", 81000, 82999);

        Shipment shipment = new Shipment();
        shipment.setWarehouse(new BriefWarehouse(warehouseId, warehouseName, "large_base.xlsx"));
        shipment.setWeight(50f);
        shipment.setVolume(150f);
        shipment.setDest_region(82101);

        float result = ShipmentService.calculateBaseCost(shipment);

        assertEquals(30f, result, 0.001f);
    }

    @Test
    void getAllShipmentsReturnsNonNullList() {
        List<Shipment> shipments = assertDoesNotThrow(() -> shipmentService.getAllShipments());

        assertNotNull(shipments);
    }

    @Test
    void getShipmentsByUserReturnsInsertedShipment() throws SQLException {
        int userId = insertUserDirectly(uniqueEmail("shipments.by.user"));
        int warehouseId = insertWarehouseDirectly(uniqueName("Shipments By User Warehouse"));
        int shipmentId = insertShipmentDirectly(userId, warehouseId, Timestamp.valueOf("2099-01-15 10:00:00"), 88.8f);

        List<Shipment> shipments = shipmentService.getShipmentsByUser(userId);

        assertNotNull(shipments);
        assertTrue(shipments.stream().anyMatch(s -> s.getShipment_id() == shipmentId));
    }

    @Test
    void getShipmentsForDayReturnsInsertedShipment() throws SQLException {
        int userId = insertUserDirectly(uniqueEmail("shipments.for.day"));
        int warehouseId = insertWarehouseDirectly(uniqueName("Shipments For Day Warehouse"));
        int shipmentId = insertShipmentDirectly(userId, warehouseId, Timestamp.valueOf("2099-01-16 10:00:00"), 77.7f);

        List<Shipment> shipments = shipmentService.getShipmentsForDay(LocalDate.of(2099, 1, 16));

        assertNotNull(shipments);
        assertTrue(shipments.stream().anyMatch(s -> s.getShipment_id() == shipmentId));
    }

    @Test
    void getDailySummariesAggregatesInsertedShipmentCostByDate() throws SQLException {
        int userId = insertUserDirectly(uniqueEmail("daily.summary"));
        int warehouseId = insertWarehouseDirectly(uniqueName("Daily Summary Warehouse"));
        insertShipmentDirectly(userId, warehouseId, Timestamp.valueOf("2099-01-17 10:00:00"), 77.5f);

        Map<LocalDate, Double> result = shipmentService.getDailySummaries(YearMonth.of(2099, 1));

        assertNotNull(result);
        assertTrue(result.containsKey(LocalDate.of(2099, 1, 17)));
        assertTrue(result.get(LocalDate.of(2099, 1, 17)) >= 77.5d);
    }

    @Test
    void getShipmentHistoryReturnsInsertedHistory() throws SQLException {
        int userId = insertUserDirectly(uniqueEmail("shipment.history"));
        int warehouseId = insertWarehouseDirectly(uniqueName("Shipment History Warehouse"));
        int shipmentId = insertShipmentDirectly(userId, warehouseId, Timestamp.valueOf("2099-01-18 10:00:00"), 99.9f);
        int historyId = insertHistoryDirectly(userId, shipmentId, State.READY_FOR_DELIVERY);

        List<ShipmentHistory> history = shipmentService.getShipmentHistory(shipmentId);

        assertNotNull(history);
        assertTrue(history.stream().anyMatch(h -> h.getHistory_id() == historyId));
    }

    @Test
    void saveShipmentInsertsShipmentAndUpdatesState() throws SQLException {
        int userId = -1;
        int warehouseId = -1;
        int shipmentId = -1;

        try {
            userId = insertUserDirectly(uniqueEmail("save.shipment"));
            warehouseId = insertWarehouseDirectly(uniqueName("Save Shipment Warehouse"));

            Shipment shipment = new Shipment();
            shipment.setWarehouse(new BriefWarehouse(warehouseId, "Save Shipment Warehouse", "save_shipment.xlsx"));
            shipment.setDest_region(82101);
            shipment.setWeight(10f);
            shipment.setVolume(1f);
            shipment.setFuel_payment(0.1f);
            shipment.setToll(0.2f);
            shipment.setTotalCost(150f);
            shipment.setCreated_at(new Timestamp(System.currentTimeMillis()));
            shipment.setState(State.NOT_READY);
            shipment.setServices(new ArrayList<>());

            Shipment result = shipmentService.saveShipment(shipment, userId);
            shipmentId = result.getShipment_id();

            assertSame(shipment, result);
            assertTrue(result.getShipment_id() > 0);
            assertEquals(State.NOT_READY, result.getState());
        } finally {
            cleanupCommittedShipmentRows(shipmentId, warehouseId, userId);
        }
    }

    @Test
    void createShipmentThrowsWhenWarehouseDoesNotExist() {
        assertThrows(
                NullPointerException.class,
                () -> shipmentService.createShipment(
                        "Invalid shipment",
                        new Date(),
                        82101,
                        0.1f,
                        0.2f,
                        10f,
                        1f,
                        -999999,
                        List.of()
                )
        );
    }

    @Test
    void findCostInPriceListThrowsWhenNoVolumeMatches() {
        PriceList priceList = createPriceList();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> invokeFindCostInPriceList(priceList, "BA", 1000f, false)
        );

        assertTrue(exception.getMessage().contains("No price found"));
        assertTrue(exception.getMessage().contains("volume"));
    }

    @Test
    void createShipmentBuildsShipmentWithSelectedServicesAndCalculatedTotalCost() throws SQLException {
        String warehouseName = uniqueName("Create Shipment Warehouse");
        int warehouseId = insertWarehouseDirectly(warehouseName);
        int serviceId = insertServiceDirectly(uniqueName("Create Shipment Service"), 5f, 0.10f);

        insertSmallPriceListEntryDirectly(0.5f, 123.45f);

        Timestamp deliveryTimestamp = Timestamp.valueOf("2099-02-01 12:30:00");
        Date deliveryDate = new Date(deliveryTimestamp.getTime());

        Shipment result = shipmentService.createShipment(
                "Created shipment",
                deliveryDate,
                82101,
                0.10f,
                0.20f,
                0.5f,
                1.0f,
                warehouseId,
                List.of(serviceId)
        );

        assertNotNull(result);
        assertEquals(State.NOT_READY, result.getState());
        assertEquals(82101, result.getDest_region());
        assertEquals(0.5f, result.getWeight(), 0.001f);
        assertEquals(1.0f, result.getVolume(), 0.001f);
        assertEquals(0.10f, result.getFuel_payment(), 0.001f);
        assertEquals(0.20f, result.getToll(), 0.001f);

        assertNotNull(result.getWarehouse());
        assertEquals(warehouseId, result.getWarehouse().getId());
        assertEquals(warehouseName, result.getWarehouse().getName());

        assertNotNull(result.getServices());
        assertEquals(1, result.getServices().size());
        assertEquals(serviceId, result.getServices().get(0).getId());

        assertTrue(result.getTotalCost() > 0f);
    }

    @Test
    void updateShipmentStateUsesLoggedUserAndRollsBackWhenHistoryInsertFails() throws SQLException {
        int userId = insertUserDirectly(uniqueEmail("logged.rollback"));

        User loggedUser = new User(
                "Shipment",
                "Logged",
                uniqueEmail("logged.user"),
                Role.USER
        );
        loggedUser.setId(userId);

        UserService.login(loggedUser);

        Shipment shipment = new Shipment();
        shipment.setShipment_id(-999999);

        try {
            SQLException exception = assertThrows(
                    SQLException.class,
                    () -> shipmentService.updateShipmentState(shipment, State.CANCELED)
            );

            assertNotNull(exception);
            assertEquals(State.CANCELED, shipment.getState());
        } finally {
            UserService.logout();
        }
    }

    private int insertServiceDirectly(String name, float defaultCost, float costModifier) throws SQLException {
        PreparedStatement stmt = DBConnector.getInstance().getConnection().prepareStatement("""
            INSERT INTO Service(service_name, default_cost, cost_modificator, description, service_type)
            VALUES (?, ?, ?, ?, ?)
            RETURNING service_ID
        """);

        stmt.setString(1, name);
        stmt.setFloat(2, defaultCost);
        stmt.setFloat(3, costModifier);
        stmt.setString(4, "Test service");
        stmt.setString(5, ServiceType.SERVICES.name());

        ResultSet rs = stmt.executeQuery();
        rs.next();

        return rs.getInt("service_ID");
    }

    private RegionTable createRegionTable() {
        RegionTable regionTable = new RegionTable();

        ArrayList<Range> baRanges = new ArrayList<>();
        baRanges.add(new Range(80000, 89999));

        ArrayList<Range> nrRanges = new ArrayList<>();
        nrRanges.add(new Range(90000, 99999));

        regionTable.setEntries(List.of(
                new RegionTableEntry(1, baRanges, "BA"),
                new RegionTableEntry(2, nrRanges, "NR")
        ));

        return regionTable;
    }

    private PriceList createPriceList() {
        PriceList priceList = new PriceList();

        priceList.setEntries(List.of(
                new PriceListEntry(1, 10f, 100f, 5f, "BA"),
                new PriceListEntry(2, 20f, 200f, 7f, "BA"),
                new PriceListEntry(3, 50f, 500f, 12f, "BA"),
                new PriceListEntry(4, 20f, 200f, 9f, "NR")
        ));

        return priceList;
    }

    private SmallPriceList createSmallPriceList() {
        SmallPriceList smallPriceList = new SmallPriceList();

        smallPriceList.setEntries(List.of(
                new SmallPriceListEntry(1, 5f, 2.5f),
                new SmallPriceListEntry(2, 10f, 4f),
                new SmallPriceListEntry(3, 20f, 7f)
        ));

        return smallPriceList;
    }

    private Shipment createShipmentWithCoefficientsAndServices() {
        Shipment shipment = new Shipment();
        shipment.setFuel_payment(0.10f);
        shipment.setToll(0.20f);

        ArrayList<AdditionalService> services = new ArrayList<>();
        services.add(new AdditionalService(
                1,
                "Insurance",
                5f,
                0.10f,
                ServiceType.SERVICES,
                "Insurance service"
        ));
        services.add(new AdditionalService(
                2,
                "Fragile handling",
                3f,
                0.20f,
                ServiceType.SERVICES,
                "Fragile handling service"
        ));

        shipment.setServices(services);
        return shipment;
    }

    private String invokeFindRegionForPostalCode(RegionTable regionTable, int postalCode) throws Exception {
        Method method = ShipmentService.class.getDeclaredMethod(
                "findRegionForPostalCode",
                RegionTable.class,
                int.class
        );

        method.setAccessible(true);

        try {
            return (String) method.invoke(shipmentService, regionTable, postalCode);
        } catch (InvocationTargetException e) {
            throwOriginalException(e);
            return null;
        }
    }

    private float invokeFindCostInPriceList(
            PriceList priceList,
            String regionCode,
            float value,
            boolean byWeight
    ) throws Exception {
        Method method = ShipmentService.class.getDeclaredMethod(
                "findCostInPriceList",
                PriceList.class,
                String.class,
                float.class,
                boolean.class
        );

        method.setAccessible(true);

        try {
            return (Float) method.invoke(shipmentService, priceList, regionCode, value, byWeight);
        } catch (InvocationTargetException e) {
            throwOriginalException(e);
            return -1;
        }
    }

    private float invokeFindCostInSmallPriceList(SmallPriceList smallPriceList, float weight) throws Exception {
        Method method = ShipmentService.class.getDeclaredMethod(
                "findCostInSmallPriceList",
                SmallPriceList.class,
                float.class
        );

        method.setAccessible(true);

        try {
            return (Float) method.invoke(shipmentService, smallPriceList, weight);
        } catch (InvocationTargetException e) {
            throwOriginalException(e);
            return -1;
        }
    }

    private void throwOriginalException(InvocationTargetException e) throws Exception {
        Throwable cause = e.getCause();

        if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
        }

        if (cause instanceof Exception) {
            throw (Exception) cause;
        }

        throw e;
    }

    private int insertUserDirectly(String email) throws SQLException {
        PreparedStatement stmt = DBConnector.getInstance().getConnection().prepareStatement("""
            INSERT INTO Users(first_name, last_name, password, email, role)
            VALUES ('Shipment', 'Tester', 'pass', ?, 0)
            RETURNING user_ID
        """);

        stmt.setString(1, email);

        ResultSet rs = stmt.executeQuery();
        rs.next();

        return rs.getInt("user_ID");
    }

    private int insertWarehouseDirectly(String name) throws SQLException {
        PreparedStatement stmt = DBConnector.getInstance().getConnection().prepareStatement("""
            INSERT INTO Warehouse(storage_region, warehouse_region_name, price_list_file, latitude, longitude, is_active)
            VALUES (100, ?, ?, 0, 0, true)
            RETURNING warehouse_ID
        """);

        stmt.setString(1, name);
        stmt.setString(2, name + ".xlsx");

        ResultSet rs = stmt.executeQuery();
        rs.next();

        return rs.getInt("warehouse_ID");
    }

    private int insertShipmentDirectly(
            int userId,
            int warehouseId,
            Timestamp createdAt,
            float totalCost
    ) throws SQLException {
        PreparedStatement stmt = DBConnector.getInstance().getConnection().prepareStatement("""
            INSERT INTO Shipment(user_ID, warehouse_ID, dest_region, weight, volume, fuel_payment, toll, total_cost, created_at, status, is_sp)
            VALUES (?, ?, 82101, 10, 1, 0.1, 0.2, ?, ?, 'NOT_READY', false)
            RETURNING shipment_ID
        """);

        stmt.setInt(1, userId);
        stmt.setInt(2, warehouseId);
        stmt.setFloat(3, totalCost);
        stmt.setTimestamp(4, createdAt);

        ResultSet rs = stmt.executeQuery();
        rs.next();

        return rs.getInt("shipment_ID");
    }

    private int insertHistoryDirectly(int userId, int shipmentId, State state) throws SQLException {
        PreparedStatement stmt = DBConnector.getInstance().getConnection().prepareStatement("""
            INSERT INTO History(timestamp, state, shipment_ID, user_id)
            VALUES (?, ?, ?, ?)
            RETURNING history_ID
        """);

        stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
        stmt.setString(2, state.name());
        stmt.setInt(3, shipmentId);
        stmt.setInt(4, userId);

        ResultSet rs = stmt.executeQuery();
        rs.next();

        return rs.getInt("history_ID");
    }

    private void insertSmallPriceListEntryDirectly(float weight, float cost) throws SQLException {
        PreparedStatement stmt = DBConnector.getInstance().getConnection().prepareStatement("""
            INSERT INTO Sp_price_list(weight_sp, cost_sp)
            VALUES (?, ?)
        """);

        stmt.setFloat(1, weight);
        stmt.setFloat(2, cost);

        stmt.executeUpdate();
    }

    private void insertRegionWithPostalRangeAndPriceListDirectly(
            int warehouseId,
            String regionName,
            int downBound,
            int upBound
    ) throws SQLException {
        Connection connection = DBConnector.getInstance().getConnection();

        PreparedStatement regionStmt = connection.prepareStatement("""
            INSERT INTO Region(warehouse_ID, region_name)
            VALUES (?, ?)
            RETURNING region_ID
        """);

        regionStmt.setInt(1, warehouseId);
        regionStmt.setString(2, regionName);

        ResultSet regionRs = regionStmt.executeQuery();
        regionRs.next();
        int regionId = regionRs.getInt("region_ID");

        PreparedStatement postalStmt = connection.prepareStatement("""
            INSERT INTO Postal_code(down_bound, up_bound)
            VALUES (?, ?)
            ON CONFLICT (up_bound, down_bound) DO UPDATE
            SET up_bound = EXCLUDED.up_bound
            RETURNING postal_code_ID
        """);

        postalStmt.setInt(1, downBound);
        postalStmt.setInt(2, upBound);

        ResultSet postalRs = postalStmt.executeQuery();
        postalRs.next();
        int postalCodeId = postalRs.getInt("postal_code_ID");

        PreparedStatement linkStmt = connection.prepareStatement("""
            INSERT INTO Postal_code_list(region_ID, postal_code_ID)
            VALUES (?, ?)
        """);

        linkStmt.setInt(1, regionId);
        linkStmt.setInt(2, postalCodeId);
        linkStmt.executeUpdate();

        PreparedStatement firstPriceStmt = connection.prepareStatement("""
            INSERT INTO Parameter_list(region_ID, weight, volume, cost)
            VALUES (?, 60, 100, 20)
        """);

        firstPriceStmt.setInt(1, regionId);
        firstPriceStmt.executeUpdate();

        PreparedStatement secondPriceStmt = connection.prepareStatement("""
            INSERT INTO Parameter_list(region_ID, weight, volume, cost)
            VALUES (?, 80, 200, 30)
        """);

        secondPriceStmt.setInt(1, regionId);
        secondPriceStmt.executeUpdate();
    }

    private void cleanupCommittedShipmentRows(int shipmentId, int warehouseId, int userId) throws SQLException {
        Connection connection = DBConnector.getInstance().getConnection();

        if (!connection.getAutoCommit()) {
            connection.rollback();
            connection.setAutoCommit(true);
        }

        if (shipmentId > 0) {
            PreparedStatement historyStmt = connection.prepareStatement("""
                DELETE FROM History WHERE shipment_ID = ?
            """);
            historyStmt.setInt(1, shipmentId);
            historyStmt.executeUpdate();

            PreparedStatement serviceListStmt = connection.prepareStatement("""
                DELETE FROM Service_list WHERE shipment_ID = ?
            """);
            serviceListStmt.setInt(1, shipmentId);
            serviceListStmt.executeUpdate();

            PreparedStatement shipmentStmt = connection.prepareStatement("""
                DELETE FROM Shipment WHERE shipment_ID = ?
            """);
            shipmentStmt.setInt(1, shipmentId);
            shipmentStmt.executeUpdate();
        }

        if (warehouseId > 0) {
            PreparedStatement warehouseStmt = connection.prepareStatement("""
                DELETE FROM Warehouse WHERE warehouse_ID = ?
            """);
            warehouseStmt.setInt(1, warehouseId);
            warehouseStmt.executeUpdate();
        }

        if (userId > 0) {
            PreparedStatement userStmt = connection.prepareStatement("""
                DELETE FROM Users WHERE user_ID = ?
            """);
            userStmt.setInt(1, userId);
            userStmt.executeUpdate();
        }
    }

    private String uniqueEmail(String prefix) {
        return prefix + "." + System.nanoTime() + "@test.com";
    }

    private String uniqueName(String prefix) {
        return prefix + " " + System.nanoTime();
    }
}