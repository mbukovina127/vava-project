package org.shippin.database.dao;

import org.shippin.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO extends BaseDAO {

    public UserDAO(Connection conn) {
        super(conn);
    }

    public void insert(User user) throws SQLException {

        String sql = "INSERT INTO users(name,role) VALUES (?,?)";

        PreparedStatement stmt = connection.prepareStatement(sql);

        stmt.setString(1, user.getName());
        stmt.setString(2, user.getRole());

        stmt.executeUpdate();
    }

    public User GetUser(int id) throws SQLException {

        String sql = "SELECT * FROM balicky.users WHERE id=?";

        PreparedStatement stmt = connection.prepareStatement(sql);

        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("role")
            );
        }
        return null;
    }
}