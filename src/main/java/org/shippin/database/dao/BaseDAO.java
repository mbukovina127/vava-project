package org.shippin.database.dao;


import java.sql.Connection;
import org.shippin.database.DBConnector;

public abstract class BaseDAO {

    protected Connection connection;

    protected BaseDAO() {
        this.connection = DBConnector.getInstance().getConnection();
    }

}