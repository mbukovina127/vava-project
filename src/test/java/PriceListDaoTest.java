import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.shippin.database.Config;
import org.shippin.database.DBConnector;
import org.shippin.database.dao.PriceListDAO;
import org.shippin.domain.PriceList;
import org.shippin.domain.PriceListEntry;

public class PriceListDaoTest {

    private static DBConnector dbc;
    private static PriceListDAO priceListDAO;

    @BeforeAll
    static void connect() throws SQLException {
        dbc = new DBConnector(new Config());
        dbc.connect();
        priceListDAO = new PriceListDAO(dbc.getConnection());
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

    private int insertRegion(int warehouseId, String regionName) throws SQLException {
        PreparedStatement stmt = dbc.getConnection().prepareStatement("""
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
        PreparedStatement stmt = dbc.getConnection().prepareStatement("""
            INSERT INTO Parameter_list(region_ID, weight, volume, cost)
            VALUES (?, ?, ?, ?)
        """);

        stmt.setInt(1, regionId);
        stmt.setFloat(2, weight);
        stmt.setFloat(3, volume);
        stmt.setFloat(4, cost);

        stmt.executeUpdate();
    }

    @Test
    @DisplayName("getPriceList returns non-null result")
    void getPriceListReturnsNonNull() throws SQLException {
        int warehouseId = insertWarehouse("PL Test BA");
        int regionId = insertRegion(warehouseId, "BA1");
        insertPriceItem(regionId, 5, 1, 10);

        PriceList list = priceListDAO.getPriceList("PL Test BA", "BA1");

        assertNotNull(list);
    }

    @Test
    @DisplayName("getFullPriceList returns non-null result")
    void getFullPriceListReturnsNonNull() throws SQLException {
        int warehouseId = insertWarehouse("PL Test TT");
        int regionId = insertRegion(warehouseId, "TT1");
        insertPriceItem(regionId, 10, 2, 20);

        PriceList list = priceListDAO.getFullPriceList("PL Test TT");

        assertNotNull(list);
    }

    @Test
    @DisplayName("getPriceListEntryByWeight returns item or exposes bug")
    void getPriceListEntryByWeight() throws SQLException {
        int warehouseId = insertWarehouse("PL Test KE");
        int regionId = insertRegion(warehouseId, "KE1");
        insertPriceItem(regionId, 15, 3, 30);

        PriceListEntry item =
                priceListDAO.getPriceListEntryByWeight("PL Test KE", "KE1", 15);

        assertNotNull(item);
    }

    @Test
    @DisplayName("getPriceListEntryByVolume returns item or exposes bug")
    void getPriceListEntryByVolume() throws SQLException {
        int warehouseId = insertWarehouse("PL Test ZA");
        int regionId = insertRegion(warehouseId, "ZA1");
        insertPriceItem(regionId, 20, 4, 40);

        PriceListEntry item =
                priceListDAO.getPriceListEntryByVolume("PL Test ZA", "ZA1", 4);

        assertNotNull(item);
    }

    @Test
    @DisplayName("insertPriceListEntry executes without exception")
    void insertPriceListEntryExecutes() throws SQLException {
        insertWarehouse("PL Test NR");

        PriceListEntry item =
                new PriceListEntry(0, 25, 5, 50, "NR1");

        assertDoesNotThrow(() ->
                priceListDAO.insertPriceListEntry(item, "PL Test NR")
        );
    }

    @Test
    @DisplayName("insertPriceList executes or exposes bug")
    void insertPriceListExecutes() throws SQLException {
        insertWarehouse("PL Full Insert Test");

        PriceList priceList = new PriceList();
        priceList.setEntries(List.of(
                new PriceListEntry(0, 5, 1, 10, "FULL_PL_1"),
                new PriceListEntry(0, 10, 2, 20, "FULL_PL_2")
        ));

        assertDoesNotThrow(() ->
                priceListDAO.insertPriceList(priceList, "PL Full Insert Test")
        );
    }
}