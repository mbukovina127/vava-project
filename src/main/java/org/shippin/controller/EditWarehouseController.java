package org.shippin.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import org.shippin.controller.utils.CostEstimationInput;
import org.shippin.controller.utils.GenericPopup;
import org.shippin.controller.utils.InputValidator;
import org.shippin.domain.BriefWarehouse;
import org.shippin.domain.Warehouse;
import org.shippin.domain.formatted.PriceListFormatted;
import org.shippin.domain.formatted.PriceListRow;
import org.shippin.domain.formatted.RegionTableFormatted;
import org.shippin.domain.formatted.RegionTableRow;
import org.shippin.domain.formatted.WarehouseFormatted;
import org.shippin.services.WarehouseService;
import org.shippin.util.Range;
import static org.shippin.dto.Screens.WAREHOUSE_MANAGEMENT;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class EditWarehouseController extends BaseController<BriefWarehouse> implements Initializable {

    @FXML
    private GridPane regionGrid;
    @FXML
    private TextField documentTitleField;
    @FXML
    private TextField pickupPlaceField;
    @FXML
    private TextField storageRegionField;

    private PriceListFormatted priceList;
    private RegionTableFormatted regionTableFormatted;

	private WarehouseService warehouseService;

	private WarehouseFormatted warehouse;
	private ResourceBundle resources;

    private void addCell(GridPane grid, String text, int col, int row, int colspan, int rowspan, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);

        label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        label.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        label.setAlignment(Pos.CENTER);

        GridPane.setHgrow(label, Priority.ALWAYS);
        GridPane.setVgrow(label, Priority.ALWAYS);
        GridPane.setFillWidth(label, true);
        GridPane.setFillHeight(label, true);

        grid.add(label, col, row, colspan, rowspan);
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	this.resources = resources;
    }
    
    @Override
    protected void onData(BriefWarehouse briefWarehouse) {
    	this.warehouseService = new WarehouseService();
    	this.briefWarehouse = briefWarehouse;
    	try {
			this.warehouse = this.warehouseService.getWarehouseFormatted(briefWarehouse);
		} catch (SQLException e) {
			e.printStackTrace();
			new GenericPopup(this.resources).showOkPopup(this, "%generic.failed_to_fetch", "%generic.database_problem");
		}
    	documentTitleField.setText(briefWarehouse.getName()); 
    	pickupPlaceField.setText(briefWarehouse.getRegionName());
    	System.out.println(briefWarehouse.getPostal_code());  
    	System.out.println(String.valueOf(briefWarehouse.getPostal_code())); 
    	storageRegionField.setText(String.valueOf(briefWarehouse.getPostal_code()));
    	this.priceList = this.warehouse.getPriceList();
    	setupPriceListTable();
    	this.regionTableFormatted = this.warehouse.getRegionTable();
        setupRegionGrid();
        this.warehouseService = WarehouseService.getInstance();
    }
    
    @Override
    protected Class<BriefWarehouse> getDataType() {return BriefWarehouse.class;}
    
    private void setupRegionGrid() {
        regionGrid.getChildren().clear();

        int columnsPerLine = 4;
        int rowIndex = 0;

        addCell(regionGrid, "%edit_warehouse.postal_code_label", 0, rowIndex, 1, 1, "header-cell");
        addCell(regionGrid, "", 1, rowIndex, columnsPerLine, 1, "header-cell");

        rowIndex++;

        for (RegionTableRow row : regionTableFormatted.getRows()) {
            List<Range> ranges = row.getRanges();
            int lines = (int) Math.ceil(ranges.size() / (double) columnsPerLine);

            addCell(regionGrid, row.getRegionCode(), 0, rowIndex, 1, lines, "region-cell");

            for (int line = 0; line < lines; line++) {
                for (int col = 0; col < columnsPerLine; col++) {
                    int rangeIndex = line * columnsPerLine + col;

                    String text = "";

                    if (rangeIndex < ranges.size()) {
                        Range range = ranges.get(rangeIndex);
                        text = String.format("%05d-%05d", range.getMin(), range.getMax());
                    }

                    addCell(regionGrid, text, col + 1, rowIndex + line, 1, 1, "range-cell");
                }
            }

            rowIndex += lines;
        }
    }
    
    @FXML
    public void initialize() {
    }

    @FXML
    private GridPane priceListGrid;
	private BriefWarehouse briefWarehouse;

    private void setupPriceListTable() {
        // Remove only dynamically added children, keep the static FXML labels
        priceListGrid.getChildren().removeIf(
            node -> GridPane.getColumnIndex(node) != null && GridPane.getColumnIndex(node) > 1
                    || GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0
        );

        List<String> regionNames = priceList.getRows().isEmpty()
                ? List.of()
                : new ArrayList<>(priceList.getRows().get(0).getRegions().keySet());

        // Only dynamic header cells (region names)
        for (int i = 0; i < regionNames.size(); i++) {
            addCell(priceListGrid, regionNames.get(i), i + 2, 0, 1, 1, "header-cell");
        }

        // Data rows
        List<PriceListRow> rows = priceList.getRows();
        for (int rowIdx = 0; rowIdx < rows.size(); rowIdx++) {
            PriceListRow row = rows.get(rowIdx);
            int gridRow = rowIdx + 1;

            addCell(priceListGrid, String.format("%.0f", row.getWeight()),
                    0, gridRow, 1, 1, "range-cell");
            addCell(priceListGrid, String.format("%.1f", row.getVolume()).replace(".", ","),
                    1, gridRow, 1, 1, "range-cell");

            for (int i = 0; i < regionNames.size(); i++) {
                Float price = row.getRegions().get(regionNames.get(i));
                String text = price == null ? "" : String.format("%.2f €", price).replace(".", ",");
                addCell(priceListGrid, text, i + 2, gridRow, 1, 1, "range-cell");
            }
        }
    }
    
    @FXML
    private void handleLeave() {
    	try {
        loadScreen(WAREHOUSE_MANAGEMENT);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
    @FXML
    private void handleSave() {
    	try {
    		String title = documentTitleField.getText();  		
    		if(!InputValidator.isNotBlank(title)) {
    			new GenericPopup(this.resources).showOkPopup(this, "%generic.failed_to_update", "generic.regex.blank_name");
    			return;
    		} else if(!InputValidator.isValidLength(title, 20)) {
    			new GenericPopup(this.resources).showOkPopup(this, "%generic.failed_to_update", "%warehouse_management.add.regex.name_too_long");
    			return;
    		}
    		
    		String pickup = pickupPlaceField.getText(); 		
    		if(!InputValidator.isNotBlank(pickup)) {
    			new GenericPopup(this.resources).showOkPopup(this, "%generic.failed_to_update", "generic.regex.blank_pickup_place");
    			return;
    		} else if(!InputValidator.isValidLength(pickup, 30)) {
    			new GenericPopup(this.resources).showOkPopup(this, "%generic.failed_to_update", "%warehouse_management.add.regex.pickup_too_long");
    			return;
    		}
    		
    		String postalCodeString = storageRegionField.getText();
    		if(!InputValidator.isPostalCode(postalCodeString)) {
    			new GenericPopup(this.resources).showOkPopup(this, "%generic.failed_to_update", "%generic.regex.invalid_postal_code");
    			return;
    		}
    		
    		int postalCode = Integer.valueOf(postalCodeString.replaceAll("\\s+", ""));
    		
    		this.warehouseService.updateWarehouse(this.briefWarehouse, title, pickup, postalCode);
			loadScreen(WAREHOUSE_MANAGEMENT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
			new GenericPopup(this.resources).showOkPopup(this, "%generic.failed_to_update", "%generic.database_problem");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			new GenericPopup(this.resources).showOkPopup(this, "%generic.failed_to_update", "%generic.map.postal_code_to_coordinates_failed");
		}
    }
    
}