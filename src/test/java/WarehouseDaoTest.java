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
import org.shippin.database.Config;
import org.shippin.database.DBConnector;
import org.shippin.database.dao.WarehouseDAO;
import org.shippin.domain.BriefWarehouse;
import org.shippin.domain.Warehouse;

public class WarehouseDaoTest {

    private static DBConnector dbc;
    private static WarehouseDAO warehouseDAO;

    @BeforeAll
    static void connect() throws SQLException {
        dbc = new DBConnector(new Config());
        dbc.connect();
        warehouseDAO = new WarehouseDAO(dbc.getConnection());
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

    private int insertWarehouseDirectly(String name, int storageRegion, String priceListFile) throws SQLException {
        String sql = """
                INSERT INTO Warehouse(storage_region, warehouse_region_name, price_list_file)
                VALUES (?, ?, ?)
                RETURNING warehouse_ID;
                """;

        PreparedStatement stmt = dbc.getConnection().prepareStatement(sql);
        stmt.setInt(1, storageRegion);
        stmt.setString(2, name);
        stmt.setString(3, priceListFile);

        ResultSet rs = stmt.executeQuery();
        assertTrue(rs.next());

        return rs.getInt("warehouse_ID");
    }

    @Test
    @DisplayName("getById returns warehouse for existing ID")
    void getByIdReturnsWarehouseForExistingId() throws SQLException {
        int warehouseId = insertWarehouseDirectly("Warehouse Test BA", 100, "ba_prices.xlsx");

        Warehouse warehouse = warehouseDAO.getById(warehouseId);

        assertNotNull(warehouse);
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
        insertWarehouseDirectly("Warehouse Test TT", 200, "tt_prices.xlsx");

        List<BriefWarehouse> warehouses = warehouseDAO.getAllBriefWarehouses();

        assertNotNull(warehouses);
        assertFalse(warehouses.isEmpty());
    }

    @Test
    @DisplayName("getlBriefWarehouse returns warehouse for existing ID")
    void getlBriefWarehouseReturnsWarehouseForExistingId() throws SQLException {
        int warehouseId = insertWarehouseDirectly("Warehouse Test KE", 300, "ke_prices.xlsx");

        BriefWarehouse briefWarehouse = warehouseDAO.getlBriefWarehouse(warehouseId);

        assertNotNull(briefWarehouse);
    }

    @Test
    @DisplayName("getlBriefWarehouse returns null for non-existing ID")
    void getlBriefWarehouseReturnsNullForNonExistingId() throws SQLException {
        BriefWarehouse briefWarehouse = warehouseDAO.getlBriefWarehouse(-999);

        assertNull(briefWarehouse);
    }

    @Test
    @DisplayName("upsertWarehouse stores warehouse in database")
    void upsertWarehouseStoresWarehouseInDatabase() throws SQLException {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(99901);
        warehouse.setName("Warehouse Upsert Test");
        warehouse.setRegionName("upsert_prices.xlsx");

        warehouseDAO.upsertWarehouse(warehouse);

        PreparedStatement stmt = dbc.getConnection().prepareStatement("""
                SELECT warehouse_ID, warehouse_region_name, price_list_file
                FROM Warehouse
                WHERE warehouse_ID = ?;
                """);
        stmt.setInt(1, 99901);

        ResultSet rs = stmt.executeQuery();

        assertTrue(rs.next());
        assertEquals(99901, rs.getInt("warehouse_ID"));
        assertEquals("Warehouse Upsert Test", rs.getString("warehouse_region_name"));
        assertEquals("upsert_prices.xlsx", rs.getString("price_list_file"));
    }

    @Test
    @DisplayName("insertFullWarehouse executes or exposes bug")
    void insertFullWarehouseExecutes() {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(99902);
        warehouse.setName("Full Warehouse Test");
        warehouse.setRegionName("full_test.xlsx");
        warehouse.setRegionTable(null);
        warehouse.setPriceList(null);

        assertDoesNotThrow(() ->
                warehouseDAO.insertFullWarehouse(warehouse)
        );
    }
}