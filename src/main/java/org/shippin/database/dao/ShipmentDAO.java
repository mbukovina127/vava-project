package org.shippin.database.dao;

import lombok.extern.log4j.Log4j2;
import org.shippin.domain.*;
import org.shippin.domain.enums.ServiceType;
import org.shippin.domain.enums.State;
import org.shippin.domain.BriefShippment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Log4j2
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
        sh.setWeight(rs.getFloat("weight"));
        sh.setVolume(rs.getFloat("volume"));
        sh.setFuel_payment(rs.getFloat("fuel_payment"));
        sh.setToll(rs.getFloat("toll"));
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
        sh.setWeight(rs.getFloat("weight"));
        sh.setVolume(rs.getFloat("volume"));
        sh.setFuel_payment(rs.getFloat("fuel_payment"));
        sh.setToll(rs.getFloat("toll"));
        sh.setTotalCost(rs.getFloat("total_cost"));
        sh.setCreated_at(rs.getTimestamp("created_at"));
        sh.setDest_region(rs.getInt("dest_region"));
        sh.setUser_ID(rs.getInt("user_ID"));
        return sh;
    }


    public Shipment getShipmentById(int shipmentID) throws SQLException {
        String sql = """
                    SELECT s.shipment_ID, s.status, s.weight, s.volume, s.fuel_payment, s.toll, s.total_cost,
                           s.created_at, s.dest_region, s.user_ID,
                           w.warehouse_ID as wh_id, w.warehouse_region_name as wh_name, w.price_list_file as wh_region
                    FROM Shipment s
                    JOIN Warehouse w ON s.warehouse_ID = w.warehouse_ID
                    WHERE s.shipment_ID = ?;
                    """;

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, shipmentID);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            Shipment sh = mapShipment(rs);
            sh.setWarehouse(new BriefWarehouse(
                    rs.getInt("wh_id"),
                    rs.getString("wh_name"),
                    rs.getString("wh_region")));
            return sh;
        }

        return null;
    }

    public BriefShippment getBriefShippmentById(int shipmentID) throws SQLException {
        String sql = """
                    SELECT s.shipment_ID, s.status, s.weight, s.volume, s.fuel_payment, s.toll, s.total_cost,
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
                    SELECT s.service_ID, s.service_name, s.default_cost, s.cost_modificator, s.description, s.service_type
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
                    rs.getFloat("cost_modificator"),
                    ServiceType.valueOf(rs.getString("service_type")),
                    rs.getString("description")
            );
            serviceList.add(as);
        }

        return serviceList;
    }

    public ArrayList<AdditionalService> getSAllServices() throws SQLException {
        ArrayList<AdditionalService> serviceList = new ArrayList<>();
        String sql = """
                    SELECT s.service_ID, s.service_name, s.default_cost, s.cost_modificator, s.description, s.service_type
                    FROM Service s;
                    """;

        PreparedStatement stmt = connection.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            AdditionalService as = new AdditionalService(
                    rs.getInt("service_ID"),
                    rs.getString("service_name"),
                    rs.getFloat("default_cost"),
                    rs.getFloat("cost_modificator"),
                    ServiceType.valueOf(rs.getString("service_type")),
                    rs.getString("description")
            );
            serviceList.add(as);
        }

        return serviceList;
    }

    public List<ShipmentHistory> getShipmentHistoryByShipmentID(int shipmentID) throws SQLException {
        String sql = """
        SELECT h.history_ID, h.timestamp, h.state, h.shipment_ID, h.user_id,
               u.first_name, u.last_name
        FROM History h
        LEFT JOIN Users u ON h.user_id = u.user_ID
        WHERE h.shipment_ID = ?
        ORDER BY h.timestamp ASC;
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
            history.setUser_id(rs.getInt("user_id"));
            String first = rs.getString("first_name");
            String last  = rs.getString("last_name");
            history.setUserName(first != null ? first + " " + last : null);

            historyList.add(history);
        }

        return historyList;
    }

    /*
     * adds event into shipments history addShipmentHistory(new history(timestamp,state,shipmentID))
     */
    public int addShipmentHistory(ShipmentHistory history) throws SQLException {
        String sql = "INSERT INTO History(timestamp, state, shipment_ID, user_id) VALUES(?, ?, ?, ?)";

        PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        stmt.setTimestamp(1, history.getTimestamp());
        stmt.setString(2, history.getState().name());
        stmt.setInt(3, history.getShipment_id());
        stmt.setObject(4, history.getUser_id() > 0 ? history.getUser_id() : null);

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
                    SELECT s.shipment_ID, s.status, s.weight, s.volume, s.fuel_payment, s.toll, s.total_cost,
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
                    SELECT s.shipment_ID, s.status, s.weight, s.volume, s.fuel_payment, s.toll, s.total_cost,
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
                    SELECT s.shipment_ID, s.status, s.weight, s.volume, s.fuel_payment, s.toll, s.total_cost,
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
                    SELECT s.shipment_ID, s.status, s.weight, s.volume, s.fuel_payment, s.toll, s.total_cost,
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

    //STARA VERZIA, MAX UROBIL UPDATE NOVA FUNKCIA JE NIZSIE.
    /*public List<Shipment> getAllShipments() throws SQLException {
        String sql = """
                    SELECT s.shipment_ID, s.status, s.weight, s.volume, s.fuel_payment, s.toll, s.total_cost,
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
    }*/

    public List<Shipment> getAllShipments() throws SQLException {
        String sql = """
    SELECT s.shipment_ID, s.status, s.weight, s.volume, s.fuel_payment, s.toll, s.total_cost,
    s.created_at, s.dest_region, s.user_ID,
    w.warehouse_ID as wh_id, w.warehouse_region_name as wh_name, w.price_list_file as wh_region
    FROM Shipment s
    JOIN Warehouse w ON s.warehouse_ID = w.warehouse_ID;
    """;

        PreparedStatement stmt = connection.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        List<Shipment> shipments = new ArrayList<>();

        while (rs.next()) {
            Shipment sh = mapShipment(rs);
            sh.setWarehouse(new BriefWarehouse(
                    rs.getInt("wh_id"),
                    rs.getString("wh_name"),
                    rs.getString("wh_region")));

            shipments.add(sh);
        }

        return shipments;
    }

    public List<BriefShippment> getAllBriefShippments() throws SQLException {
        String sql = """
                    SELECT s.shipment_ID, s.status, s.weight, s.volume, s.fuel_payment, s.total_cost,
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
                SELECT s.shipment_ID, s.status, s.weight, s.volume, s.fuel_payment, s.toll,
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
                SELECT s.shipment_ID, s.status, s.weight, s.volume, s.fuel_payment, s.toll,
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

    public boolean updateShipmentStatus(int shipmentId, State state) throws SQLException {
        String sql = "UPDATE Shipment SET status = ? WHERE shipment_ID = ?;";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, state.name());
        stmt.setInt(2, shipmentId);
        boolean updated = stmt.executeUpdate() > 0;
        if (updated) log.info("Shipment #{} status -> {}", shipmentId, state);
        else log.warn("updateShipmentStatus: shipment #{} not found", shipmentId);
        return updated;
    }

    public boolean updateShipmentByID(Shipment sh) throws SQLException {
        String sql = """
                UPDATE Shipment
                SET status = ?,
                weight = ?,
                volume = ?,
                fuel_payment = ?,
                toll = ?,
                total_cost = ?,
                dest_region = ?,
                created_at = ?,
                user_ID = ?
                WHERE shipment_ID = ?;""";

        PreparedStatement stmt = connection.prepareStatement(sql);

        stmt.setString(1, sh.getState().name());
        stmt.setFloat(2, sh.getWeight());
        stmt.setFloat(3, sh.getVolume());
        stmt.setFloat(4, sh.getFuel_payment());
        stmt.setFloat(5, sh.getToll());
        stmt.setFloat(6, sh.getTotalCost());
        stmt.setInt(7, sh.getDest_region());
        stmt.setTimestamp(8, sh.getCreated_at());
        stmt.setInt(9, sh.getUser_ID());
        stmt.setInt(10, sh.getShipment_id());

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
                INSERT INTO Shipment (user_ID, warehouse_ID, dest_region, weight, volume, fuel_payment, toll, total_cost, created_at, status, is_sp)
                    VALUES (?,?,?,?,?,?,?,?,?,?,?)
                    ON CONFLICT (shipment_ID)
                    DO UPDATE SET
                    	user_ID = EXCLUDED.user_ID,
                    	warehouse_ID = EXCLUDED.warehouse_ID,
                    	dest_region = EXCLUDED.dest_region,
                    	weight = EXCLUDED.weight,
                    	volume = EXCLUDED.volume,
                    	fuel_payment = EXCLUDED.fuel_payment,
                    	toll = EXCLUDED.toll,
                    	total_cost = EXCLUDED.total_cost,
                    	created_at = EXCLUDED.created_at,
                    	status = EXCLUDED.status,
                    	is_sp = EXCLUDED.is_sp;
                """;

        PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        stmt.setInt(1, userID);
        stmt.setInt(2, warehouseID);
        stmt.setInt(3, sh.getDest_region());
        stmt.setFloat(4, sh.getWeight());
        stmt.setFloat(5, sh.getVolume());
        stmt.setFloat(6, sh.getFuel_payment());
        stmt.setFloat(7, sh.getToll());
        stmt.setFloat(8, sh.getTotalCost());
        stmt.setTimestamp(9, sh.getCreated_at());
        stmt.setString(10, sh.getState().name());
        stmt.setBoolean(11, false);

        stmt.executeUpdate();


        ResultSet generatedKeys = stmt.getGeneratedKeys();
        if (generatedKeys.next()) {
            int shipmentId = generatedKeys.getInt(1);
            sh.setShipment_id(shipmentId);
            insertShipmentServices(shipmentId, sh.getServices());
            log.info("Inserted shipment #{} for user #{}", shipmentId, userID);
            return shipmentId;
        }
        log.warn("insertShipment: no generated key returned");
        return -1;
    }

    public boolean deleteShipment(int shipmentID) throws SQLException {
        String sql = """
        DELETE FROM Shipment WHERE shipment_ID = ?;
        """;
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, shipmentID);
        boolean deleted = stmt.executeUpdate() > 0;
        if (deleted) log.info("Deleted shipment #{}", shipmentID);
        else log.warn("deleteShipment: shipment #{} not found", shipmentID);
        return deleted;
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