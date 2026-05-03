package org.shippin.controller.utils.warehousemanagement.popups;

import java.io.File;
import java.sql.SQLException;
import java.util.ResourceBundle;

import org.shippin.controller.WarehouseManagementController;
import org.shippin.controller.utils.GenericPopup;
import org.shippin.controller.utils.WarehouseManagementPopup;
import org.shippin.domain.BriefWarehouse;
import org.shippin.domain.formatted.PriceListFormatted;
import org.shippin.domain.formatted.RegionTableFormatted;
import org.shippin.services.WarehouseParsingService;
import org.shippin.services.WarehouseService;
import org.shippin.util.WarehouseConvertor;
import org.shippin.util.io.FilePicker;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class ReplaceWarehousePopup extends WarehouseManagementPopup {

	public ReplaceWarehousePopup(ResourceBundle resources) {
		super(resources);
		// TODO Auto-generated constructor stub
	}
	
	public void show(WarehouseManagementController controller, BriefWarehouse briefWarehouse) throws SQLException {
        controller.setSelectedWarehouse(WarehouseService.getInstance().getWarehouse(briefWarehouse));
        
        PriceListFormatted priceListFormatted = WarehouseConvertor.convertPriceListFormatted(
        		controller.getSelectedWarehouse().getPriceList());
        controller.setSelectedPriceListFormatted(priceListFormatted);
        
        RegionTableFormatted regionTableFormatted = WarehouseConvertor.convertRegionTableFormatted(
        		controller.getSelectedWarehouse().getRegionTable());
        controller.setSelectedRegionTableFormatted(regionTableFormatted);
        
        VBox popup = createPopupRoot();
        popup.setMaxWidth(560);
        popup.setPrefWidth(560);

        Label title = createPopupTitle(t("%warehouse_management.replace.title"));

        GridPane formGrid = new GridPane();
        formGrid.setHgap(16);
        formGrid.setVgap(18);

        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setPrefWidth(135);

        ColumnConstraints fieldColumn = new ColumnConstraints();
        fieldColumn.setHgrow(Priority.ALWAYS);

        ColumnConstraints buttonColumn = new ColumnConstraints();
        buttonColumn.setPrefWidth(130);

        formGrid.getColumnConstraints().addAll(labelColumn, fieldColumn, buttonColumn);

        Label titleDocLabel = createFormLabel(t("%warehouse_management.popup.name"));
        Label titleDocValue = createValueLabel(briefWarehouse.getName());

        Label pickupPlaceLabel = createFormLabel(t("%warehouse_management.replace.pickup_place"));
        Label pickupPlaceValue = createValueLabel(briefWarehouse.getRegionName());

        Label priceListLabel = createFormLabel(t("%warehouse_management.replace.price_list"));
        Label priceListFile = createFileLabel("Original price list");
        Button replacePriceListButton = new Button(t("%warehouse_management.button.replace"));
        replacePriceListButton.getStyleClass().addAll("popup-button", "danger-button");
        replacePriceListButton.setPrefSize(120, 38);
        replacePriceListButton.setOnAction(event -> {
            Window currentWindow = replacePriceListButton.getScene().getWindow();

            File file = FilePicker.pickFile(currentWindow,
                    new FileChooser.ExtensionFilter("CSV files", "*.csv"),
                    new FileChooser.ExtensionFilter("XML files", "*.xml")
            );

            if (file == null) { return; }

            PriceListFormatted chosenPriceList = WarehouseParsingService.getInstance().parsePriceList(file);

            if (chosenPriceList == null) { return; }
            controller.setSelectedPriceListFormatted(chosenPriceList);

            priceListFile.setText(file.getName());
        });

        Label regionTableLabel = createFormLabel(t("%warehouse_management.replace.region_table"));
        Label regionTableFile = createFileLabel("Original region table");
        Button replaceRegionTableButton = new Button(t("%warehouse_management.button.replace"));
        replaceRegionTableButton.getStyleClass().addAll("popup-button", "danger-button");
        replaceRegionTableButton.setPrefSize(120, 38);
        replaceRegionTableButton.setOnAction(event -> {
            Window currentWindow = replaceRegionTableButton.getScene().getWindow();

            File file = FilePicker.pickFile(currentWindow,
                    new FileChooser.ExtensionFilter("CSV files", "*.csv"),
                    new FileChooser.ExtensionFilter("XML files", "*.xml")
            );

            if (file == null) { return; }

            RegionTableFormatted chosenRegionTable = WarehouseParsingService.getInstance().parseRegionTable(file);

            if (chosenRegionTable == null) { return; }
            controller.setSelectedRegionTableFormatted(chosenRegionTable);
            
            regionTableFile.setText(file.getName());
        });

        formGrid.add(titleDocLabel, 0, 0);
        formGrid.add(titleDocValue, 1, 0);

        formGrid.add(pickupPlaceLabel, 0, 1);
        formGrid.add(pickupPlaceValue, 1, 1);

        formGrid.add(priceListLabel, 0, 2);
        formGrid.add(priceListFile, 1, 2);
        formGrid.add(replacePriceListButton, 2, 2);

        formGrid.add(regionTableLabel, 0, 3);
        formGrid.add(regionTableFile, 1, 3);
        formGrid.add(replaceRegionTableButton, 2, 3);

        HBox buttons = new HBox(18);
        buttons.setAlignment(Pos.CENTER_LEFT);

        Button cancelButton = new Button(t("%warehouse_management.button.cancel"));
        cancelButton.getStyleClass().addAll("popup-button", "popup-secondary-button");
        cancelButton.setPrefSize(160, 42);
        cancelButton.setOnAction(e -> controller.hideModal());

        Button addButton = new Button(t("%warehouse_management.replace.button_confirm"));
        addButton.getStyleClass().addAll("popup-button", "popup-primary-button");
        addButton.setPrefSize(160, 42);
        addButton.setOnAction(e -> controller.handleReplaceSaved());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        buttons.getChildren().addAll(cancelButton, spacer, addButton);

        popup.getChildren().addAll(title, formGrid, buttons);

        controller.showModal(popup);
    }

}
