package org.shippin.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.shippin.domain.Shipment;
import org.shippin.domain.enums.State;
import org.shippin.services.ShipmentService;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Controller for DailyCostsSummaryDetail.fxml
 *
 * <p>Displays all shipment entries for a single selected date.

 * parent controller (e.g. DailyCostsSummariesController) right after
 * loading this view.</p>
 *
 * <p>Data model: {@link ShipmentEntry} — a lightweight record holding
 * the time string, description, and cost for one shipment row.</p>
 */
public class DailyCostsSummaryDetailController
        extends BaseController<LocalDate>
        implements Initializable {

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
    private ShipmentService shipmentService;

    @Override
    protected Class<LocalDate> getDataType() {
        return LocalDate.class;
    }

    @Override
    protected void onData(LocalDate date) {
        this.currentDate = date;
        loadFromService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Nothing to do here — data is pushed via setDate().
        // If you wire this screen standalone (e.g. in Scene Builder preview)
        // you can call loadSampleData() for convenience.
        shipmentService = new ShipmentService();
    }

    // ══════════════════════════════════════════════════════════════════
    // Public API — called by the parent controller
    // ══════════════════════════════════════════════════════════════════


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
        double total = entries.stream().mapToDouble(ShipmentEntry::totalCost).sum();
        lblTotal.setText(formatCost(total));
    }

    /**
     * Builds one shipment row HBox from a {@link ShipmentEntry}.
     *
     * Layout matches the screenshot:
     *   [time]  [description ──── grow]  [amount]  [✎]  [🗑]
     */
    private HBox buildRow(ShipmentEntry entry) {
        HBox row = new HBox(16);
        row.getStyleClass().add("dcd-row");
        row.setAlignment(Pos.CENTER_LEFT);

        // ── Left: ID + time ───────────────────────────────────────────
        VBox leftBox = new VBox(2);
        Label lblId   = new Label("#" + entry.shipmentId());
        lblId.getStyleClass().add("dcd-row-id");
        Label lblTime = new Label(entry.time());
        lblTime.getStyleClass().add("dcd-row-time");
        leftBox.getChildren().addAll(lblId, lblTime);

        // ── Center: destination + fuel ────────────────────────────────
        VBox centerBox = new VBox(2);
        HBox.setHgrow(centerBox, Priority.ALWAYS);

        Label lblDest = new Label("Dest. region: " + entry.destRegion());
        lblDest.getStyleClass().add("dcd-row-desc");

        Label lblFuel = new Label("Fuel surcharge: " + formatCost(entry.fuelPayment()));
        lblFuel.getStyleClass().add("dcd-row-sub");

        centerBox.getChildren().addAll(lblDest, lblFuel);

        // ── Right: state + total ──────────────────────────────────────
        VBox rightBox = new VBox(2);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        Label lblState = new Label(entry.state().toString());
        lblState.getStyleClass().addAll("dcd-row-state", stateStyleClass(entry.state()));

        Label lblAmount = new Label(formatCost(entry.totalCost()));
        lblAmount.getStyleClass().add("dcd-row-amount");

        rightBox.getChildren().addAll(lblState, lblAmount);

        // ── Action buttons ────────────────────────────────────────────
        VBox btnBox = new VBox(4);
        btnBox.setAlignment(Pos.CENTER);

        Button btnEdit = new Button("✎");
        btnEdit.getStyleClass().add("dcd-icon-btn");
        btnEdit.setOnAction(e -> handleEdit(entry));

        Button btnDelete = new Button("🗑");
        btnDelete.getStyleClass().addAll("dcd-icon-btn", "dcd-icon-btn-delete");
        btnDelete.setOnAction(e -> handleDelete(entry));

        btnBox.getChildren().addAll(btnEdit, btnDelete);

        row.getChildren().addAll(leftBox, centerBox, rightBox, btnBox);
        return row;
    }

    private String stateStyleClass(State state) {
        return switch (state) {
            case NOT_READY  -> "dcd-state-not-ready";
            case READY_FOR_DELIVERY      -> "dcd-state-ready";
            case BEING_DELIVERED -> "dcd-state-transit";
            case DELIVERED  -> "dcd-state-delivered";
            case CANCELED  -> "dcd-state-cancelled";
            case FAILED  -> "dcd-state-failed";
            default         -> "";
        };

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
    private static String formatCost(float cost)  { return formatCost((double) cost); }

    // ══════════════════════════════════════════════════════════════════
    // Sample data helper (for standalone Scene Builder preview)
    // ══════════════════════════════════════════════════════════════════

    /** Loads hard-coded sample data identical to the screenshot. */


    private void loadFromService() {
        try {
            List<Shipment> shipments = shipmentService.getShipmentsForDay(currentDate);
            entries.clear();

            for (Shipment s : shipments) {
                entries.add(new ShipmentEntry(
                        s.getShipment_id(),
                        s.getCreated_at()
                                .toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalTime()
                                .format(DateTimeFormatter.ofPattern("HH:mm")),
                        s.getDest_region(),
                        s.getFuel_payment(),
                        s.getTotalCost(),
                        s.getState()
                ));
            }
            refresh();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // Data model
    // ══════════════════════════════════════════════════════════════════

    /**
     * Immutable record representing one shipment row.
     *
     * @param time        display time string, e.g. "8:23"
     */
    // Replace the ShipmentEntry record
    public record ShipmentEntry(
            int shipmentId,
            String time,
            int destRegion,
            float fuelPayment,
            double totalCost,
            State state
    ) {}
}
