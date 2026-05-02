package org.shippin.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;
import org.shippin.services.NavigationService;

@Log4j2
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the initial screen manually so we can create the Scene with a root
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Login.fxml"));
        loader.setResources(NavigationService.getBundle());
        Parent root = loader.load();

        primaryStage.setTitle("Shippin");
        primaryStage.setScene(new Scene(root));

        // Register the stage so NavigationUtilities can switch scenes from anywhere
        NavigationService.setPrimaryStage(primaryStage);

        primaryStage.show();
        log.info("Application started");
    }
}
