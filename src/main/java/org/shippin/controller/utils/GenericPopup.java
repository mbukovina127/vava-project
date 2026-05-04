package org.shippin.controller.utils;

import java.util.ResourceBundle;

import org.shippin.controller.BaseController;
import org.shippin.services.NavigationService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GenericPopup {

	protected final ResourceBundle resources;
    
	protected Label createFormLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("popup-label");
        return label;
    }
	
    protected VBox createPopupRoot() {
        VBox root = new VBox(28);
        root.setPadding(new Insets(28, 30, 24, 30));
        root.setAlignment(Pos.TOP_LEFT);
        root.getStyleClass().add("popup-root");
        return root;
    }
    
    protected Label createFileLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("popup-file-label");
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    protected TextField createPopupTextField(String prompt) {
        TextField textField = new TextField();
        textField.setPromptText(prompt);
        textField.getStyleClass().add("popup-text-field");
        textField.setPrefHeight(38);
        return textField;
    }
    
    protected Label createValueLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("popup-value-label");
        return label;
    }
    
    protected Stage createPopupStage(Window owner, String titleText) {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle(titleText);

        if (owner != null) {
            stage.initOwner(owner);
        }

        return stage;
    }
    
    protected Label createPopupTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("popup-title");
        return label;
    }

    public void showOkPopup(BaseController controller, String titleKey, String messageKey) {
        VBox popup = createPopupRoot();
        popup.setMaxWidth(420);
        popup.setPrefWidth(420);

        Label title = createPopupTitle(t(titleKey));

        Label message = new Label(t(messageKey));
        message.getStyleClass().add("popup-message");
        message.setWrapText(true);

        HBox buttons = new HBox(18);
        buttons.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button okButton = new Button(t("%generic.popup.ok"));
        okButton.getStyleClass().addAll("popup-button", "tertiary-button");
        okButton.setPrefSize(160, 42);
        okButton.setOnAction(e -> controller.hideModal());

        buttons.getChildren().addAll(spacer, okButton);

        popup.getChildren().addAll(title, message, buttons);

        controller.showModal(popup);
    }


    protected String t(String key) {
        if (this.resources == null) { return key; }
        try {
            return NavigationService.getBundle().getString(key.substring(1));
        } catch (java.util.MissingResourceException e) {
            System.err.println("Missing i18n key: " + key);
            return key;
        }
    }
}
