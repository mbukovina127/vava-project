package org.shippin.controller.utils.warehousemanagement.popups;

import lombok.extern.log4j.Log4j2;
import java.io.File;
import java.sql.SQLException;
import java.util.ResourceBundle;

import org.shippin.controller.WarehouseManagementController;
import org.shippin.controller.utils.GenericPopup;
import org.shippin.controller.utils.InputValidator;
import org.shippin.controller.utils.WarehouseManagementPopup;
import org.shippin.domain.formatted.PriceListFormatted;
import org.shippin.domain.formatted.RegionTableFormatted;
import org.shippin.exception.IncompatibleTablesException;
import org.shippin.exception.ValidationException;
import org.shippin.services.WarehouseParsingService;
import org.shippin.services.WarehouseService;
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

@Log4j2
public class AddWarehousePopup extends WarehouseManagementPopup {

	private WarehouseManagementController controller;

	public AddWarehousePopup(ResourceBundle resources) {
		super(resources);
		// TODO Auto-generated constructor stub
	}
	
	public void handleAddWarehouseSaved(String name, String pickup, String postalCodeString,
	        Label nameError, Label pickupError, Label postalCodeError,
	        Label priceListError, Label regionTableError) {

	    boolean valid = true;

	    if (!InputValidator.isNotBlank(name)) {
	        nameError.setText(t("%generic.regex.blank_name"));
	        valid = false;
	    } else if (!InputValidator.isValidLength(name, 40)) {
	        nameError.setText(t("%warehouse_management.add.name_too_long"));
	        valid = false;
	    }

	    if (!InputValidator.isNotBlank(pickup)) {
	        pickupError.setText(t("%generic.regex.blank_pickup_place"));
	        valid = false;
	    } else if (!InputValidator.isValidLength(pickup, 40)) {
	        pickupError.setText(t("%warehouse_management.add.regex.pickup_too_long"));
	        valid = false;
	    }

	    if (!InputValidator.isPostalCode(postalCodeString)) {
	        postalCodeError.setText(t("%generic.regex.invalid_postal_code"));
	        valid = false;
	    }

	    if (this.controller.getSelectedPriceListFormatted() == null) {
	        priceListError.setText(t("%generic.missing_price_list"));
	        valid = false;
	    }

	    if (this.controller.getSelectedRegionTableFormatted() == null) {
	        regionTableError.setText(t("%generic.missing_region_table"));
	        valid = false;
	    }

	    if (!valid) { return; } // stop here, errors are shown inline

	    int postalCode = Integer.parseInt(postalCodeString.replaceAll("\\s+", ""));

	    try {
	        WarehouseService.getInstance().addWarehouse(name, pickup, postalCode,
	                this.controller.getSelectedPriceListFormatted(), this.controller.getSelectedRegionTableFormatted());
	        controller.hideModal();
	    } catch (IncompatibleTablesException e) {
	        priceListError.setText(t("%generic.incompatible_tables"));
	        regionTableError.setText(t("%generic.incompatible_tables"));
	        valid = false;
	    } catch (SQLException e) {
	        log.error("Add warehouse failed", e);
	        new GenericPopup(this.resources).showOkPopup(this.controller, "%generic.failed_to_import", "%generic.database_problem");
	    } catch (Exception e) {
	        log.error("Add warehouse failed", e);
	        postalCodeError.setText(t("%generic.map.postal_code_to_coordinates_failed"));
	    }

	    try {
	        this.controller.setWarehouseList(WarehouseService.getInstance().getBriefWarehouses());
	    } catch (SQLException e) {
	        log.error("Add warehouse failed", e);
	        new GenericPopup(this.resources).showOkPopup(this.controller, "%generic.failed_to_fetch", "%generic.database_problem");
	    }
	    this.controller.renderWarehouses(this.controller.getWarehouseList());
	}
	
	public void show(WarehouseManagementController controller) {
	    this.controller = controller;
	    this.controller.setSelectedPriceListFormatted(null);
	    this.controller.setSelectedRegionTableFormatted(null);
	    VBox popup = createPopupRoot();
	    popup.setMaxWidth(510);
	    popup.setPrefWidth(510);

	    Label title = createPopupTitle(t("%warehouse_management.add.title"));

	    GridPane formGrid = new GridPane();
	    formGrid.setHgap(16);
	    formGrid.setVgap(8);

	    ColumnConstraints labelColumn = new ColumnConstraints();
	    labelColumn.setPrefWidth(135);

	    ColumnConstraints fieldColumn = new ColumnConstraints();
	    fieldColumn.setHgrow(Priority.ALWAYS);

	    formGrid.getColumnConstraints().addAll(labelColumn, fieldColumn);

	    // --- Name ---
	    Label nameLabel = createFormLabel(t("%warehouse_management.add.name"));
	    TextField nameField = createPopupTextField("");
	    Label nameError = createErrorLabel();

	    // --- Pickup ---
	    Label pickupLabel = createFormLabel(t("%warehouse_management.add.pickup_place"));
	    TextField pickupField = createPopupTextField("");
	    Label pickupError = createErrorLabel();

	    // --- Postal Code ---
	    Label postalCodeLabel = createFormLabel(t("%warehouse_management.add.postal_code"));
	    TextField postalCodeField = createPopupTextField("");
	    Label postalCodeError = createErrorLabel();

	    // --- Price List ---
	    Label priceListLabel = createFormLabel(t("%warehouse_management.add.price_list"));
	    Button addPriceListButton = createUploadButton(t("%warehouse_management.button.click_to_add"));
	    Label priceListError = createErrorLabel();
	    addPriceListButton.setOnAction(event -> {
	        Window currentWindow = addPriceListButton.getScene().getWindow();
	        File file = FilePicker.pickFile(currentWindow,
	                new FileChooser.ExtensionFilter("CSV files", "*.csv"),
	                new FileChooser.ExtensionFilter("XML files", "*.xml")
	        );
	        if (file == null) { return; }
	        PriceListFormatted priceListFormatted = null;
			try {
				priceListFormatted = WarehouseParsingService.getInstance().parsePriceList(file);
			} catch (ValidationException e) {
				// TODO Auto-generated catch block
				log.error("Add warehouse failed", e);
				new GenericPopup(this.resources).showOkPopup(controller, "%generic.failed_to_import", "%generic.validation_problem " + e.getErrors());
			}
	        if (priceListFormatted == null) { return; }
	        controller.setSelectedPriceListFormatted(priceListFormatted);
	        ((Label) addPriceListButton.getUserData()).setText(file.getName());
	        priceListError.setText("");
	    });
	    addPriceListButton.getStyleClass().add("upload-button");

	    // --- Region Table ---
	    Label regionTableLabel = createFormLabel(t("%warehouse_management.add.region_table"));
	    Button addRegionTableButton = createUploadButton(t("%warehouse_management.button.click_to_add"));
	    Label regionTableError = createErrorLabel();
	    addRegionTableButton.setOnAction(event -> {
	        Window currentWindow = addRegionTableButton.getScene().getWindow();
	        File file = FilePicker.pickFile(currentWindow,
	                new FileChooser.ExtensionFilter("CSV files", "*.csv"),
	                new FileChooser.ExtensionFilter("XML files", "*.xml")
	        );
	        if (file == null) { return; }
	        RegionTableFormatted regionTableFormatted = null;
			try {
				regionTableFormatted = WarehouseParsingService.getInstance().parseRegionTable(file);
			} catch (ValidationException e) {
				log.error("Add warehouse failed", e);
				new GenericPopup(this.resources).showOkPopup(controller, "%generic.failed_to_import", "%generic.validation_problem " + e.getErrors());
			}
	        if (regionTableFormatted == null) { return; }
	        controller.setSelectedRegionTableFormatted(regionTableFormatted);
	        ((Label) addRegionTableButton.getUserData()).setText(file.getName());
	        regionTableError.setText("");
	    });
	    addRegionTableButton.getStyleClass().add("upload-button");

	    // clear errors on type
	    nameField.textProperty().addListener((obs, old, val) -> nameError.setText(""));
	    pickupField.textProperty().addListener((obs, old, val) -> pickupError.setText(""));
	    postalCodeField.textProperty().addListener((obs, old, val) -> postalCodeError.setText(""));

	    // row layout: label | field, then span error label across both columns below
	    int row = 0;

	    formGrid.add(nameLabel,  0, row); formGrid.add(nameField,  1, row++);
	    formGrid.add(nameError,  1, row++);  // column 1 only, no columnspan

	    formGrid.add(pickupLabel,  0, row); formGrid.add(pickupField,  1, row++);
	    formGrid.add(pickupError,  1, row++);

	    formGrid.add(postalCodeLabel,  0, row); formGrid.add(postalCodeField,  1, row++);
	    formGrid.add(postalCodeError,  1, row++);

	    formGrid.add(priceListLabel,  0, row); formGrid.add(addPriceListButton,  1, row++);
	    formGrid.add(priceListError,  1, row++);

	    formGrid.add(regionTableLabel,  0, row); formGrid.add(addRegionTableButton,  1, row++);
	    formGrid.add(regionTableError,  1, row++);

	    // --- Buttons ---
	    HBox buttons = new HBox(18);
	    buttons.setAlignment(Pos.CENTER_LEFT);

	    Button cancelButton = new Button(t("%warehouse_management.button.cancel"));
	    cancelButton.getStyleClass().addAll("popup-button", "popup-secondary-button");
	    cancelButton.setPrefSize(160, 42);
	    cancelButton.setOnAction(e -> controller.hideModal());

	    Button addButton = new Button(t("%warehouse_management.add.button_confirm"));
	    addButton.getStyleClass().addAll("popup-button", "popup-primary-button");
	    addButton.setPrefSize(160, 42);
	    addButton.setOnAction(e -> this.handleAddWarehouseSaved(
	            nameField.getText(), pickupField.getText(), postalCodeField.getText(),
	            nameError, pickupError, postalCodeError, priceListError, regionTableError));

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
