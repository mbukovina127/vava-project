package org.shippin.database.dao;




import org.shippin.domain.PriceList;
import org.shippin.domain.PriceListEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PriceListDAO extends BaseDAO {

    public PriceListDAO(Connection conn) {
        super(conn);
    }

    /**
     * returns price list for warehouse & zone
     */
    public PriceList getPriceList(String sourceWarehouse, String regionName) throws SQLException {
        List<PriceListEntry> itemList = new ArrayList<>();
        String sql = """
                SELECT pl.parameter_list_ID,pl.weight, pl.volume, pl.cost
                FROM Warehouse w
                JOIN Region r ON w.warehouse_ID = r.warehouse_ID
                JOIN Parameter_list pl ON pl.region_ID = r.region_ID
                WHERE r.region_name = ?
                AND w.warehouse_region_name = ?;""";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, regionName);
        stmt.setString(2, sourceWarehouse);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            PriceListEntry item = new PriceListEntry(
                    rs.getInt("parameter_list_ID"),
                    rs.getFloat("weight"),
                    rs.getFloat("volume"),
                    rs.getFloat("cost"),
                    regionName
            );
            itemList.add(item);
        }

        PriceList pl = new PriceList();
        pl.setEntries(itemList);
        return pl;
    }

    /**
     * get price list items for specific warehouse
     */
    public PriceList getFullPriceList(String sourceWarehouse) throws SQLException {
        List<PriceListEntry> itemList = new ArrayList<>();
        String sql = """
                SELECT r.region_name, pl.parameter_list_ID,pl.weight, pl.volume, pl.cost
                FROM Warehouse w
                JOIN Region r ON w.warehouse_ID = r.warehouse_ID
                JOIN Parameter_list pl ON pl.region_ID = r.region_ID
                WHERE w.warehouse_region_name = ?;""";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, sourceWarehouse);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            String regionName = rs.getString("region_name");

            PriceListEntry item = new PriceListEntry(
                    rs.getInt("parameter_list_ID"),
                    rs.getFloat("weight"),
                    rs.getFloat("volume"),
                    rs.getFloat("cost"),
                    regionName
            );
            itemList.add(item);

        }

        PriceList pl = new PriceList();
        pl.setEntries(itemList);
        return pl;
    }

    /**
     * get PriceListEntry for specific warehouse&region&weight
     */
    public PriceListEntry getPriceListEntryByWeight(String sourceWarehouse, String regionName, float weight) throws SQLException {
        String sql = """
                SELECT pl.parameter_list_ID,pl.weight, pl.volume, pl.cost
                FROM Warehouse w
                JOIN Region r ON w.warehouse_ID = r.warehouse_ID
                JOIN Parameter_list pl ON pl.region_ID = r.region_ID
                WHERE r.region_name = ? AND w.warehouse_region_name = ? AND pl.weight = ?;""";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, regionName);
        stmt.setString(2, sourceWarehouse);
        stmt.setFloat(3, weight);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return new PriceListEntry(
                    rs.getInt("parameter_list_ID"),
                    rs.getFloat("weight"),
                    rs.getFloat("volume"),
                    rs.getFloat("cost"),
                    regionName
            );
        }
        return null;
    }

    /**
     * get PriceListEntry for specific warehouse&region&volume
     */
    public PriceListEntry getPriceListEntryByVolume(String sourceWarehouse, String regionName, float volume) throws SQLException {
        String sql = """
                SELECT pl.parameter_list_ID,pl.weight, pl.volume, pl.cost
                FROM Warehouse w
                JOIN Region r ON w.warehouse_ID = r.warehouse_ID
                JOIN Parameter_list pl ON pl.region_ID = r.region_ID
                WHERE r.region_name = ? AND w.warehouse_region_name = ? AND pl.volume = ?;""";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, regionName);
        stmt.setString(2, sourceWarehouse);
        stmt.setFloat(3, volume);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return new PriceListEntry(
                    rs.getInt("parameter_list_ID"),
                    rs.getFloat("weight"),
                    rs.getFloat("volume"),
                    rs.getFloat("cost"),
                    regionName
            );
        }
        return null;
    }




    /**
     * appends PriceListEntry to warehouse, returns its ID from db
     */
    public int insertPriceListEntry(PriceListEntry item, String sourceWarehouse) throws SQLException {

        //get region_ID where region_name=BA1 BA2 AND warehouseID=warehouseID
        String regionSql = """
        SELECT r.region_ID
        FROM Region r
        JOIN Warehouse w ON w.warehouse_ID = r.warehouse_ID
        WHERE w.warehouse_region_name = ?
          AND r.region_name = ?
    """;

        PreparedStatement regionStmt = connection.prepareStatement(regionSql);
        regionStmt.setString(1, sourceWarehouse);
        regionStmt.setString(2, item.getZone());

        ResultSet rs = regionStmt.executeQuery();

        if (!rs.next()) {
            throw new SQLException("region not found warehouse=" + sourceWarehouse + " zone=" + item.getZone());
        }

        int regionId = rs.getInt("region_ID");

        //insert into Parameter_list
        String insertSql = """
        INSERT INTO Parameter_list(region_ID, weight, volume, cost)
        VALUES (?, ?, ?, ?)
        RETURNING parameter_list_ID
    """;

        PreparedStatement insertStmt = connection.prepareStatement(insertSql);
        insertStmt.setInt(1, regionId);
        insertStmt.setFloat(2, item.getWeight());
        insertStmt.setFloat(3, item.getVolume());
        insertStmt.setFloat(4, item.getCost());

        ResultSet insertRs = insertStmt.executeQuery();

        if (insertRs.next()) {
            return insertRs.getInt("parameter_list_ID");
        }

        throw new SQLException("Insert failed for Parameter_list");
    }

    /**
     * insert PriceList into warehouse
     */
    public void insertPriceList(PriceList pl, String sourceWarehouse) throws SQLException {

        for (PriceListEntry item : pl.getEntries()) {
            insertPriceListEntry(item, sourceWarehouse);
        }

    }




























}



