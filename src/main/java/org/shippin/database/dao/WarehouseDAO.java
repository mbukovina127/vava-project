package org.shippin.database.dao;

import org.shippin.domain.BriefWarehouse;
import org.shippin.domain.Warehouse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WarehouseDAO extends BaseDAO {
    public WarehouseDAO(Connection conn) {
        super(conn);
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


        RegionDAO regionDAO = new RegionDAO(connection);
        PriceListDAO priceListDAO = new PriceListDAO(connection);

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
        //FIXME price_list_file
        String sql = """
                INSERT INTO Warehouse(warehouse_id, warehouse_region_name,storage_region, price_list_file)
                VALUES (?,?,?)
                ON CONFLICT(warehouse_id)
                DO UPDATE SET
                    warehouse_id = EXCLUDED.warehouse_id,
                    warehouse_region_name = EXCLUDED.warehouse_region_name,
                    price_list_file = EXCLUDED.price_list_file;""";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, w.getId());
        stmt.setString(2, w.getName()); //SK PSC+region aka name
        stmt.setString(3, w.getRegionName()); //F ZBS-BA aka filename aka excel sheet name // parameter index from 4 to 3

        stmt.executeUpdate();
    }

    /**
     * inserts warehouse core info with both tables
     */
    public void insertFullWarehouse(Warehouse warehouse) throws SQLException {

        try {
            connection.setAutoCommit(false);

            WarehouseDAO warehouseDAO = new WarehouseDAO(connection);
            warehouseDAO.upsertWarehouse(warehouse);

            if (warehouse.getRegionTable() != null) {
                RegionDAO regionDAO = new RegionDAO(connection);

                for (var entry : warehouse.getRegionTable().getEntries()) {
                    regionDAO.insertFullRegion(entry, warehouse);
                }
            }

            if (warehouse.getPriceList() != null) {
                PriceListDAO priceListDAO = new PriceListDAO(connection);

                for (var item : warehouse.getPriceList().getEntries()) {
                    priceListDAO.insertPriceListEntry(item, warehouse.getName());
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


}
