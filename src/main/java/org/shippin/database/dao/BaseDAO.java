package org.shippin.database.dao;


import java.sql.Connection;
import java.sql.SQLException;
import org.shippin.database.DBConnector;

public abstract class BaseDAO {

    protected Connection connection;

    protected BaseDAO() {
        this.connection = DBConnector.getInstance().getConnection();
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        connection.setAutoCommit(autoCommit);
    }

    public void commit() throws SQLException {
        connection.commit();
    }

    public void rollback() throws SQLException {
        connection.rollback();
    }
}