package ServiceTest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shippin.database.DBConnector;
import org.shippin.domain.User;
import org.shippin.domain.enums.Role;
import org.shippin.services.UserService;


public class UserServiceTest {

    @BeforeEach
    void beginTransaction() throws SQLException {
        DBConnector.getInstance().getConnection().setAutoCommit(false);
        UserService.logout();
    }

    @AfterEach
    void rollbackTransaction() throws SQLException {
        UserService.logout();

        Connection connection = DBConnector.getInstance().getConnection();

        if (!connection.getAutoCommit()) {
            connection.rollback();
        }

        connection.setAutoCommit(true);
    }

    @Test
    void loginStoresCurrentUser() {
        User user = new User("Test", "User", "test@test.com", Role.USER);

        UserService.login(user);

        assertEquals(user, UserService.getUser());
        assertEquals(Role.USER, UserService.getRole());
    }

    @Test
    void logoutClearsCurrentUser() {
        User user = new User("Test", "User", "test@test.com", Role.USER);
        UserService.login(user);

        UserService.logout();

        assertNull(UserService.getUser());
        assertNull(UserService.getRole());
    }

    @Test
    void logoutWhenNoUserIsLoggedInKeepsCurrentUserNull() {
        UserService.logout();

        assertNull(UserService.getUser());
        assertNull(UserService.getRole());
    }

    @Test
    void getRoleReturnsNullWhenNoUserIsLoggedIn() {
        UserService.logout();

        assertNull(UserService.getRole());
    }

    @Test
    void hasAccessReturnsFalseWhenNoUserIsLoggedIn() {
        UserService.logout();

        assertFalse(UserService.hasAccess(Role.USER));
    }

    @Test
    void userHasAccessToUserLevel() {
        User user = new User("Normal", "User", "user@test.com", Role.USER);
        UserService.login(user);

        assertTrue(UserService.hasAccess(Role.USER));
    }

    @Test
    void userDoesNotHaveAccessToPowerUserLevel() {
        User user = new User("Normal", "User", "user@test.com", Role.USER);
        UserService.login(user);

        assertFalse(UserService.hasAccess(Role.POWER_USER));
    }

    @Test
    void powerUserHasAccessToUserLevel() {
        User powerUser = new User("Power", "User", "power@test.com", Role.POWER_USER);
        UserService.login(powerUser);

        assertTrue(UserService.hasAccess(Role.USER));
    }

    @Test
    void powerUserHasAccessToPowerUserLevel() {
        User powerUser = new User("Power", "User", "power@test.com", Role.POWER_USER);
        UserService.login(powerUser);

        assertTrue(UserService.hasAccess(Role.POWER_USER));
    }

    @Test
    void powerUserDoesNotHaveAccessToAdminLevel() {
        User powerUser = new User("Power", "User", "power@test.com", Role.POWER_USER);
        UserService.login(powerUser);

        assertFalse(UserService.hasAccess(Role.ADMIN));
    }

    @Test
    void adminHasAccessToAllLowerLevels() {
        User admin = new User("Admin", "User", "admin@test.com", Role.ADMIN);
        UserService.login(admin);

        assertTrue(UserService.hasAccess(Role.USER));
        assertTrue(UserService.hasAccess(Role.POWER_USER));
        assertTrue(UserService.hasAccess(Role.ADMIN));
    }

    @Test
    void registerStoresUserWithHashedPassword() throws SQLException {
        String email = uniqueEmail("register.hash");
        User user = new User("Registered", "User", email, Role.USER);

        UserService.register(user, "secret-password");

        User stored = UserService.findByEmail(email);
        String storedPassword = findPasswordByEmail(email);

        assertNotNull(stored);
        assertEquals(email, stored.getEmail());
        assertEquals(Role.USER, stored.getRole());
        assertNotNull(storedPassword);
        assertNotEquals("secret-password", storedPassword);
        assertEquals(64, storedPassword.length());
    }

    @Test
    void authenticateReturnsUserAndLogsInWhenPasswordIsCorrect() throws SQLException {
        String email = uniqueEmail("auth.success");
        User user = new User("Auth", "Success", email, Role.POWER_USER);

        UserService.register(user, "correct-password");

        User authenticated = UserService.authenticate(email, "correct-password");

        assertNotNull(authenticated);
        assertEquals(email, authenticated.getEmail());
        assertEquals(Role.POWER_USER, authenticated.getRole());
        assertEquals(authenticated, UserService.getUser());
        assertEquals(Role.POWER_USER, UserService.getRole());
    }

    @Test
    void authenticateReturnsNullAndDoesNotLoginWhenPasswordIsWrong() throws SQLException {
        String email = uniqueEmail("auth.fail");
        User user = new User("Auth", "Fail", email, Role.USER);

        UserService.register(user, "correct-password");
        UserService.logout();

        User authenticated = UserService.authenticate(email, "wrong-password");

        assertNull(authenticated);
        assertNull(UserService.getUser());
        assertNull(UserService.getRole());
    }

    @Test
    void findByEmailReturnsInsertedUser() throws SQLException {
        String email = uniqueEmail("find.by.email");
        int userId = insertUserDirectly("Find", "Email", email, Role.USER, "password");

        User result = UserService.findByEmail(email);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("Find", result.getFirstName());
        assertEquals("Email", result.getLastName());
        assertEquals(email, result.getEmail());
        assertEquals(Role.USER, result.getRole());
    }

    @Test
    void findByEmailReturnsNullForMissingUser() throws SQLException {
        User result = UserService.findByEmail(uniqueEmail("missing.email"));

        assertNull(result);
    }

    @Test
    void getUserByIdReturnsInsertedUser() throws SQLException {
        String email = uniqueEmail("get.user");
        int userId = insertUserDirectly("Get", "User", email, Role.ADMIN, "password");

        User result = UserService.getUser(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("Get", result.getFirstName());
        assertEquals("User", result.getLastName());
        assertEquals(email, result.getEmail());
        assertEquals(Role.ADMIN, result.getRole());
    }

    @Test
    void getUserByIdReturnsNullForMissingUser() throws SQLException {
        User result = UserService.getUser(-999999);

        assertNull(result);
    }

    @Test
    void getAllUsersReturnsInsertedUser() throws SQLException {
        String email = uniqueEmail("get.all");
        int userId = insertUserDirectly("All", "Users", email, Role.USER, "password");

        List<User> users = UserService.getAllUsers();

        assertNotNull(users);
        assertTrue(users.stream().anyMatch(user ->
                user.getId() == userId
                        && user.getEmail().equals(email)
                        && user.getRole() == Role.USER
        ));
    }

    @Test
    void deleteUserReturnsTrueAndMakesUserInactive() throws SQLException {
        String email = uniqueEmail("delete.user");
        int userId = insertUserDirectly("Delete", "User", email, Role.USER, "password");

        boolean deleted = UserService.deleteUser(userId);
        User found = UserService.findByEmail(email);

        assertTrue(deleted);
        assertNull(found);
    }

    @Test
    void deleteUserReturnsFalseForMissingUser() throws SQLException {
        boolean deleted = UserService.deleteUser(-999999);

        assertFalse(deleted);
    }

    @Test
    void updateRoleChangesUserRole() throws SQLException {
        String email = uniqueEmail("update.role");
        int userId = insertUserDirectly("Update", "Role", email, Role.USER, "password");

        UserService.updateRole(userId, Role.ADMIN);

        User result = UserService.getUser(userId);

        assertNotNull(result);
        assertEquals(Role.ADMIN, result.getRole());
    }

    @Test
    void constructorCreatesUserServiceInstance() {
        UserService service = new UserService();
        assertNotNull(service);
    }

    private int insertUserDirectly(
            String firstName,
            String lastName,
            String email,
            Role role,
            String password
    ) throws SQLException {
        PreparedStatement stmt = DBConnector.getInstance().getConnection().prepareStatement("""
            INSERT INTO Users(first_name, last_name, email, password, role, is_active)
            VALUES (?, ?, ?, ?, ?, true)
            RETURNING user_ID
        """);

        stmt.setString(1, firstName);
        stmt.setString(2, lastName);
        stmt.setString(3, email);
        stmt.setString(4, password);
        stmt.setInt(5, role.ordinal());

        ResultSet rs = stmt.executeQuery();
        rs.next();

        return rs.getInt("user_ID");
    }

    private String findPasswordByEmail(String email) throws SQLException {
        PreparedStatement stmt = DBConnector.getInstance().getConnection().prepareStatement("""
            SELECT password
            FROM Users
            WHERE email = ?
        """);

        stmt.setString(1, email);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return rs.getString("password");
        }

        return null;
    }

    private String uniqueEmail(String prefix) {
        return prefix + "." + System.nanoTime() + "@test.com";
    }
}