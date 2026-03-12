package org.shippin.database.dao;

import java.sql.Connection;

public abstract class BaseDAO {

    protected Connection connection;

    public BaseDAO(Connection conn){
        connection = conn;
    }

}