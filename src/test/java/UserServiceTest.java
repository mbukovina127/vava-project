import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.shippin.domain.User;
import org.shippin.domain.enums.Role;
import org.shippin.services.UserService;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    @AfterEach
    void cleanUp() {
        UserService.logout();
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
    void adminHasAccessToAllLowerLevels() {
        User admin = new User("Admin", "User", "admin@test.com", Role.ADMIN);
        UserService.login(admin);

        assertTrue(UserService.hasAccess(Role.USER));
        assertTrue(UserService.hasAccess(Role.POWER_USER));
        assertTrue(UserService.hasAccess(Role.ADMIN));
    }
}