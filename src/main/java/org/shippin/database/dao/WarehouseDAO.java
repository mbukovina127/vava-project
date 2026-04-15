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

    /**
     * inserts warehouse core info
     */
    public void upsertWarehouse(Warehouse w) throws SQLException {
        String sql = "";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, w.getId());
        stmt.setString(2, w.getName()); //SK PSC+region aka name
        stmt.setString(4, w.getRegionName()); //F ZBS-BA aka filename aka excel sheet name

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
