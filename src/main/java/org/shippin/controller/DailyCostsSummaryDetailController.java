package org.shippin.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Controller for DailyCostsSummaryDetail.fxml
 *
 * <p>Displays all shipment entries for a single selected date.
 * Populated by calling {@link #setDate(LocalDate, List)} from the
 * parent controller (e.g. DailyCostsSummariesController) right after
 * loading this view.</p>
 *
 * <p>Data model: {@link ShipmentEntry} — a lightweight record holding
 * the time string, description, and cost for one shipment row.</p>
 */
public class DailyCostsSummaryDetailController implements Initializable {

    // ── FXML injections ────────────────────────────────────────────────
    @FXML private Label  lblTitle;
    @FXML private VBox   shipmentList;
    @FXML private Button btnAddShipment;
    @FXML private Label  lblTotal;

    // ── State ──────────────────────────────────────────────────────────
    private LocalDate            currentDate;
    private List<ShipmentEntry>  entries = new ArrayList<>();

    // ── Date formatter shown in the title (e.g. "1.4.2026") ───────────
    private static final DateTimeFormatter TITLE_FMT =
            DateTimeFormatter.ofPattern("d.M.yyyy");

    // ══════════════════════════════════════════════════════════════════
    // Initializable
    // ══════════════════════════════════════════════════════════════════

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Nothing to do here — data is pushed via setDate().
        // If you wire this screen standalone (e.g. in Scene Builder preview)
        // you can call loadSampleData() for convenience.
    }

    // ══════════════════════════════════════════════════════════════════
    // Public API — called by the parent controller
    // ══════════════════════════════════════════════════════════════════

    /**
     * Sets the date and shipment list, then re-renders the screen.
     *
     * @param date    the day whose costs are displayed
     * @param entries list of {@link ShipmentEntry} for that day
     */
    public void setDate(LocalDate date, List<ShipmentEntry> entries) {
        this.currentDate = date;
        this.entries     = entries != null ? entries : new ArrayList<>();
        refresh();
    }

    // ══════════════════════════════════════════════════════════════════
    // FXML handlers
    // ══════════════════════════════════════════════════════════════════

    /** "+ Add new shipment" button — open add-shipment dialog/screen. */
    @FXML
    private void handleAddShipment() {
        // TODO: open AddShipmentDialog or navigate to Add Shipment screen.
        System.out.println("Add shipment for " + currentDate);
    }

    // ══════════════════════════════════════════════════════════════════
    // Private helpers
    // ══════════════════════════════════════════════════════════════════

    /** Re-renders the title, shipment list and total from current state. */
    private void refresh() {
        // Title
        if (currentDate != null) {
            lblTitle.setText(currentDate.format(TITLE_FMT) + " – Daily costs summary");
        }

        // Shipment rows
        shipmentList.getChildren().clear();
        for (ShipmentEntry entry : entries) {
            shipmentList.getChildren().add(buildRow(entry));
        }

        // Total
        double total = entries.stream().mapToDouble(ShipmentEntry::cost).sum();
        lblTotal.setText(formatCost(total));
    }

    /**
     * Builds one shipment row HBox from a {@link ShipmentEntry}.
     *
     * Layout matches the screenshot:
     *   [time]  [description ──── grow]  [amount]  [✎]  [🗑]
     */
    private HBox buildRow(ShipmentEntry entry) {
        HBox row = new HBox();
        row.getStyleClass().add("dcd-row");
        row.setAlignment(Pos.CENTER_LEFT);

        // Time
        Label lblTime = new Label(entry.time());
        lblTime.getStyleClass().add("dcd-row-time");

        // Description (grows)
        Label lblDesc = new Label(entry.description());
        lblDesc.getStyleClass().add("dcd-row-desc");
        HBox.setHgrow(lblDesc, javafx.scene.layout.Priority.ALWAYS);

        // Amount
        Label lblAmount = new Label(formatCost(entry.cost()));
        lblAmount.getStyleClass().add("dcd-row-amount");

        // Edit button
        Button btnEdit = new Button("✎");
        btnEdit.getStyleClass().add("dcd-icon-btn");
        btnEdit.setOnAction(e -> handleEdit(entry));

        // Delete button
        Button btnDelete = new Button("🗑");
        btnDelete.getStyleClass().addAll("dcd-icon-btn", "dcd-icon-btn-delete");
        btnDelete.setOnAction(e -> handleDelete(entry));

        row.getChildren().addAll(lblTime, lblDesc, lblAmount, btnEdit, btnDelete);
        return row;
    }

    /** Edit action for a shipment row. */
    private void handleEdit(ShipmentEntry entry) {
        // TODO: open Edit Shipment dialog pre-filled with entry data.
        System.out.println("Edit: " + entry);
    }

    /** Delete action for a shipment row. */
    private void handleDelete(ShipmentEntry entry) {
        // TODO: show confirmation dialog, then remove from DB and refresh.
        entries.remove(entry);
        refresh();
        System.out.println("Deleted: " + entry);
    }

    /**
     * Formats a cost value to the app's locale style.
     * Examples: 58.4 → "58,40€"   288.0 → "288€"
     */
    private static String formatCost(double cost) {
        if (cost == Math.floor(cost)) {
            return String.format(Locale.ROOT, "%.0f€", cost);
        }
        // Replace decimal point with comma (European style)
        return String.format(Locale.ROOT, "%.2f€", cost).replace('.', ',');
    }

    // ══════════════════════════════════════════════════════════════════
    // Sample data helper (for standalone Scene Builder preview)
    // ══════════════════════════════════════════════════════════════════

    /** Loads hard-coded sample data identical to the screenshot. */
    public void loadSampleData() {
        List<ShipmentEntry> sample = List.of(
            new ShipmentEntry(" 8:23", "Shipment for  Saint Gobain 1",           58.40),
            new ShipmentEntry("10:58", "Shipment some random company from other city", 22.10),
            new ShipmentEntry("11:44", "Shipment some random company from other city", 122.00),
            new ShipmentEntry("12:08", "Shipment some random company from other city", 128.70)
        );
        setDate(LocalDate.of(2026, 4, 1), sample);
    }

    // ══════════════════════════════════════════════════════════════════
    // Data model
    // ══════════════════════════════════════════════════════════════════

    /**
     * Immutable record representing one shipment row.
     *
     * @param time        display time string, e.g. "8:23"
     * @param description shipment description
     * @param cost        cost in euros (double)
     */
    public record ShipmentEntry(String time, String description, double cost) {}
}
