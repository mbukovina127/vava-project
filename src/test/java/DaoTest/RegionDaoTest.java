import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.shippin.database.Config;
import org.shippin.database.DBConnector;
import org.shippin.database.dao.RegionDAO;
import org.shippin.domain.RegionTable;
import org.shippin.domain.RegionTableEntry;
import org.shippin.domain.Warehouse;
import org.shippin.util.Range;


public class RegionDaoTest {

    private static DBConnector dbc;
    private static RegionDAO regionDAO;

    @BeforeAll
    static void connect() throws SQLException {
        dbc = new DBConnector(new Config());
        dbc.connect();
        regionDAO = new RegionDAO(dbc.getConnection());
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

    private int insertWarehouse(String name) throws SQLException {
        PreparedStatement stmt = dbc.getConnection().prepareStatement("""
            INSERT INTO Warehouse(storage_region, warehouse_region_name, price_list_file)
            VALUES (100, ?, 'test.xlsx')
            RETURNING warehouse_ID
        """);
        stmt.setString(1, name);

        ResultSet rs = stmt.executeQuery();
        rs.next();
        return rs.getInt("warehouse_ID");
    }

    @Test
    @DisplayName("insertRegion returns generated region id")
    void insertRegionReturnsGeneratedId() throws SQLException {
        int warehouseId = insertWarehouse("Region Test BA");

        int regionId = regionDAO.insertRegion("BA1", warehouseId);

        assertTrue(regionId > 0);
    }

    @Test
    @DisplayName("getRegionsForWarehouse returns non-null table")
    void getRegionsForWarehouseReturnsNonNull() throws SQLException {
        int warehouseId = insertWarehouse("Region Test TT");
        regionDAO.insertRegion("TT1", warehouseId);

        RegionTable table = regionDAO.getRegionsForWarehouse("Region Test TT");

        assertNotNull(table);
    }

    @Test
    @DisplayName("getRegion returns null for non-existing region")
    void getRegionReturnsNullForMissingRegion() throws SQLException {
        int warehouseId = insertWarehouse("Region Test KE");
        regionDAO.insertRegion("KE1", warehouseId);

        RegionTableEntry entry = regionDAO.getRegion("Region Test KE", "DOES_NOT_EXIST");

        assertNull(entry);
    }

    @Test
    @DisplayName("getRegionByPsc returns null for unknown psc")
    void getRegionByPscReturnsNullForUnknownPsc() throws SQLException {
        insertWarehouse("Region Test NR");

        RegionTableEntry entry = regionDAO.getRegionByPsc("Region Test NR", 99999);

        assertNull(entry);
    }

    @Test
    @DisplayName("insertPSCRange executes without exception")
    void insertPSCRangeExecutes() throws SQLException {
        int warehouseId = insertWarehouse("Region Test ZA");
        int regionId = regionDAO.insertRegion("ZA1", warehouseId);

        assertDoesNotThrow(() ->
            regionDAO.insertPSCRange(regionId, 10000, 19999)
        );
    }

    @Test
    @DisplayName("getRegionByPsc returns region for existing PSC")
    void getRegionByPscReturnsExistingRegion() throws SQLException {
        int warehouseId = insertWarehouse("Region PSC Test");
        int regionId = regionDAO.insertRegion("PSC1", warehouseId);

        regionDAO.insertPSCRange(regionId, 10000, 19999);

        RegionTableEntry entry = regionDAO.getRegionByPsc("Region PSC Test", 15000);

        assertNotNull(entry);
    }

    @Test
    @DisplayName("insertFullRegion executes or exposes bug")
    void insertFullRegionExecutes() throws SQLException {
        int warehouseId = insertWarehouse("Full Region Warehouse");

        Warehouse warehouse = new Warehouse();
        warehouse.setId(warehouseId);
        warehouse.setName("Full Region Warehouse");
        warehouse.setRegionName("full_region_test.xlsx");

        ArrayList<Range> ranges = new ArrayList<>();
        ranges.add(new Range(20000, 29999));

        RegionTableEntry entry = new RegionTableEntry(0, ranges, "FULL1");

        assertDoesNotThrow(() ->
                regionDAO.insertFullRegion(entry, warehouse)
        );
    }
}