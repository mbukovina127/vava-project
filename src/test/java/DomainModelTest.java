import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.shippin.domain.AdditionalService;
import org.shippin.domain.BriefShippment;
import org.shippin.domain.BriefWarehouse;
import org.shippin.domain.Coordinates;
import org.shippin.domain.PriceList;
import org.shippin.domain.PriceListEntry;
import org.shippin.domain.RegionTable;
import org.shippin.domain.RegionTableEntry;
import org.shippin.domain.Row;
import org.shippin.domain.Shipment;
import org.shippin.domain.ShipmentHistory;
import org.shippin.domain.SmallPriceList;
import org.shippin.domain.SmallPriceListEntry;
import org.shippin.domain.Table;
import org.shippin.domain.User;
import org.shippin.domain.Warehouse;
import org.shippin.domain.enums.Role;
import org.shippin.domain.enums.ServiceType;
import org.shippin.domain.enums.State;
import org.shippin.util.Range;

public class DomainModelTest {

    @Test
    void additionalServiceConstructorsGettersSettersAndLombokMethodsWork() {
        AdditionalService service = new AdditionalService();

        service.setId(1);
        service.setName("Insurance");
        service.setDefaultCost(10.5f);
        service.setCostModifier(0.2f);
        service.setServiceType(ServiceType.SERVICES);
        service.setDescription("Shipment insurance");
    service.setDescription_en("Shipment insurance");
    service.setName_en("Insurance");

        assertEquals(1, service.getId());
        assertEquals("Insurance", service.getName());
        assertEquals(10.5f, service.getDefaultCost(), 0.001f);
        assertEquals(0.2f, service.getCostModifier(), 0.001f);
        assertEquals(ServiceType.SERVICES, service.getServiceType());
        assertEquals("Shipment insurance", service.getDescription());
        assertEquals("Shipment insurance", service.getDescription_en());
        assertEquals("Insurance", service.getName_en());

        AdditionalService same = new AdditionalService(
                1,
                "Insurance",
                10.5f,
                0.2f,
                ServiceType.SERVICES,
                "Shipment insurance",
                "Shipment insurance",
                "Insurance"
        );

        AdditionalService different = new AdditionalService(
                2,
                "Packing",
                4f,
                0.1f,
                ServiceType.PRODUCTS,
                "Packing material",
                "Packing material",
                "Packing"
        );

        assertEquals(service, same);
        assertEquals(service.hashCode(), same.hashCode());
        assertNotEquals(service, different);
        assertTrue(service.toString().contains("Insurance"));
    }

    @Test
    void coordinatesConstructorsGettersSettersAndLombokMethodsWork() {
        Coordinates coordinates = new Coordinates();

        coordinates.setX(48.1486);
        coordinates.setY(17.1077);

        assertEquals(48.1486, coordinates.getX(), 0.0001);
        assertEquals(17.1077, coordinates.getY(), 0.0001);

        Coordinates same = new Coordinates(48.1486, 17.1077);
        Coordinates different = new Coordinates(49.0, 18.0);

        assertEquals(coordinates, same);
        assertEquals(coordinates.hashCode(), same.hashCode());
        assertNotEquals(coordinates, different);
        assertTrue(coordinates.toString().contains("48.1486"));
    }

    @Test
    void priceListEntryConstructorsGettersSettersAndLombokMethodsWork() {
        PriceListEntry entry = new PriceListEntry();

        entry.setId(1);
        entry.setWeight(10f);
        entry.setVolume(0.5f);
        entry.setCost(5.2f);
        entry.setZone("BA");

        assertEquals(1, entry.getId());
        assertEquals(10f, entry.getWeight(), 0.001f);
        assertEquals(0.5f, entry.getVolume(), 0.001f);
        assertEquals(5.2f, entry.getCost(), 0.001f);
        assertEquals("BA", entry.getZone());

        PriceListEntry same = new PriceListEntry(1, 10f, 0.5f, 5.2f, "BA");
        PriceListEntry different = new PriceListEntry(2, 20f, 1.0f, 8.5f, "NR");

        assertEquals(entry, same);
        assertEquals(entry.hashCode(), same.hashCode());
        assertNotEquals(entry, different);
        assertTrue(entry.toString().contains("BA"));
    }

    @Test
    void smallPriceListEntryConstructorsGettersSettersAndLombokMethodsWork() {
        SmallPriceListEntry entry = new SmallPriceListEntry();

        entry.setId(1);
        entry.setWeight(5f);
        entry.setCost(2.5f);

        assertEquals(1, entry.getId());
        assertEquals(5f, entry.getWeight(), 0.001f);
        assertEquals(2.5f, entry.getCost(), 0.001f);

        SmallPriceListEntry same = new SmallPriceListEntry(1, 5f, 2.5f);
        SmallPriceListEntry different = new SmallPriceListEntry(2, 10f, 5f);

        assertEquals(entry, same);
        assertEquals(entry.hashCode(), same.hashCode());
        assertNotEquals(entry, different);
        assertTrue(entry.toString().contains("2.5"));
    }

    @Test
    void userConstructorsGettersSettersFullNameAndLombokMethodsWork() {
        User user = new User("John", "Doe", "john@example.com", Role.USER);

        user.setId(10);
        user.setPassword("secret");
        user.setAccessToken("token");

        assertEquals(10, user.getId());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("secret", user.getPassword());
        assertEquals(Role.USER, user.getRole());
        assertEquals("token", user.getAccessToken());
        assertEquals("John Doe", user.getFullUserName());

        User same = new User(
                10,
                "John",
                "Doe",
                "john@example.com",
                "secret",
                Role.USER,
                "token"
        );

        User different = new User(
                11,
                "Jane",
                "Doe",
                "jane@example.com",
                "secret",
                Role.ADMIN,
                "token-2"
        );

        assertEquals(user, same);
        assertEquals(user.hashCode(), same.hashCode());
        assertNotEquals(user, different);
        assertTrue(user.toString().contains("john@example.com"));
    }

    @Test
    void coreShipmentInfoFieldsWorkThroughBriefShipmentSubclass() {
        BriefShippment shipment = new BriefShippment();
        Timestamp createdAt = Timestamp.valueOf("2026-01-01 10:15:30");
        Coordinates startCoordinates = new Coordinates(48.1, 17.1);

        shipment.setShipment_id(1);
        shipment.setUser_ID(2);
        shipment.setCreated_at(createdAt);
        shipment.setDest_region(82101);
        shipment.setStartCoordinate(startCoordinates);
        shipment.setWeight(12.5f);
        shipment.setVolume(0.7f);
        shipment.setFuel_payment(1.2f);
        shipment.setToll(3.4f);
        shipment.setTotalCost(50.6f);
        shipment.setState(State.NOT_READY);

        assertEquals(1, shipment.getShipment_id());
        assertEquals(2, shipment.getUser_ID());
        assertEquals(createdAt, shipment.getCreated_at());
        assertEquals(82101, shipment.getDest_region());
        assertEquals(startCoordinates, shipment.getStartCoordinate());
        assertEquals(12.5f, shipment.getWeight(), 0.001f);
        assertEquals(0.7f, shipment.getVolume(), 0.001f);
        assertEquals(1.2f, shipment.getFuel_payment(), 0.001f);
        assertEquals(3.4f, shipment.getToll(), 0.001f);
        assertEquals(50.6f, shipment.getTotalCost(), 0.001f);
        assertEquals(State.NOT_READY, shipment.getState());

        BriefShippment same = new BriefShippment();
        same.setShipment_id(1);
        same.setUser_ID(2);
        same.setCreated_at(createdAt);
        same.setDest_region(82101);
        same.setStartCoordinate(startCoordinates);
        same.setWeight(12.5f);
        same.setVolume(0.7f);
        same.setFuel_payment(1.2f);
        same.setToll(3.4f);
        same.setTotalCost(50.6f);
        same.setState(State.NOT_READY);

        assertEquals(shipment, same);
        assertEquals(shipment.hashCode(), same.hashCode());
        assertTrue(shipment.toString().contains("NOT_READY"));
    }

    @Test
    void coreWarehouseInfoFieldsWorkThroughBriefWarehouseConstructors() {
        Coordinates coordinates = new Coordinates(48.1, 17.1);

        BriefWarehouse full = new BriefWarehouse(1, "Bratislava", "ba.xlsx", 82101, coordinates);

        assertEquals(1, full.getId());
        assertEquals("Bratislava", full.getName());
        assertEquals("ba.xlsx", full.getRegionName());
        assertEquals(82101, full.getPostalCode());
        assertEquals(coordinates, full.getCoord());

        BriefWarehouse withoutPostalCode = new BriefWarehouse(2, "Nitra", "nr.xlsx");

        assertEquals(2, withoutPostalCode.getId());
        assertEquals("Nitra", withoutPostalCode.getName());
        assertEquals("nr.xlsx", withoutPostalCode.getRegionName());
        assertEquals(0, withoutPostalCode.getPostalCode());
        assertEquals(new Coordinates(0, 0), withoutPostalCode.getCoord());

        BriefWarehouse withPostalCode = new BriefWarehouse(3, "Trnava", "tt.xlsx", 91701);

        assertEquals(3, withPostalCode.getId());
        assertEquals("Trnava", withPostalCode.getName());
        assertEquals("tt.xlsx", withPostalCode.getRegionName());
        assertEquals(91701, withPostalCode.getPostalCode());
        assertEquals(new Coordinates(0, 0), withPostalCode.getCoord());

        BriefWarehouse mutable = new BriefWarehouse();
        mutable.setId(4);
        mutable.setName("Kosice");
        mutable.setRegionName("ke.xlsx");
        mutable.setPostalCode(40001);
        mutable.setCoord(new Coordinates(49, 21));

        assertEquals(4, mutable.getId());
        assertEquals("Kosice", mutable.getName());
        assertEquals("ke.xlsx", mutable.getRegionName());
        assertEquals(40001, mutable.getPostalCode());
        assertEquals(new Coordinates(49, 21), mutable.getCoord());

        assertTrue(mutable.toString().contains("Kosice"));
        assertNotEquals(full, mutable);
    }

    @Test
    void priceListStoresEntriesAndReturnsDistinctRegions() {
        PriceListEntry baFirst = new PriceListEntry(1, 10f, 0.5f, 5f, "BA");
        PriceListEntry baSecond = new PriceListEntry(2, 20f, 1.0f, 10f, "BA");
        PriceListEntry nr = new PriceListEntry(3, 10f, 0.5f, 6f, "NR");

        PriceList priceList = new PriceList();

        assertTrue(priceList.getEntries().isEmpty());
        assertTrue(priceList.setEntries(List.of(baFirst, baSecond, nr)));

        assertEquals(List.of(baFirst, baSecond, nr), priceList.getEntries());
        assertEquals(List.of("BA", "NR"), priceList.getRegions());
    }

    @Test
    void smallPriceListStoresEntries() {
        SmallPriceListEntry first = new SmallPriceListEntry(1, 5f, 2.5f);
        SmallPriceListEntry second = new SmallPriceListEntry(2, 10f, 5f);

        SmallPriceList smallPriceList = new SmallPriceList();

        assertTrue(smallPriceList.getEntries().isEmpty());
        assertTrue(smallPriceList.setEntries(List.of(first, second)));

        assertEquals(List.of(first, second), smallPriceList.getEntries());
    }

    @Test
    void regionTableStoresEntriesAndReturnsRegionsInOrder() {
        RegionTableEntry ba = new RegionTableEntry(
                1,
                new ArrayList<>(List.of(new Range(81000, 81999))),
                "BA"
        );

        RegionTableEntry nr = new RegionTableEntry(
                2,
                new ArrayList<>(List.of(new Range(94900, 94999))),
                "NR"
        );

        RegionTable regionTable = new RegionTable();

        assertTrue(regionTable.getEntries().isEmpty());
        assertTrue(regionTable.setEntries(List.of(ba, nr)));

        assertEquals(List.of(ba, nr), regionTable.getEntries());
        assertEquals(List.of("BA", "NR"), regionTable.getRegions());
    }

    @Test
    void regionTableEntryReturnsCopyOfRangesAndAllowsAddingRange() {
        Range firstRange = new Range(81000, 81999);
        RegionTableEntry entry = new RegionTableEntry(
                1,
                new ArrayList<>(List.of(firstRange)),
                "BA"
        );

        List<Range> returnedRanges = entry.getRanges();
        returnedRanges.clear();

        assertEquals(1, entry.getRanges().size());
        assertSame(firstRange, entry.getRanges().getFirst());

        Range secondRange = new Range(82000, 82999);
        entry.addRange(secondRange);
        entry.setRegionCode("BA2");

        assertEquals("BA2", entry.getRegionCode());
        assertEquals(2, entry.getRanges().size());
        assertSame(secondRange, entry.getRanges().get(1));
    }

    @Test
    void shipmentConstructorsGettersAndSettersWork() {
        AdditionalService service = new AdditionalService(
                1,
                "Insurance",
                10f,
                0.2f,
                ServiceType.SERVICES,
                "Insurance description",
                "Insurance description",
                "Insurance"
        );

        ArrayList<AdditionalService> services = new ArrayList<>(List.of(service));
        BriefWarehouse warehouse = new BriefWarehouse(1, "Bratislava", "ba.xlsx");

        Shipment shipment = new Shipment();

        shipment.setServices(services);
        shipment.setWarehouse(warehouse);

        assertEquals(services, shipment.getServices());
        assertEquals(warehouse, shipment.getWarehouse());

        Shipment constructed = new Shipment(services, warehouse);

        assertEquals(services, constructed.getServices());
        assertEquals(warehouse, constructed.getWarehouse());
    }

    @Test
    void shipmentHistoryConstructorsGettersAndSettersWork() {
        Timestamp timestamp = Timestamp.valueOf("2026-01-01 10:15:30");

        ShipmentHistory history = new ShipmentHistory();

        history.setHistory_id(1);
        history.setTimestamp(timestamp);
        history.setState(State.READY_FOR_DELIVERY);
        history.setShipment_id(2);
        history.setUser_id(3);
        history.setUserName("John Doe");

        assertEquals(1, history.getHistory_id());
        assertEquals(timestamp, history.getTimestamp());
        assertEquals(State.READY_FOR_DELIVERY, history.getState());
        assertEquals(2, history.getShipment_id());
        assertEquals(3, history.getUser_id());
        assertEquals("John Doe", history.getUserName());

        ShipmentHistory constructed = new ShipmentHistory(
                1,
                timestamp,
                State.READY_FOR_DELIVERY,
                2,
                3,
                "John Doe"
        );

        assertEquals(1, constructed.getHistory_id());
        assertEquals(timestamp, constructed.getTimestamp());
        assertEquals(State.READY_FOR_DELIVERY, constructed.getState());
        assertEquals(2, constructed.getShipment_id());
        assertEquals(3, constructed.getUser_id());
        assertEquals("John Doe", constructed.getUserName());
    }

    @Test
    void warehouseConstructorsGettersAndSettersWork() {
        PriceList priceList = new PriceList();
        RegionTable regionTable = new RegionTable();
        Coordinates coordinates = new Coordinates(48.1, 17.1);

        Warehouse empty = new Warehouse();

        empty.setId(1);
        empty.setName("Bratislava");
        empty.setRegionName("ba.xlsx");
        empty.setPostalCode(82101);
        empty.setCoord(coordinates);
        empty.setPriceList(priceList);
        empty.setRegionTable(regionTable);

        assertEquals(1, empty.getId());
        assertEquals("Bratislava", empty.getName());
        assertEquals("ba.xlsx", empty.getRegionName());
        assertEquals(82101, empty.getPostalCode());
        assertEquals(coordinates, empty.getCoord());
        assertEquals(priceList, empty.getPriceList());
        assertEquals(regionTable, empty.getRegionTable());

        Warehouse withTables = new Warehouse("Nitra", "nr.xlsx", priceList, regionTable);

        assertEquals(-1, withTables.getId());
        assertEquals("Nitra", withTables.getName());
        assertEquals("nr.xlsx", withTables.getRegionName());
        assertEquals(0, withTables.getPostalCode());
        assertEquals(new Coordinates(0, 0), withTables.getCoord());
        assertEquals(priceList, withTables.getPriceList());
        assertEquals(regionTable, withTables.getRegionTable());

        Warehouse withPostalCode = new Warehouse("Trnava", "tt.xlsx", 91701, priceList, regionTable);

        assertEquals(-1, withPostalCode.getId());
        assertEquals("Trnava", withPostalCode.getName());
        assertEquals("tt.xlsx", withPostalCode.getRegionName());
        assertEquals(91701, withPostalCode.getPostalCode());
        assertEquals(new Coordinates(0, 0), withPostalCode.getCoord());
        assertEquals(priceList, withPostalCode.getPriceList());
        assertEquals(regionTable, withPostalCode.getRegionTable());

        Warehouse constructorWithCoordinates = new Warehouse(
                "Kosice",
                "ke.xlsx",
                priceList,
                regionTable,
                40001,
                coordinates
        );

        assertEquals(-1, constructorWithCoordinates.getId());
        assertEquals("Kosice", constructorWithCoordinates.getName());
        assertEquals("ke.xlsx", constructorWithCoordinates.getRegionName());
        assertEquals(40001, constructorWithCoordinates.getPostalCode());
        assertEquals(coordinates, constructorWithCoordinates.getCoord());
    }

    @Test
    void tableDefaultAddRowAddsRowToRowsList() {
        TestTable table = new TestTable();
        TestRow row = new TestRow("row-1");

        table.addRow(row);

        assertEquals(1, table.getRows().size());
        assertSame(row, table.getRows().getFirst());
    }

    @Test
    void roleEnumContainsExpectedValues() {
        assertEquals(Role.USER, Role.valueOf("USER"));
        assertEquals(Role.POWER_USER, Role.valueOf("POWER_USER"));
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));

        assertEquals(List.of(Role.USER, Role.POWER_USER, Role.ADMIN), List.of(Role.values()));
    }

    @Test
    void serviceTypeEnumContainsExpectedValues() {
        assertEquals(ServiceType.SERVICES, ServiceType.valueOf("SERVICES"));
        assertEquals(ServiceType.PRODUCTS, ServiceType.valueOf("PRODUCTS"));
        assertEquals(ServiceType.ADDITIONAL_PAYMENTS, ServiceType.valueOf("ADDITIONAL_PAYMENTS"));

        assertEquals(
                List.of(ServiceType.SERVICES, ServiceType.PRODUCTS, ServiceType.ADDITIONAL_PAYMENTS),
                List.of(ServiceType.values())
        );
    }

    @Test
    void stateAllowedTransitionsMatchWorkflow() {
        assertEquals(
                List.of(State.READY_FOR_DELIVERY, State.CANCELED),
                State.NOT_READY.allowedTransitions()
        );

        assertEquals(
                List.of(State.BEING_DELIVERED, State.CANCELED),
                State.READY_FOR_DELIVERY.allowedTransitions()
        );

        assertEquals(
                List.of(State.DELIVERED, State.FAILED),
                State.BEING_DELIVERED.allowedTransitions()
        );

        assertTrue(State.DELIVERED.allowedTransitions().isEmpty());
        assertTrue(State.CANCELED.allowedTransitions().isEmpty());
        assertTrue(State.FAILED.allowedTransitions().isEmpty());
    }

    @Test
    void enumValuesCanBeUsedWithoutExceptions() {
        assertDoesNotThrow(() -> {
            for (Role role : Role.values()) {
                assertNotNull(role.name());
            }

            for (ServiceType serviceType : ServiceType.values()) {
                assertNotNull(serviceType.name());
            }

            for (State state : State.values()) {
                assertNotNull(state.name());
                assertNotNull(state.allowedTransitions());
            }
        });
    }

    private static class TestRow implements Row {
        private final String value;

        private TestRow(String value) {
            this.value = value;
        }
    }

    private static class TestTable implements Table<TestRow> {
        private List<TestRow> rows = new ArrayList<>();

        @Override
        public List<TestRow> getRows() {
            return rows;
        }

        @Override
        public void setRows(List<TestRow> rows) {
            this.rows = rows;
        }
    }
}