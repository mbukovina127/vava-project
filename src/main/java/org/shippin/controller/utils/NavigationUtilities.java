package org.shippin.controller.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;
import org.shippin.dto.Screens;

import java.io.IOException;

@Log4j2
public class NavigationUtilities {

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void navigateTo(Screens screen) {
        String path = resolveScreen(screen);
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

    private static String resolveScreen(Screens screen) {
        return switch (screen) {
            case LOGIN -> "/views/Login.fxml";
            case REGISTER -> "/views/Register.fxml";
            case HOME -> "/views/Cost_estimation.fxml";
        };
    }
}
