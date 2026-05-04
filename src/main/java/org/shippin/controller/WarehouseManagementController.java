package org.shippin.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import org.shippin.services.MapService;
import org.shippin.controller.utils.GenericPopup;
import org.shippin.controller.utils.InputValidator;
import org.shippin.controller.utils.warehousemanagement.popups.AddWarehousePopup;
import org.shippin.controller.utils.warehousemanagement.popups.DeleteWarehousePopup;
import org.shippin.controller.utils.warehousemanagement.popups.ExportChoicePopup;
import org.shippin.controller.utils.warehousemanagement.popups.ReplaceWarehousePopup;
import org.shippin.domain.BriefWarehouse;
import org.shippin.domain.Coordinates;
import org.shippin.domain.CoreWarehouseInfo;
import org.shippin.domain.Warehouse;
import org.shippin.services.WarehouseParsingService;
import org.shippin.util.WarehouseConvertor;
import org.shippin.util.io.FilePicker;
import org.shippin.domain.formatted.PriceListFormatted;
import org.shippin.domain.formatted.RegionTableFormatted;
import org.shippin.domain.formatted.SmallPriceListFormatted;
import org.shippin.domain.formatted.WarehouseFormatted;
import org.shippin.exception.IncompatibleTablesException;
import org.shippin.services.WarehouseService;

import static org.shippin.dto.Screens.EDIT_WAREHOUSE;
import static org.shippin.dto.Screens.SMALL_PRICE_LIST_VIEW;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import lombok.Getter;
import lombok.Setter;

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
    private ResourceBundle resources;
    
    private WarehouseParsingService warehouseParsingService;
	private WarehouseService warehouseService;
	
	@Getter @Setter
	private List<BriefWarehouse> warehouseList;
	@Getter @Setter
	private Warehouse selectedWarehouse;
	@Getter @Setter
	private RegionTableFormatted selectedRegionTableFormatted;
	@Getter @Setter
	private PriceListFormatted selectedPriceListFormatted;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	this.resources = resources;
        Image addIcon = loadImage("/icons/png-dark/plus_no_circle_black.png");
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
        try {
			this.warehouseList = warehouseService.getBriefWarehouses();
		} catch (SQLException e) {
			e.printStackTrace();
			new GenericPopup(this.resources).showOkPopup(this, "SQL exception", "There is a problem with the database.");
		}
        renderWarehouses(warehouseList);
    }

    public void renderWarehouses(List<BriefWarehouse> warehouses) {
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
        replaceButton.setOnAction(event -> {
			try {
				new ReplaceWarehousePopup(resources).show(this, warehouse);
			} catch (SQLException e) {
				e.printStackTrace();
				new GenericPopup(this.resources).showOkPopup(this, "SQL exception", "There is a problem with the database.");
			}
		});
        GridPane.setColumnIndex(replaceButton, 2);

        Button exportButton = createTableIconButton(exportIcon, 26.0, 26.0, 34.0, 32.0);
        GridPane.setColumnIndex(exportButton, 3);
        exportButton.setOnAction(event -> {
        	new ExportChoicePopup(resources).show(this, warehouse);
        });

        Button deleteButton = createTableIconButton(deleteIcon, 22.0, 22.0, 32.0, 32.0);
        GridPane.setColumnIndex(deleteButton, 4);
        deleteButton.setOnAction(event -> new DeleteWarehousePopup(resources).show(this, warehouse));

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
        new AddWarehousePopup(resources).show(this);
    }

    private Image loadImage(String path) {
        URL resource = getClass().getResource(path);
        if (resource == null) {
            throw new IllegalStateException("Missing icon resource: " + path);
        }
        return new Image(resource.toExternalForm());
    }
    
    private void handleOpenWarehouse(BriefWarehouse briefWarehouse) {
    	
    	try {
			loadScreen(EDIT_WAREHOUSE, briefWarehouse);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void handleAddWarehouseSaved(String name, String pickup) {
    	if(!InputValidator.isNotBlank(name)) {
			new GenericPopup(this.resources).showOkPopup(this, "Save failed", "Invalid name: name must not be blank.");
			return;
		} else if(!InputValidator.isValidLength(name, 20)) {
			new GenericPopup(this.resources).showOkPopup(this, "Save failed", "Invalid name: name must be less than 20 characters.");
			return;
		}
				
		if(!InputValidator.isNotBlank(pickup)) {
			new GenericPopup(this.resources).showOkPopup(this, "Save failed", "Invalid pickup place: pickup place must not be blank.");
			return;
		} else if(!InputValidator.isValidLength(pickup, 30)) {
			new GenericPopup(this.resources).showOkPopup(this, "Save failed", "Invalid pickup: pickup place must be less than 30 characters.");
			return;
		} 
    	
    	if (this.selectedPriceListFormatted == null || this.selectedRegionTableFormatted == null) {
    		new GenericPopup(this.resources).showOkPopup(this, "Insert failed", "Missing table: you need to upload both a price list and a region table.");
    	}
    	
    	try {
    		this.warehouseService.addWarehouse(name, pickup, 0,
    				this.selectedPriceListFormatted, this.selectedRegionTableFormatted);
    		hideModal();
    	} catch (IncompatibleTablesException e) {
    		System.out.println(this.selectedPriceListFormatted);
    		System.out.println(this.selectedRegionTableFormatted);
    		new GenericPopup(this.resources).showOkPopup(this, "Insert failed", "Invalid tables: imported price list and region tables are incompatible. Both tables must include the same set of regions.");
    	} catch (SQLException e) {
			e.printStackTrace();
			new GenericPopup(this.resources).showOkPopup(this, "SQL exception", "There is a problem with the database.");
		}
    	
        try {
			this.warehouseList = warehouseService.getBriefWarehouses();
		} catch (SQLException e) {
			e.printStackTrace();
			new GenericPopup(this.resources).showOkPopup(this, "SQL exception", "There is a problem with the database.");
		}
        renderWarehouses(warehouseList);
    }
    
    @FXML
    private void handleOpenSmallPriceList() {
    	try {
    		try {
				SmallPriceListFormatted priceListFormatted = this.warehouseService.getSmallPriceListFormatted();
			} catch (SQLException e) {
				e.printStackTrace();
				new GenericPopup(this.resources).showOkPopup(this, "SQL exception", "There is a problem with the database.");
			}
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

    	try {
        this.warehouseService.setSmallPriceListFormatted(smallPriceListFormatted);
      } catch (SQLException e) {
        e.printStackTrace();
        new GenericPopup(this.resources).showOkPopup(this, "SQL exception", "There is a problem with the database.");
      }
    }
    
    public void deleteWarehouse(BriefWarehouse briefWarehouse) {
    	try {
			this.warehouseService.deleteWarehouse(briefWarehouse);
		} catch (SQLException e) {
			e.printStackTrace();
			new GenericPopup(this.resources).showOkPopup(this, "%generic.failed_to_insert", "%generic.database_problem");
		}
    	
    	try {
			this.warehouseList = warehouseService.getBriefWarehouses();
		} catch (SQLException e) {
			e.printStackTrace();
			new GenericPopup(this.resources).showOkPopup(this, "%generic.failed_to_insert", "%generic.database_problem");
		}
        renderWarehouses(warehouseList);
    }
    
}