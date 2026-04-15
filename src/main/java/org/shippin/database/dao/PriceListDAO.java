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
        String sql = ""; //TODO

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, sourceWarehouse);
        stmt.setString(2, regionName);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            PriceListEntry item = new PriceListEntry(
                    rs.getInt("id"),
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
        String sql = ""; //TODO

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, sourceWarehouse);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            String regionName = rs.getString("region_name");

            PriceListEntry item = new PriceListEntry(
                    rs.getInt("id"),
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
        String sql = ""; //TODO

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, sourceWarehouse);
        stmt.setString(2, regionName);
        stmt.setFloat(3, weight);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return new PriceListEntry(
                    rs.getInt("id"),
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
        String sql = ""; //TODO

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, sourceWarehouse);
        stmt.setString(2, regionName);
        stmt.setFloat(3, volume);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return new PriceListEntry(
                    rs.getInt("id"),
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
        listStmt.setString(2, item.getZone());
        listStmt.setString(3, sourceWarehouse);
        listStmt.executeUpdate();

        return newParameterID;
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



