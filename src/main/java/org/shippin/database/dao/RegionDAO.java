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
        String sql = ""; //TODO
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
     * get region by warehouse&name
     */
    public RegionTableEntry getRegion(String warehouseName, String regionName) throws SQLException {
        String sql = ""; //TODO

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, warehouseName);
        stmt.setString(2, regionName);
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
        String sql = ""; //TODO

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, warehouseName);
        stmt.setInt(2, psc);
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
    public int insertRegion(String regionName, int warehouseid) throws SQLException {
        String sql = ""; //TODO

        PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, regionName);
        stmt.setInt(2, warehouseid);
        stmt.executeUpdate();

        ResultSet keys = stmt.getGeneratedKeys();
        return keys.next() ? keys.getInt(1) : -1;
    }

    /**
     * add PSC range to region
     */
    public void insertPSCRange(int regionID, int downBound, int upBound) throws SQLException {
        String insertPSC = ""; //TODO

        PreparedStatement pcStmt = connection.prepareStatement(insertPSC, Statement.RETURN_GENERATED_KEYS);
        pcStmt.setInt(1, downBound);
        pcStmt.setInt(2, upBound);
        pcStmt.executeUpdate();

        ResultSet keys = pcStmt.getGeneratedKeys();
        int postalCodeID = keys.getInt(1);

        String insertPostalCodeList = ""; //TODO

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