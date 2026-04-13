package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

@Override
public void start(Stage stage) {

    Button btn = new Button("Vyber miesto");

    btn.setOnAction(e -> {MapPicker.open();});

    stage.setScene(new Scene(new StackPane(btn), 300,200));
    stage.setTitle("Courier app");
    stage.show();
	}
	
	public static void main(String[] args) 
	{
	    launch();
	}

}