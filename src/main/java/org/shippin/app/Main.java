package org.shippin.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Label label = new Label("Hello Shippin!");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 300, 250);
        primaryStage.setTitle("Shippin");
        primaryStage.setScene(scene);
        primaryStage.show();
        log.info("Application started");
    }
}
