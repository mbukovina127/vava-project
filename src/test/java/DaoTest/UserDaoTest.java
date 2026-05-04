package DaoTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.shippin.database.DBConnector;
import org.shippin.database.dao.UserDAO;
import org.shippin.domain.User;
import org.shippin.domain.enums.Role;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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

    @Test
    @DisplayName("insert stores user in database")
    void insertStoresUserInDatabase() throws SQLException {
        String email = uniqueEmail("alice");

        userDAO.insert(new User("Alice", "Goober", email, Role.USER));

        PreparedStatement stmt = DBConnector.getInstance().getConnection()
                .prepareStatement("SELECT first_name, last_name, email, role FROM Users WHERE email = ?");
        stmt.setString(1, email);

        ResultSet rs = stmt.executeQuery();

        assertTrue(rs.next());
        assertEquals("Alice", rs.getString("first_name"));
        assertEquals("Goober", rs.getString("last_name"));
        assertEquals(email, rs.getString("email"));
        assertEquals(Role.USER.ordinal(), rs.getInt("role"));
    }

    @Test
    @DisplayName("GetUser returns inserted user by generated ID")
    void getUserReturnsInsertedUserByGeneratedId() throws SQLException {
        String email = uniqueEmail("bob");

        userDAO.insert(new User("Bob", "Tester", email, Role.ADMIN));

        int id = getUserIdByEmail(email);
        User fetched = userDAO.GetUser(id);

        assertNotNull(fetched);
        assertEquals("Bob", fetched.getFirstName());
        assertEquals("Tester", fetched.getLastName());
        assertEquals(email, fetched.getEmail());
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
        String email = uniqueEmail("duplicate");

        userDAO.insert(new User("First", "User", email, Role.USER));

        assertThrows(SQLException.class, () ->
                userDAO.insert(new User("Second", "User", email, Role.ADMIN))
        );
    }

    @Test
    @DisplayName("role ordinal is persisted correctly")
    void roleOrdinalIsPersistedCorrectly() throws SQLException {
        String email = uniqueEmail("power");

        userDAO.insert(new User("Power", "User", email, Role.POWER_USER));

        int id = getUserIdByEmail(email);
        User fetched = userDAO.GetUser(id);

        assertNotNull(fetched);
        assertEquals(Role.POWER_USER, fetched.getRole());
    }

    @Test
    @DisplayName("insert multiple users produces distinct generated IDs")
    void insertMultipleUsersProducesDistinctGeneratedIds() throws SQLException {
        String carolEmail = uniqueEmail("carol");
        String daveEmail = uniqueEmail("dave");

        userDAO.insert(new User("Carol", "One", carolEmail, Role.USER));
        userDAO.insert(new User("Dave", "Two", daveEmail, Role.ADMIN));

        int carolId = getUserIdByEmail(carolEmail);
        int daveId = getUserIdByEmail(daveEmail);

        assertNotEquals(carolId, daveId);
        assertTrue(carolId > 0);
        assertTrue(daveId > 0);
    }

    @Test
    @DisplayName("multiple inserted users can be retrieved independently")
    void multipleInsertedUsersCanBeRetrievedIndependently() throws SQLException {
        String eveEmail = uniqueEmail("eve");
        String frankEmail = uniqueEmail("frank");

        userDAO.insert(new User("Eve", "Alpha", eveEmail, Role.USER));
        userDAO.insert(new User("Frank", "Beta", frankEmail, Role.ADMIN));

        User eve = userDAO.GetUser(getUserIdByEmail(eveEmail));
        User frank = userDAO.GetUser(getUserIdByEmail(frankEmail));

        assertNotNull(eve);
        assertNotNull(frank);

        assertEquals("Eve", eve.getFirstName());
        assertEquals("Alpha", eve.getLastName());
        assertEquals(eveEmail, eve.getEmail());
        assertEquals(Role.USER, eve.getRole());

        assertEquals("Frank", frank.getFirstName());
        assertEquals("Beta", frank.getLastName());
        assertEquals(frankEmail, frank.getEmail());
        assertEquals(Role.ADMIN, frank.getRole());
    }

    @Test
    @DisplayName("findByEmail returns inserted user")
    void findByEmailReturnsInsertedUser() throws SQLException {
        String email = uniqueEmail("find");

        userDAO.insert(new User("Find", "Me", email, Role.POWER_USER));

        User fetched = userDAO.findByEmail(email);

        assertNotNull(fetched);
        assertTrue(fetched.getId() > 0);
        assertEquals("Find", fetched.getFirstName());
        assertEquals("Me", fetched.getLastName());
        assertEquals(email, fetched.getEmail());
        assertEquals(Role.POWER_USER, fetched.getRole());
        assertNull(fetched.getPassword());
    }

    @Test
    @DisplayName("findByEmail returns null for missing email")
    void findByEmailReturnsNullForMissingEmail() throws SQLException {
        User fetched = userDAO.findByEmail(uniqueEmail("missing"));

        assertNull(fetched);
    }

    @Test
    @DisplayName("authenticate returns user when email and password match")
    void authenticateReturnsUserWhenCredentialsMatch() throws SQLException {
        String email = uniqueEmail("auth");

        userDAO.insert(new User("Auth", "User", email, Role.ADMIN));

        User authenticated = userDAO.authenticate(email, "default");

        assertNotNull(authenticated);
        assertTrue(authenticated.getId() > 0);
        assertEquals("Auth", authenticated.getFirstName());
        assertEquals("User", authenticated.getLastName());
        assertEquals(email, authenticated.getEmail());
        assertEquals(Role.ADMIN, authenticated.getRole());
        assertNull(authenticated.getPassword());
    }

    @Test
    @DisplayName("authenticate returns null when password does not match")
    void authenticateReturnsNullWhenPasswordDoesNotMatch() throws SQLException {
        String email = uniqueEmail("authwrong");

        userDAO.insert(new User("Auth", "Wrong", email, Role.USER));

        User authenticated = userDAO.authenticate(email, "wrong-password");

        assertNull(authenticated);
    }

    @Test
    @DisplayName("authenticate returns null for missing email")
    void authenticateReturnsNullForMissingEmail() throws SQLException {
        User authenticated = userDAO.authenticate(uniqueEmail("missingauth"), "default");

        assertNull(authenticated);
    }

    @Test
    @DisplayName("getAllUsers includes inserted users")
    void getAllUsersIncludesInsertedUsers() throws SQLException {
        String firstEmail = uniqueEmail("allone");
        String secondEmail = uniqueEmail("alltwo");

        userDAO.insert(new User("All", "One", firstEmail, Role.USER));
        userDAO.insert(new User("All", "Two", secondEmail, Role.ADMIN));

        List<User> users = userDAO.getAllUsers();

        assertNotNull(users);
        assertTrue(users.stream().anyMatch(user -> firstEmail.equals(user.getEmail())));
        assertTrue(users.stream().anyMatch(user -> secondEmail.equals(user.getEmail())));
    }

    @Test
    @DisplayName("updateRole changes user role")
    void updateRoleChangesUserRole() throws SQLException {
        String email = uniqueEmail("updaterole");

        userDAO.insert(new User("Role", "Before", email, Role.USER));
        int userId = getUserIdByEmail(email);

        userDAO.updateRole(userId, Role.ADMIN);

        User updated = userDAO.findByEmail(email);

        assertNotNull(updated);
        assertEquals(Role.ADMIN, updated.getRole());
    }

    @Test
    @DisplayName("deleteUser removes existing user")
    void deleteUserRemovesExistingUser() throws SQLException {
        String email = uniqueEmail("delete");

        userDAO.insert(new User("Delete", "Me", email, Role.USER));
        int userId = getUserIdByEmail(email);

        boolean deleted = userDAO.deleteUser(userId);

        assertTrue(deleted);
        assertNull(userDAO.GetUser(userId));
    }

    @Test
    @DisplayName("deleteUser returns false for non-existing user")
    void deleteUserReturnsFalseForNonExistingUser() throws SQLException {
        boolean deleted = userDAO.deleteUser(-999);

        assertFalse(deleted);
    }

    private int getUserIdByEmail(String email) throws SQLException {
        PreparedStatement stmt = DBConnector.getInstance().getConnection()
                .prepareStatement("SELECT user_ID FROM Users WHERE email = ?");
        stmt.setString(1, email);

        ResultSet rs = stmt.executeQuery();
        assertTrue(rs.next(), "Inserted user should exist in database");

        return rs.getInt("user_ID");
    }

    private String uniqueEmail(String prefix) {
        return prefix + "." + System.nanoTime() + "@dao.test";
    }
}