import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.shippin.domain.User;
import org.shippin.domain.enums.Role;
import org.shippin.dto.Screens;
import org.shippin.services.NavigationService;
import org.shippin.services.UserService;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

public class NavigationServiceTest {

    @AfterEach
    void cleanUp() {
        UserService.logout();
        NavigationService.setLocale(Locale.ENGLISH);
    }

    @Test
    void getBundleReturnsNonNullBundle() {
        ResourceBundle bundle = NavigationService.getBundle();

        assertNotNull(bundle);
    }

    @Test
    void setLocaleChangesBundleToEnglish() {
        NavigationService.setLocale(Locale.ENGLISH);

        ResourceBundle bundle = NavigationService.getBundle();

        assertEquals("Log in", bundle.getString("login.button"));
    }

    @Test
    void setLocaleChangesBundleToSlovak() {
        NavigationService.setLocale(new Locale("sk"));

        ResourceBundle bundle = NavigationService.getBundle();

        assertEquals("Prihlásiť sa", bundle.getString("login.button"));
    }

    @Test
    void navigateToDoesNothingWhenUserHasNoAccess() {
        UserService.logout();

        assertDoesNotThrow(() -> NavigationService.navigateTo(Screens.HOME));
    }

    @Test
    void navigateToDoesNothingWhenUserHasInsufficientRole() {
        User user = new User("Normal", "User", "user@test.com", Role.USER);
        UserService.login(user);

        assertDoesNotThrow(() -> NavigationService.navigateTo(Screens.USER_MANAGEMENT));
    }
}