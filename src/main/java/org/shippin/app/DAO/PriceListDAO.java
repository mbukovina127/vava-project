package org.shippin.app.DAO;


import org.shippin.app.models.PriceListItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PriceListDAO extends BaseDAO {

    public PriceListDAO(Connection conn) {
        super(conn);
    }

    /**
     * returns entire table for warehouse
     */
    public List<PriceListItem> getPriceItems(String sourceWarehouse, String regionName) throws SQLException {
        List<PriceListItem> itemList = new ArrayList<>();
        String sql = ""; //TODO

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, sourceWarehouse);
        stmt.setString(2, regionName);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            PriceListItem item = new PriceListItem(

                    rs.getFloat("weight"),
                    rs.getFloat("volume"),
                    rs.getFloat("cost"),
                    regionName
            );
            itemList.add(item);
        }

        return itemList;
    }

    /**
     * get price list items for specific warehouse & zone
     */
    public Map<String, List<PriceListItem>> getPriceListForWarehouse(String sourceWarehouse) throws SQLException {
        Map<String, List<PriceListItem>> result = new HashMap<>();
        String sql = ""; //TODO

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, sourceWarehouse);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            String regionName = rs.getString("region_name");

            PriceListItem item = new PriceListItem(
                    rs.getFloat("weight"),
                    rs.getFloat("volume"),
                    rs.getFloat("cost"),
                    regionName
            );

            result.computeIfAbsent(regionName, k -> new ArrayList<>()).add(item);
        }

        return result;
    }

    /**
     * get PriceListItem for specific warehouse&region&weight
     */
    public PriceListItem getPriceListItemByWeight(String sourceWarehouse, String regionName, float weight) throws SQLException {
        String sql = ""; //TODO

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, sourceWarehouse);
        stmt.setString(2, regionName);
        stmt.setFloat(3, weight);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return new PriceListItem(

                    rs.getFloat("weight"),
                    rs.getFloat("volume"),
                    rs.getFloat("cost"),
                    regionName
            );
        }
        return null;
    }

    /**
     * get PriceListItem for specific warehouse&region&volume
     */
    public PriceListItem getPriceListItemByVolume(String sourceWarehouse, String regionName, float volume) throws SQLException {
        String sql = ""; //TODO

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, sourceWarehouse);
        stmt.setString(2, regionName);
        stmt.setFloat(3, volume);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return new PriceListItem(
                    rs.getFloat("weight"),
                    rs.getFloat("volume"),
                    rs.getFloat("cost"),
                    regionName
            );
        }
        return null;
    }




    /**
     * appends PriceListItem to warehouse, returns its ID from db
     */
    public int insertPriceListItem(PriceListItem item, String sourceWarehouse) throws SQLException {
        String insertParameter = ""; //TODO

        PreparedStatement paramStmt = connection.prepareStatement(insertParameter, Statement.RETURN_GENERATED_KEYS);
        paramStmt.setFloat(1, item.getWeight());
        paramStmt.setFloat(2, item.getVolume());
        paramStmt.setFloat(3, item.getCost());

        int affected = paramStmt.executeUpdate();
        if (affected == 0) return -1;

        ResultSet generatedKeys = paramStmt.getGeneratedKeys();
        if (!generatedKeys.next()) return -1;
        int newParameterID = generatedKeys.getInt(1);

        String insertParameterList = "";//TODO

        PreparedStatement listStmt = connection.prepareStatement(insertParameterList);
        listStmt.setInt(1, newParameterID);
        listStmt.setString(2, item.getRegion());
        listStmt.setString(3, sourceWarehouse);
        listStmt.executeUpdate();

        return newParameterID;
    }

    /**
     * insert PriceListItems
     */
    public void insertPriceListItems(List<PriceListItem> items, String sourceWarehouse) throws SQLException {

        for (PriceListItem item : items) {
            insertPriceListItem(item, sourceWarehouse);
        }

    }




























}



