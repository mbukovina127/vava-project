package org.shippin.database;

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
                + cfg.getDbDomain()
                + ":"
                + cfg.getDbPort()
                + "/"
                + cfg.getDbName();

        connection = DriverManager.getConnection(
                url,
                cfg.getDbUser(),
                cfg.getDbUserPassword()
        );

        connection.createStatement().execute("SET search_path TO balicky");
    }

    public Connection getConnection() {
        return connection;
    }

}