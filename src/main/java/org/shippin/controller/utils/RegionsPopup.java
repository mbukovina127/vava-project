package org.shippin.controller.utils;

import org.shippin.controller.BaseController;
import org.shippin.domain.BriefWarehouse;
import org.shippin.domain.formatted.RegionTableFormatted;
import org.shippin.domain.formatted.RegionTableRow;
import org.shippin.util.Range;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.ResourceBundle;

public class RegionsPopup extends GenericPopup {

    private static final int RANGES_PER_ROW = 4;

    public RegionsPopup(ResourceBundle resources) {
        super(resources);
    }

    public void show(BaseController<?> controller, BriefWarehouse warehouse, RegionTableFormatted regionTable) {
        VBox popup = createPopupRoot();
        popup.setMaxWidth(720);
        popup.setPrefWidth(720);

        Label title = createPopupTitle(t("%cost_estimation.regions_popup.title"));

        Label subtitle = new Label(warehouse.getName() + "  —  " + warehouse.getRegionName());
        subtitle.getStyleClass().add("popup-label");
        subtitle.setStyle("-fx-text-fill: #000000;");

        GridPane grid = buildRegionGrid(regionTable);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setMaxHeight(420);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        HBox buttons = new HBox();
        buttons.setAlignment(Pos.CENTER_RIGHT);

        Button closeButton = new Button(t("%cost_estimation.regions_popup.close"));
        closeButton.getStyleClass().addAll("popup-button", "tertiary-button");
        closeButton.setPrefSize(160, 42);
        closeButton.setOnAction(e -> controller.hideModal());

        buttons.getChildren().add(closeButton);

        popup.getChildren().addAll(title, subtitle, scroll, buttons);
        controller.showModal(popup);
    }

    private GridPane buildRegionGrid(RegionTableFormatted regionTable) {
        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(0);

        ColumnConstraints regionCol = new ColumnConstraints();
        regionCol.setPrefWidth(130);
        regionCol.setMinWidth(120);
        grid.getColumnConstraints().add(regionCol);

        for (int i = 0; i < RANGES_PER_ROW; i++) {
            ColumnConstraints rangeCol = new ColumnConstraints();
            rangeCol.setPrefWidth(148);
            rangeCol.setMinWidth(145);
            rangeCol.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(rangeCol);
        }

        int rowIndex = 0;

        addCell(grid, t("%cost_estimation.regions_popup.col_region"), 0, rowIndex, 1, 1, true, false);
        addCell(grid, t("%cost_estimation.regions_popup.col_ranges"), 1, rowIndex, RANGES_PER_ROW, 1, true, false);
        rowIndex++;

        for (RegionTableRow row : regionTable.getRows()) {
            List<Range> ranges = row.getRanges();
            int lines = (int) Math.ceil(ranges.size() / (double) RANGES_PER_ROW);

            addCell(grid, row.getRegionCode(), 0, rowIndex, 1, lines, false, true);

            for (int line = 0; line < lines; line++) {
                for (int col = 0; col < RANGES_PER_ROW; col++) {
                    int rangeIndex = line * RANGES_PER_ROW + col;
                    String text = rangeIndex < ranges.size()
                            ? String.format("%05d-%05d", ranges.get(rangeIndex).getMin(), ranges.get(rangeIndex).getMax())
                            : "";
                    addCell(grid, text, col + 1, rowIndex + line, 1, 1, false, false);
                }
            }

            rowIndex += lines;
        }

        return grid;
    }

    private void addCell(GridPane grid, String text, int col, int row, int colspan, int rowspan,
                         boolean isHeader, boolean isRegion) {
        Label label = new Label(text);
        label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        label.setPadding(new Insets(6, 10, 6, 10));

        String base = "-fx-border-color: #cccccc; -fx-border-width: 0.5; -fx-text-fill: #000000;";
        if (isHeader) {
            label.setStyle(base + " -fx-font-weight: bold; -fx-background-color: #e8e8e8;");
        } else if (isRegion) {
            label.setStyle(base + " -fx-font-weight: bold; -fx-background-color: #f4f4f4;");
        } else {
            label.setStyle(base);
        }

        GridPane.setHgrow(label, Priority.ALWAYS);
        GridPane.setVgrow(label, Priority.ALWAYS);
        GridPane.setFillWidth(label, true);
        GridPane.setFillHeight(label, true);

        grid.add(label, col, row, colspan, rowspan);
    }
}
