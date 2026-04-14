package org.shippin.database.dao;

import org.shippin.domain.Warehouse;

import java.sql.*;

public class WarehouseDAO extends BaseDAO {
    public WarehouseDAO(Connection conn) {
        super(conn);
    }

    public Warehouse getById(int id) throws SQLException {
        String sql = "";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        if (!rs.next()) return null;

        Warehouse warehouse = new Warehouse();
        warehouse.setId(rs.getInt("id"));
        warehouse.setName(rs.getString("name"));
        warehouse.setRegionName(rs.getString("region_name"));


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


    public void upsertWarehouse(Warehouse w) throws SQLException {
        String sql = """
                INSERT INTO Warehouse(warehouse_id, storage_region, warehouse_region_name, price_list_file)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (warehouse_id)
                DO UPDATE SET
                    warehouse_id = EXCLUDED.warehouse_id,
                    storage_region = EXCLUDED.storage_region,
                    warehouse_region_name = EXCLUDED.warehouse_region_name,
                    price_list_file = EXCLUDED.price_list_file
                """;

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, w.getId());
        //stmt.setInt(2, w.getStorageRegion()); //FIXME
        stmt.setString(3, w.getName());
        //stmt.setString(4, w.getPriceListFile()); //FIXME

        stmt.executeUpdate();
    }


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
