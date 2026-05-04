package org.shippin.controller.utils.warehousemanagement.popups;

import java.io.File;
import java.sql.SQLException;
import java.util.ResourceBundle;

import org.shippin.controller.BaseController;
import org.shippin.controller.WarehouseManagementController;
import org.shippin.controller.utils.GenericPopup;
import org.shippin.controller.utils.WarehouseManagementPopup;
import org.shippin.domain.BriefWarehouse;
import org.shippin.services.WarehouseParsingService;
import org.shippin.util.io.FilePicker;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class ExportChoicePopup extends WarehouseManagementPopup {

	public ExportChoicePopup(ResourceBundle resources) {
		super(resources);
	}
	
	public void show(WarehouseManagementController controller, BriefWarehouse briefWarehouse) {
        VBox popup = createPopupRoot();
        popup.setMaxWidth(420);
        popup.setPrefWidth(420);

        Label title = createPopupTitle(t("%warehouse_management.export.title"));

        Label message = new Label(t("%warehouse_management.export.message"));
        message.getStyleClass().add("popup-message");
        message.setWrapText(true);

        ToggleGroup toggleGroup = new ToggleGroup();

        RadioButton priceListOption = new RadioButton(t("%warehouse_management.export.option_price_list"));
        priceListOption.setToggleGroup(toggleGroup);
        priceListOption.setSelected(true);
        priceListOption.getStyleClass().add("popup-toggle-button");

        RadioButton regionTableOption = new RadioButton(t("%warehouse_management.export.option_region_table"));
        regionTableOption.setToggleGroup(toggleGroup);
        regionTableOption.getStyleClass().add("popup-toggle-button");

        VBox options = new VBox(10, priceListOption, regionTableOption);

        HBox buttons = new HBox(18);
        buttons.setAlignment(Pos.CENTER_LEFT);

        Button cancelButton = new Button(t("%warehouse_management.button.cancel"));
        cancelButton.getStyleClass().addAll("popup-button", "secondary-button");
        cancelButton.setPrefSize(160, 42);
        cancelButton.setOnAction(e -> controller.hideModal());

        Button exportButton = new Button(t("%warehouse_management.export.button_confirm"));
        exportButton.getStyleClass().addAll("popup-button", "primary-button");
        exportButton.setPrefSize(160, 42);
        exportButton.setOnAction(e -> {
            boolean isPriceList = priceListOption.isSelected();

            Window currentWindow = exportButton.getScene().getWindow();
            
            File file = FilePicker.saveFile(currentWindow,
            	    new FileChooser.ExtensionFilter("CSV files", "*.csv"),
            	    new FileChooser.ExtensionFilter("XML files", "*.xml")
            	);

            if (file == null) { return; }
            
            try {
				WarehouseParsingService.getInstance().exportTable(briefWarehouse, isPriceList, file);
			} catch (SQLException e1) {
				e1.printStackTrace();
				new GenericPopup(this.resources).showOkPopup(controller, "%generic.failed_to_insert", "%generic.database_problem");
			}
            controller.hideModal();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        buttons.getChildren().addAll(cancelButton, spacer, exportButton);

        popup.getChildren().addAll(title, message, options, buttons);

        controller.showModal(popup);
    }
}
