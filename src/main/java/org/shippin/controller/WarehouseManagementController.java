package org.shippin.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.*;

/**
 * Controller for WarehouseManagement.fxml
 *
 * Responsibilities:
 *  - Render the list of warehouses with Edit / Replace / Export / Delete actions
 *  - Handle "Add new warehouse" dialog trigger
 *  - Handle "View / Change small package price list" buttons
 */
public class WarehouseManagementController implements Initializable {

    // ── FXML injections ──────────────────────────────────────────────────────

    @FXML private VBox warehouseList;
    @FXML private Button addWarehouseBtn;
    @FXML private Button viewPriceListBtn;
    @FXML private Button changePriceListBtn;

    // ── Sample data (replace with DB / service call) ──────────────────────────

    /** Mutable list so add/delete update the UI instantly */
    private final List<String> warehouses = new ArrayList<>(List.of(
            "ZBS - BA",
            "ZBS - BB",
            "ZBS - RK",
            "ZBS - PO",
            "ZBS - ZA",
            "ZBS - KE",
            "ZBS - PE"
    ));

    // ── Initializable ─────────────────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buildWarehouseList();
    }

    // ── List builder ──────────────────────────────────────────────────────────

    /** Clears and rebuilds the warehouse VBox from the current list */
    private void buildWarehouseList() {
        warehouseList.getChildren().clear();

        for (String code : warehouses) {
            HBox row = buildWarehouseRow(code);
            warehouseList.getChildren().add(row);
        }
    }

    /**
     * Builds a single warehouse row:
     *   [ code label ] [spacer] [ edit ] [ replace ] [ export ] [ delete ]
     */
    private HBox buildWarehouseRow(String code) {
        // Warehouse code label
        Label nameLabel = new Label(code);
        nameLabel.getStyleClass().add("wm-row-name");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        // Edit button — pencil icon
        Button editBtn = makeIconBtn("✎", "wm-icon-btn", 60);
        editBtn.setTooltip(new Tooltip("Edit warehouse"));
        editBtn.setOnAction(e -> onEdit(code));

        // Replace button — upload arrow icon
        Button replaceBtn = makeIconBtn("Replace", "wm-icon-btn wm-icon-btn-replace", 72);
        replaceBtn.setTooltip(new Tooltip("Replace data"));
        replaceBtn.setOnAction(e -> onReplace(code));

        // Export to CSV button — file icon
        Button exportBtn = makeIconBtn("Export", "wm-icon-btn wm-icon-btn-export", 96);
        exportBtn.setTooltip(new Tooltip("Export to CSV"));
        exportBtn.setOnAction(e -> onExportCsv(code));

        // Delete button — trash icon
        Button deleteBtn = makeIconBtn("🗑", "wm-icon-btn wm-icon-btn-delete", 60);
        deleteBtn.setTooltip(new Tooltip("Delete warehouse"));
        deleteBtn.setOnAction(e -> onDelete(code));

        HBox row = new HBox(nameLabel, editBtn, replaceBtn, exportBtn, deleteBtn);
        row.getStyleClass().add("wm-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMaxWidth(Double.MAX_VALUE);

        return row;
    }

    /** Creates a reusable icon button with given style class(es) and fixed width */
    private Button makeIconBtn(String icon, String styleClasses, double prefWidth) {
        Button btn = new Button(icon);
        // Support space-separated multiple style classes
        for (String sc : styleClasses.split(" ")) {
            if (!sc.isBlank()) btn.getStyleClass().add(sc.trim());
        }
        btn.setPrefWidth(prefWidth);
        btn.setFocusTraversable(false);
        return btn;
    }

    // ── Row action handlers ───────────────────────────────────────────────────

    private void onEdit(String code) {
        // TODO: open edit dialog for this warehouse
        System.out.println("Edit: " + code);
    }

    private void onReplace(String code) {
        // TODO: open file chooser to replace warehouse data
        System.out.println("Replace: " + code);
    }

    private void onExportCsv(String code) {
        // TODO: export warehouse data to CSV file
        System.out.println("Export CSV: " + code);
    }

    private void onDelete(String code) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Warehouse");
        confirm.setHeaderText("Delete " + code + "?");
        confirm.setContentText("This action cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                warehouses.remove(code);
                buildWarehouseList();  // re-render
            }
        });
    }

    // ── Top bar handler ───────────────────────────────────────────────────────

    @FXML
    private void onAddWarehouse() {
        // TODO: open "Add new warehouse" dialog
        // After dialog confirms, call:
        //   warehouses.add(newCode);
        //   buildWarehouseList();
        System.out.println("Add new warehouse");
    }

    // ── Bottom button handlers ────────────────────────────────────────────────

    @FXML
    private void onViewPriceList() {
        // TODO: navigate to / show the small package price list view
        System.out.println("View small package price list");
    }

    @FXML
    private void onChangePriceList() {
        // TODO: open editor for the small package price list
        System.out.println("Change small package price list");
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Reload warehouse list from an external source (e.g. DB result).
     * @param codes ordered list of warehouse codes
     */
    public void loadWarehouses(List<String> codes) {
        warehouses.clear();
        warehouses.addAll(codes);
        buildWarehouseList();
    }
}
