package ServiceTest;

import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.shippin.domain.User;
import org.shippin.domain.enums.Role;
import org.shippin.dto.Screens;
import org.shippin.services.NavigationService;
import org.shippin.services.UserService;

public class NavigationServiceTest {

    @AfterEach
    void cleanUp() {
        UserService.logout();
        NavigationService.setLocale(Locale.ENGLISH);
        NavigationService.setPrimaryStage(null);
    }

    @Test
    void constructorShouldCreateInstance() {
        NavigationService service = new NavigationService();

        assertNotNull(service);
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
    void navigateToThrowsNullPointerExceptionWhenScreenIsNull() {
        assertThrows(NullPointerException.class, () -> NavigationService.navigateTo(null));
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

    @Test
    void navigateToThrowsNullPointerExceptionWhenPrimaryStageIsNotInitialized() {
        User user = new User("Normal", "User", "user@test.com", Role.USER);
        UserService.login(user);
        NavigationService.setPrimaryStage(null);

        assertThrows(NullPointerException.class, () -> NavigationService.navigateTo(Screens.HOME));
    }
}