package org.shippin.app.DAO;

import java.sql.Connection;
import java.sql.SQLException;
public abstract class BaseDAO {

    protected Connection connection;

    public BaseDAO(Connection conn){
        connection = conn;
    }

}