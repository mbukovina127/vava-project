import org.junit.jupiter.api.Test;
import org.shippin.database.DBConnector;
import org.shippin.database.dao.UserDAO;
import org.shippin.database.Config;
import org.shippin.models.User;

import java.sql.SQLException;

/**
 * Tests Dao
 */
public class DaoTest {
    @Test
    public void test_dao(){
      Config cfg=new Config();

        DBConnector dbc = new DBConnector(cfg);

        try {
            dbc.connect();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        UserDAO Udao = new UserDAO(dbc.getConnection());


        User u = null;
        try {
            u = Udao.GetUser(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println(u.getName());
    }
}
