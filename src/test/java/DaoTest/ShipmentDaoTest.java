package DaoTest;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.shippin.database.DBConnector;
import org.shippin.database.dao.ShipmentDAO;
import org.shippin.domain.AdditionalService;
import org.shippin.domain.BriefShippment;
import org.shippin.domain.Shipment;
import org.shippin.domain.ShipmentHistory;
import org.shippin.domain.enums.ServiceType;
import org.shippin.domain.enums.State;

public class ShipmentDaoTest {

    private static ShipmentDAO shipmentDAO;

    @BeforeAll
    static void connect() {
        shipmentDAO = ShipmentDAO.getInstance();
    }

    @BeforeEach
    void begin() throws SQLException {
        DBConnector.getInstance().getConnection().setAutoCommit(false);
    }

    @AfterEach
    void rollback() throws SQLException {
        DBConnector.getInstance().getConnection().rollback();
        DBConnector.getInstance().getConnection().setAutoCommit(true);
    }

    @Test
    @DisplayName("getShipmentById returns null for non-existing id")
    void getShipmentByIdReturnsNull() throws SQLException {
        Shipment shipment = shipmentDAO.getShipmentById(-999);

        assertNull(shipment);
    }

    @Test
    @DisplayName("getShipmentById returns shipment for existing id")
    void getShipmentByIdReturnsExistingShipment() throws SQLException {
        int userId = insertUserDirectly("shipment.byid@test.com");
        int warehouseId = insertWarehouseDirectly("Shipment By Id Warehouse");
        int shipmentId = insertShipmentDirectly(userId, warehouseId);

        Shipment shipment = shipmentDAO.getShipmentById(shipmentId);

        assertNotNull(shipment);
        assertEquals(shipmentId, shipment.getShipment_id());
        assertEquals(userId, shipment.getUser_ID());
    }

    @Test
    @DisplayName("getBriefShippmentById returns null for non-existing id")
    void getBriefShipmentByIdReturnsNull() throws SQLException {
        BriefShippment shipment = shipmentDAO.getBriefShippmentById(-999);

        assertNull(shipment);
    }

    @Test
    @DisplayName("getBriefShippmentById returns shipment for existing id")
    void getBriefShipmentByIdReturnsExistingShipment() throws SQLException {
        int userId = insertUserDirectly("brief.byid@test.com");
        int warehouseId = insertWarehouseDirectly("Brief By Id Warehouse");
        int shipmentId = insertShipmentDirectly(userId, warehouseId);

        BriefShippment shipment = shipmentDAO.getBriefShippmentById(shipmentId);

        assertNotNull(shipment);
        assertEquals(shipmentId, shipment.getShipment_id());
        assertEquals(userId, shipment.getUser_ID());
        assertEquals(State.NOT_READY, shipment.getState());
    }

    @Test
    @DisplayName("getShipmentByWarehouseID returns non-null list")
    void getShipmentByWarehouseReturnsList() throws SQLException {
        List<Shipment> shipments = shipmentDAO.getShipmentByWarehouseID(1);

        assertNotNull(shipments);
    }

    @Test
    @DisplayName("getShipmentByWarehouseID returns inserted shipment")
    void getShipmentByWarehouseReturnsInsertedShipment() throws SQLException {
        int userId = insertUserDirectly("shipment.bywarehouse@test.com");
        int warehouseId = insertWarehouseDirectly("Shipment By Warehouse");
        int shipmentId = insertShipmentDirectly(userId, warehouseId);

        List<Shipment> shipments = shipmentDAO.getShipmentByWarehouseID(warehouseId);

        assertNotNull(shipments);
        assertTrue(shipments.stream().anyMatch(shipment -> shipment.getShipment_id() == shipmentId));
    }

    @Test
    @DisplayName("getBriefShippmentsByWarehouseID returns non-null list")
    void getBriefShipmentsByWarehouseReturnsList() throws SQLException {
        List<BriefShippment> shipments = shipmentDAO.getBriefShippmentsByWarehouseID(1);

        assertNotNull(shipments);
    }

    @Test
    @DisplayName("getBriefShippmentsByWarehouseID returns inserted shipment")
    void getBriefShipmentsByWarehouseReturnsInsertedShipment() throws SQLException {
        int userId = insertUserDirectly("brief.bywarehouse@test.com");
        int warehouseId = insertWarehouseDirectly("Brief By Warehouse");
        int shipmentId = insertShipmentDirectly(userId, warehouseId);

        List<BriefShippment> shipments = shipmentDAO.getBriefShippmentsByWarehouseID(warehouseId);

        assertNotNull(shipments);
        assertTrue(shipments.stream().anyMatch(shipment -> shipment.getShipment_id() == shipmentId));
    }

    @Test
    @DisplayName("getShipmentByUserID returns non-null list")
    void getShipmentByUserReturnsList() throws SQLException {
        List<Shipment> shipments = shipmentDAO.getShipmentByUserID(1);

        assertNotNull(shipments);
    }

    @Test
    @DisplayName("getShipmentByUserID returns inserted shipment")
    void getShipmentByUserReturnsInsertedShipment() throws SQLException {
        int userId = insertUserDirectly("shipment.byuser@test.com");
        int warehouseId = insertWarehouseDirectly("Shipment By User");
        int shipmentId = insertShipmentDirectly(userId, warehouseId);

        List<Shipment> shipments = shipmentDAO.getShipmentByUserID(userId);

        assertNotNull(shipments);
        assertTrue(shipments.stream().anyMatch(shipment -> shipment.getShipment_id() == shipmentId));
    }

    @Test
    @DisplayName("getBriefShippmentsByUserID returns non-null list")
    void getBriefShipmentsByUserReturnsList() throws SQLException {
        List<BriefShippment> shipments = shipmentDAO.getBriefShippmentsByUserID(1);

        assertNotNull(shipments);
    }

    @Test
    @DisplayName("getBriefShippmentsByUserID returns inserted shipment")
    void getBriefShipmentsByUserReturnsInsertedShipment() throws SQLException {
        int userId = insertUserDirectly("brief.byuser@test.com");
        int warehouseId = insertWarehouseDirectly("Brief By User");
        int shipmentId = insertShipmentDirectly(userId, warehouseId);

        List<BriefShippment> shipments = shipmentDAO.getBriefShippmentsByUserID(userId);

        assertNotNull(shipments);
        assertTrue(shipments.stream().anyMatch(shipment -> shipment.getShipment_id() == shipmentId));
    }

    @Test
    @DisplayName("getAllShipments returns non-null list")
    void getAllShipmentsReturnsList() throws SQLException {
        List<Shipment> shipments = shipmentDAO.getAllShipments();

        assertNotNull(shipments);
    }

    @Test
    @DisplayName("getAllShipmentsByDate returns non-null list")
    void getAllShipmentsByDateReturnsList() throws SQLException {
        Timestamp from = Timestamp.valueOf("2000-01-01 00:00:00");
        Timestamp to = new Timestamp(System.currentTimeMillis() + 86_400_000);

        List<Shipment> shipments = shipmentDAO.getAllShipmentsByDate(from, to);

        assertNotNull(shipments);
    }

    @Test
    @DisplayName("getAllShipmentsByDate returns inserted shipment")
    void getAllShipmentsByDateReturnsInsertedShipment() throws SQLException {
        int userId = insertUserDirectly("shipment.bydate@test.com");
        int warehouseId = insertWarehouseDirectly("Shipment By Date");
        int shipmentId = insertShipmentDirectly(userId, warehouseId);

        Timestamp from = Timestamp.valueOf("2000-01-01 00:00:00");
        Timestamp to = new Timestamp(System.currentTimeMillis() + 86_400_000);

        List<Shipment> shipments = shipmentDAO.getAllShipmentsByDate(from, to);

        assertNotNull(shipments);
        assertTrue(shipments.stream().anyMatch(shipment -> shipment.getShipment_id() == shipmentId));
    }

    @Test
    @DisplayName("getBriefShippmentsByDate returns non-null list")
    void getBriefShipmentsByDateReturnsList() throws SQLException {
        Timestamp from = Timestamp.valueOf("2000-01-01 00:00:00");
        Timestamp to = new Timestamp(System.currentTimeMillis() + 86_400_000);

        List<BriefShippment> shipments = shipmentDAO.getBriefShippmentsByDate(from, to);

        assertNotNull(shipments);
    }

    @Test
    @DisplayName("getBriefShippmentsByDate returns inserted shipment")
    void getBriefShipmentsByDateReturnsInsertedShipment() throws SQLException {
        int userId = insertUserDirectly("brief.bydate@test.com");
        int warehouseId = insertWarehouseDirectly("Brief By Date");
        int shipmentId = insertShipmentDirectly(userId, warehouseId);

        Timestamp from = Timestamp.valueOf("2000-01-01 00:00:00");
        Timestamp to = new Timestamp(System.currentTimeMillis() + 86_400_000);

        List<BriefShippment> shipments = shipmentDAO.getBriefShippmentsByDate(from, to);

        assertNotNull(shipments);
        assertTrue(shipments.stream().anyMatch(shipment -> shipment.getShipment_id() == shipmentId));
    }

    @Test
    @DisplayName("insertShipment stores shipment with valid user and warehouse")
    void insertShipmentExecutes() throws SQLException {
        int userId = insertUserDirectly("insert.shipment@test.com");
        int warehouseId = insertWarehouseDirectly("Insert Shipment Warehouse");
        int serviceId = insertServiceDirectly("Insert Shipment Service");

        Shipment shipment = new Shipment();

        shipment.setDest_region(1);
        shipment.setWeight(10f);
        shipment.setVolume(1.5f);
        shipment.setFuel_payment(5f);
        shipment.setToll(2f);
        shipment.setTotalCost(100f);
        shipment.setCreated_at(new Timestamp(System.currentTimeMillis()));
        shipment.setState(State.NOT_READY);

        ArrayList<AdditionalService> services = new ArrayList<>();
        services.add(new AdditionalService(
                serviceId,
                "Insert Shipment Service",
                5f,
                1f,
                ServiceType.SERVICES,
                "Test service"
        ));
        shipment.setServices(services);

        int shipmentId = shipmentDAO.insertShipment(shipment, warehouseId, userId);

        assertTrue(shipmentId > 0);
        assertEquals(shipmentId, shipment.getShipment_id());

        ArrayList<AdditionalService> insertedServices = shipmentDAO.getShipmentServices(shipmentId);

        assertNotNull(insertedServices);
        assertFalse(insertedServices.isEmpty());
        assertEquals(serviceId, insertedServices.get(0).getId());
    }

    @Test
    @DisplayName("getSAllServices returns non-null list")
    void getAllServicesReturnsList() throws SQLException {
        ArrayList<AdditionalService> services = shipmentDAO.getSAllServices();

        assertNotNull(services);
    }

    @Test
    @DisplayName("getSAllServices returns inserted service")
    void getAllServicesReturnsInsertedService() throws SQLException {
        int serviceId = insertServiceDirectly("All Services Test Service");

        ArrayList<AdditionalService> services = shipmentDAO.getSAllServices();

        assertNotNull(services);
        assertTrue(services.stream().anyMatch(service -> service.getId() == serviceId));
    }

    @Test
    @DisplayName("getShipmentServices returns non-null list")
    void getShipmentServicesReturnsNonNullList() throws SQLException {
        int userId = insertUserDirectly("shipment.service@test.com");
        int warehouseId = insertWarehouseDirectly("Shipment Service Warehouse");
        int shipmentId = insertShipmentDirectly(userId, warehouseId);

        ArrayList<AdditionalService> services = shipmentDAO.getShipmentServices(shipmentId);

        assertNotNull(services);
    }

    @Test
    @DisplayName("getShipmentServices returns services for shipment")
    void getShipmentServicesReturnsInsertedServices() throws SQLException {
        int userId = insertUserDirectly("shipment.service.insert@test.com");
        int warehouseId = insertWarehouseDirectly("Shipment Service Insert Warehouse");
        int shipmentId = insertShipmentDirectly(userId, warehouseId);
        int serviceId = insertServiceDirectly("Fragile Test Service");

        insertServiceListDirectly(serviceId, shipmentId);

        ArrayList<AdditionalService> services = shipmentDAO.getShipmentServices(shipmentId);

        assertNotNull(services);
        assertFalse(services.isEmpty());
        assertEquals(serviceId, services.get(0).getId());
    }

    @Test
    @DisplayName("updateShipmentStatus returns false for non-existing shipment")
    void updateShipmentStatusReturnsFalseForNonExistingShipment() throws SQLException {
        boolean updated = shipmentDAO.updateShipmentStatus(-999, State.READY_FOR_DELIVERY);

        assertFalse(updated);
    }

    @Test
    @DisplayName("updateShipmentStatus updates existing shipment")
    void updateShipmentStatusUpdatesExistingShipment() throws SQLException {
        int userId = insertUserDirectly("status.update@test.com");
        int warehouseId = insertWarehouseDirectly("Status Update Warehouse");
        int shipmentId = insertShipmentDirectly(userId, warehouseId);

        boolean updated = shipmentDAO.updateShipmentStatus(shipmentId, State.READY_FOR_DELIVERY);

        assertTrue(updated);

        BriefShippment shipment = shipmentDAO.getBriefShippmentById(shipmentId);

        assertNotNull(shipment);
        assertEquals(State.READY_FOR_DELIVERY, shipment.getState());
    }

    @Test
    @DisplayName("updateShipmentByID returns false for non-existing shipment")
    void updateShipmentByIdReturnsFalseForNonExistingShipment() throws SQLException {
        Shipment shipment = new Shipment();

        shipment.setShipment_id(-999);
        shipment.setUser_ID(1);
        shipment.setDest_region(1);
        shipment.setWeight(10f);
        shipment.setVolume(1f);
        shipment.setFuel_payment(5f);
        shipment.setToll(2f);
        shipment.setTotalCost(100f);
        shipment.setCreated_at(new Timestamp(System.currentTimeMillis()));
        shipment.setState(State.NOT_READY);
        shipment.setServices(new ArrayList<>());

        boolean updated = shipmentDAO.updateShipmentByID(shipment);

        assertFalse(updated);
    }

    @Test
    @DisplayName("updateShipmentByID updates existing shipment")
    void updateShipmentByIdUpdatesExistingShipment() throws SQLException {
        int userId = insertUserDirectly("update.shipment@test.com");
        int warehouseId = insertWarehouseDirectly("Update Shipment Warehouse");
        int shipmentId = insertShipmentDirectly(userId, warehouseId);

        Shipment shipment = new Shipment();

        shipment.setShipment_id(shipmentId);
        shipment.setUser_ID(userId);
        shipment.setDest_region(2);
        shipment.setWeight(20f);
        shipment.setVolume(2f);
        shipment.setFuel_payment(7f);
        shipment.setToll(3f);
        shipment.setTotalCost(150f);
        shipment.setCreated_at(new Timestamp(System.currentTimeMillis()));
        shipment.setState(State.READY_FOR_DELIVERY);
        shipment.setServices(new ArrayList<>());

        boolean updated = shipmentDAO.updateShipmentByID(shipment);

        assertTrue(updated);

        BriefShippment updatedShipment = shipmentDAO.getBriefShippmentById(shipmentId);

        assertNotNull(updatedShipment);
        assertEquals(State.READY_FOR_DELIVERY, updatedShipment.getState());
        assertEquals(20f, updatedShipment.getWeight(), 0.001f);
        assertEquals(2f, updatedShipment.getVolume(), 0.001f);
    }

    @Test
    @DisplayName("deleteShipment returns false for non-existing shipment")
    void deleteShipmentReturnsFalseForNonExistingShipment() throws SQLException {
        boolean deleted = shipmentDAO.deleteShipment(-999);

        assertFalse(deleted);
    }

    @Test
    @DisplayName("deleteShipment deletes existing shipment")
    void deleteShipmentDeletesExistingShipment() throws SQLException {
        int userId = insertUserDirectly("delete.shipment@test.com");
        int warehouseId = insertWarehouseDirectly("Delete Shipment Warehouse");
        int shipmentId = insertShipmentDirectly(userId, warehouseId);

        boolean deleted = shipmentDAO.deleteShipment(shipmentId);

        assertTrue(deleted);
        assertNull(shipmentDAO.getBriefShippmentById(shipmentId));
    }

    @Test
    @DisplayName("addShipmentHistory stores history for shipment")
    void addShipmentHistoryStoresHistory() throws SQLException {
        int userId = insertUserDirectly("history.shipment@test.com");
        int warehouseId = insertWarehouseDirectly("History Shipment Warehouse");
        int shipmentId = insertShipmentDirectly(userId, warehouseId);

        ShipmentHistory history = new ShipmentHistory();

        history.setTimestamp(new Timestamp(System.currentTimeMillis()));
        history.setState(State.READY_FOR_DELIVERY);
        history.setShipment_id(shipmentId);
        history.setUser_id(userId);

        int historyId = shipmentDAO.addShipmentHistory(history);

        assertTrue(historyId > 0);

        List<ShipmentHistory> historyList = shipmentDAO.getShipmentHistoryByShipmentID(shipmentId);

        assertNotNull(historyList);
        assertFalse(historyList.isEmpty());
        assertEquals(State.READY_FOR_DELIVERY, historyList.get(0).getState());
        assertEquals(shipmentId, historyList.get(0).getShipment_id());
        assertNotNull(historyList.get(0).getUserName());
    }

    @Test
    @DisplayName("getShipmentHistoryByShipmentID returns empty list for non-existing shipment")
    void getShipmentHistoryByShipmentIdReturnsEmptyListForNonExistingShipment() throws SQLException {
        List<ShipmentHistory> historyList = shipmentDAO.getShipmentHistoryByShipmentID(-999);

        assertNotNull(historyList);
        assertTrue(historyList.isEmpty());
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
            INSERT INTO Warehouse(storage_region, warehouse_region_name, price_list_file)
            VALUES (100, ?, 'shipment_test.xlsx')
            RETURNING warehouse_ID
        """);

        stmt.setString(1, name);

        ResultSet rs = stmt.executeQuery();
        rs.next();

        return rs.getInt("warehouse_ID");
    }

    private int insertShipmentDirectly(int userId, int warehouseId) throws SQLException {
        PreparedStatement stmt = DBConnector.getInstance().getConnection().prepareStatement("""
            INSERT INTO Shipment(user_ID, warehouse_ID, dest_region, fuel_payment, toll, total_cost, weight, volume, status)
            VALUES (?, ?, 1, 5, 2, 100, 10, 1, 'NOT_READY')
            RETURNING shipment_ID
        """);

        stmt.setInt(1, userId);
        stmt.setInt(2, warehouseId);

        ResultSet rs = stmt.executeQuery();
        rs.next();

        return rs.getInt("shipment_ID");
    }

private int insertServiceDirectly(String name) throws SQLException {
    PreparedStatement stmt = DBConnector.getInstance().getConnection().prepareStatement("""
        INSERT INTO Service(
            service_name,
            service_name_en,
            default_cost,
            cost_modificator,
            description,
            description_en,
            service_type
        )
        VALUES (?, ?, 5, 1, 'Test service', 'Test service', 'SERVICES')
        RETURNING service_ID
    """);

    stmt.setString(1, name);
    stmt.setString(2, name);

    ResultSet rs = stmt.executeQuery();
    rs.next();

    return rs.getInt("service_ID");
}

    private void insertServiceListDirectly(int serviceId, int shipmentId) throws SQLException {
        PreparedStatement stmt = DBConnector.getInstance().getConnection().prepareStatement("""
            INSERT INTO Service_list(service_ID, shipment_ID)
            VALUES (?, ?)
        """);

        stmt.setInt(1, serviceId);
        stmt.setInt(2, shipmentId);

        stmt.executeUpdate();
    }
}