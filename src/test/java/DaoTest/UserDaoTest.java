package DaoTest;

import org.junit.jupiter.api.*;
import org.shippin.database.DBConnector;
import org.shippin.database.dao.UserDAO;
import org.shippin.domain.User;
import org.shippin.domain.enums.Role;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

public class UserDaoTest {

    private static UserDAO userDAO;

    @BeforeAll
    static void connect() {
        userDAO = UserDAO.getInstance();
    }

    @BeforeEach
    void begin() throws SQLException {
        DBConnector.getInstance().getConnection().setAutoCommit(false);
    }

    @AfterEach
    void rollback() throws SQLException {
        DBConnector.getInstance().getConnection().rollback();
        DBConnector.getInstance().getConnection().setAutoCommit(true);
    }

    private int getUserIdByEmail(String email) throws SQLException {
        PreparedStatement stmt = DBConnector.getInstance().getConnection()
                .prepareStatement("SELECT user_ID FROM Users WHERE email = ?");
        stmt.setString(1, email);

        ResultSet rs = stmt.executeQuery();
        assertTrue(rs.next(), "Inserted user should exist in database");

        return rs.getInt("user_ID");
    }

    @Test
    @DisplayName("insert stores user in database")
    void insertStoresUserInDatabase() throws SQLException {
        userDAO.insert(new User("Alice", "Goober", "alice.dao@test.com", Role.USER));

        PreparedStatement stmt = DBConnector.getInstance().getConnection()
                .prepareStatement("SELECT first_name, last_name, email, role FROM Users WHERE email = ?");
        stmt.setString(1, "alice.dao@test.com");

        ResultSet rs = stmt.executeQuery();

        assertTrue(rs.next());
        assertEquals("Alice", rs.getString("first_name"));
        assertEquals("Goober", rs.getString("last_name"));
        assertEquals("alice.dao@test.com", rs.getString("email"));
        assertEquals(Role.USER.ordinal(), rs.getInt("role"));
    }

    @Test
    @DisplayName("GetUser returns inserted user by generated ID")
    void getUserReturnsInsertedUserByGeneratedId() throws SQLException {
        userDAO.insert(new User("Bob", "Tester", "bob.dao@test.com", Role.ADMIN));

        int id = getUserIdByEmail("bob.dao@test.com");
        User fetched = userDAO.GetUser(id);

        assertNotNull(fetched);
        assertEquals("Bob", fetched.getFirstName());
        assertEquals("Tester", fetched.getLastName());
        assertEquals("bob.dao@test.com", fetched.getEmail());
        assertEquals(Role.ADMIN, fetched.getRole());
    }

    @Test
    @DisplayName("GetUser returns null for non-existent ID")
    void getUserReturnsNullForNonExistentId() throws SQLException {
        assertNull(userDAO.GetUser(-999));
    }

    @Test
    @DisplayName("insert with duplicate email throws SQLException")
    void insertDuplicateEmailThrowsSQLException() throws SQLException {
        userDAO.insert(new User("First", "User", "duplicate.dao@test.com", Role.USER));

        assertThrows(SQLException.class, () ->
                userDAO.insert(new User("Second", "User", "duplicate.dao@test.com", Role.ADMIN))
        );
    }

    @Test
    @DisplayName("role ordinal is persisted correctly")
    void roleOrdinalIsPersistedCorrectly() throws SQLException {
        userDAO.insert(new User("Power", "User", "power.dao@test.com", Role.POWER_USER));

        int id = getUserIdByEmail("power.dao@test.com");
        User fetched = userDAO.GetUser(id);

        assertNotNull(fetched);
        assertEquals(Role.POWER_USER, fetched.getRole());
    }

    @Test
    @DisplayName("insert multiple users produces distinct generated IDs")
    void insertMultipleUsersProducesDistinctGeneratedIds() throws SQLException {
        userDAO.insert(new User("Carol", "One", "carol.one@test.com", Role.USER));
        userDAO.insert(new User("Dave", "Two", "dave.two@test.com", Role.ADMIN));

        int carolId = getUserIdByEmail("carol.one@test.com");
        int daveId = getUserIdByEmail("dave.two@test.com");

        assertNotEquals(carolId, daveId);
        assertTrue(carolId > 0);
        assertTrue(daveId > 0);
    }

    @Test
    @DisplayName("multiple inserted users can be retrieved independently")
    void multipleInsertedUsersCanBeRetrievedIndependently() throws SQLException {
        userDAO.insert(new User("Eve", "Alpha", "eve.alpha@test.com", Role.USER));
        userDAO.insert(new User("Frank", "Beta", "frank.beta@test.com", Role.ADMIN));

        User eve = userDAO.GetUser(getUserIdByEmail("eve.alpha@test.com"));
        User frank = userDAO.GetUser(getUserIdByEmail("frank.beta@test.com"));

        assertNotNull(eve);
        assertNotNull(frank);

        assertEquals("Eve", eve.getFirstName());
        assertEquals("Alpha", eve.getLastName());
        assertEquals(Role.USER, eve.getRole());

        assertEquals("Frank", frank.getFirstName());
        assertEquals("Beta", frank.getLastName());
        assertEquals(Role.ADMIN, frank.getRole());
    }
}