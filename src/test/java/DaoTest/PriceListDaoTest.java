package DaoTest;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.shippin.database.DBConnector;
import org.shippin.database.dao.PriceListDAO;
import org.shippin.domain.PriceList;
import org.shippin.domain.PriceListEntry;
import org.shippin.domain.SmallPriceList;
import org.shippin.domain.SmallPriceListEntry;

import static org.junit.jupiter.api.Assertions.*;

public class PriceListDaoTest {

    private static PriceListDAO priceListDAO;

    @BeforeAll
    static void connect() {
        priceListDAO = PriceListDAO.getInstance();
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
    @DisplayName("getInstance returns same instance")
    void getInstanceReturnsSameInstance() {
        PriceListDAO first = PriceListDAO.getInstance();
        PriceListDAO second = PriceListDAO.getInstance();

        assertNotNull(first);
        assertSame(first, second);
    }

    @Test
    @DisplayName("getPriceList returns non-null result")
    void getPriceListReturnsNonNull() throws SQLException {
        int warehouseId = insertWarehouse("PL Test BA");
        int regionId = insertRegion(warehouseId, "BA1");
        insertPriceItem(regionId, 5, 1, 10);

        PriceList list = priceListDAO.getPriceList("PL Test BA", "BA1");

        assertNotNull(list);
        assertEquals(1, list.getEntries().size());
        assertEquals(5f, list.getEntries().get(0).getWeight(), 0.001f);
        assertEquals(1f, list.getEntries().get(0).getVolume(), 0.001f);
        assertEquals(10f, list.getEntries().get(0).getCost(), 0.001f);
        assertEquals("BA1", list.getEntries().get(0).getZone());
    }

    @Test
    @DisplayName("getPriceList returns empty list for non-existing data")
    void getPriceListReturnsEmptyListForNonExistingData() throws SQLException {
        PriceList list = priceListDAO.getPriceList("Missing Warehouse", "NO_REGION");

        assertNotNull(list);
        assertTrue(list.getEntries().isEmpty());
    }

    @Test
    @DisplayName("getFullPriceList returns non-null result")
    void getFullPriceListReturnsNonNull() throws SQLException {
        int warehouseId = insertWarehouse("PL Test TT");
        int firstRegionId = insertRegion(warehouseId, "TT1");
        int secondRegionId = insertRegion(warehouseId, "TT2");

        insertPriceItem(firstRegionId, 10, 2, 20);
        insertPriceItem(secondRegionId, 15, 3, 30);

        PriceList list = priceListDAO.getFullPriceList("PL Test TT");

        assertNotNull(list);
        assertEquals(2, list.getEntries().size());
        assertTrue(list.getEntries().stream().anyMatch(entry -> entry.getZone().equals("TT1")));
        assertTrue(list.getEntries().stream().anyMatch(entry -> entry.getZone().equals("TT2")));
    }

    @Test
    @DisplayName("getFullPriceList returns empty list for non-existing warehouse")
    void getFullPriceListReturnsEmptyListForNonExistingWarehouse() throws SQLException {
        PriceList list = priceListDAO.getFullPriceList("Missing Warehouse");

        assertNotNull(list);
        assertTrue(list.getEntries().isEmpty());
    }

    @Test
    @DisplayName("getPriceListEntryByWeight returns item")
    void getPriceListEntryByWeightReturnsItem() throws SQLException {
        int warehouseId = insertWarehouse("PL Test KE");
        int regionId = insertRegion(warehouseId, "KE1");
        insertPriceItem(regionId, 15, 3, 30);

        PriceListEntry item = priceListDAO.getPriceListEntryByWeight("PL Test KE", "KE1", 15);

        assertNotNull(item);
        assertEquals(15f, item.getWeight(), 0.001f);
        assertEquals(3f, item.getVolume(), 0.001f);
        assertEquals(30f, item.getCost(), 0.001f);
        assertEquals("KE1", item.getZone());
    }

    @Test
    @DisplayName("getPriceListEntryByWeight returns null for non-existing item")
    void getPriceListEntryByWeightReturnsNullForNonExistingItem() throws SQLException {
        PriceListEntry item = priceListDAO.getPriceListEntryByWeight("Missing Warehouse", "NO_REGION", 999);

        assertNull(item);
    }

    @Test
    @DisplayName("getPriceListEntryByVolume returns item")
    void getPriceListEntryByVolumeReturnsItem() throws SQLException {
        int warehouseId = insertWarehouse("PL Test ZA");
        int regionId = insertRegion(warehouseId, "ZA1");
        insertPriceItem(regionId, 20, 4, 40);

        PriceListEntry item = priceListDAO.getPriceListEntryByVolume("PL Test ZA", "ZA1", 4);

        assertNotNull(item);
        assertEquals(20f, item.getWeight(), 0.001f);
        assertEquals(4f, item.getVolume(), 0.001f);
        assertEquals(40f, item.getCost(), 0.001f);
        assertEquals("ZA1", item.getZone());
    }

    @Test
    @DisplayName("getPriceListEntryByVolume returns null for non-existing item")
    void getPriceListEntryByVolumeReturnsNullForNonExistingItem() throws SQLException {
        PriceListEntry item = priceListDAO.getPriceListEntryByVolume("Missing Warehouse", "NO_REGION", 999);

        assertNull(item);
    }

    @Test
    @DisplayName("insertPriceListEntry inserts item")
    void insertPriceListEntryInsertsItem() throws SQLException {
        int warehouseId = insertWarehouse("PL Test NR");
        insertRegion(warehouseId, "NR1");

        PriceListEntry item = new PriceListEntry(0, 25, 5, 50, "NR1");

        int itemId = priceListDAO.insertPriceListEntry(item, warehouseId);

        assertTrue(itemId > 0);

        PriceListEntry inserted = priceListDAO.getPriceListEntryByWeight("PL Test NR", "NR1", 25);

        assertNotNull(inserted);
        assertEquals(itemId, inserted.getId());
        assertEquals(25f, inserted.getWeight(), 0.001f);
        assertEquals(5f, inserted.getVolume(), 0.001f);
        assertEquals(50f, inserted.getCost(), 0.001f);
    }

    @Test
    @DisplayName("insertPriceListEntry throws when region does not exist")
    void insertPriceListEntryThrowsWhenRegionDoesNotExist() throws SQLException {
        int warehouseId = insertWarehouse("PL Missing Region Test");

        PriceListEntry item = new PriceListEntry(0, 25, 5, 50, "MISSING_REGION");

        SQLException exception = assertThrows(
                SQLException.class,
                () -> priceListDAO.insertPriceListEntry(item, warehouseId)
        );

        assertTrue(exception.getMessage().contains("region not found"));
    }

    @Test
    @DisplayName("insertPriceList inserts full price list")
    void insertPriceListInsertsFullPriceList() throws SQLException {
        int warehouseId = insertWarehouse("PL Full Insert Test");

        PriceList priceList = new PriceList();
        priceList.setEntries(List.of(
                new PriceListEntry(0, 5, 1, 10, "FULL_PL_1"),
                new PriceListEntry(0, 10, 2, 20, "FULL_PL_2")
        ));

        assertDoesNotThrow(() -> priceListDAO.insertPriceList(priceList, warehouseId));

        PriceList inserted = priceListDAO.getFullPriceList("PL Full Insert Test");

        assertNotNull(inserted);
        assertEquals(2, inserted.getEntries().size());
        assertTrue(inserted.getEntries().stream().anyMatch(entry -> entry.getZone().equals("FULL_PL_1")));
        assertTrue(inserted.getEntries().stream().anyMatch(entry -> entry.getZone().equals("FULL_PL_2")));
    }

    @Test
    @DisplayName("deletePriceListByWarehouseID returns false for warehouse without price list")
    void deletePriceListByWarehouseIdReturnsFalseForWarehouseWithoutPriceList() throws SQLException {
        int warehouseId = insertWarehouse("PL Delete Empty Warehouse");

        boolean deleted = priceListDAO.deletePriceListByWarehouseID(warehouseId);

        assertFalse(deleted);
    }

    @Test
    @DisplayName("deletePriceListByWarehouseID deletes price list")
    void deletePriceListByWarehouseIdDeletesPriceList() throws SQLException {
        int warehouseId = insertWarehouse("PL Delete Warehouse");
        int regionId = insertRegion(warehouseId, "DEL1");
        insertPriceItem(regionId, 5, 1, 10);

        boolean deleted = priceListDAO.deletePriceListByWarehouseID(warehouseId);

        assertTrue(deleted);

        PriceList list = priceListDAO.getFullPriceList("PL Delete Warehouse");

        assertNotNull(list);
        assertTrue(list.getEntries().isEmpty());
    }

    @Test
    @DisplayName("deletePriceListByWarehouseAndRegionID returns false for missing region")
    void deletePriceListByWarehouseAndRegionIdReturnsFalseForMissingRegion() throws SQLException {
        int warehouseId = insertWarehouse("PL Delete Missing Region Warehouse");

        boolean deleted = priceListDAO.deletePriceListByWarehouseAndRegionID(warehouseId, -999);

        assertFalse(deleted);
    }

    @Test
    @DisplayName("deletePriceListByWarehouseAndRegionID deletes selected region price list")
    void deletePriceListByWarehouseAndRegionIdDeletesSelectedRegionPriceList() throws SQLException {
        int warehouseId = insertWarehouse("PL Delete Region Warehouse");
        int firstRegionId = insertRegion(warehouseId, "DEL_REGION_1");
        int secondRegionId = insertRegion(warehouseId, "DEL_REGION_2");

        insertPriceItem(firstRegionId, 5, 1, 10);
        insertPriceItem(secondRegionId, 10, 2, 20);

        boolean deleted = priceListDAO.deletePriceListByWarehouseAndRegionID(warehouseId, firstRegionId);

        assertTrue(deleted);

        PriceList list = priceListDAO.getFullPriceList("PL Delete Region Warehouse");

        assertNotNull(list);
        assertEquals(1, list.getEntries().size());
        assertEquals("DEL_REGION_2", list.getEntries().get(0).getZone());
    }

    @Test
    @DisplayName("insertSmallPriceListEntry inserts item")
    void insertSmallPriceListEntryInsertsItem() throws SQLException {
        SmallPriceListEntry entry = new SmallPriceListEntry(0, 1234, 3.29f);

        int entryId = priceListDAO.insertSmallPriceListEntry(entry);

        assertTrue(entryId > 0);

        PreparedStatement stmt = DBConnector.getInstance().getConnection().prepareStatement("""
            SELECT sp_price_list_ID, weight_sp, cost_sp
            FROM Sp_price_list
            WHERE sp_price_list_ID = ?
        """);

        stmt.setInt(1, entryId);

        ResultSet rs = stmt.executeQuery();

        assertTrue(rs.next());
        assertEquals(entryId, rs.getInt("sp_price_list_ID"));
        assertEquals(1234f, rs.getFloat("weight_sp"), 0.001f);
        assertEquals(3.29f, rs.getFloat("cost_sp"), 0.001f);
    }

    @Test
    @DisplayName("insertSmallPriceList inserts multiple items")
    void insertSmallPriceListInsertsMultipleItems() throws SQLException {
        SmallPriceList list = new SmallPriceList();
        list.setEntries(List.of(
                new SmallPriceListEntry(0, 2, 4.29f),
                new SmallPriceListEntry(0, 3, 5.29f)
        ));

        assertDoesNotThrow(() -> priceListDAO.insertSmallPriceList(list));

        SmallPriceList inserted = priceListDAO.getSmallPriceList();

        assertNotNull(inserted);
        assertTrue(inserted.getEntries().stream().anyMatch(entry -> entry.getWeight() == 2f));
        assertTrue(inserted.getEntries().stream().anyMatch(entry -> entry.getWeight() == 3f));
    }

    @Test
    @DisplayName("getSmallPriceList returns non-null list")
    void getSmallPriceListReturnsNonNullList() throws SQLException {
        insertSmallPriceItemDirectly(7, 9.99f);

        SmallPriceList list = priceListDAO.getSmallPriceList();

        assertNotNull(list);
        assertFalse(list.getEntries().isEmpty());
        assertTrue(list.getEntries().stream().anyMatch(entry -> entry.getWeight() == 7f));
    }

    @Test
    @DisplayName("getSmallPriceListEntryByWeight returns null for missing weight")
    void getSmallPriceListEntryByWeightReturnsNullForMissingWeight() throws SQLException {
        SmallPriceListEntry entry = priceListDAO.getSmallPriceListEntryByWeight(9999);

        assertNull(entry);
    }

    @Test
    @DisplayName("deleteSmallPriceListEntry returns false for missing item")
    void deleteSmallPriceListEntryReturnsFalseForMissingItem() throws SQLException {
        boolean deleted = priceListDAO.deleteSmallPriceListEntry(-999);

        assertFalse(deleted);
    }

    @Test
    @DisplayName("deleteSmallPriceListEntry deletes item")
    void deleteSmallPriceListEntryDeletesItem() throws SQLException {
        int entryId = insertSmallPriceItemDirectly(8, 10.99f);

        boolean deleted = priceListDAO.deleteSmallPriceListEntry(entryId);

        assertTrue(deleted);
        assertNull(priceListDAO.getSmallPriceListEntryByWeight(8));
    }

    @Test
    @DisplayName("deleteSmallPriceList executes")
    void deleteSmallPriceListExecutes() throws SQLException {
        insertSmallPriceItemDirectly(11, 15.99f);

        boolean deleted = priceListDAO.deleteSmallPriceList();

        assertTrue(deleted);

        SmallPriceList list = priceListDAO.getSmallPriceList();

        assertNotNull(list);
        assertTrue(list.getEntries().isEmpty());
    }

    private int insertWarehouse(String name) throws SQLException {
        PreparedStatement stmt = DBConnector.getInstance().getConnection().prepareStatement("""
            INSERT INTO Warehouse(storage_region, warehouse_region_name, price_list_file)
            VALUES (100, ?, 'test.xlsx')
            RETURNING warehouse_ID
        """);

        stmt.setString(1, name);

        ResultSet rs = stmt.executeQuery();
        rs.next();

        return rs.getInt("warehouse_ID");
    }

    private int insertRegion(int warehouseId, String regionName) throws SQLException {
        PreparedStatement stmt = DBConnector.getInstance().getConnection().prepareStatement("""
            INSERT INTO Region(warehouse_ID, region_name)
            VALUES (?, ?)
            RETURNING region_ID
        """);

        stmt.setInt(1, warehouseId);
        stmt.setString(2, regionName);

        ResultSet rs = stmt.executeQuery();
        rs.next();

        return rs.getInt("region_ID");
    }

    private void insertPriceItem(int regionId, float weight, float volume, float cost) throws SQLException {
        PreparedStatement stmt = DBConnector.getInstance().getConnection().prepareStatement("""
            INSERT INTO Parameter_list(region_ID, weight, volume, cost)
            VALUES (?, ?, ?, ?)
        """);

        stmt.setInt(1, regionId);
        stmt.setFloat(2, weight);
        stmt.setFloat(3, volume);
        stmt.setFloat(4, cost);

        stmt.executeUpdate();
    }

    private int insertSmallPriceItemDirectly(float weight, float cost) throws SQLException {
        PreparedStatement stmt = DBConnector.getInstance().getConnection().prepareStatement("""
            INSERT INTO Sp_price_list(weight_sp, cost_sp)
            VALUES (?, ?)
            RETURNING sp_price_list_ID
        """);

        stmt.setFloat(1, weight);
        stmt.setFloat(2, cost);

        ResultSet rs = stmt.executeQuery();
        rs.next();

        return rs.getInt("sp_price_list_ID");
    }
}