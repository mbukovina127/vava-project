package org.shippin.database;

import lombok.Getter;

@Getter
public class Config {
    //FIXME should be loaded from separate config file in future
    private final String db_domain = "nxt.scay.net";
    private final int db_port = 5454;
    private final String db_user = "admin";
    private final String db_user_password = "nimdanimda*123";
    private final String db = "mydb";
}
