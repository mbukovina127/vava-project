package org.shippin.controller.utils.warehousemanagement.popups;

import java.io.File;
import java.util.ResourceBundle;

import org.shippin.controller.WarehouseManagementController;
import org.shippin.controller.utils.GenericPopup;
import org.shippin.controller.utils.WarehouseManagementPopup;
import org.shippin.domain.formatted.PriceListFormatted;
import org.shippin.domain.formatted.RegionTableFormatted;
import org.shippin.services.WarehouseParsingService;
import org.shippin.util.io.FilePicker;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class AddWarehousePopup extends WarehouseManagementPopup {

	public AddWarehousePopup(ResourceBundle resources) {
		super(resources);
		// TODO Auto-generated constructor stub
	}
	
	public void show(WarehouseManagementController controller) {
        VBox popup = createPopupRoot();
        popup.setMaxWidth(510);
        popup.setPrefWidth(510);

        Label title = createPopupTitle(t("%warehouse_management.add.title"));

        GridPane formGrid = new GridPane();
        formGrid.setHgap(16);
        formGrid.setVgap(14);

        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setPrefWidth(135);

        ColumnConstraints fieldColumn = new ColumnConstraints();
        fieldColumn.setHgrow(Priority.ALWAYS);

        formGrid.getColumnConstraints().addAll(labelColumn, fieldColumn);

        Label nameLabel = createFormLabel(t("%warehouse_management.add.name"));
        TextField nameField = createPopupTextField("Value");

        Label pickupLabel = createFormLabel(t("%warehouse_management.add.pickup_place"));
        TextField pickupField = createPopupTextField("Value");

        Label priceListLabel = createFormLabel(t("%warehouse_management.add.price_list"));
        Button addPriceListButton = createUploadButton(t("%warehouse_management.button.click_to_add"));
        addPriceListButton.setOnAction(event -> {
            Window currentWindow = addPriceListButton.getScene().getWindow();

            File file = FilePicker.pickFile(currentWindow,
                    new FileChooser.ExtensionFilter("CSV files", "*.csv"),
                    new FileChooser.ExtensionFilter("XML files", "*.xml")
            );

            if (file == null) { return; }

            PriceListFormatted priceListFormatted = WarehouseParsingService.getInstance().parsePriceList(file);

            if (priceListFormatted == null) { return; }

            controller.setSelectedPriceListFormatted(priceListFormatted);
            ((Label) addPriceListButton.getUserData()).setText(file.getName());
        });
        addPriceListButton.getStyleClass().addAll("upload-button");

        Label regionTableLabel = createFormLabel(t("%warehouse_management.add.region_table"));
        Button addRegionTableButton = createUploadButton(t("%warehouse_management.button.click_to_add"));
        addRegionTableButton.setOnAction(event -> {
            Window currentWindow = addRegionTableButton.getScene().getWindow();

            File file = FilePicker.pickFile(currentWindow,
                    new FileChooser.ExtensionFilter("CSV files", "*.csv"),
                    new FileChooser.ExtensionFilter("XML files", "*.xml")
            );

            if (file == null) { return; }

            RegionTableFormatted regionTableFormatted = WarehouseParsingService.getInstance().parseRegionTable(file);

            if (regionTableFormatted == null) { return; }

            controller.setSelectedRegionTableFormatted(regionTableFormatted);
            ((Label) addRegionTableButton.getUserData()).setText(file.getName());
        });
        addRegionTableButton.getStyleClass().addAll("upload-button");

        formGrid.add(nameLabel, 0, 0);
        formGrid.add(nameField, 1, 0);

        formGrid.add(pickupLabel, 0, 1);
        formGrid.add(pickupField, 1, 1);

        formGrid.add(priceListLabel, 0, 2);
        formGrid.add(addPriceListButton, 1, 2);

        formGrid.add(regionTableLabel, 0, 3);
        formGrid.add(addRegionTableButton, 1, 3);

        HBox buttons = new HBox(18);
        buttons.setAlignment(Pos.CENTER_LEFT);

        Button cancelButton = new Button(t("%warehouse_management.button.cancel"));
        cancelButton.getStyleClass().addAll("popup-button", "popup-secondary-button");
        cancelButton.setPrefSize(160, 42);
        cancelButton.setOnAction(e -> controller.hideModal());

        Button addButton = new Button(t("%warehouse_management.add.button_confirm"));
        addButton.getStyleClass().addAll("popup-button", "popup-primary-button");
        addButton.setPrefSize(160, 42);
        addButton.setOnAction(e -> controller.handleAddWarehouseSaved(nameField.getText(), pickupField.getText()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        buttons.getChildren().addAll(cancelButton, spacer, addButton);

        popup.getChildren().addAll(title, formGrid, buttons);

        controller.showModal(popup);
    }
}
