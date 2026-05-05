package org.shippin.controller;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.extern.log4j.Log4j2;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.shippin.domain.Shipment;
import org.shippin.domain.enums.State;
import org.shippin.dto.Screens;
import org.shippin.controller.utils.GenericPopup;
import org.shippin.services.NavigationService;
import org.shippin.services.ShipmentService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


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
@Log4j2
public class DailyCostsSummaryDetailController
        extends BaseController<LocalDate>
        implements Initializable {

    // FXML injections
    @FXML private Label               lblTitle;
    @FXML private VBox                shipmentList;
    @FXML private Label               lblTotal;
    @FXML private TextField           searchField;
    @FXML private ComboBox<String>    sortCombo;

    private Image editIcon;
    private Image deleteIcon;

    // State
    private LocalDate            currentDate = java.time.LocalDate.now();
    private List<Shipment>       rawShipments = new ArrayList<>();
    private List<ShipmentEntry>  entries = new ArrayList<>();

    private enum SortType { TIME, COST, STATE }

    // Date formatter shown in the title (e.g. "1.4.2026")
    private static final DateTimeFormatter TITLE_FMT =
            DateTimeFormatter.ofPattern("d.M.yyyy");


    // Initializable
    private ShipmentService shipmentService;

    @Override
    protected Class<LocalDate> getDataType() {
        return LocalDate.class;
    }

    @Override
    protected void onData(LocalDate date) {
        if(date != null){
            this.currentDate = date;
        }

        loadFromService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        shipmentService = new ShipmentService();

        editIcon = loadImage("/icons/png-dark/rewrite_black.png");
        deleteIcon = loadImage("/icons/png-dark/delete_black.png");

        ResourceBundle bundle = NavigationService.getBundle();
        sortCombo.getItems().addAll(
                bundle.getString("my_shipments.sort_time"),
                bundle.getString("my_shipments.sort_cost"),
                bundle.getString("my_shipments.sort_state")
        );
        sortCombo.getSelectionModel().selectFirst();

        searchField.textProperty().addListener((obs, old, val) -> applyFilterAndSort());
        sortCombo.valueProperty().addListener((obs, old, val) -> applyFilterAndSort());

        if (currentDate != null) {
            lblTitle.setText(currentDate.format(TITLE_FMT) + " – " + bundle.getString("daily_cost.header"));
        }
    }

    // Public API — called by the parent controller


    // Private helpers

    private void applyFilterAndSort() {
        String query = searchField.getText().trim();
        int selectedIndex = sortCombo.getSelectionModel().getSelectedIndex();
        SortType sort = SortType.values()[Math.max(0, selectedIndex)];

        List<ShipmentEntry> filtered = entries.stream()
                .filter(e -> query.isEmpty() || String.valueOf(e.shipmentId()).startsWith(query))
                .collect(Collectors.toCollection(ArrayList::new));

        Comparator<ShipmentEntry> comparator = switch (sort) {
            case TIME  -> Comparator.comparing(ShipmentEntry::time);
            case COST  -> Comparator.comparingDouble(ShipmentEntry::totalCost).reversed();
            case STATE -> Comparator.comparingInt(e -> e.state().ordinal());
        };
        filtered.sort(comparator);

        renderRows(filtered);
    }

    private void renderRows(List<ShipmentEntry> rows) {


        shipmentList.getChildren().clear();
        for (ShipmentEntry entry : rows) {
            shipmentList.getChildren().add(buildRow(entry));
        }

        double total = rows.stream().mapToDouble(ShipmentEntry::totalCost).sum();
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
        row.setCursor(javafx.scene.Cursor.HAND);
        row.setOnMouseClicked(e -> handleEdit(entry));

        // Left: ID + time
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

        // Action buttons
        HBox btnBox = new HBox(6);
        btnBox.setAlignment(Pos.CENTER);

        Button btnEdit = new Button();
        ImageView editView = new ImageView(editIcon);
        editView.setFitWidth(18);
        editView.setFitHeight(18);
        editView.setPreserveRatio(true);
        btnEdit.setGraphic(editView);
        btnEdit.getStyleClass().add("dcd-icon-btn");
        btnEdit.setOnAction(e -> handleEdit(entry));

        Button btnDelete = new Button();
        ImageView deleteView = new ImageView(deleteIcon);
        deleteView.setFitWidth(18);
        deleteView.setFitHeight(18);
        deleteView.setPreserveRatio(true);
        btnDelete.setGraphic(deleteView);
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

    @FXML
    private void handleShowOnMap() throws IOException {
        List<Shipment> active = rawShipments == null ? List.of() : rawShipments.stream()
                .filter(s -> s.getState() != State.FAILED && s.getState() != State.DELIVERED)
                .toList();
        if (active.isEmpty()) {
            new GenericPopup(NavigationService.getBundle())
                    .showOkPopup(this, "%map_of_shipments.no_active.title", "%map_of_shipments.no_active.message");
            return;
        }
        loadScreen(Screens.MAP_OF_SHIPMENTS, active);
    }

    private void handleEdit(ShipmentEntry entry) {
        try {
            Shipment shipment = shipmentService.getShipmentById(entry.shipmentId());
            loadScreen(Screens.SHIPMENT_DETAIL, shipment);
        } catch (SQLException | java.io.IOException e) {
            log.error("Daily summary operation failed", e);
        }
    }

    /** Delete action for a shipment row. */
    private void handleDelete(ShipmentEntry entry) {
        // TODO: show confirmation dialog, then remove from DB and refresh.
        entries.remove(entry);
        applyFilterAndSort();
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
            rawShipments = shipments;
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
            applyFilterAndSort();
        } catch (SQLException e) {
            log.error("Daily summary operation failed", e);
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
