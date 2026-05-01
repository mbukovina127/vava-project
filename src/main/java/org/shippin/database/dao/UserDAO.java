package org.shippin.database.dao;

import org.shippin.domain.User;
import org.shippin.domain.enums.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO extends BaseDAO {

    public UserDAO(Connection conn) {
        super(conn);
    }

    public void insert(User user) throws SQLException {

        String sql = "INSERT INTO Users(first_name, last_name, email, password, role)VALUES (?,?,?,?,?);";
        PreparedStatement stmt = connection.prepareStatement(sql);

        stmt.setString(1, user.getFirstName());
        stmt.setString(2, user.getLastName());
        stmt.setString(3, user.getEmail());
        stmt.setString(4, user.getPassword());
        stmt.setInt(5, user.getRole().ordinal());
        stmt.executeUpdate();
    }

    public User GetUser(int id) throws SQLException {

        String sql = "SELECT first_name, last_name, email, role FROM Users WHERE user_ID = ?;";

        PreparedStatement stmt = connection.prepareStatement(sql);

        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return new User(
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    Role.values()[(rs.getInt("role"))]
            );
        }
        return null;
    }

    /** Returns the user (without password) if email + already-hashed password match, null otherwise. */
    public User authenticate(String email, String passwordHash) throws SQLException {
        String sql = "SELECT user_ID, first_name, last_name, email, role FROM Users WHERE email = ? AND password = ?;";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, email);
        stmt.setString(2, passwordHash);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return new User(
                    rs.getInt("user_ID"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    null,
                    Role.values()[rs.getInt("role")],
                    null
            );
        }
        return null;
    }

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT user_ID, first_name, last_name, email, role FROM Users WHERE email = ?;";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, email);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return new User(
                    rs.getInt("user_ID"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    null,
                    Role.values()[rs.getInt("role")],
                    null
            );
        }
        return null;
    }

    public List<User> getAllUsers() throws SQLException {
        String sql = "SELECT user_ID, first_name, last_name, email, role FROM Users ORDER BY user_ID;";
        PreparedStatement stmt = connection.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        List<User> result = new ArrayList<>();
        while (rs.next()) {
            result.add(new User(
                    rs.getInt("user_ID"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    null,
                    Role.values()[rs.getInt("role")],
                    null
            ));
        }
        return result;
    }

    public void updateRole(int userId, Role role) throws SQLException {
        String sql = "UPDATE Users SET role = ? WHERE user_ID = ?;";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, role.ordinal());
        stmt.setInt(2, userId);
        stmt.executeUpdate();
    }

    public boolean deleteUser(int userID) throws SQLException {

        String sql = """
        DELETE FROM Users WHERE user_ID = ?;
        """;//Will set null when deleted from shipment

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, userID);

        int affectedRows = stmt.executeUpdate();

        return affectedRows > 0;
    }

/*
Won't be needed since no token will be used ↓
 */


/*
called at login, after login deletes old token, requests new
 */
    public String createAccessToken(int userId) throws SQLException {
        String sql = "";//TODO insert into table with tokens:  delete old token where userID + gen_random_uuid(), expire date, user id + cron to delete old tokens

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, userId);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return rs.getString("token");
        }

        return null;
    }


    public Integer validateAccessToken(String token) throws SQLException {
        String sql = ""; //TODO select WHERE token=token

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, token);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return rs.getInt("user_id"); //valid token
        }

        return null; //invalid or expired
    }


    public void deleteAccessToken(String token) throws SQLException {
        String sql = ""; //TODO

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, token);
        stmt.executeUpdate();
    }

}