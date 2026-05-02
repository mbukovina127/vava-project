package org.shippin.database.dao;

import org.shippin.domain.BriefWarehouse;
import org.shippin.domain.Warehouse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WarehouseDAO extends BaseDAO {

    private static WarehouseDAO instance;

    private WarehouseDAO() {
        super();
    }

    public static WarehouseDAO getInstance() {
        if (instance == null) {
            instance = new WarehouseDAO();
        }
        return instance;
    }

    public Warehouse getById(int id) throws SQLException {
        String sql = """
                    SELECT w.warehouse_ID, w.warehouse_region_name, w.price_list_file
                    FROM Warehouse w WHERE w.warehouse_id = ?;""";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        if (!rs.next()) return null;

        Warehouse warehouse = new Warehouse();
        warehouse.setId(rs.getInt("warehouse_ID"));
        warehouse.setName(rs.getString("warehouse_region_name"));
        warehouse.setRegionName(rs.getString("price_list_file"));


        RegionDAO regionDAO = RegionDAO.getInstance();
        PriceListDAO priceListDAO = PriceListDAO.getInstance();

        String warehouseName = warehouse.getName();

        warehouse.setRegionTable(
                regionDAO.getRegionsForWarehouse(warehouseName)
        );

        warehouse.setPriceList(
                priceListDAO.getFullPriceList(warehouseName)
        );

        return warehouse;
    }


    public List<BriefWarehouse> getAllBriefWarehouses() throws SQLException {
        String sql = "SELECT w.warehouse_ID, w.warehouse_region_name, w.price_list_file FROM Warehouse w;";

        PreparedStatement stmt = connection.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        List<BriefWarehouse> list = new ArrayList<>();

        while (rs.next()) {
            BriefWarehouse bw = new BriefWarehouse(
                    rs.getInt("warehouse_ID"),
                    rs.getString("warehouse_region_name"),
                    rs.getString("price_list_file")
            );
            list.add(bw);
        }

        return list;
    }

    public BriefWarehouse getlBriefWarehouse(int briefWarehouseID) throws SQLException {
        String sql = """
                    SELECT w.warehouse_ID, w.warehouse_region_name, w.price_list_file
                    FROM Warehouse w WHERE w.warehouse_id = ?;""";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, briefWarehouseID);


        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            BriefWarehouse bw = new BriefWarehouse(
                    rs.getInt("warehouse_ID"),
                    rs.getString("warehouse_region_name"),
                    rs.getString("price_list_file")
            );
            return bw;
        }
        return null;
    }


    /**
     * inserts warehouse core info
     */
    public void upsertWarehouse(Warehouse w) throws SQLException {
        String sql = """
                INSERT INTO Warehouse(warehouse_id, warehouse_region_name, price_list_file)
                VALUES (?,?,?)
                ON CONFLICT(warehouse_id)
                DO UPDATE SET
                    warehouse_id = EXCLUDED.warehouse_id,
                    warehouse_region_name = EXCLUDED.warehouse_region_name,
                    price_list_file = EXCLUDED.price_list_file;""";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, w.getId());
        stmt.setString(2, w.getName()); //SK PSC+region aka name
        stmt.setString(3, w.getRegionName()); //F ZBS-BA aka filename aka excel sheet name

        stmt.executeUpdate();
    }

    /**
     * inserts warehouse core info with both tables
     */
    public void insertFullWarehouse(Warehouse warehouse) throws SQLException {

        try {
            connection.setAutoCommit(false);

            int newId = insertWarehouse(warehouse);
            warehouse.setId(newId);

            if (warehouse.getRegionTable() != null) {
                RegionDAO regionDAO = RegionDAO.getInstance();

                for (var entry : warehouse.getRegionTable().getEntries()) {
                    regionDAO.insertFullRegion(entry, warehouse);
                }
            }

            if (warehouse.getPriceList() != null) {
                PriceListDAO priceListDAO = PriceListDAO.getInstance();

                for (var item : warehouse.getPriceList().getEntries()) {
                    priceListDAO.insertPriceListEntry(item, warehouse.getId());
                }
            }

            connection.commit();

        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
    
    public int insertWarehouse(Warehouse w) throws SQLException {
        String sql = """
                INSERT INTO Warehouse(warehouse_region_name, price_list_file)
                VALUES (?,?)
                RETURNING warehouse_id;
                ;""";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, w.getName()); //SK PSC+region aka name
        stmt.setString(2, w.getRegionName()); //F ZBS-BA aka filename aka excel sheet name

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            int id = rs.getInt("warehouse_id");
            return id;
        }
        
        throw new SQLException("insert failed");
    }


    public boolean deleteFullWarehouse(int warehouseID) throws SQLException {
        boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        String sql = "DELETE FROM Warehouse where warehouse_ID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, warehouseID);


            int removed = stmt.executeUpdate();
            connection.commit();
            return removed > 0;
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(autoCommit);
        }
    }


    public boolean updateFullWarehouse(Warehouse w) throws SQLException {

        try {
            connection.setAutoCommit(false);

            //update core info
            boolean updated = updateWarehouse(w);

            if (!updated) {
                connection.rollback();
                return false;
            }

            //reinsert regions + pl
            RegionDAO regionDAO = RegionDAO.getInstance();
            PriceListDAO priceListDAO = PriceListDAO.getInstance();

            //remove old regions+pl cascade
            regionDAO.deleteAllRegions(w.getId());

            //reinsert regions
            if (w.getRegionTable() != null) {
                for (var entry : w.getRegionTable().getEntries()) {
                    regionDAO.insertFullRegion(entry, w);
                }
            }

            //reinsert pricelist
            if (w.getPriceList() != null) {
                priceListDAO.deletePriceListByWarehouseID(w.getId());

                for (var item : w.getPriceList().getEntries()) {
                    priceListDAO.insertPriceListEntry(item, w.getId());
                }
            }

            connection.commit();
            return true;

        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public boolean updateWarehouse(Warehouse w) throws SQLException {

        String sql = """
        UPDATE Warehouse
        SET warehouse_region_name = ?,
            price_list_file = ?
        WHERE warehouse_ID = ?;
    """;

        PreparedStatement stmt = connection.prepareStatement(sql);

        stmt.setString(1, w.getName());          // warehouse_region_name
        stmt.setString(2, w.getRegionName());    // price_list_file (your file/ref field)
        stmt.setInt(3, w.getId());

        int affectedRows = stmt.executeUpdate();

        return affectedRows > 0;
    }

}
