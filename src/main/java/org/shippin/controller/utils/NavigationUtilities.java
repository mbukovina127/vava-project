package org.shippin.controller.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.shippin.controller.MenuController;
import org.shippin.dto.Screens;

import java.io.IOException;

@Log4j2
public class NavigationUtilities {

    @Setter
    private static Stage primaryStage;

    /**
     * Navigate to a specific screen using Screens enum
     * @param screen
     */
    // Pre full-screen prechody (Login → Menu)
    public static void navigateTo(Screens screen) {
        String path = Screens.resolveScreen(screen);
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtilities.class.getResource(path));
            Parent root = loader.load();
            primaryStage.getScene().setRoot(root);
            log.debug("Navigated to screen: {}", screen);
        } catch (IOException e) {
            log.error("Failed to load screen: {}", screen, e);
            throw new RuntimeException("Failed to load screen: " + screen, e);
        }
    }
}
