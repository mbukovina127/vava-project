package org.shippin.app.DAO;
import org.shippin.app.models.Region;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RegionDAO extends BaseDAO {

    public RegionDAO(Connection conn) {
        super(conn);
    }

    /**
     * gets all regions for warehouse
     */
    public List<Region> getRegionsForWarehouse(String warehouseName) throws SQLException {
        String sql = ""; //TODO

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, warehouseName);
        ResultSet rs = stmt.executeQuery();

        Map<String, Region> regionMap = new LinkedHashMap<>();

        while (rs.next()) {
            String regionName = rs.getString("region_name");
            int upBound = rs.getInt("up_bound");
            int downBound = rs.getInt("down_bound");

            Region region = regionMap.computeIfAbsent(regionName, Region::new);
            region.addZoneRange(downBound + "-" + upBound);
        }

        return new ArrayList<>(regionMap.values());
    }

    /**
     * get region by warehouse&name
     */
    public Region getRegion(String warehouseName, String regionName) throws SQLException {
        String sql = ""; //TODO

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, warehouseName);
        stmt.setString(2, regionName);
        ResultSet rs = stmt.executeQuery();

        Region region = null;

        while (rs.next()) {
            if (region == null) {
                region = new Region(rs.getString("region_name"));
            }
            region.addZoneRange(rs.getInt("down_bound") + "-" + rs.getInt("up_bound"));
        }

        return region;
    }

    /**
     * get region by warehouseName& PSC
     */
    public Region getRegionByPsc(String warehouseName, int psc) throws SQLException {
        String sql = ""; //TODO

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, warehouseName);
        stmt.setInt(2, psc);
        stmt.setInt(3, psc);
        ResultSet rs = stmt.executeQuery();

        Region region = null;

        while (rs.next()) {
            if (region == null) {
                region = new Region(rs.getString("region_name"));
            }
            region.addZoneRange(rs.getInt("down_bound") + "-" + rs.getInt("up_bound"));
        }

        return region;
    }

    /**
     * add region to warehouse
     */
    public int insertRegion(String regionName, String warehouseName) throws SQLException {
        String sql = ""; //TODO

        PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, regionName);
        stmt.setString(2, warehouseName);
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
    public void insertFullRegion(Region region) throws SQLException {
            int regionID = insertRegion(region.getCode(), region.getSourceWarehouse());
            if (regionID == -1){
                throw new SQLException("Failed to insert region: " + region.getCode());
            }

            List<Integer> pscList = region.getPscList();
            int rangeStart = pscList.get(0);
            int prev = rangeStart;

            for (int i = 1; i <= pscList.size(); i++) {
                boolean last = (i == pscList.size());
                int curr = last ? -1 : pscList.get(i);

                if (last || curr != prev + 1) {
                    insertPSCRange(regionID, rangeStart, prev);
                    rangeStart = last ? -1 : curr;
                }
                prev = curr;
            }

            connection.commit();

    }
}