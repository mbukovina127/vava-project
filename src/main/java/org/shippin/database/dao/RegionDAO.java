package org.shippin.database.dao;
import org.shippin.util.Range;
import org.shippin.domain.Warehouse;

import org.shippin.domain.RegionTable;
import org.shippin.domain.RegionTableEntry;

import java.sql.*;
import java.util.*;

public class RegionDAO extends BaseDAO {

    public RegionDAO(Connection conn) {
        super(conn);
    }

    /**
     * gets all regions for warehouse
     */
    public RegionTable getRegionsForWarehouse(String warehouseName) throws SQLException {
        String sql = """
                SELECT r.region_ID, r.region_name, pc.up_bound, pc.down_bound
                FROM Region r JOIN Postal_code_list pcl ON pcl.region_ID = r.region_ID
                JOIN Postal_code pc ON pcl.postal_code_ID = pc.postal_code_ID
                JOIN Warehouse w ON w.warehouse_ID = r.warehouse_ID
                WHERE w.warehouse_region_name = ?;""";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, warehouseName);
        ResultSet rs = stmt.executeQuery();


        Map<Integer, RegionTableEntry> regionMap = new HashMap<>();

        while (rs.next()) {
            int regionId = rs.getInt("region_id"); // id in "Region" table
            String regionCode = rs.getString("region_name"); //BA1 BA2

            int upBound = rs.getInt("up_bound");
            int downBound = rs.getInt("down_bound");

            Range range = new Range(downBound, upBound);

            // create entry if not exists
            RegionTableEntry entry = regionMap.get(regionId);
            if (entry == null) {
                entry = new RegionTableEntry(regionId, new ArrayList<>(), regionCode);
                regionMap.put(regionId, entry);
            }

            // add range to region
            entry.addRange(range);
        }

        RegionTable table = new RegionTable();
        table.setEntries(new ArrayList<>(regionMap.values()));

        return table;
    }

    /**
     * get region by warehouse&region code
     *
     */
    public RegionTableEntry getRegion(String warehouseName, String regionCode) throws SQLException {
        String sql = """
                SELECT r.region_ID, r.region_name, pc.up_bound, pc.down_bound
                FROM Region r JOIN Postal_code_list pcl ON pcl.region_ID = r.region_ID
                JOIN Postal_code pc ON pcl.postal_code_ID = pc.postal_code_ID
                JOIN Warehouse w ON w.warehouse_ID = r.warehouse_ID
                WHERE w.warehouse_region_name = ?
                AND r.region_ID = ?;""";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, warehouseName);
        stmt.setString(2, regionCode);
        ResultSet rs = stmt.executeQuery();

        RegionTableEntry entry = null;

        while (rs.next()) {
            if (entry == null) {
                entry = new RegionTableEntry(rs.getInt("id"), new ArrayList<>(), rs.getString("region_name"));
            }
            entry.addRange(new Range(rs.getInt("down_bound"), rs.getInt("up_bound")));
        }

        return entry;
    }

    /**
     * get region by warehouseName& PSC
     */
    public RegionTableEntry getRegionByPsc(String warehouseName, int psc) throws SQLException {
        String sql = """
                SELECT r.region_ID, r.region_name, pc.up_bound, pc.down_bound
                FROM Region r JOIN Postal_code_list pcl ON pcl.region_ID = r.region_ID
                JOIN Postal_code pc ON pcl.postal_code_ID = pc.postal_code_ID
                JOIN Warehouse w ON w.warehouse_ID = r.warehouse_ID
                WHERE w.warehouse_region_name = ?
                AND pc.up_bound <= ? AND pc.down_bound >= ?;""";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, warehouseName);
        stmt.setInt(2, psc);
        stmt.setInt(3, psc);
        ResultSet rs = stmt.executeQuery();

        RegionTableEntry entry = null;

        while (rs.next()) {
            if (entry == null) {
                entry = new RegionTableEntry(rs.getInt("id"), new ArrayList<>(), rs.getString("region_name"));
            }
            entry.addRange(new Range(rs.getInt("down_bound"), rs.getInt("up_bound")));
        }

        return entry;

    }

    /**
     * add region to warehouse
     */
    public int insertRegion(String regionName, int warehouseId) throws SQLException {
        String sql = "INSERT INTO Region(warehouse_ID, region_name)VALUES(?,?);";

        PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, regionName);
        stmt.setInt(2, warehouseId);
        stmt.executeUpdate();

        ResultSet keys = stmt.getGeneratedKeys();
        return keys.next() ? keys.getInt(1) : -1;
    }

    /**
     * add PSC range to region
     */
    public void insertPSCRange(int regionID, int downBound, int upBound) throws SQLException {
        String insertPSC = """
                INSERT INTO Postal_code(up_bound, down_bound) VALUES(?,?)
                ON CONFLICT (up_bound, down_bound) DO UPDATE
                SET up_bound = EXCLUDED.up_bound
                RETURNING postal_code_ID;""";

        PreparedStatement pcStmt = connection.prepareStatement(insertPSC);
        pcStmt.setInt(1, downBound);
        pcStmt.setInt(2, upBound);

        ResultSet rs = pcStmt.executeQuery();
        rs.next();

        int postalCodeID = rs.getInt("postal_code_ID");

        String insertPostalCodeList = "INSERT INTO Postal_code_list(region_ID, postal_code_ID)VALUES(?,?);";

        PreparedStatement listStmt = connection.prepareStatement(insertPostalCodeList);
        listStmt.setInt(1, regionID);
        listStmt.setInt(2, postalCodeID);
        listStmt.executeUpdate();
    }

    /**
     * insert full region with PSC ranges
     */
    public void insertFullRegion(RegionTableEntry region, Warehouse wareHouse) throws SQLException {
            int regionID = insertRegion(region.getRegionCode(), wareHouse.getId());
            if (regionID == -1){
                throw new SQLException("Failed to insert region: " + region.getRegionCode());
            }

        List<Range> ranges = region.getRanges();

        for (Range r : ranges) {
            insertPSCRange(regionID, r.getMin(), r.getMax());
        }

        connection.commit();

    }



}