package org.shippin.database.dao;

import org.shippin.domain.*;
import org.shippin.domain.enums.State;
import org.shippin.domain.BriefShippment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShipmentDAO extends BaseDAO {

    private static ShipmentDAO instance;

    private ShipmentDAO() {
        super();
    }

    public static ShipmentDAO getInstance() {
        if (instance == null) {
            instance = new ShipmentDAO();
        }
        return instance;
    }

    //maps db to object
    private Shipment mapShipment(ResultSet rs) throws SQLException {
        Shipment sh = new Shipment();
        sh.setShipment_id(rs.getInt("shipment_ID"));
        sh.setState(State.valueOf(rs.getString("status")));
        sh.setFuel_payment(rs.getFloat("fuel_payment"));
        sh.setTotalCost(rs.getFloat("total_cost"));
        sh.setCreated_at(rs.getTimestamp("created_at"));
        sh.setDest_region(rs.getInt("dest_region"));
        sh.setServices(getShipmentServices(sh.getShipment_id()));
        sh.setUser_ID(rs.getInt("user_ID"));
        return sh;
    }

    private BriefShippment mapBriefShipment(ResultSet rs) throws SQLException {
        BriefShippment sh = new BriefShippment();

        sh.setShipment_id(rs.getInt("shipment_ID"));
        sh.setState(State.valueOf(rs.getString("status")));
        sh.setFuel_payment(rs.getFloat("fuel_payment"));
        sh.setTotalCost(rs.getFloat("total_cost"));
        sh.setCreated_at(rs.getTimestamp("created_at"));
        sh.setDest_region(rs.getInt("dest_region"));
        sh.setUser_ID(rs.getInt("user_ID"));

        // sh.setStartCoordinate(new Coordinates(rs.getFloat("x"), rs.getFloat("y")));

        return sh;
    }


    public Shipment getShipmentById(int shipmentID) throws SQLException {
        String sql = """
                    SELECT s.shipment_ID, s.status, s.fuel_payment, s.total_cost,
                           s.created_at, s.dest_region, s.user_ID
                    FROM Shipment s WHERE s.shipment_ID = ?;
                    """;

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, shipmentID);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return mapShipment(rs);
        }

        return null;
    }

    public BriefShippment getBriefShippmentById(int shipmentID) throws SQLException {
        String sql = """
                    SELECT s.shipment_ID, s.status, s.fuel_payment, s.total_cost,
                           s.created_at, s.dest_region, s.user_ID
                    FROM Shipment s WHERE s.shipment_ID = ?;
            """;

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, shipmentID);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return mapBriefShipment(rs);
        }

        return null;
    }

    public ArrayList<AdditionalService> getShipmentServices(int shipmentID) throws SQLException {
        ArrayList<AdditionalService> serviceList = new ArrayList<>();
        String sql = """
                    SELECT s.service_ID, s.service_name, s.default_cost, s.cost_modificator
                    FROM Service_list sl JOIN Service s ON sl.service_ID = s.service_id
                    WHERE sl.shipment_ID = ?;
                    """;

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, shipmentID);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            AdditionalService as = new AdditionalService(
                    rs.getInt("service_ID"),
                    rs.getString("service_name"),
                    rs.getFloat("default_cost"),
                    rs.getFloat("cost_modificator")
            );
            serviceList.add(as);
        }

        return serviceList;
    }

    public ArrayList<AdditionalService> getSAllServices() throws SQLException {
        ArrayList<AdditionalService> serviceList = new ArrayList<>();
        String sql = """
                    SELECT s.service_ID, s.service_name, s.default_cost, s.cost_modificator
                    FROM Service s;
                    """;

        PreparedStatement stmt = connection.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            AdditionalService as = new AdditionalService(
                    rs.getInt("service_ID"),
                    rs.getString("service_name"),
                    rs.getFloat("default_cost"),
                    rs.getFloat("cost_modificator")
            );
            serviceList.add(as);
        }

        return serviceList;
    }

    public List<ShipmentHistory> getShipmentHistoryByShipmentID(int shipmentID) throws SQLException {
        String sql = """
        SELECT history_ID, timestamp, state, shipment_ID FROM History
        WHERE shipment_ID = ?;
        """;

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, shipmentID);

        ResultSet rs = stmt.executeQuery();

        List<ShipmentHistory> historyList = new ArrayList<>();

        while (rs.next()) {
            ShipmentHistory history = new ShipmentHistory();
            history.setHistory_id(rs.getInt("history_ID"));
            history.setTimestamp(rs.getTimestamp("timestamp"));
            history.setState(State.valueOf(rs.getString("state")));
            history.setShipment_id(rs.getInt("shipment_ID"));

            historyList.add(history);
        }

        return historyList;
    }

    /*
     * adds event into shipments history addShipmentHistory(new history(timestamp,state,shipmentID))
     */
    public int addShipmentHistory(ShipmentHistory history) throws SQLException {
        String sql = "INSERT INTO History(timestamp, state, shipment_ID)VALUES(?, ?, ?)";

        PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        stmt.setTimestamp(1, history.getTimestamp());
        stmt.setString(2, history.getState().name());
        stmt.setInt(3, history.getShipment_id());

        stmt.executeUpdate();

        ResultSet generatedKeys = stmt.getGeneratedKeys();
        if (generatedKeys.next()) {
            int id = generatedKeys.getInt(1);
            history.setHistory_id(id);
            return id;
        }

        return -1;
    }

    public List<Shipment> getShipmentByWarehouseID(int warehouseID) throws SQLException {
        String sql = """
                    SELECT s.shipment_ID, s.status, s.fuel_payment, s.total_cost,
                           s.created_at, s.dest_region, s.user_ID
                    FROM Shipment s WHERE s.warehouse_ID = ?;
                    """;


        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, warehouseID);
        ResultSet rs = stmt.executeQuery();

        List<Shipment> shipments = new ArrayList<>();

        while (rs.next()) {
            shipments.add(mapShipment(rs));
        }

        return shipments;
    }

    public List<BriefShippment> getBriefShippmentsByWarehouseID(int warehouseID) throws SQLException {
        String sql = """
                    SELECT s.shipment_ID, s.status, s.fuel_payment, s.total_cost,
                           s.created_at, s.dest_region, s.user_ID
                    FROM Shipment s WHERE s.warehouse_ID = ?;
                    """;

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, warehouseID);

        ResultSet rs = stmt.executeQuery();

        List<BriefShippment> list = new ArrayList<>();

        while (rs.next()) {
            list.add(mapBriefShipment(rs));
        }

        return list;
    }

    public List<Shipment> getShipmentByUserID(int userID) throws SQLException {
        String sql = """
                    SELECT s.shipment_ID, s.status, s.fuel_payment, s.total_cost,
                           s.created_at, s.dest_region, s.user_ID
                    FROM Shipment s WHERE s.user_ID = ?;
                    """;


        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, userID);
        ResultSet rs = stmt.executeQuery();

        List<Shipment> shipments = new ArrayList<>();

        while (rs.next()) {
            shipments.add(mapShipment(rs));
        }

        return shipments;
    }

    public List<BriefShippment> getBriefShippmentsByUserID(int userID) throws SQLException {
        String sql = """
                    SELECT s.shipment_ID, s.status, s.fuel_payment, s.total_cost,
                           s.created_at, s.dest_region, s.user_ID
                    FROM Shipment s WHERE s.user_ID = ?;
            """;

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, userID);

        ResultSet rs = stmt.executeQuery();

        List<BriefShippment> list = new ArrayList<>();

        while (rs.next()) {
            list.add(mapBriefShipment(rs));
        }

        return list;
    }

    public List<Shipment> getAllShipments() throws SQLException {
        String sql = """
                    SELECT s.shipment_ID, s.status, s.fuel_payment, s.total_cost,
                    s.created_at, s.dest_region, s.user_ID
                    FROM Shipment s;
                    """;

        PreparedStatement stmt = connection.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        List<Shipment> shipments = new ArrayList<>();

        while (rs.next()) {
            shipments.add(mapShipment(rs));
        }

        return shipments;
    }

    public List<BriefShippment> getAllBriefShippments() throws SQLException {
        String sql = """
                    SELECT s.shipment_ID, s.status, s.fuel_payment, s.total_cost,
                    s.created_at, s.dest_region, s.user_ID
                    FROM Shipment s;
            """;

        PreparedStatement stmt = connection.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        List<BriefShippment> list = new ArrayList<>();

        while (rs.next()) {
            list.add(mapBriefShipment(rs));
        }

        return list;
    }

    public List<Shipment> getAllShipmentsByDate(Timestamp from, Timestamp to) throws SQLException {
        String sql = """
                SELECT s.shipment_ID, s.status, s.fuel_payment,
                s.total_cost, s.created_at, s.dest_region, s.user_ID
                FROM Shipment s
                WHERE s.created_at >=  ?
                AND s.created_at <  ?;""";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setTimestamp(1, from);
        stmt.setTimestamp(2, to);

        ResultSet rs = stmt.executeQuery();

        List<Shipment> shipments = new ArrayList<>();

        while (rs.next()) {
            shipments.add(mapShipment(rs));
        }

        return shipments;
    }

    public List<BriefShippment> getBriefShippmentsByDate(Timestamp from, Timestamp to) throws SQLException {
        String sql = """
                SELECT s.shipment_ID, s.status, s.fuel_payment,
                s.total_cost, s.created_at, s.dest_region, s.user_ID
                FROM Shipment s
                WHERE s.created_at >=  ?
                AND s.created_at <  ?;""";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setTimestamp(1, from);
        stmt.setTimestamp(2, to);

        ResultSet rs = stmt.executeQuery();

        List<BriefShippment> list = new ArrayList<>();

        while (rs.next()) {
            list.add(mapBriefShipment(rs));
        }

        return list;
    }

    public boolean updateShipmentByID(Shipment sh) throws SQLException {
        String sql = """
                UPDATE Shipment
                SET status = ?,
                fuel_payment = ?,
                total_cost = ?,
                dest_region = ?,
                created_at = ?,
                user_ID = ?
                WHERE shipment_ID = ?;""";

        PreparedStatement stmt = connection.prepareStatement(sql);

        stmt.setString(1, sh.getState().name());
        stmt.setFloat(2, sh.getFuel_payment());
        stmt.setFloat(3, sh.getTotalCost());
        stmt.setInt(4, sh.getDest_region());
        stmt.setTimestamp(5, (Timestamp) sh.getCreated_at());
        stmt.setInt(6, sh.getUser_ID());
        stmt.setInt(7, sh.getShipment_id());

        int affectedRows = stmt.executeUpdate();

        //update related service list
        if (affectedRows > 0) {
            //delete old services
            String deleteSql = "DELETE FROM Service_list WHERE shipment_ID = ?;";
            PreparedStatement deleteStmt = connection.prepareStatement(deleteSql);
            deleteStmt.setInt(1, sh.getShipment_id());
            deleteStmt.executeUpdate();

            insertShipmentServices(sh.getShipment_id(), sh.getServices());
        }

        return affectedRows > 0;
    }




    public int insertShipment(Shipment sh, int warehouseID, int userID) throws SQLException {
        String sql = """
                INSERT INTO Shipment (user_ID, warehouse_ID, dest_region, fuel_payment, total_cost, created_at, status, is_sp)
                    VALUES (?,?,?,?,?,?,?,?)
                    ON CONFLICT (shipment_ID)
                    DO UPDATE SET
                    	user_ID = EXCLUDED.user_ID,
                    	warehouse_ID = EXCLUDED.warehouse_ID,
                    	dest_region = EXCLUDED.dest_region,
                    	fuel_payment = EXCLUDED.fuel_payment,
                    	total_cost = EXCLUDED.total_cost,
                    	created_at = EXCLUDED.created_at,
                    	status = EXCLUDED.status,
                    	is_sp = EXCLUDED.is_sp;
                """;

        PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        stmt.setInt(1, userID);
        stmt.setInt(2, warehouseID);
        stmt.setInt(3, sh.getDest_region());
        stmt.setFloat(4, sh.getFuel_payment());
        stmt.setFloat(5, sh.getTotalCost());
        stmt.setTimestamp(6, (Timestamp) sh.getCreated_at());
        stmt.setString(7, sh.getState().name());
        stmt.setBoolean(8, false);

        stmt.executeUpdate();


        ResultSet generatedKeys = stmt.getGeneratedKeys();
        if (generatedKeys.next()) {
            int shipmentId = generatedKeys.getInt(1);
            sh.setShipment_id(shipmentId);

            insertShipmentServices(shipmentId, sh.getServices());

            return shipmentId;
        }
        return -1;
    }

    public boolean deleteShipment(int shipmentID) throws SQLException {
        String sql = """
        DELETE FROM Shipment WHERE shipment_ID = ?;
        """;
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, shipmentID);
        return stmt.executeUpdate() > 0;
    }

    private void insertShipmentServices(int shipmentId, List<AdditionalService> services) throws SQLException {
        String sql = """
                    INSERT INTO Service_list(shipment_ID, service_ID)  VALUES (?, ?);
                    """;
        PreparedStatement stmt = connection.prepareStatement(sql);
        for (AdditionalService service : services) {
            stmt.setInt(1, shipmentId);
            stmt.setInt(2, service.getId());
            stmt.addBatch();
        }
        stmt.executeBatch();
    }

}