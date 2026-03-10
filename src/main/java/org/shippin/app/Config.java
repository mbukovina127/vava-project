package org.shippin.app;

public class Config {
    //should be loaded from separate config file in future
    private final String db_domain = "nxt.scay.net";
    private final int db_port = 5454;
    private final String db_user = "admin";
    private final String db_user_password = "nimdanimda*123";
    private final String db = "mydb";

    public String getDb_domain() {
        return db_domain;
    }

    public int getDb_port() {
        return db_port;
    }

    public String getDb_user() {
        return db_user;
    }

    public String getDb_user_password() {
        return db_user_password;
    }

    public String getDb() {
        return db;
    }
}
