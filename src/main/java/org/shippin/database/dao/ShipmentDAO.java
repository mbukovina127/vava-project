package org.shippin.database.dao;

import org.shippin.domain.*;
import org.shippin.domain.enums.State;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShipmentDAO extends BaseDAO {

    public ShipmentDAO(Connection conn) {
        super(conn);
    }

    //maps db to object
    private Shipment mapShipment(ResultSet rs) throws SQLException {
        Shipment sh = new Shipment();
        sh.setId(rs.getInt("id"));
        sh.setState(State.valueOf(rs.getString("status")));
        sh.setFuelCost(rs.getFloat("fuel_cost"));
        sh.setTotalCost(rs.getFloat("total_cost"));
        sh.setDeliveryDate(rs.getTimestamp("delivery_date"));
        sh.setDestinationPostalCode(rs.getString("destination_postal_code"));
        sh.setServices(getServices(sh.getId()));
        return sh;
    }


    public Shipment getShipmentById(int shipmentID) throws SQLException {
        String sql = ""; //TODO

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, shipmentID);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            State state = State.valueOf(rs.getString("status"));

            Shipment sh = new Shipment();
            sh.setId(rs.getInt("id"));
            sh.setState(state);
            sh.setFuelCost(rs.getFloat("fuel_cost"));
            sh.setTotalCost(rs.getFloat("total_cost"));
            sh.setDeliveryDate(rs.getTimestamp("delivery_date"));
            sh.setDestinationPostalCode(rs.getString("destination_postal_code"));

            // services
            sh.setServices(getServices(shipmentID));

            return sh;
        }

        return null;
    }

    public ArrayList<AdditionalService> getServices(int shipmentID) throws SQLException {
        ArrayList<AdditionalService> serviceList = new ArrayList<>();
        String sql = ""; //TODO

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, shipmentID);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            AdditionalService as = new AdditionalService(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getFloat("price"),
                    rs.getFloat("weight")
            );
            serviceList.add(as);
        }

        return serviceList;
    }

    public List<Shipment> getShipmentByWarehouseID(int warehouseID) throws SQLException {
        String sql = ""; //TODO

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, warehouseID);
        ResultSet rs = stmt.executeQuery();

        List<Shipment> shipments = new ArrayList<>();

        while (rs.next()) {
            shipments.add(mapShipment(rs));
        }

        return shipments;
    }

    public List<Shipment> getShipmentByUserID(int userID) throws SQLException {
        String sql = ""; //TODO

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, userID);
        ResultSet rs = stmt.executeQuery();

        List<Shipment> shipments = new ArrayList<>();

        while (rs.next()) {
            shipments.add(mapShipment(rs));
        }

        return shipments;
    }

    public List<Shipment> getAllShipments() throws SQLException {
        String sql = ""; //TODO

        PreparedStatement stmt = connection.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        List<Shipment> shipments = new ArrayList<>();

        while (rs.next()) {
            shipments.add(mapShipment(rs));
        }

        return shipments;
    }




    public int insertShipment(Shipment sh, int warehouseID, int userID) throws SQLException {
        String sql = ""; //TODO

        PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        stmt.setString(1, sh.getState().name());
        stmt.setFloat(2, sh.getFuelCost());
        stmt.setFloat(3, sh.getTotalCost());
        stmt.setTimestamp(4, (Timestamp) sh.getDeliveryDate());
        stmt.setString(5, sh.getDestinationPostalCode());
        stmt.setInt(6, warehouseID);
        stmt.setInt(7, userID);

        stmt.executeUpdate();


        ResultSet generatedKeys = stmt.getGeneratedKeys();
        if (generatedKeys.next()) {
            int shipmentId = generatedKeys.getInt(1);
            sh.setId(shipmentId);

            insertShipmentServices(shipmentId, sh.getServices());

            return shipmentId;
        }
        return -1;
    }

    private void insertShipmentServices(int shipmentId, List<AdditionalService> services) throws SQLException {
        String sql = ""; //TODO
        PreparedStatement stmt = connection.prepareStatement(sql);
        for (AdditionalService service : services) {
            stmt.setInt(1, shipmentId);
            stmt.setInt(2, service.getId());
            stmt.addBatch();
        }
        stmt.executeBatch();
    }

}