package ServiceTest;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shippin.database.DBConnector;
import org.shippin.database.dao.BaseDAO;
import org.shippin.database.dao.PriceListDAO;
import org.shippin.database.dao.RegionDAO;
import org.shippin.domain.BriefWarehouse;
import org.shippin.domain.Coordinates;
import org.shippin.domain.SmallPriceList;
import org.shippin.domain.SmallPriceListEntry;
import org.shippin.domain.Warehouse;
import org.shippin.domain.formatted.PriceListFormatted;
import org.shippin.domain.formatted.PriceListRow;
import org.shippin.domain.formatted.RegionTableFormatted;
import org.shippin.domain.formatted.RegionTableRow;
import org.shippin.domain.formatted.SmallPriceListFormatted;
import org.shippin.domain.formatted.SmallPriceListRow;
import org.shippin.domain.formatted.WarehouseFormatted;
import org.shippin.exception.IncompatibleTablesException;
import org.shippin.services.MapService;
import org.shippin.services.WarehouseService;
import org.shippin.util.Range;

public class WarehouseServiceTest {

    private static final String TEST_PREFIX = "WS_TEST_";
    private static final double FAKE_LATITUDE = 48.1234;
    private static final double FAKE_LONGITUDE = 17.5678;

    private final WarehouseService warehouseService = new WarehouseService();

    private MapService originalMapService;
    private SmallPriceList originalSmallPriceList;
    private boolean smallPriceListChanged;

    @BeforeEach
    void setUp() throws Exception {
        Connection connection = DBConnector.getInstance().getConnection();
        connection.setAutoCommit(true);
        cleanupTestWarehouses();

        originalSmallPriceList = warehouseService.getSmallPriceList();
        smallPriceListChanged = false;

        originalMapService = getMapServiceSingleton();
        setMapServiceSingleton(new FakeMapService());

        connection.setAutoCommit(false);
    }

    @AfterEach
    void tearDown() throws Exception {
        setMapServiceSingleton(originalMapService);

        Connection connection = DBConnector.getInstance().getConnection();
        if (!connection.getAutoCommit()) {
            connection.rollback();
        }
        connection.setAutoCommit(true);

        if (smallPriceListChanged) {
            warehouseService.setSmallPriceList(originalSmallPriceList);
        }

        cleanupTestWarehouses();
    }

    @Test
    void constructorInitializesDaoDependencies() {
        WarehouseService service = new WarehouseService();

        assertNotNull(service.getWarehouseDao());
        assertNotNull(service.getPriceListDao());
        assertNotNull(service.getRegionDao());
    }

    @Test
    void getInstanceReturnsSameSingletonInstance() throws Exception {
        Field instanceField = WarehouseService.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        Object originalInstance = instanceField.get(null);

        try {
            instanceField.set(null, null);

            WarehouseService first = WarehouseService.getInstance();
            WarehouseService second = WarehouseService.getInstance();

            assertNotNull(first);
            assertSame(first, second);
        } finally {
            instanceField.set(null, originalInstance);
        }
    }

    @Test
    void getBriefWarehousesReturnsInsertedWarehouse() throws SQLException {
        int warehouseId = insertWarehouseDirectly("WS_TEST_BRIEF", "ws-test-brief.xlsx", 91701);

        List<BriefWarehouse> result = warehouseService.getBriefWarehouses();

        assertNotNull(result);
        assertTrue(result.stream().anyMatch(warehouse -> warehouse.getId() == warehouseId));
    }

    @Test
    void getWarehouseReturnsWarehouseFromDao() throws SQLException {
        int warehouseId = insertWarehouseDirectly("WS_TEST_GET", "ws-test-get.xlsx", 91701);

        Warehouse result = warehouseService.getWarehouse(
                new BriefWarehouse(warehouseId, "WS_TEST_GET", "ws-test-get.xlsx")
        );

        assertNotNull(result);
        assertEquals(warehouseId, result.getId());
        assertEquals("WS_TEST_GET", result.getName());
        assertEquals("ws-test-get.xlsx", result.getRegionName());
        assertEquals(91701, result.getPostalCode());
    }

    @Test
    void getWarehouseFormattedConvertsWarehouseFromDao() throws SQLException {
        int warehouseId = insertWarehouseDirectly("WS_TEST_FORMATTED", "ws-test-formatted.xlsx", 91701);

        WarehouseFormatted result = warehouseService.getWarehouseFormatted(
                new BriefWarehouse(warehouseId, "WS_TEST_FORMATTED", "ws-test-formatted.xlsx")
        );

        assertNotNull(result);
        assertEquals("WS_TEST_FORMATTED", result.getName());
        assertEquals("ws-test-formatted.xlsx", result.getTitle());
        assertNotNull(result.getPriceList());
        assertNotNull(result.getRegionTable());
    }

    @Test
    void updateWarehouseUpdatesCoreDataAndCoordinates() throws Exception {
        int warehouseId = insertWarehouseDirectly("WS_TEST_UPDATE_OLD", "ws-test-update-old.xlsx", 91701);

        warehouseService.updateWarehouse(
                new BriefWarehouse(warehouseId, "WS_TEST_UPDATE_OLD", "ws-test-update-old.xlsx"),
                "WS_TEST_UPDATE_NEW",
                "ws-test-update-new.xlsx",
                94901
        );

        PreparedStatement stmt = DBConnector.getInstance().getConnection().prepareStatement("""
                SELECT warehouse_region_name, price_list_file, storage_region, latitude, longitude
                FROM Warehouse
                WHERE warehouse_ID = ?;
                """);
        stmt.setInt(1, warehouseId);
        ResultSet rs = stmt.executeQuery();

        assertTrue(rs.next());
        assertEquals("WS_TEST_UPDATE_NEW", rs.getString("warehouse_region_name"));
        assertEquals("ws-test-update-new.xlsx", rs.getString("price_list_file"));
        assertEquals(94901, rs.getInt("storage_region"));
        assertEquals(FAKE_LATITUDE, rs.getDouble("latitude"), 0.0001);
        assertEquals(FAKE_LONGITUDE, rs.getDouble("longitude"), 0.0001);
    }

    @Test
    void addWarehouseStoresWarehouseWhenTablesAreCompatible() throws Exception {
        warehouseService.addWarehouse(
                "WS_TEST_ADD",
                "ws-test-add.xlsx",
                91701,
                createCompatiblePriceListFormatted(),
                createCompatibleRegionTableFormatted()
        );

        PreparedStatement stmt = DBConnector.getInstance().getConnection().prepareStatement("""
                SELECT warehouse_region_name, price_list_file, storage_region, latitude, longitude
                FROM Warehouse
                WHERE warehouse_region_name = ? AND price_list_file = ?;
                """);
        stmt.setString(1, "WS_TEST_ADD");
        stmt.setString(2, "ws-test-add.xlsx");
        ResultSet rs = stmt.executeQuery();

        assertTrue(rs.next());
        assertEquals("WS_TEST_ADD", rs.getString("warehouse_region_name"));
        assertEquals("ws-test-add.xlsx", rs.getString("price_list_file"));
        assertEquals(91701, rs.getInt("storage_region"));
        assertEquals(FAKE_LATITUDE, rs.getDouble("latitude"), 0.0001);
        assertEquals(FAKE_LONGITUDE, rs.getDouble("longitude"), 0.0001);
    }

    @Test
    void addWarehouseThrowsWhenTablesAreIncompatible() {
        assertThrows(IncompatibleTablesException.class, () -> warehouseService.addWarehouse(
                "WS_TEST_ADD_INCOMPATIBLE",
                "ws-test-add-incompatible.xlsx",
                91701,
                createIncompatiblePriceListFormatted(),
                createCompatibleRegionTableFormatted()
        ));
    }

    @Test
    void deleteWarehouseSoftDeletesWarehouse() throws SQLException {
        int warehouseId = insertWarehouseDirectly("WS_TEST_DELETE", "ws-test-delete.xlsx", 91701);

        warehouseService.deleteWarehouse(new BriefWarehouse(warehouseId, "WS_TEST_DELETE", "ws-test-delete.xlsx"));

        PreparedStatement stmt = DBConnector.getInstance().getConnection().prepareStatement("""
                SELECT is_active
                FROM Warehouse
                WHERE warehouse_ID = ?;
                """);
        stmt.setInt(1, warehouseId);
        ResultSet rs = stmt.executeQuery();

        assertTrue(rs.next());
        assertFalse(rs.getBoolean("is_active"));
    }

    @Test
    void getSmallPriceListReturnsRowsFromDatabase() throws SQLException {
        insertSmallPriceListEntryDirectly(9876.5f, 43.21f);

        SmallPriceList result = warehouseService.getSmallPriceList();

        assertNotNull(result);
        assertTrue(result.getEntries().stream().anyMatch(entry ->
                Math.abs(entry.getWeight() - 9876.5f) < 0.001f
                        && Math.abs(entry.getCost() - 43.21f) < 0.001f
        ));
    }

    @Test
    void getSmallPriceListFormattedConvertsRowsFromDatabase() throws SQLException {
        insertSmallPriceListEntryDirectly(9877.5f, 44.21f);

        SmallPriceListFormatted result = warehouseService.getSmallPriceListFormatted();

        assertNotNull(result);
        assertTrue(result.getRows().stream().anyMatch(row ->
                Math.abs(row.getWeight() - 9877.5f) < 0.001f
                        && Math.abs(row.getCost() - 44.21f) < 0.001f
        ));
    }

    @Test
    void setSmallPriceListReplacesSmallPriceList() throws SQLException {
        smallPriceListChanged = true;

        SmallPriceList replacement = new SmallPriceList();
        replacement.setEntries(List.of(
                new SmallPriceListEntry(0, 12.5f, 3.5f),
                new SmallPriceListEntry(0, 25.0f, 7.0f)
        ));

        warehouseService.setSmallPriceList(replacement);

        SmallPriceList result = warehouseService.getSmallPriceList();

        assertTrue(result.getEntries().stream().anyMatch(entry ->
                Math.abs(entry.getWeight() - 12.5f) < 0.001f
                        && Math.abs(entry.getCost() - 3.5f) < 0.001f
        ));
        assertTrue(result.getEntries().stream().anyMatch(entry ->
                Math.abs(entry.getWeight() - 25.0f) < 0.001f
                        && Math.abs(entry.getCost() - 7.0f) < 0.001f
        ));
    }

    @Test
    void setSmallPriceListFormattedConvertsAndReplacesSmallPriceList() throws SQLException {
        smallPriceListChanged = true;

        SmallPriceListFormatted formatted = new SmallPriceListFormatted();
        formatted.addRow(new SmallPriceListRow(8.5f, 2.25f));
        formatted.addRow(new SmallPriceListRow(16.0f, 4.50f));

        warehouseService.setSmallPriceListFormatted(formatted);

        SmallPriceList result = warehouseService.getSmallPriceList();

        assertTrue(result.getEntries().stream().anyMatch(entry ->
                Math.abs(entry.getWeight() - 8.5f) < 0.001f
                        && Math.abs(entry.getCost() - 2.25f) < 0.001f
        ));
        assertTrue(result.getEntries().stream().anyMatch(entry ->
                Math.abs(entry.getWeight() - 16.0f) < 0.001f
                        && Math.abs(entry.getCost() - 4.50f) < 0.001f
        ));
    }

    @Test
    void setSmallPriceListRollsBackAndRestoresAutoCommitWhenDaoThrows() throws Exception {
        PriceListDAO priceListDao = warehouseService.getPriceListDao();
        Connection originalConnection = getDaoConnection(priceListDao);
        ThrowingConnectionHandler handler = new ThrowingConnectionHandler();

        try {
            setDaoConnection(priceListDao, handler.createProxy());

            assertDoesNotThrow(() -> warehouseService.setSmallPriceList(new SmallPriceList()));

            assertEquals(1, handler.rollbackCalls);
            assertEquals(1, handler.setAutoCommitFalseCalls);
            assertEquals(1, handler.setAutoCommitTrueCalls);
            assertEquals(1, handler.prepareStatementCalls);
        } finally {
            setDaoConnection(priceListDao, originalConnection);
        }
    }

    @Test
    void replaceTablesStoresCompatiblePriceListAndRegionTable() throws Exception {
        int warehouseId = insertWarehouseDirectly("WS_TEST_REPLACE", "ws-test-replace.xlsx", 91701);
        Warehouse warehouse = createWarehouseReference(warehouseId, "WS_TEST_REPLACE", "ws-test-replace.xlsx", 91701);

        warehouseService.replaceTables(
                createCompatiblePriceListFormatted(),
                createCompatibleRegionTableFormatted(),
                warehouse
        );

        assertTrue(countPriceListRowsForWarehouse(warehouseId) > 0);
        assertTrue(countPostalCodeRowsForWarehouse(warehouseId) > 0);
    }

    @Test
    void replaceTablesThrowsWhenTablesAreIncompatible() {
        Warehouse warehouse = createWarehouseReference(-999, "WS_TEST_REPLACE_BAD", "ws-test-replace-bad.xlsx", 91701);

        assertThrows(IncompatibleTablesException.class, () -> warehouseService.replaceTables(
                createIncompatiblePriceListFormatted(),
                createCompatibleRegionTableFormatted(),
                warehouse
        ));
    }

    @Test
    void replaceTablesRollsBackAndRestoresAutoCommitWhenDaoThrows() throws Exception {
        PriceListDAO priceListDao = warehouseService.getPriceListDao();
        RegionDAO regionDao = warehouseService.getRegionDao();

        Connection originalPriceConnection = getDaoConnection(priceListDao);
        Connection originalRegionConnection = getDaoConnection(regionDao);

        ThrowingConnectionHandler priceHandler = new ThrowingConnectionHandler();
        ThrowingConnectionHandler regionHandler = new ThrowingConnectionHandler(false);

        try {
            setDaoConnection(priceListDao, priceHandler.createProxy());
            setDaoConnection(regionDao, regionHandler.createProxy());

            assertDoesNotThrow(() -> warehouseService.replaceTables(
                    createCompatiblePriceListFormatted(),
                    createCompatibleRegionTableFormatted(),
                    createWarehouseReference(1, "WS_TEST_THROW", "ws-test-throw.xlsx", 91701)
            ));

            assertEquals(1, priceHandler.rollbackCalls);
            assertEquals(1, regionHandler.rollbackCalls);
            assertEquals(1, priceHandler.setAutoCommitFalseCalls);
            assertEquals(1, regionHandler.setAutoCommitFalseCalls);
            assertEquals(1, priceHandler.setAutoCommitTrueCalls);
            assertEquals(1, regionHandler.setAutoCommitTrueCalls);
            assertEquals(1, priceHandler.prepareStatementCalls);
        } finally {
            setDaoConnection(priceListDao, originalPriceConnection);
            setDaoConnection(regionDao, originalRegionConnection);
        }
    }

    private int insertWarehouseDirectly(String name, String priceListFile, int postalCode) throws SQLException {
        PreparedStatement stmt = DBConnector.getInstance().getConnection().prepareStatement("""
                INSERT INTO Warehouse(warehouse_region_name, price_list_file, storage_region, latitude, longitude, is_active)
                VALUES (?, ?, ?, ?, ?, true)
                RETURNING warehouse_ID;
                """);
        stmt.setString(1, name);
        stmt.setString(2, priceListFile);
        stmt.setInt(3, postalCode);
        stmt.setDouble(4, 0.0);
        stmt.setDouble(5, 0.0);

        ResultSet rs = stmt.executeQuery();
        assertTrue(rs.next());

        return rs.getInt("warehouse_ID");
    }

    private void insertSmallPriceListEntryDirectly(float weight, float cost) throws SQLException {
        PreparedStatement stmt = DBConnector.getInstance().getConnection().prepareStatement("""
                INSERT INTO Sp_price_list(weight_sp, cost_sp)
                VALUES (?, ?);
                """);
        stmt.setFloat(1, weight);
        stmt.setFloat(2, cost);
        stmt.executeUpdate();
    }

    private int countPriceListRowsForWarehouse(int warehouseId) throws SQLException {
        PreparedStatement stmt = DBConnector.getInstance().getConnection().prepareStatement("""
                SELECT COUNT(*)
                FROM Parameter_list pl
                JOIN Region r ON r.region_ID = pl.region_ID
                WHERE r.warehouse_ID = ?;
                """);
        stmt.setInt(1, warehouseId);

        ResultSet rs = stmt.executeQuery();
        assertTrue(rs.next());

        return rs.getInt(1);
    }

    private int countPostalCodeRowsForWarehouse(int warehouseId) throws SQLException {
        PreparedStatement stmt = DBConnector.getInstance().getConnection().prepareStatement("""
                SELECT COUNT(*)
                FROM Postal_code_list pcl
                JOIN Region r ON r.region_ID = pcl.region_ID
                WHERE r.warehouse_ID = ?;
                """);
        stmt.setInt(1, warehouseId);

        ResultSet rs = stmt.executeQuery();
        assertTrue(rs.next());

        return rs.getInt(1);
    }

    private void cleanupTestWarehouses() throws SQLException {
        PreparedStatement stmt = DBConnector.getInstance().getConnection().prepareStatement("""
                DELETE FROM Warehouse
                WHERE warehouse_region_name LIKE ? OR price_list_file LIKE ?;
                """);
        stmt.setString(1, TEST_PREFIX + "%");
        stmt.setString(2, "ws-test-%");
        stmt.executeUpdate();
    }

    private PriceListFormatted createCompatiblePriceListFormatted() {
        PriceListFormatted priceList = new PriceListFormatted();

        PriceListRow firstRow = new PriceListRow(10.0f, 100.0f);
        firstRow.getRegions().put("BA", 5.0f);
        firstRow.getRegions().put("TT", 6.0f);
        priceList.addRow(firstRow);

        PriceListRow secondRow = new PriceListRow(20.0f, 200.0f);
        secondRow.getRegions().put("BA", 8.0f);
        secondRow.getRegions().put("TT", 9.0f);
        priceList.addRow(secondRow);

        return priceList;
    }

    private PriceListFormatted createIncompatiblePriceListFormatted() {
        PriceListFormatted priceList = new PriceListFormatted();

        PriceListRow row = new PriceListRow(10.0f, 100.0f);
        row.getRegions().put("BA", 5.0f);
        priceList.addRow(row);

        return priceList;
    }

    private RegionTableFormatted createCompatibleRegionTableFormatted() {
        RegionTableFormatted regionTable = new RegionTableFormatted();

        RegionTableRow ba = new RegionTableRow("BA", new ArrayList<>(List.of(new Range(81000, 82999))));
        RegionTableRow tt = new RegionTableRow("TT", new ArrayList<>(List.of(new Range(91700, 91799))));

        regionTable.addRow(ba);
        regionTable.addRow(tt);

        return regionTable;
    }

    private Warehouse createWarehouseReference(int id, String name, String regionName, int postalCode) {
        Warehouse warehouse = new Warehouse();

        warehouse.setId(id);
        warehouse.setName(name);
        warehouse.setRegionName(regionName);
        warehouse.setPostalCode(postalCode);
        warehouse.setCoord(new Coordinates(0.0, 0.0));

        return warehouse;
    }

    private MapService getMapServiceSingleton() throws Exception {
        Field field = MapService.class.getDeclaredField("instance");
        field.setAccessible(true);

        return (MapService) field.get(null);
    }

    private void setMapServiceSingleton(MapService mapService) throws Exception {
        Field field = MapService.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, mapService);
    }

    private Connection getDaoConnection(BaseDAO dao) throws Exception {
        Field field = BaseDAO.class.getDeclaredField("connection");
        field.setAccessible(true);

        return (Connection) field.get(dao);
    }

    private void setDaoConnection(BaseDAO dao, Connection connection) throws Exception {
        Field field = BaseDAO.class.getDeclaredField("connection");
        field.setAccessible(true);
        field.set(dao, connection);
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0f;
        }
        if (type == double.class) {
            return 0d;
        }
        if (type == char.class) {
            return '\0';
        }

        return null;
    }

    private static class FakeMapService extends MapService {
        @Override
        public double[] fetchCoordinatesForPostalCode(int postalCode) {
            return new double[]{FAKE_LATITUDE, FAKE_LONGITUDE};
        }
    }

    private static class ThrowingConnectionHandler implements InvocationHandler {

        private final boolean throwOnPrepareStatement;

        private int setAutoCommitFalseCalls;
        private int setAutoCommitTrueCalls;
        private int rollbackCalls;
        private int prepareStatementCalls;

        private ThrowingConnectionHandler() {
            this(true);
        }

        private ThrowingConnectionHandler(boolean throwOnPrepareStatement) {
            this.throwOnPrepareStatement = throwOnPrepareStatement;
        }

        private Connection createProxy() {
            return (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class<?>[]{Connection.class},
                    this
            );
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();

            if ("setAutoCommit".equals(name)) {
                boolean value = (boolean) args[0];

                if (value) {
                    setAutoCommitTrueCalls++;
                } else {
                    setAutoCommitFalseCalls++;
                }

                return null;
            }

            if ("rollback".equals(name)) {
                rollbackCalls++;
                return null;
            }

            if ("prepareStatement".equals(name)) {
                prepareStatementCalls++;

                if (throwOnPrepareStatement) {
                    throw new SQLException("Forced test exception");
                }

                return null;
            }

            if ("toString".equals(name)) {
                return "ThrowingConnectionProxy";
            }

            return defaultValue(method.getReturnType());
        }
    }
}