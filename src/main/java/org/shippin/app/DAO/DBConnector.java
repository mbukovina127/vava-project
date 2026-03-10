package org.shippin.app.DAO;

import org.shippin.app.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {

    private Connection connection;
    private final Config cfg;

    public DBConnector(Config cfg) {
        this.cfg = cfg;
    }

    public void connect() throws SQLException {

        String url = "jdbc:postgresql://"
                + cfg.getDb_domain()
                + ":"
                + cfg.getDb_port()
                + "/"
                + cfg.getDb();

        connection = DriverManager.getConnection(
                url,
                cfg.getDb_user(),
                cfg.getDb_user_password()
        );
        //set working schema
        connection.createStatement().execute("SET search_path TO balicky");
    }

    public Connection getConnection() {
        return connection;
    }

}