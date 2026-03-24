package org.shippin.app.DAO;


import java.sql.Connection;
public abstract class BaseDAO {

    protected Connection connection;

    public BaseDAO(Connection conn){
        connection = conn;
    }

}