package org.shippin.database.dao;

import org.shippin.domain.User;
import org.shippin.domain.enums.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO extends BaseDAO {

    public UserDAO(Connection conn) {
        super(conn);
    }

    public void insert(User user) throws SQLException {

        String sql = "";
        PreparedStatement stmt = connection.prepareStatement(sql);

        stmt.setString(1, user.getFirstName());
        stmt.setString(2, user.getLastName());
        stmt.setString(3, user.getEmail());
        stmt.setString(4, user.getPassword());
        stmt.setInt(5, user.getRole().ordinal());
        stmt.executeUpdate();
    }

    public User GetUser(int id) throws SQLException {

        String sql = "";

        PreparedStatement stmt = connection.prepareStatement(sql);

        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return new User(
                    rs.getString("firstName"),
                    rs.getString("lastName"),
                    rs.getString("email"),
                    Role.values()[(rs.getInt("role"))]
            );
        }
        return null;
    }

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
            return rs.getInt("user_id"); // valid token
        }

        return null; // invalid or expired
    }

}