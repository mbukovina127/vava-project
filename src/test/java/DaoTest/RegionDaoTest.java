package DaoTest;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.shippin.database.DBConnector;
import org.shippin.database.dao.RegionDAO;
import org.shippin.domain.RegionTable;
import org.shippin.domain.RegionTableEntry;
import org.shippin.domain.Warehouse;
import org.shippin.util.Range;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegionDaoTest {

    private static RegionDAO regionDAO;

    @BeforeAll
    static void connect() {
        regionDAO = RegionDAO.getInstance();
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
    }

    @Test
    @DisplayName("getInstance returns same instance")
    void getInstanceReturnsSameInstance() {
        RegionDAO first = RegionDAO.getInstance();
        RegionDAO second = RegionDAO.getInstance();

        assertNotNull(first);
        assertSame(first, second);
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
    @DisplayName("getRegionsForWarehouse returns inserted regions with ranges")
    void getRegionsForWarehouseReturnsInsertedRegionsWithRanges() throws SQLException {
        int warehouseId = insertWarehouse("Region Table Test");
        int firstRegionId = regionDAO.insertRegion("RT1", warehouseId);
        int secondRegionId = regionDAO.insertRegion("RT2", warehouseId);

        regionDAO.insertPSCRange(firstRegionId, 10000, 19999);
        regionDAO.insertPSCRange(secondRegionId, 20000, 29999);

        RegionTable table = regionDAO.getRegionsForWarehouse("Region Table Test");

        assertNotNull(table);
        assertEquals(2, table.getEntries().size());
        assertTrue(table.getEntries().stream().anyMatch(entry -> entry.getRegionCode().equals("RT1")));
        assertTrue(table.getEntries().stream().anyMatch(entry -> entry.getRegionCode().equals("RT2")));
    }

    @Test
    @DisplayName("getRegionsForWarehouse returns empty table for missing warehouse")
    void getRegionsForWarehouseReturnsEmptyTableForMissingWarehouse() throws SQLException {
        RegionTable table = regionDAO.getRegionsForWarehouse("Missing Warehouse");

        assertNotNull(table);
        assertTrue(table.getEntries().isEmpty());
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
    @DisplayName("getRegion returns inserted region with range")
    void getRegionReturnsInsertedRegionWithRange() throws SQLException {
        int warehouseId = insertWarehouse("Region Get Test");
        int regionId = regionDAO.insertRegion("GET1", warehouseId);

        regionDAO.insertPSCRange(regionId, 30000, 39999);

        RegionTableEntry entry = regionDAO.getRegion("Region Get Test", "GET1");

        assertNotNull(entry);
        assertEquals("GET1", entry.getRegionCode());
        assertEquals(1, entry.getRanges().size());
        assertEquals(30000, entry.getRanges().get(0).getMin());
        assertEquals(39999, entry.getRanges().get(0).getMax());
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
    @DisplayName("insertPSCRange reuses existing postal code range")
    void insertPSCRangeReusesExistingPostalCodeRange() throws SQLException {
        int warehouseId = insertWarehouse("Region Reuse PSC Test");
        int firstRegionId = regionDAO.insertRegion("PSC_REUSE_1", warehouseId);
        int secondRegionId = regionDAO.insertRegion("PSC_REUSE_2", warehouseId);

        regionDAO.insertPSCRange(firstRegionId, 40000, 49999);

        assertDoesNotThrow(() ->
                regionDAO.insertPSCRange(secondRegionId, 40000, 49999)
        );

        RegionTableEntry firstEntry = regionDAO.getRegion("Region Reuse PSC Test", "PSC_REUSE_1");
        RegionTableEntry secondEntry = regionDAO.getRegion("Region Reuse PSC Test", "PSC_REUSE_2");

        assertNotNull(firstEntry);
        assertNotNull(secondEntry);
    }

    @Test
    @DisplayName("getRegionByPsc returns region for existing PSC")
    void getRegionByPscReturnsExistingRegion() throws SQLException {
        int warehouseId = insertWarehouse("Region PSC Test");
        int regionId = regionDAO.insertRegion("PSC1", warehouseId);

        regionDAO.insertPSCRange(regionId, 10000, 19999);

        RegionTableEntry entry = regionDAO.getRegionByPsc("Region PSC Test", 15000);

        assertNotNull(entry);
        assertEquals("PSC1", entry.getRegionCode());
        assertEquals(1, entry.getRanges().size());
    }

    @Test
    @DisplayName("deleteRegion returns false for missing region")
    void deleteRegionReturnsFalseForMissingRegion() throws SQLException {
        int warehouseId = insertWarehouse("Region Delete Missing Test");

        boolean deleted = regionDAO.deleteRegion(warehouseId, "MISSING_REGION");

        assertFalse(deleted);
    }

    @Test
    @DisplayName("deleteRegion deletes existing region")
    void deleteRegionDeletesExistingRegion() throws SQLException {
        int warehouseId = insertWarehouse("Region Delete Test");
        int regionId = regionDAO.insertRegion("DEL1", warehouseId);

        regionDAO.insertPSCRange(regionId, 50000, 59999);

        boolean deleted = regionDAO.deleteRegion(warehouseId, "DEL1");

        assertTrue(deleted);
        assertNull(regionDAO.getRegion("Region Delete Test", "DEL1"));
    }

    @Test
    @DisplayName("deleteAllRegions returns false for warehouse without regions")
    void deleteAllRegionsReturnsFalseForWarehouseWithoutRegions() throws SQLException {
        int warehouseId = insertWarehouse("Region Delete All Empty Test");

        boolean deleted = regionDAO.deleteAllRegions(warehouseId);

        assertFalse(deleted);
    }

    @Test
    @DisplayName("deleteAllRegions deletes all regions for warehouse")
    void deleteAllRegionsDeletesAllRegionsForWarehouse() throws SQLException {
        int warehouseId = insertWarehouse("Region Delete All Test");
        int firstRegionId = regionDAO.insertRegion("DEL_ALL_1", warehouseId);
        int secondRegionId = regionDAO.insertRegion("DEL_ALL_2", warehouseId);

        regionDAO.insertPSCRange(firstRegionId, 60000, 69999);
        regionDAO.insertPSCRange(secondRegionId, 70000, 79999);

        boolean deleted = regionDAO.deleteAllRegions(warehouseId);

        assertTrue(deleted);

        RegionTable table = regionDAO.getRegionsForWarehouse("Region Delete All Test");

        assertNotNull(table);
        assertTrue(table.getEntries().isEmpty());
    }

    @Test
    @DisplayName("deleteFullRegionTable returns false for warehouse without ranges")
    void deleteFullRegionTableReturnsFalseForWarehouseWithoutRanges() throws SQLException {
        int warehouseId = insertWarehouse("Region Full Delete Empty Test");
        regionDAO.insertRegion("EMPTY1", warehouseId);

        boolean deleted = regionDAO.deleteFullRegionTable(warehouseId);

        assertFalse(deleted);
    }

    @Test
    @DisplayName("deleteFullRegionTable deletes postal code links")
    void deleteFullRegionTableDeletesPostalCodeLinks() throws SQLException {
        int warehouseId = insertWarehouse("Region Full Delete Test");
        int regionId = regionDAO.insertRegion("FULL_DEL1", warehouseId);

        regionDAO.insertPSCRange(regionId, 80000, 89999);

        assertNotNull(regionDAO.getRegion("Region Full Delete Test", "FULL_DEL1"));

        boolean deleted = regionDAO.deleteFullRegionTable(warehouseId);

        assertTrue(deleted);
        assertNull(regionDAO.getRegion("Region Full Delete Test", "FULL_DEL1"));
    }

    @Test
    @DisplayName("insertFullRegion executes without exception")
    void insertFullRegionExecutes() throws SQLException {
        String warehouseName = "Full Region Warehouse " + System.currentTimeMillis();
        int warehouseId = insertWarehouse(warehouseName);

        try {
            Warehouse warehouse = new Warehouse();
            warehouse.setId(warehouseId);
            warehouse.setName(warehouseName);
            warehouse.setRegionName("full_region_test.xlsx");

            ArrayList<Range> ranges = new ArrayList<>();
            ranges.add(new Range(20000, 29999));

            RegionTableEntry entry = new RegionTableEntry(0, ranges, "FULL1");

            assertDoesNotThrow(() ->
                    regionDAO.insertFullRegion(entry, warehouse)
            );

            RegionTableEntry insertedEntry = regionDAO.getRegion(warehouseName, "FULL1");

            assertNotNull(insertedEntry);
            assertEquals("FULL1", insertedEntry.getRegionCode());
            assertEquals(1, insertedEntry.getRanges().size());
        } finally {
            deleteWarehouseByIdAndCommit(warehouseId);
        }
    }

    @Test
    @DisplayName("insertFullRegionTable inserts all regions")
    void insertFullRegionTableInsertsAllRegions() throws SQLException {
        String warehouseName = "Full Region Table Warehouse " + System.currentTimeMillis();
        int warehouseId = insertWarehouse(warehouseName);

        try {
            Warehouse warehouse = new Warehouse();
            warehouse.setId(warehouseId);
            warehouse.setName(warehouseName);
            warehouse.setRegionName("full_region_table_test.xlsx");

            ArrayList<Range> firstRanges = new ArrayList<>();
            firstRanges.add(new Range(11000, 11999));

            ArrayList<Range> secondRanges = new ArrayList<>();
            secondRanges.add(new Range(12000, 12999));
            secondRanges.add(new Range(13000, 13999));

            RegionTableEntry firstEntry = new RegionTableEntry(0, firstRanges, "FULL_TABLE_1");
            RegionTableEntry secondEntry = new RegionTableEntry(0, secondRanges, "FULL_TABLE_2");

            RegionTable table = new RegionTable();
            table.setEntries(List.of(firstEntry, secondEntry));

            assertDoesNotThrow(() ->
                    regionDAO.insertFullRegionTable(table, warehouse)
            );

            RegionTable insertedTable = regionDAO.getRegionsForWarehouse(warehouseName);

            assertNotNull(insertedTable);
            assertEquals(2, insertedTable.getEntries().size());
            assertTrue(insertedTable.getEntries().stream().anyMatch(entry -> entry.getRegionCode().equals("FULL_TABLE_1")));
            assertTrue(insertedTable.getEntries().stream().anyMatch(entry -> entry.getRegionCode().equals("FULL_TABLE_2")));
        } finally {
            deleteWarehouseByIdAndCommit(warehouseId);
        }
    }

    private int insertWarehouse(String name) throws SQLException {
        PreparedStatement stmt = DBConnector.getInstance().getConnection().prepareStatement("""
            INSERT INTO Warehouse(storage_region, warehouse_region_name, price_list_file)
            VALUES (100, ?, 'test.xlsx')
            RETURNING warehouse_ID
        """);

        stmt.setString(1, name);

        ResultSet rs = stmt.executeQuery();

        assertTrue(rs.next());

        return rs.getInt("warehouse_ID");
    }

    private void deleteWarehouseByIdAndCommit(int warehouseId) throws SQLException {
        var connection = DBConnector.getInstance().getConnection();

        PreparedStatement stmt = connection.prepareStatement("""
            DELETE FROM Warehouse
            WHERE warehouse_ID = ?
        """);

        stmt.setInt(1, warehouseId);
        stmt.executeUpdate();

        connection.commit();
        connection.setAutoCommit(false);
    }
}