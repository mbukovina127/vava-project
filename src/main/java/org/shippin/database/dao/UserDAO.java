package org.shippin.database.dao;

import lombok.extern.log4j.Log4j2;
import org.shippin.domain.User;
import org.shippin.domain.enums.Role;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class UserDAO extends BaseDAO {

    private static UserDAO instance;

    private UserDAO() {
        super();
    }

    public static UserDAO getInstance() {
        if (instance == null) {
            instance = new UserDAO();
        }
        return instance;
    }

    public void insert(User user) throws SQLException {

        String sql = "INSERT INTO Users(first_name, last_name, email, password, role, is_active) VALUES (?,?,?,?,?, true);";
        PreparedStatement stmt = connection.prepareStatement(sql);

        stmt.setString(1, user.getFirstName());
        stmt.setString(2, user.getLastName());
        stmt.setString(3, user.getEmail());
        stmt.setString(4, user.getPassword());
        stmt.setInt(5, user.getRole().ordinal());
        stmt.executeUpdate();
        log.info("Inserted user: {}", user.getEmail());
    }

    public User GetUser(int id) throws SQLException {

        String sql = "SELECT user_ID, first_name, last_name, email, role FROM Users WHERE user_ID = ? AND is_active = true;";

        PreparedStatement stmt = connection.prepareStatement(sql);

        stmt.setInt(1, id);
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

    /** Returns the user (without password) if email + already-hashed password match, null otherwise. */
    public User authenticate(String email, String passwordHash) throws SQLException {
        String sql = "SELECT user_ID, first_name, last_name, email, role FROM Users WHERE email = ? AND password = ? AND is_active = true;";
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
        String sql = "SELECT user_ID, first_name, last_name, email, role FROM Users WHERE email = ? AND is_active = true;";
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
        String sql = """
        SELECT user_ID, first_name, last_name, email, role
        FROM Users
        WHERE is_active = true
        ORDER BY user_ID;
        """;
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
        log.info("Updated role for user #{} -> {}", userId, role);
    }

    public boolean deleteUser(int userID) throws SQLException {

        String sql = "UPDATE Users SET is_active = false WHERE user_ID = ?;";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, userID);

        int affectedRows = stmt.executeUpdate();

        if (affectedRows > 0) log.info("Soft-deleted user #{}", userID);
        else log.warn("deleteUser: user #{} not found", userID);

        return affectedRows > 0;
    }



}