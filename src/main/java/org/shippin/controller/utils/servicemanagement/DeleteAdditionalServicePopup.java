package org.shippin.controller.utils.servicemanagement;

import java.util.ResourceBundle;

import org.shippin.controller.ServicesManagementController;
import org.shippin.controller.utils.ServicesManagementPopup;
import org.shippin.domain.AdditionalService;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class DeleteAdditionalServicePopup extends ServicesManagementPopup {

    public DeleteAdditionalServicePopup(ResourceBundle resources) {
        super(resources);
    }

    public void show(ServicesManagementController controller, AdditionalService service) {
        VBox popup = createPopupRoot();
        popup.setMaxWidth(420);
        popup.setPrefWidth(420);

        Label title = createPopupTitle(t("%services_management.delete.title"));

        Label message = new Label(t("%services_management.delete.message"));
        message.getStyleClass().add("popup-message");
        message.setWrapText(true);

        HBox buttons = new HBox(18);
        buttons.setAlignment(Pos.CENTER_LEFT);

        Button cancelButton = new Button(t("%services_management.button.cancel"));
        cancelButton.getStyleClass().addAll("popup-button", "secondary-button");
        cancelButton.setPrefSize(160, 42);
        cancelButton.setOnAction(e -> controller.hideModal());

        Button deleteButton = new Button(t("%services_management.delete.button_confirm"));
        deleteButton.getStyleClass().addAll("popup-button", "danger-button");
        deleteButton.setPrefSize(160, 42);
        deleteButton.setOnAction(e -> {
            controller.deleteAdditionalService(service);
            controller.hideModal();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        buttons.getChildren().addAll(cancelButton, spacer, deleteButton);

        popup.getChildren().addAll(title, message, buttons);

        controller.showModal(popup);
    }
}