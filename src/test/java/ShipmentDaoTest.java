import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.shippin.database.Config;
import org.shippin.database.DBConnector;
import org.shippin.database.dao.ShipmentDAO;
import org.shippin.domain.Shipment;
import org.shippin.domain.enums.State;

public class ShipmentDaoTest {

    private static DBConnector dbc;
    private static ShipmentDAO shipmentDAO;

    @BeforeAll
    static void connect() throws SQLException {
        dbc = new DBConnector(new Config());
        dbc.connect();
        shipmentDAO = new ShipmentDAO(dbc.getConnection());
    }

    @BeforeEach
    void begin() throws SQLException {
        dbc.getConnection().setAutoCommit(false);
    }

    @AfterEach
    void rollback() throws SQLException {
        dbc.getConnection().rollback();
        dbc.getConnection().setAutoCommit(true);
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
    @DisplayName("insertShipment executes or exposes bug")
    void insertShipmentExecutes() {
        Shipment sh = new Shipment();

        sh.setDest_region("BA1");
        sh.setFuel_payment(5f);
        sh.setTotalCost(100f);
        sh.setCreated_at(new Timestamp(System.currentTimeMillis()));
        sh.setState(State.NOT_READY);
        sh.setServices(new ArrayList<>());

        assertDoesNotThrow(() ->
                shipmentDAO.insertShipment(sh, 1, 1)
        );
    }
}