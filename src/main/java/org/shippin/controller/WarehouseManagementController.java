package org.shippin.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.shippin.domain.BriefWarehouse;
import org.shippin.domain.CoreWarehouseInfo;
import org.shippin.domain.Warehouse;
import org.shippin.services.WarehouseParsingService;
import org.shippin.util.io.FilePicker;
import org.shippin.domain.formatted.PriceListFormatted;
import org.shippin.domain.formatted.RegionTableFormatted;
import org.shippin.domain.formatted.SmallPriceListFormatted;
import org.shippin.domain.formatted.WarehouseFormatted;
import org.shippin.services.WarehouseService;

import static org.shippin.dto.Screens.EDIT_WAREHOUSE;
import static org.shippin.dto.Screens.SMALL_PRICE_LIST_VIEW;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class WarehouseManagementController extends BaseController<BriefWarehouse> implements Initializable {

    @FXML private ImageView addWarehouseIcon;
    @FXML private ImageView changePriceListIcon;
    @FXML private VBox warehouseRowsContainer;
    @FXML private Button addWarehouseButton;

    private Image editIcon;
    private Image replaceIcon;
    private Image exportIcon;
    private Image deleteIcon;
    private String stylesheetUrl;
    
    private WarehouseParsingService warehouseParsingService;
	private WarehouseService warehouseService;
	private List<BriefWarehouse> warehouseList;
	private Warehouse selectedWarehouse;
	private RegionTableFormatted selectedRegionTableFormatted;
	private PriceListFormatted selectedPriceListFormatted;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Image addIcon = loadImage("/icons/png-dark/plus_black.png");
        editIcon = loadImage("/icons/png-dark/rewrite_black.png");
        replaceIcon = loadImage("/icons/png-dark/upload_black.png");
        exportIcon = loadImage("/icons/png-dark/export_black.png");
        deleteIcon = loadImage("/icons/png-dark/delete_black.png");

        addWarehouseIcon.setImage(addIcon);
        changePriceListIcon.setImage(replaceIcon);

        URL cssResource = getClass().getResource("/views/warehouse-management.css");
        if (cssResource != null) {
            stylesheetUrl = cssResource.toExternalForm();
        }

        this.warehouseParsingService = new WarehouseParsingService();
        this.warehouseService = new WarehouseService();
        
        //List<CoreWarehouseInfo> warehouses = createWarehouses();
        this.warehouseList = warehouseService.getBriefWarehouses();
        renderWarehouses(warehouseList);
    }

    // Dummy data
    private List<CoreWarehouseInfo> createWarehouses() {
        return List.of(
                new BriefWarehouse(1, "ZBS - BA", "Bratislava"),
                new BriefWarehouse(2, "ZBS - BB", "Banska Bystrica"),
                new BriefWarehouse(3, "ZBS - RK", "Ruzomberok")
        );
    }

    private void renderWarehouses(List<BriefWarehouse> warehouses) {
        warehouseRowsContainer.getChildren().clear();

        for (BriefWarehouse warehouse : warehouses) {
            warehouseRowsContainer.getChildren().add(createWarehouseRow(warehouse));
        }
    }

    private GridPane createWarehouseRow(BriefWarehouse warehouse) {
        GridPane row = new GridPane();
        row.setStyle("-fx-border-color: transparent transparent #2b2b2b transparent; -fx-border-width: 0 0 2 0;");

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPrefWidth(95.0);
        col2.setHalignment(HPos.CENTER);

        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPrefWidth(115.0);
        col3.setHalignment(HPos.CENTER);

        ColumnConstraints col4 = new ColumnConstraints();
        col4.setPrefWidth(145.0);
        col4.setHalignment(HPos.CENTER);

        ColumnConstraints col5 = new ColumnConstraints();
        col5.setPrefWidth(90.0);
        col5.setHalignment(HPos.CENTER);

        row.getColumnConstraints().addAll(col1, col2, col3, col4, col5);
        row.setPadding(new Insets(9.0, 6.0, 9.0, 0.0));

        Label nameLabel = new Label(warehouse.getName());
        nameLabel.setStyle("-fx-font-size: 16; -fx-font-weight: 700; -fx-text-fill: #1a1a1a;");
        GridPane.setColumnIndex(nameLabel, 0);
        GridPane.setMargin(nameLabel, new Insets(0.0, 0.0, 0.0, 8.0));

        Button editButton = createTableIconButton(editIcon, 22.0, 22.0, 32.0, 32.0);
        editButton.setOnAction(event -> {
				this.handleOpenWarehouse(warehouse);
		});
        GridPane.setColumnIndex(editButton, 1);

        Button replaceButton = createTableIconButton(replaceIcon, 26.0, 26.0, 32.0, 32.0);
        replaceButton.setOnAction(event -> showReplaceWarehousePopup(warehouse));
        GridPane.setColumnIndex(replaceButton, 2);

        Button exportButton = createTableIconButton(exportIcon, 26.0, 26.0, 34.0, 32.0);
        GridPane.setColumnIndex(exportButton, 3);

        Button deleteButton = createTableIconButton(deleteIcon, 22.0, 22.0, 32.0, 32.0);
        GridPane.setColumnIndex(deleteButton, 4);

        row.getChildren().addAll(
                nameLabel,
                editButton,
                replaceButton,
                exportButton,
                deleteButton
        );

        return row;
    }

    private Button createTableIconButton(Image image,
                                         double imageWidth,
                                         double imageHeight,
                                         double buttonWidth,
                                         double buttonHeight) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(imageWidth);
        imageView.setFitHeight(imageHeight);
        imageView.setPreserveRatio(true);

        Button button = new Button();
        button.setGraphic(imageView);
        button.setPrefWidth(buttonWidth);
        button.setPrefHeight(buttonHeight);
        button.getStyleClass().add("table-icon-button");
        button.setAlignment(Pos.CENTER);

        return button;
    }

    @FXML
    private void handleAddWarehouse() {
        showAddWarehousePopup();
    }
    
    private Warehouse fetchFullWarehouse(BriefWarehouse briefWarehouse) {
    	return this.warehouseService.getWarehouse(briefWarehouse);
    }

    private void showAddWarehousePopup() {
        VBox popup = createPopupRoot();
        popup.setMaxWidth(510);
        popup.setPrefWidth(510);

        Label title = createPopupTitle("Add new warehouse");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(16);
        formGrid.setVgap(14);

        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setPrefWidth(135);

        ColumnConstraints fieldColumn = new ColumnConstraints();
        fieldColumn.setHgrow(Priority.ALWAYS);

        formGrid.getColumnConstraints().addAll(labelColumn, fieldColumn);

        Label nameLabel = createFormLabel("Name:");
        TextField nameField = createPopupTextField("Value");

        Label pickupLabel = createFormLabel("Pickup place:");
        TextField pickupField = createPopupTextField("Value");

        Label priceListLabel = createFormLabel("Import price list:");
        Button addPriceListButton = createUploadButton("Click to add");
        addPriceListButton.setOnAction(event -> {
            Window currentWindow = addPriceListButton.getScene().getWindow();

            File file = FilePicker.pickFile(currentWindow,
            	    new FileChooser.ExtensionFilter("CSV files", "*.csv"),
            	    new FileChooser.ExtensionFilter("XML files", "*.xml")
            	);

            if (file == null) { return; }
            
        	PriceListFormatted priceListFormatted = warehouseParsingService.parsePriceList(file);

            // Once the parser has some kind of format check, I can add a popup informing the user the format is wrong
        	if (priceListFormatted == null) { return; }
        	
        	addPriceListButton.setText(file.getName());
        });

        Label regionTableLabel = createFormLabel("Import region table:");
        Button addRegionTableButton = createUploadButton("Click to add");
        addRegionTableButton.setOnAction(event -> {
            Window currentWindow = addRegionTableButton.getScene().getWindow();

            File file = FilePicker.pickFile(currentWindow,
            	    new FileChooser.ExtensionFilter("CSV files", "*.csv"),
            	    new FileChooser.ExtensionFilter("XML files", "*.xml")
            	);

            if (file == null) { return; }
            
        	RegionTableFormatted regionTableFormatted = warehouseParsingService.parseRegionTable(file);

            // Once the parser has some kind of format check, I can add a popup informing the user the format is wrong
        	if (regionTableFormatted == null) { return; }
        	
        	addRegionTableButton.setText(file.getName());
        });

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

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().addAll("popup-button", "popup-secondary-button");
        cancelButton.setPrefSize(160, 42);
        cancelButton.setOnAction(e -> hideModal());

        Button addButton = new Button("Add warehouse");
        addButton.getStyleClass().addAll("popup-button", "popup-primary-button");
        addButton.setPrefSize(160, 42);
        addButton.setOnAction(e -> hideModal());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        buttons.getChildren().addAll(cancelButton, spacer, addButton);

        popup.getChildren().addAll(title, formGrid, buttons);

        showModal(popup);
    }

    private void showReplaceWarehousePopup(BriefWarehouse warehouse) {
    	this.selectedWarehouse = fetchFullWarehouse(warehouse);
        VBox popup = createPopupRoot();
        popup.setMaxWidth(560);
        popup.setPrefWidth(560);

        Label title = createPopupTitle("Replace warehouse tables");

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

        Label titleDocLabel = createFormLabel("Title of document:");
        Label titleDocValue = createValueLabel(warehouse.getName());

        Label pickupPlaceLabel = createFormLabel("Pickup place:");
        Label pickupPlaceValue = createValueLabel(warehouse.getRegionName());

        Label priceListLabel = createFormLabel("Price list:");
        Label priceListFile = createFileLabel("pricelist2020.csv");
        Button replacePriceListButton = new Button("Replace");
        replacePriceListButton.getStyleClass().addAll("popup-button", "popup-danger-button");
        replacePriceListButton.setPrefSize(120, 38);
        replacePriceListButton.setOnAction(event -> {
            Window currentWindow = replacePriceListButton.getScene().getWindow();

            File file = FilePicker.pickFile(currentWindow,
            	    new FileChooser.ExtensionFilter("CSV files", "*.csv"),
            	    new FileChooser.ExtensionFilter("XML files", "*.xml")
            	);

            if (file == null) { return; }
            
        	PriceListFormatted priceListFormatted = warehouseParsingService.parsePriceList(file);

            // Once the parser has some kind of format check, I can add a popup informing the user the format is wrong
        	if (priceListFormatted == null) { return; }
        	this.selectedPriceListFormatted = priceListFormatted;
        	
        	priceListFile.setText(file.getName());
        });

        Label regionTableLabel = createFormLabel("Region table:");
        Label regionTableFile = createFileLabel("regiontable2019.csv");
        Button replaceRegionTableButton = new Button("Replace");
        replaceRegionTableButton.getStyleClass().addAll("popup-button", "popup-danger-button");
        replaceRegionTableButton.setPrefSize(120, 38);
        replaceRegionTableButton.setOnAction(event -> {
            Window currentWindow = replaceRegionTableButton.getScene().getWindow();

            File file = FilePicker.pickFile(currentWindow,
            	    new FileChooser.ExtensionFilter("CSV files", "*.csv"),
            	    new FileChooser.ExtensionFilter("XML files", "*.xml")
            	);
            
            if(file == null) { return; }

            RegionTableFormatted regionTableFormatted = warehouseParsingService.parseRegionTable(file);
        	
            // Once the parser has some kind of format check, I can add a popup informing the user the format is wrong
            if(regionTableFormatted == null) { return; }
            this.selectedRegionTableFormatted = regionTableFormatted;
            
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

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().addAll("popup-button", "popup-secondary-button");
        cancelButton.setPrefSize(160, 42);
        cancelButton.setOnAction(e -> hideModal());

        Button addButton = new Button("Apply changes");
        addButton.getStyleClass().addAll("popup-button", "popup-primary-button");
        addButton.setPrefSize(160, 42);
        addButton.setOnAction(e -> this.handleReplaceSaved());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        buttons.getChildren().addAll(cancelButton, spacer, addButton);

        popup.getChildren().addAll(title, formGrid, buttons);

        showModal(popup);
    }
    
    private Stage createPopupStage(String titleText) {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle(titleText);

        Window owner = warehouseRowsContainer.getScene() != null
                ? warehouseRowsContainer.getScene().getWindow()
                : null;

        if (owner != null) {
            stage.initOwner(owner);
        }

        return stage;
    }

    private VBox createPopupRoot() {
        VBox root = new VBox(28);
        root.setPadding(new Insets(28, 30, 24, 30));
        root.setAlignment(Pos.TOP_LEFT);
        root.getStyleClass().add("popup-root");
        return root;
    }

    private Label createPopupTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("popup-title");
        return label;
    }

    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("popup-label");
        return label;
    }

    private Label createValueLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("popup-value-label");
        return label;
    }

    private Label createFileLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("popup-file-label");
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    private TextField createPopupTextField(String prompt) {
        TextField textField = new TextField();
        textField.setPromptText(prompt);
        textField.getStyleClass().add("popup-text-field");
        textField.setPrefHeight(38);
        return textField;
    }

    private Button createUploadButton(String text) {
        Image importIcon = loadImage("/icons/png-dark/import_black.png");

        ImageView iconView = new ImageView(importIcon);
        iconView.setFitWidth(18);
        iconView.setFitHeight(18);
        iconView.setPreserveRatio(true);

        Label label = new Label(text);
        label.getStyleClass().add("upload-button-text");

        HBox content = new HBox(12, iconView, label);
        content.setAlignment(Pos.CENTER_LEFT);

        Button button = new Button();
        button.setGraphic(content);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(40);
        button.getStyleClass().addAll("popup-button", "upload-button");

        return button;
    }

    private Image loadImage(String path) {
        URL resource = getClass().getResource(path);
        if (resource == null) {
            throw new IllegalStateException("Missing icon resource: " + path);
        }
        return new Image(resource.toExternalForm());
    }
    
    private void handleOpenWarehouse(BriefWarehouse briefWarehouse) {
    	System.out.println("EDIT WAREHOUSE CLICKED");
    	try {
    		WarehouseFormatted warehouseFormatted = this.warehouseService.getWarehouseFormatted(briefWarehouse);
			loadScreen(EDIT_WAREHOUSE, warehouseFormatted);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @FXML
    private void handleOpenSmallPriceList() {
    	try {
    		SmallPriceListFormatted priceListFormatted = this.warehouseService.getSmallPriceListFormatted();
			loadScreen(SMALL_PRICE_LIST_VIEW);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @FXML
    private void handleReplaceSmallPriceList() {
    	Window currentWindow = addWarehouseButton.getScene().getWindow();

        File file = FilePicker.pickFile(currentWindow,
        	    new FileChooser.ExtensionFilter("CSV files", "*.csv"),
        	    new FileChooser.ExtensionFilter("XML files", "*.xml")
        	);

        if (file == null) { return; }
        
    	SmallPriceListFormatted smallPriceListFormatted = warehouseParsingService.parseSmallPriceList(file);

        // Once the parser has some kind of format check, I can add a popup informing the user the format is wrong
    	if (smallPriceListFormatted == null) { return; }
    }
    
    private void handleReplaceSaved() {
    	if(this.selectedPriceListFormatted == null && this.selectedRegionTableFormatted == null) {
    		return;
    		// Make popup about not adding anything
    	}
    	
    	if(this.selectedPriceListFormatted != null) {
    		warehouseService.replacePriceList(this.selectedPriceListFormatted, this.selectedWarehouse);
    	}
    	if(this.selectedRegionTableFormatted != null) {
    		warehouseService.replaceRegionTable(this.selectedRegionTableFormatted, this.selectedWarehouse);
    	}
    	hideModal();
    }
}