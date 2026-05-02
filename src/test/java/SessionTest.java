import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.shippin.domain.User;
import org.shippin.domain.enums.Role;
import org.shippin.session.Session;

import static org.junit.jupiter.api.Assertions.*;

public class SessionTest {

    @AfterEach
    void cleanUp() {
        Session.logout();
    }

    @Test
    void loginStoresCurrentUser() {
        User user = new User("Test", "User", "test@test.com", Role.USER);

        Session.login(user);

        assertEquals(user, Session.getUser());
        assertEquals(Role.USER, Session.getRole());
    }

    @Test
    void logoutClearsCurrentUser() {
        User user = new User("Test", "User", "test@test.com", Role.USER);
        Session.login(user);

        Session.logout();

        assertNull(Session.getUser());
        assertNull(Session.getRole());
    }

    @Test
    void getRoleReturnsNullWhenNoUserIsLoggedIn() {
        Session.logout();

        assertNull(Session.getRole());
    }

    @Test
    void hasAccessReturnsFalseWhenNoUserIsLoggedIn() {
        Session.logout();

        assertFalse(Session.hasAccess(Role.USER));
    }

    @Test
    void userDoesNotHaveAdminAccess() {
        User user = new User("Normal", "User", "user@test.com", Role.USER);
        Session.login(user);

        assertFalse(Session.hasAccess(Role.ADMIN));
    }

    @Test
    void adminHasAccessToUserAndAdminLevel() {
        User admin = new User("Admin", "User", "admin@test.com", Role.ADMIN);
        Session.login(admin);

        assertTrue(Session.hasAccess(Role.USER));
        assertTrue(Session.hasAccess(Role.ADMIN));
    }
}