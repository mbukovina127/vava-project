package org.shippin.controller.utils.menu;

import java.util.ResourceBundle;

import org.shippin.controller.MenuController;
import org.shippin.controller.WarehouseManagementController;
import org.shippin.controller.utils.WarehouseManagementPopup;
import org.shippin.domain.BriefWarehouse;
import org.shippin.dto.Screens;
import org.shippin.services.NavigationService;
import org.shippin.services.UserService;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class LogoutChoicePopup extends WarehouseManagementPopup {

	public LogoutChoicePopup(ResourceBundle resources) {
		super(resources);
	}

	public void show(MenuController controller) {
        VBox popup = createPopupRoot();
        popup.setMaxWidth(420);
        popup.setPrefWidth(420);

        Label title = createPopupTitle(t("%menu.logout_title"));

        Label message = new Label(t("%menu.logout_msg"));
        message.getStyleClass().add("popup-message");
        message.setWrapText(true);

        HBox buttons = new HBox(18);
        buttons.setAlignment(Pos.CENTER_LEFT);

        Button cancelButton = new Button(t("%menu.button.cancel"));
        cancelButton.getStyleClass().addAll("popup-button", "secondary-button");
        cancelButton.setPrefSize(160, 42);
        cancelButton.setOnAction(e -> controller.hideOverlay());

        Button deleteButton = new Button(t("%menu.logout.button_confirm"));
        deleteButton.getStyleClass().addAll("popup-button", "danger-button");
        deleteButton.setPrefSize(160, 42);
        deleteButton.setOnAction(e -> {
        	UserService.logout();
        	NavigationService.navigateTo(Screens.LOGIN);
        	controller.hideOverlay();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        buttons.getChildren().addAll(cancelButton, spacer, deleteButton);

        popup.getChildren().addAll(title, message, buttons);

        controller.showOverlay(popup);
    }
}
