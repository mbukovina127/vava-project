package org.shippin.controller;

import lombok.extern.log4j.Log4j2;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

import org.shippin.controller.utils.GenericPopup;
import org.shippin.domain.Table;
import org.shippin.domain.formatted.SmallPriceListFormatted;
import org.shippin.domain.formatted.SmallPriceListRow;
import org.shippin.services.WarehouseService;

import static org.shippin.dto.Screens.WAREHOUSE_MANAGEMENT;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

@Log4j2
public class SmallPriceListViewController extends BaseController<Void> implements Initializable {

    @FXML
    private GridPane tableGrid;

    // Inject any Table<SmallPriceListRow> here — swap out for a backend-provided instance as needed
    private Table<SmallPriceListRow> table;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
        this.table = WarehouseService.getInstance().getSmallPriceListFormatted();
      } catch (SQLException e) {
        log.error("Small price list operation failed", e);
        new GenericPopup(resources).showOkPopup(this, "SQL exception", "There is a problem with the database.");
      }
        populateGrid(table);
    }

    /**
     * Inject a table from the backend instead of using the static one.
     * Call this before the scene is shown, or trigger a refresh afterward.
     */
    public void setTable(Table<SmallPriceListRow> table) {
        this.table = table;
        populateGrid(table);
    }

    // -------------------------------------------------------------------------
    // Grid population — works with any Table<SmallPriceListRow>
    // -------------------------------------------------------------------------
    private void addCell(GridPane grid, String text, int col, int row, boolean isHeader) {
        Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        label.setWrapText(true);

        label.getStyleClass().add(isHeader ? "header-cell" : "data-cell");
        if (col == 0) label.getStyleClass().add("cell-first-col");
        if (row == 0) label.getStyleClass().add("cell-first-row");

        GridPane.setHalignment(label, HPos.CENTER);
        GridPane.setFillWidth(label, true);
        GridPane.setFillHeight(label, true);
        grid.add(label, col, row);
    }
    
    private void addRowConstraint(GridPane grid, double height) {
        RowConstraints rc = new RowConstraints();
        rc.setMinHeight(height);
        rc.setPrefHeight(height);
        grid.getRowConstraints().add(rc);
    }
    
    private void populateGrid(Table<SmallPriceListRow> table) {
        tableGrid.getChildren().clear();
        tableGrid.getRowConstraints().clear();

        addCell(tableGrid, "Hmotnosť do (v kg)", 0, 0, true);
        addCell(tableGrid, "Cena",                1, 0, true);
        addRowConstraint(tableGrid, 52);

        List<SmallPriceListRow> rows = table.getRows();
        for (int i = 0; i < rows.size(); i++) {
            SmallPriceListRow row = rows.get(i);
            int gridRow = i + 1;
            addCell(tableGrid, formatWeight(row.getWeight()), 0, gridRow, false);
            addCell(tableGrid, formatPrice(row.getCost()),    1, gridRow, false);
            addRowConstraint(tableGrid, 40);
        }
    }

    private String formatWeight(float weight) {
        if (weight == Math.floor(weight)) {
            return String.valueOf((int) weight);
        }
        return String.format("%.1f", weight);
    }

    private String formatPrice(float cost) {
        return String.format("%.2f €", cost).replace('.', ',');
    }
    
    @FXML
    private void handleLeave() {
        try {
        loadScreen(WAREHOUSE_MANAGEMENT);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        log.error("Small price list operation failed", e);
      }
    }
}
