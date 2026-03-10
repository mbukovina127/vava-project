package org.shippin.app;

import org.shippin.app.DAO.DBConnector;
import org.shippin.app.DAO.UserDAO;
import org.shippin.app.models.User;

import java.sql.SQLException;


//temporary class for test
public class dbtest {
    public static void test_dao(){
      Config  cfg=new Config();

        DBConnector dbc = new DBConnector(cfg);

        try {
            dbc.connect();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        UserDAO Udao = new UserDAO(dbc.getConnection());

        User jozko = new User("jozko", "admin");

        try {
            Udao.insert(jozko);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        User u = null;
        try {
            u = Udao.GetUser(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println(u.getName());
    }
}
