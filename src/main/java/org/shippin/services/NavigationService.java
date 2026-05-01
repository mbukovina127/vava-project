package org.shippin.services;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.shippin.dto.Screens;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

@Log4j2
public class NavigationService {

    @Setter
    private static Stage primaryStage;

    private static ResourceBundle bundle = ResourceBundle.getBundle("i18n/messages", Locale.ENGLISH);

    public static ResourceBundle getBundle() {
        return bundle;
    }

    public static void setLocale(Locale locale) {
        bundle = ResourceBundle.getBundle("i18n/messages", locale);
    }

    // Pre full-screen prechody (Login → Menu)
    public static void navigateTo(Screens screen) {
        if (!screen.isAccessibleBy(UserService.getRole())) {
            log.warn("Access denied to screen: {} (current role: {})", screen, UserService.getRole());
            return;
        }
        String path = Screens.resolveScreen(screen);
        try {
            FXMLLoader loader = new FXMLLoader(NavigationService.class.getResource(path));
            loader.setResources(bundle);
            Parent root = loader.load();
            primaryStage.getScene().setRoot(root);
            log.debug("Navigated to screen: {}", screen);
        } catch (IOException e) {
            log.error("Failed to load screen: {}", screen, e);
            throw new RuntimeException("Failed to load screen: " + screen, e);
        }
    }
}
