import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shippin.database.DBConnector;
import org.shippin.database.dao.UserDAO;
import org.shippin.database.Config;
import org.shippin.domain.User;
import org.shippin.domain.enums.Role;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Tests Dao
 */
public class DaoTest {
    @Test
    public void test_dao() throws SQLException{
        Config cfg=new Config();

        DBConnector dbc = new DBConnector(cfg);
        dbc.connect();


        UserDAO Udao = new UserDAO(dbc.getConnection());
        //testing environment
        try (Statement stmt = dbc.getConnection().createStatement()) {
            stmt.execute("CREATE SCHEMA IF NOT EXISTS balicky");
            stmt.execute("SET search_path TO balicky");
            stmt.execute("DROP TABLE IF EXISTS users");
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                  id SERIAL PRIMARY KEY,
                  name VARCHAR(100) NOT NULL UNIQUE,
                  email VARCHAR(255),
                  role INT NOT NULL
                );
                """);
        }

        User test_user = new User(1,"John Green", "j.green@stuba.sk", Role.USER);

        Udao.insert(test_user);

        User fetched_user = Udao.GetUser(1);

        Assertions.assertNotNull(fetched_user);
        Assertions.assertEquals("John Green", fetched_user.getName());
        Assertions.assertEquals("j.green@stuba.sk", fetched_user.getEmail());
        Assertions.assertEquals(Role.USER, fetched_user.getRole());

    }
}
