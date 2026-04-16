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

        stmt.setString(1, user.getName());
        stmt.setString(2, user.getEmail());
        stmt.setInt(3, user.getRole().ordinal());

        stmt.executeUpdate();
    }

    public User GetUser(int id) throws SQLException {

        String sql = "";

        PreparedStatement stmt = connection.prepareStatement(sql);

        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    Role.values()[(rs.getInt("role"))]
            );
        }
        return null;
    }
}