package DaoTest;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.shippin.database.DBConnector;
import org.shippin.database.dao.ShipmentDAO;
import org.shippin.domain.AdditionalService;
import org.shippin.domain.Shipment;
import org.shippin.domain.enums.State;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
    @DisplayName("getShipmentByWarehouseID returns non-null list")
    void getShipmentByWarehouseReturnsList() throws SQLException {
        List<Shipment> shipments = shipmentDAO.getShipmentByWarehouseID(1);

        assertNotNull(shipments);
    }

    @Test
    @DisplayName("getShipmentByUserID returns non-null list")
    void getShipmentByUserReturnsList() throws SQLException {
        List<Shipment> shipments = shipmentDAO.getShipmentByUserID(1);

        assertNotNull(shipments);
    }

    @Test
    @DisplayName("getAllShipments returns non-null list")
    void getAllShipmentsReturnsList() throws SQLException {
        List<Shipment> shipments = shipmentDAO.getAllShipments();

        assertNotNull(shipments);
    }

    @Test
    @DisplayName("insertShipment stores shipment with valid user and warehouse")
    void insertShipmentExecutes() throws SQLException {
        int userId = insertUserDirectly("insert.shipment@test.com");
        int warehouseId = insertWarehouseDirectly("Insert Shipment Warehouse");

        Shipment shipment = new Shipment();

        shipment.setDest_region(1);
        shipment.setFuel_payment(5f);
        shipment.setTotalCost(100f);
        shipment.setCreated_at(new Timestamp(System.currentTimeMillis()));
        shipment.setState(State.NOT_READY);
        shipment.setServices(new ArrayList<>());

        assertDoesNotThrow(() ->
                shipmentDAO.insertShipment(shipment, warehouseId, userId)
        );
    }

    @Test
    @DisplayName("getSAllServices returns non-null list")
    void getAllServicesReturnsList() throws SQLException {
        ArrayList<AdditionalService> services = shipmentDAO.getSAllServices();

        assertNotNull(services);
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
    @DisplayName("getShipmentById returns shipment for existing id")
    void getShipmentByIdReturnsExistingShipment() throws SQLException {
        int userId = insertUserDirectly("shipment.byid@test.com");
        int warehouseId = insertWarehouseDirectly("Shipment By Id Warehouse");
        int shipmentId = insertShipmentDirectly(userId, warehouseId);

        Shipment shipment = shipmentDAO.getShipmentById(shipmentId);

        assertNotNull(shipment);
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
            INSERT INTO Shipment(user_ID, warehouse_ID, dest_region, fuel_payment, total_cost, status)
            VALUES (?, ?, 1, 5, 100, 'NOT_READY')
            RETURNING shipment_ID
        """);

        stmt.setInt(1, userId);
        stmt.setInt(2, warehouseId);

        ResultSet rs = stmt.executeQuery();
        rs.next();

        return rs.getInt("shipment_ID");
    }
}