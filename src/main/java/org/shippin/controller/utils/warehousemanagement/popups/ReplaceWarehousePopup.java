package org.shippin.controller.utils.warehousemanagement.popups;

import lombok.extern.log4j.Log4j2;
import java.io.File;
import java.sql.SQLException;
import java.util.ResourceBundle;

import org.shippin.controller.WarehouseManagementController;
import org.shippin.controller.utils.GenericPopup;
import org.shippin.controller.utils.WarehouseManagementPopup;
import org.shippin.domain.BriefWarehouse;
import org.shippin.domain.formatted.PriceListFormatted;
import org.shippin.domain.formatted.RegionTableFormatted;
import org.shippin.exception.IncompatibleTablesException;
import org.shippin.exception.ValidationException;
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

@Log4j2
public class ReplaceWarehousePopup extends WarehouseManagementPopup {

	private WarehouseManagementController controller;

	public ReplaceWarehousePopup(ResourceBundle resources) {
		super(resources);
		// TODO Auto-generated constructor stub
	}
	
	private void handleReplaceSaved(Label priceListError, Label regionTableError) {
		boolean valid = true;
    	
    	if(this.controller.getSelectedPriceListFormatted() == null) {
    		priceListError.setText(t("%generic.missing_price_list"));
    		valid = false;
    	}
    	if(this.controller.getSelectedRegionTableFormatted() == null) {
    		regionTableError.setText(t("%generic.missing_region_table"));
    		valid = false;
    	}
    	
    	if(!valid) { return; }
    	
    	try {
    		WarehouseService.getInstance().replaceTables(this.controller.getSelectedPriceListFormatted(), 
				this.controller.getSelectedRegionTableFormatted(), this.controller.getSelectedWarehouse());
    		this.controller.hideModal();
    	} catch (SQLException e) {
    		log.error("Replace warehouse tables failed", e);
    		new GenericPopup(this.resources).showOkPopup(this.controller, "%generic.failed_to_insert", "%generic.database_problem");
    	} catch (IncompatibleTablesException e) {
    		priceListError.setText(t("%generic.incompatible_tables"));
    		regionTableError.setText(t("%generic.incompatible_tables"));
    	}
    }

	public void show(WarehouseManagementController controller, BriefWarehouse briefWarehouse) throws SQLException {
		this.controller = controller;
        controller.setSelectedWarehouse(WarehouseService.getInstance().getWarehouse(briefWarehouse));
        
        PriceListFormatted priceListFormatted = WarehouseConvertor.convertPriceListFormatted(
        		controller.getSelectedWarehouse().getPriceList());
        controller.setSelectedPriceListFormatted(priceListFormatted);
        
        RegionTableFormatted regionTableFormatted = WarehouseConvertor.convertRegionTableFormatted(
        		controller.getSelectedWarehouse().getRegionTable());
        controller.setSelectedRegionTableFormatted(regionTableFormatted);
        
        VBox popup = createPopupRoot();
        popup.setMaxWidth(560);
        popup.setPrefWidth(700);

        Label title = createPopupTitle(t("%warehouse_management.replace.title"));

        GridPane formGrid = new GridPane();
        formGrid.setHgap(16);
        formGrid.setVgap(12);

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
        
        Label postalCodeLabel = createFormLabel(t("%warehouse_management.replace.postal_code"));
        Label postalCodeValue = createValueLabel(String.valueOf(briefWarehouse.getPostalCode()));

        Label priceListLabel = createFormLabel(t("%warehouse_management.replace.price_list"));
        Label priceListFile = createFileLabel(t("%warehouse_management.replace.original_price_list"));
        Label priceListError = createErrorLabel();
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

            PriceListFormatted chosenPriceList = null;
			try {
				chosenPriceList = WarehouseParsingService.getInstance().parsePriceList(file);
			} catch (ValidationException e) {
				// TODO Auto-generated catch block
				log.error("Replace warehouse tables failed", e);
				priceListError.setText(t("%generic.validation_problem") + e.getErrors().getFirst());
			}

            if (chosenPriceList == null) { return; }
            controller.setSelectedPriceListFormatted(chosenPriceList);
            
            priceListError.setText("");
            priceListFile.setText(file.getName());
        });

        Label regionTableLabel = createFormLabel(t("%warehouse_management.replace.region_table"));
        Label regionTableFile = createFileLabel(t("%warehouse_management.replace.original_region_table"));
        Label regionTableError = createErrorLabel();
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

            RegionTableFormatted chosenRegionTable = null;
			try {
				chosenRegionTable = WarehouseParsingService.getInstance().parseRegionTable(file);
			} catch (ValidationException e) {
				// TODO Auto-generated catch block
				log.error("Replace warehouse tables failed", e);
				regionTableError.setText(t("%generic.validation_problem") + e.getErrors().getFirst());
			}

            if (chosenRegionTable == null) { return; }
            controller.setSelectedRegionTableFormatted(chosenRegionTable);
            
            regionTableError.setText("");
            regionTableFile.setText(file.getName());
        });

        int row = 0;
        
        formGrid.add(titleDocLabel, 0, row);
        formGrid.add(titleDocValue, 1, row++);

        formGrid.add(pickupPlaceLabel, 0, row);
        formGrid.add(pickupPlaceValue, 1, row++);

        formGrid.add(postalCodeLabel, 0, row);
        formGrid.add(postalCodeValue, 1, row++);
        
        formGrid.add(priceListLabel, 0, row);
        formGrid.add(priceListFile, 1, row);
        formGrid.add(replacePriceListButton, 2, row++);
        formGrid.add(priceListError, 1, row++);

        formGrid.add(regionTableLabel, 0, row);
        formGrid.add(regionTableFile, 1, row);
        formGrid.add(replaceRegionTableButton, 2, row++);
        formGrid.add(regionTableError, 1, row++);

        HBox buttons = new HBox(18);
        buttons.setAlignment(Pos.CENTER_LEFT);

        Button cancelButton = new Button(t("%warehouse_management.button.cancel"));
        cancelButton.getStyleClass().addAll("popup-button", "popup-secondary-button");
        cancelButton.setPrefSize(160, 42);
        cancelButton.setOnAction(e -> controller.hideModal());

        Button addButton = new Button(t("%warehouse_management.replace.button_confirm"));
        addButton.getStyleClass().addAll("popup-button", "popup-primary-button");
        addButton.setPrefSize(160, 42);
        addButton.setOnAction(e -> this.handleReplaceSaved(priceListError, regionTableError));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        buttons.getChildren().addAll(cancelButton, spacer, addButton);

        popup.getChildren().addAll(title, formGrid, buttons);

        controller.showModal(popup);
    }
	
	private Label createErrorLabel() {
	    Label label = new Label("");
	    label.getStyleClass().add("status-label");
	    label.setManaged(false); // takes no space when empty
	    label.textProperty().addListener((obs, old, val) -> label.setManaged(!val.isEmpty()));
	    label.setWrapText(true);
	    return label;
	}
}
