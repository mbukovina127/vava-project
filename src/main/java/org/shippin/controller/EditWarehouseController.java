package org.shippin.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import org.shippin.controller.utils.CostEstimationInput;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class EditWarehouseController extends BaseController<WarehouseFormatted> {

    @FXML
    private GridPane regionGrid;

    private PriceListFormatted priceList;
    private RegionTableFormatted regionTableFormatted;

	private WarehouseService warehouseService;

	private WarehouseFormatted warehouse;

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
    protected void onData(WarehouseFormatted warehouseFormatted) {
    	this.warehouse = warehouseFormatted;
    	this.priceList = this.warehouse.getPriceList();
    	setupPriceListTable();
    	this.regionTableFormatted = this.warehouse.getRegionTable();
        setupRegionGrid();
        this.warehouseService = WarehouseService.getInstance();
        System.out.println(warehouse);
    }
    
    @Override
    protected Class<WarehouseFormatted> getDataType() {return WarehouseFormatted.class;}
    
    private void setupRegionGrid() {
        regionGrid.getChildren().clear();

        int columnsPerLine = 4;
        int rowIndex = 0;

        addCell(regionGrid, "Rozdelenie PSČ:", 0, rowIndex, 1, 1, "header-cell");
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
        loadData();
    }

    @FXML
    private GridPane priceListGrid;

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
    
    public void loadData() {
        priceList = new PriceListFormatted();

        priceList.addRow(priceRow(50, 0.2f, 17.67f, 18.08f, 19.22f, 19.72f, 20.17f));
        priceList.addRow(priceRow(100, 0.4f, 19.34f, 19.90f, 22.57f, 23.46f, 24.39f));
        priceList.addRow(priceRow(150, 0.6f, 22.01f, 22.65f, 25.27f, 26.35f, 27.70f));
        priceList.addRow(priceRow(200, 0.8f, 23.90f, 24.56f, 28.02f, 29.37f, 31.19f));
        priceList.addRow(priceRow(250, 1.0f, 26.35f, 26.97f, 31.19f, 32.75f, 35.01f));
        priceList.addRow(priceRow(300, 1.2f, 28.71f, 29.44f, 34.28f, 36.18f, 38.90f));
        priceList.addRow(priceRow(350, 1.4f, 30.67f, 31.88f, 37.29f, 39.66f, 42.86f));
        priceList.addRow(priceRow(400, 1.6f, 32.47f, 33.81f, 39.89f, 42.58f, 46.20f));
        priceList.addRow(priceRow(450, 1.8f, 34.52f, 35.61f, 42.49f, 45.34f, 50.22f));
        priceList.addRow(priceRow(500, 2.0f, 36.21f, 37.66f, 45.18f, 48.92f, 54.81f));
        priceList.addRow(priceRow(600, 2.4f, 41.33f, 43.38f, 53.97f, 59.17f, 66.24f));
        priceList.addRow(priceRow(700, 2.8f, 47.18f, 49.79f, 63.62f, 69.58f, 77.81f));
        priceList.addRow(priceRow(800, 3.2f, 51.90f, 55.34f, 70.47f, 77.30f, 86.77f));
        priceList.addRow(priceRow(900, 3.6f, 55.99f, 59.88f, 76.78f, 84.51f, 95.16f));
        priceList.addRow(priceRow(1000, 4.0f, 60.72f, 64.42f, 83.27f, 91.56f, 103.37f));
        priceList.addRow(priceRow(1250, 5.0f, 71.94f, 74.49f, 99.20f, 108.88f, 123.63f));
        priceList.addRow(priceRow(1500, 6.0f, 83.03f, 85.23f, 114.32f, 125.59f, 143.29f));
        priceList.addRow(priceRow(1750, 7.0f, 93.15f, 96.14f, 128.34f, 141.24f, 161.89f));
        priceList.addRow(priceRow(2000, 8.0f, 102.56f, 106.07f, 142.53f, 157.25f, 180.85f));
        priceList.addRow(priceRow(2250, 9.0f, 111.65f, 115.13f, 156.18f, 172.63f, 199.20f));
        priceList.addRow(priceRow(2500, 10.0f, 120.40f, 124.40f, 169.63f, 187.95f, 217.43f));
        priceList.addRow(priceRow(3500, 14.0f, 172.04f, 179.38f, 240.26f, 265.54f, 304.74f));
        priceList.addRow(priceRow(5000, 20.0f, 216.70f, 223.92f, 305.34f, 338.31f, 391.36f));

        regionTableFormatted = new RegionTableFormatted();

        regionTableFormatted.addRow(regionRow("BA1",
                r(81000, 85999)
        ));

        regionTableFormatted.addRow(regionRow("BA2",
                r(90001, 91099),
                r(91700, 92099),
                r(92242, 93399),
                r(94000, 95499)
        ));

        regionTableFormatted.addRow(regionRow("BA3",
                r(91331, 91857),
                r(91901, 91999),
                r(91100, 91699),
                r(92100, 92241),
                r(95500, 95999)
        ));

        regionTableFormatted.addRow(regionRow("ZA",
                r(1000, 1826),
                r(1861, 1864),
                r(20000, 39999),
                r(93400, 93999),
                r(96000, 96999),
                r(97400, 99399)
        ));

        regionTableFormatted.addRow(regionRow("ZV",
                r(4901, 4901),
                r(4913, 4918),
                r(4961, 4964),
                r(5001, 5001)
        ));

        regionTableFormatted.addRow(regionRow("KE",
                r(4000, 4900),
                r(4902, 4912),
                r(4919, 4960),
                r(4965, 5000),
                r(5002, 6599),
                r(6600, 9599)
        ));
    }

    private PriceListRow priceRow(
            float weight,
            float volume,
            float za1,
            float za2,
            float zv,
            float ba,
            float ke
    ) {
        LinkedHashMap<String, Float> regions = new LinkedHashMap<>();
        regions.put("ZA1", za1);
        regions.put("ZA2", za2);
        regions.put("ZV", zv);
        regions.put("BA", ba);
        regions.put("KE", ke);

        return new PriceListRow(weight, volume, regions);
    }

    private RegionTableRow regionRow(String code, Range... ranges) {
        RegionTableRow row = new RegionTableRow(code);

        for (Range range : ranges) {
            row.addRange(range);
        }

        return row;
    }

    private Range r(int from, int to) {
        return new Range(from, to);
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
}