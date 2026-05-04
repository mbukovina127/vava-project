package DaoTest;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import org.shippin.database.dao.WarehouseDAO;
import org.shippin.domain.BriefWarehouse;
import org.shippin.domain.Coordinates;
import org.shippin.domain.Warehouse;

public class WarehouseDaoTest {

    private static WarehouseDAO warehouseDAO;

    @BeforeAll
    static void connect() {
        warehouseDAO = WarehouseDAO.getInstance();
    }

    @BeforeEach
    void begin() throws SQLException {
        DBConnector.getInstance().getConnection().setAutoCommit(false);
    }

    @AfterEach
    void rollback() throws SQLException {
        var connection = DBConnector.getInstance().getConnection();

        if (!connection.getAutoCommit()) {
            connection.rollback();
        }

        connection.setAutoCommit(true);

        try (PreparedStatement stmt = connection.prepareStatement("""
            DELETE FROM Warehouse
            WHERE warehouse_region_name IN (
                'Full Warehouse Test',
                'After Full Update Warehouse',
                'Delete Warehouse Test'
            )
            OR price_list_file IN (
                'full_test.xlsx',
                'after_full_update.xlsx',
                'delete_warehouse.xlsx'
            )
            OR warehouse_region_name LIKE 'Warehouse Test %'
            OR warehouse_region_name LIKE 'Inserted Warehouse%'
            OR warehouse_region_name LIKE 'Before Update Warehouse%'
            OR warehouse_region_name LIKE 'Before Full Update Warehouse%';
        """)) {
            stmt.executeUpdate();
        }
    }

    @Test
    @DisplayName("getById returns warehouse for existing ID")
    void getByIdReturnsWarehouseForExistingId() throws SQLException {
        int warehouseId = insertWarehouseDirectly("Warehouse Test BA", 100, "ba_prices.xlsx");

        Warehouse warehouse = warehouseDAO.getById(warehouseId);

        assertNotNull(warehouse);
        assertEquals(warehouseId, warehouse.getId());
        assertEquals("Warehouse Test BA", warehouse.getName());
        assertEquals(100, warehouse.getPostalCode());
        assertNotNull(warehouse.getCoord());
    }

    @Test
    @DisplayName("getById returns null for non-existing ID")
    void getByIdReturnsNullForNonExistingId() throws SQLException {
        Warehouse warehouse = warehouseDAO.getById(-999);

        assertNull(warehouse);
    }

    @Test
    @DisplayName("getAllBriefWarehouses returns inserted warehouse")
    void getAllBriefWarehousesReturnsInsertedWarehouse() throws SQLException {
        int warehouseId = insertWarehouseDirectly("Warehouse Test TT", 200, "tt_prices.xlsx");

        List<BriefWarehouse> warehouses = warehouseDAO.getAllBriefWarehouses();

        assertNotNull(warehouses);
        assertFalse(warehouses.isEmpty());
        assertTrue(warehouses.stream().anyMatch(warehouse -> warehouse.getId() == warehouseId));
    }

    @Test
    @DisplayName("getlBriefWarehouse returns warehouse for existing ID")
    void getlBriefWarehouseReturnsWarehouseForExistingId() throws SQLException {
        int warehouseId = insertWarehouseDirectly("Warehouse Test KE", 300, "ke_prices.xlsx");

        BriefWarehouse briefWarehouse = warehouseDAO.getlBriefWarehouse(warehouseId);

        assertNotNull(briefWarehouse);
        assertEquals(warehouseId, briefWarehouse.getId());
        assertEquals("Warehouse Test KE", briefWarehouse.getName());
    }

    @Test
    @DisplayName("getlBriefWarehouse returns null for non-existing ID")
    void getlBriefWarehouseReturnsNullForNonExistingId() throws SQLException {
        BriefWarehouse briefWarehouse = warehouseDAO.getlBriefWarehouse(-999);

        assertNull(briefWarehouse);
    }

    @Test
    @DisplayName("upsertWarehouse stores warehouse without coordinates")
    void upsertWarehouseStoresWarehouseWithoutCoordinates() throws SQLException {
        Warehouse warehouse = new Warehouse();

        warehouse.setId(99901);
        warehouse.setName("upsert_prices.xlsx");
        warehouse.setRegionName("Warehouse Upsert Test");
        warehouse.setPostalCode(901);
        warehouse.setCoord(null);

        warehouseDAO.upsertWarehouse(warehouse);

        PreparedStatement stmt = DBConnector.getInstance().getConnection().prepareStatement("""
                SELECT warehouse_ID, warehouse_region_name, price_list_file, storage_region, latitude, longitude
                FROM Warehouse
                WHERE warehouse_ID = ?;
                """);

        stmt.setInt(1, 99901);

        ResultSet rs = stmt.executeQuery();

        assertTrue(rs.next());
        assertEquals(99901, rs.getInt("warehouse_ID"));
        assertEquals("Warehouse Upsert Test", rs.getString("warehouse_region_name"));
        assertEquals("upsert_prices.xlsx", rs.getString("price_list_file"));
        assertEquals(901, rs.getInt("storage_region"));
        assertEquals(0.0, rs.getDouble("latitude"), 0.001);
        assertEquals(0.0, rs.getDouble("longitude"), 0.001);
    }

    @Test
    @DisplayName("upsertWarehouse updates existing warehouse")
    void upsertWarehouseUpdatesExistingWarehouse() throws SQLException {
        Warehouse warehouse = new Warehouse();

        warehouse.setId(99902);
        warehouse.setName("first_prices.xlsx");
        warehouse.setRegionName("Warehouse First Name");
        warehouse.setPostalCode(902);
        warehouse.setCoord(new Coordinates(48.1, 17.1));

        warehouseDAO.upsertWarehouse(warehouse);

        warehouse.setName("updated_prices.xlsx");
        warehouse.setRegionName("Warehouse Updated Name");
        warehouse.setPostalCode(903);
        warehouse.setCoord(new Coordinates(49.2, 18.2));

        warehouseDAO.upsertWarehouse(warehouse);

        BriefWarehouse updatedWarehouse = warehouseDAO.getlBriefWarehouse(99902);

        assertNotNull(updatedWarehouse);
        assertEquals("Warehouse Updated Name", updatedWarehouse.getName());
        assertEquals("updated_prices.xlsx", updatedWarehouse.getRegionName());
        assertEquals(903, updatedWarehouse.getPostalCode());
        assertEquals(49.2, updatedWarehouse.getCoord().getX(), 0.001);
        assertEquals(18.2, updatedWarehouse.getCoord().getY(), 0.001);
    }

    @Test
    @DisplayName("insertWarehouse stores warehouse with coordinates")
    void insertWarehouseStoresWarehouseWithCoordinates() throws SQLException {
        Warehouse warehouse = new Warehouse();

        warehouse.setName("Inserted Warehouse");
        warehouse.setRegionName("inserted_prices.xlsx");
        warehouse.setPostalCode(400);
        warehouse.setCoord(new Coordinates(48.5, 17.5));

        int warehouseId = warehouseDAO.insertWarehouse(warehouse);

        assertTrue(warehouseId > 0);

        BriefWarehouse insertedWarehouse = warehouseDAO.getlBriefWarehouse(warehouseId);

        assertNotNull(insertedWarehouse);
        assertEquals("Inserted Warehouse", insertedWarehouse.getName());
        assertEquals("inserted_prices.xlsx", insertedWarehouse.getRegionName());
        assertEquals(400, insertedWarehouse.getPostalCode());
        assertEquals(48.5, insertedWarehouse.getCoord().getX(), 0.001);
        assertEquals(17.5, insertedWarehouse.getCoord().getY(), 0.001);
    }

    @Test
    @DisplayName("insertWarehouse stores warehouse without coordinates")
    void insertWarehouseStoresWarehouseWithoutCoordinates() throws SQLException {
        Warehouse warehouse = new Warehouse();

        warehouse.setName("Inserted Warehouse No Coordinates");
        warehouse.setRegionName("inserted_no_coordinates.xlsx");
        warehouse.setPostalCode(401);
        warehouse.setCoord(null);

        int warehouseId = warehouseDAO.insertWarehouse(warehouse);

        assertTrue(warehouseId > 0);

        BriefWarehouse insertedWarehouse = warehouseDAO.getlBriefWarehouse(warehouseId);

        assertNotNull(insertedWarehouse);
        assertEquals(0.0, insertedWarehouse.getCoord().getX(), 0.001);
        assertEquals(0.0, insertedWarehouse.getCoord().getY(), 0.001);
    }

    @Test
    @DisplayName("insertFullWarehouse executes with null tables")
    void insertFullWarehouseExecutes() {
        Warehouse warehouse = new Warehouse();

        warehouse.setName("Full Warehouse Test");
        warehouse.setRegionName("full_test.xlsx");
        warehouse.setPostalCode(500);
        warehouse.setCoord(new Coordinates(48.7, 17.7));
        warehouse.setRegionTable(null);
        warehouse.setPriceList(null);

        assertDoesNotThrow(() -> warehouseDAO.insertFullWarehouse(warehouse));
        assertTrue(warehouse.getId() > 0);
    }

    @Test
    @DisplayName("updateWarehouse returns false for non-existing warehouse")
    void updateWarehouseReturnsFalseForNonExistingWarehouse() throws SQLException {
        Warehouse warehouse = new Warehouse();

        warehouse.setId(-999);
        warehouse.setName("Missing Warehouse");
        warehouse.setRegionName("missing.xlsx");
        warehouse.setPostalCode(999);
        warehouse.setCoord(new Coordinates(1.0, 2.0));

        boolean updated = warehouseDAO.updateWarehouse(warehouse);

        assertFalse(updated);
    }

    @Test
    @DisplayName("updateWarehouse updates existing warehouse")
    void updateWarehouseUpdatesExistingWarehouse() throws SQLException {
        int warehouseId = insertWarehouseDirectly("Before Update Warehouse", 600, "before_update.xlsx");

        Warehouse warehouse = new Warehouse();

        warehouse.setId(warehouseId);
        warehouse.setName("After Update Warehouse");
        warehouse.setRegionName("after_update.xlsx");
        warehouse.setPostalCode(601);
        warehouse.setCoord(new Coordinates(49.9, 18.9));

        boolean updated = warehouseDAO.updateWarehouse(warehouse);

        assertTrue(updated);

        BriefWarehouse updatedWarehouse = warehouseDAO.getlBriefWarehouse(warehouseId);

        assertNotNull(updatedWarehouse);
        assertEquals("After Update Warehouse", updatedWarehouse.getName());
        assertEquals("after_update.xlsx", updatedWarehouse.getRegionName());
        assertEquals(601, updatedWarehouse.getPostalCode());
        assertEquals(49.9, updatedWarehouse.getCoord().getX(), 0.001);
        assertEquals(18.9, updatedWarehouse.getCoord().getY(), 0.001);
    }

    @Test
    @DisplayName("updateFullWarehouse returns false for non-existing warehouse")
    void updateFullWarehouseReturnsFalseForNonExistingWarehouse() throws SQLException {
        Warehouse warehouse = new Warehouse();

        warehouse.setId(-999);
        warehouse.setName("Missing Full Warehouse");
        warehouse.setRegionName("missing_full.xlsx");
        warehouse.setPostalCode(998);
        warehouse.setCoord(new Coordinates(1.0, 2.0));
        warehouse.setRegionTable(null);
        warehouse.setPriceList(null);

        boolean updated = warehouseDAO.updateFullWarehouse(warehouse);

        assertFalse(updated);
    }

    @Test
    @DisplayName("updateFullWarehouse updates existing warehouse")
    void updateFullWarehouseUpdatesExistingWarehouse() throws SQLException {
        int warehouseId = insertWarehouseDirectly("Before Full Update Warehouse", 700, "before_full_update.xlsx");

        Warehouse warehouse = new Warehouse();

        warehouse.setId(warehouseId);
        warehouse.setName("After Full Update Warehouse");
        warehouse.setRegionName("after_full_update.xlsx");
        warehouse.setPostalCode(701);
        warehouse.setCoord(new Coordinates(50.5, 19.5));
        warehouse.setRegionTable(null);
        warehouse.setPriceList(null);

        boolean updated = warehouseDAO.updateFullWarehouse(warehouse);

        assertTrue(updated);

        BriefWarehouse updatedWarehouse = warehouseDAO.getlBriefWarehouse(warehouseId);

        assertNotNull(updatedWarehouse);
        assertEquals("After Full Update Warehouse", updatedWarehouse.getName());
        assertEquals("after_full_update.xlsx", updatedWarehouse.getRegionName());
        assertEquals(701, updatedWarehouse.getPostalCode());
    }

    @Test
    @DisplayName("deleteFullWarehouse returns false for non-existing warehouse")
    void deleteFullWarehouseReturnsFalseForNonExistingWarehouse() throws SQLException {
        boolean deleted = warehouseDAO.deleteFullWarehouse(-999);

        assertFalse(deleted);
    }

    @Test
    @DisplayName("deleteFullWarehouse soft deletes existing warehouse")
    void deleteFullWarehouseSoftDeletesExistingWarehouse() throws SQLException {
        int warehouseId = insertWarehouseDirectly("Delete Warehouse Test", 800, "delete_warehouse.xlsx");

        boolean deleted = warehouseDAO.deleteFullWarehouse(warehouseId);

        assertTrue(deleted);
        assertNull(warehouseDAO.getlBriefWarehouse(warehouseId));
    }

    private int insertWarehouseDirectly(String name, int storageRegion, String priceListFile) throws SQLException {
        String sql = """
                INSERT INTO Warehouse(storage_region, warehouse_region_name, price_list_file, latitude, longitude, is_active)
                VALUES (?, ?, ?, 48.0, 17.0, true)
                RETURNING warehouse_ID;
                """;

        PreparedStatement stmt = DBConnector.getInstance().getConnection().prepareStatement(sql);

        stmt.setInt(1, storageRegion);
        stmt.setString(2, name);
        stmt.setString(3, priceListFile);

        ResultSet rs = stmt.executeQuery();

        assertTrue(rs.next());

        return rs.getInt("warehouse_ID");
    }
}