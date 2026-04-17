import org.junit.jupiter.api.*;
import org.shippin.database.Config;
import org.shippin.database.DBConnector;
import org.shippin.database.dao.UserDAO;
import org.shippin.domain.User;
import org.shippin.domain.enums.Role;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;


@Disabled("I don't know in what state is the dao")
public class DaoTest {

    private static DBConnector dbc;
    private static UserDAO userDAO;

    @BeforeAll
    static void connect() throws SQLException {
        dbc = new DBConnector(new Config());
        dbc.connect();
        userDAO = new UserDAO(dbc.getConnection());
    }

    @BeforeEach
    void begin() throws SQLException {
        dbc.getConnection().setAutoCommit(false); // start transaction
    }

    @AfterEach
    void rollback() throws SQLException {
        dbc.getConnection().rollback();           // undo everything
        dbc.getConnection().setAutoCommit(true);
    }

    @Test
    void insertAndGetUser() throws SQLException {
        userDAO.insert(new User("Alice", "alice@test.com", Role.USER));

        User fetched = userDAO.GetUser(1);

        assertNotNull(fetched);
        assertEquals("Alice", fetched.getName());
        assertEquals("alice@test.com", fetched.getEmail());
        assertEquals(Role.USER, fetched.getRole());
    }

    @Test
    @DisplayName("GetUser returns null for non-existent ID")
    void getUserNotFound() throws SQLException {
        assertNull(userDAO.GetUser(999));
    }

    @Test
    @DisplayName("Insert duplicate name throws SQLException")
    void insertDuplicateNameFails() throws SQLException {
        userDAO.insert(new User("Bob", "bob@test.com", Role.USER));
        assertThrows(SQLException.class,
                () -> userDAO.insert(new User("Bob", "other@test.com", Role.ADMIN)));
    }

    @Test
    @DisplayName("Role ordinal round-trips correctly for all roles")
    void allRolesRoundTrip() throws SQLException {
        userDAO.insert(new User("User1",  "u1@test.com", Role.USER));
        userDAO.insert(new User("User2",  "u2@test.com", Role.POWER_USER));
        userDAO.insert(new User("User3",  "u3@test.com", Role.ADMIN));

        assertEquals(Role.USER,       userDAO.GetUser(1).getRole());
        assertEquals(Role.POWER_USER, userDAO.GetUser(2).getRole());
        assertEquals(Role.ADMIN,      userDAO.GetUser(3).getRole());
    }
}
