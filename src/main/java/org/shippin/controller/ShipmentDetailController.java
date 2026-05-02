package org.shippin.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.shippin.domain.Shipment;
import org.shippin.domain.ShipmentHistory;
import org.shippin.domain.enums.State;
import org.shippin.services.MapService;
import org.shippin.services.NavigationService;
import org.shippin.services.ShipmentService;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import static org.shippin.dto.Screens.DAILY_COST_SUM;

public class ShipmentDetailController extends BaseController<Shipment> implements Initializable {

    @FXML private Label      titleLabel;
    @FXML private Label      routeLabel;
    @FXML private Label      distanceLabel;
    @FXML private Label      totalCostLabel;
    @FXML private Button     costBreakdownBtn;
    @FXML private Label      statusBadge;
    @FXML private Button     changeStatusBtn;
    @FXML private GridPane   historyGrid;
    @FXML private StackPane  mapContainer;
    @FXML private ImageView  mapImageView;
    @FXML private Label      mapFallbackLabel;
    @FXML private Button     dailySummaryBtn;

    private record HistoryEntry(String time, String status) {}

    private final ShipmentService shipmentService = new ShipmentService();
    private final MapService      mapService      = new MapService();

    private Shipment shipment;
    private String   currentStatus = "UNKNOWN";

    // Map coords
    private double fromLat = 48.894;
    private double fromLon = 18.044;
    private double toLat   = 48.974;
    private double toLon   = 19.302;

    private List<HistoryEntry> history = List.of();

    // ── BaseController ────────────────────────────────────────────

    @Override
    protected Class<Shipment> getDataType() { return Shipment.class; }

    @Override
    protected void onData(Shipment data) {
        this.shipment      = data;
        this.currentStatus = stateToDisplay(data.getState());

        if (data.getStartCoordinate() != null) {
            fromLat = data.getStartCoordinate().getX();
            fromLon = data.getStartCoordinate().getY();
        }

        populateHeader();
        populateStatusRow();
        loadHistoryAsync();
        loadMapImage();

        if (data.getDest_region() > 0) {
            getCoordinatesFromPostalCode(data.getDest_region());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {}

    // ── Header ────────────────────────────────────────────────────

    private void populateHeader() {
        titleLabel.setText("Shipment details #" + shipment.getShipment_id());

        String dest = shipment.getDest_region() > 0
                ? String.format("%05d", shipment.getDest_region()) : "—";
        String from = shipment.getStartCoordinate() != null
                ? shipment.getStartCoordinate().getX() + ", " + shipment.getStartCoordinate().getY()
                : "—";
        routeLabel.setText(from + " → " + dest);
        distanceLabel.setText("(- km)");
        totalCostLabel.setText(String.format("%.2f €", shipment.getTotalCost()));
    }

    // ── Status row ────────────────────────────────────────────────

    private void populateStatusRow() {
        statusBadge.setText(currentStatus);
        statusBadge.getStyleClass().removeIf(c -> c.startsWith("sd-badge-"));
        statusBadge.getStyleClass().add(badgeStyleClass(currentStatus));
    }

    private String badgeStyleClass(String status) {
        return switch (status.toUpperCase()) {
            case "COMPLETED"       -> "sd-badge-completed";
            case "BEING DELIVERED" -> "sd-badge-delivering";
            case "READY"           -> "sd-badge-ready";
            case "NOT READY"       -> "sd-badge-not-ready";
            default                -> "sd-badge-default";
        };
    }

    private String stateToDisplay(State state) {
        if (state == null) return "UNKNOWN";
        return switch (state) {
            case NOT_READY          -> "NOT READY";
            case READY_FOR_DELIVERY -> "READY";
            case BEING_DELIVERED    -> "BEING DELIVERED";
            case DELIVERED          -> "COMPLETED";
            case CANCELED           -> "CANCELED";
            case FAILED             -> "FAILED";
        };
    }

    private State displayToState(String display) {
        return switch (display.toUpperCase()) {
            case "NOT READY"       -> State.NOT_READY;
            case "READY"           -> State.READY_FOR_DELIVERY;
            case "BEING DELIVERED" -> State.BEING_DELIVERED;
            case "COMPLETED"       -> State.DELIVERED;
            case "CANCELED"        -> State.CANCELED;
            case "FAILED"          -> State.FAILED;
            default                -> null;
        };
    }

    // ── History grid ──────────────────────────────────────────────

    private void loadHistoryAsync() {
        new Thread(() -> {
            try {
                List<ShipmentHistory> raw = shipmentService.getShipmentHistory(shipment.getShipment_id());
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
                List<HistoryEntry> entries = raw.stream()
                        .map(h -> new HistoryEntry(h.getTimestamp().toLocalDateTime().format(fmt), stateToDisplay(h.getState())
                        ))
                        .toList();

                Platform.runLater(() -> {
                    history = entries;
                    populateHistoryGrid();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void populateHistoryGrid() {
        historyGrid.getChildren().clear();
        for (int i = 0; i < history.size(); i++) {
            HistoryEntry e = history.get(i);
            historyGrid.add(historyCell(e.time(),   "sd-history-time"),   0, i);
            historyGrid.add(historyCell(e.status(), "sd-history-status"), 1, i);
        }
    }

    private Label historyCell(String text, String... styleClasses) {
        Label lbl = new Label(text);
        lbl.getStyleClass().addAll(styleClasses);
        lbl.setAlignment(Pos.CENTER_LEFT);
        lbl.setMaxWidth(Double.MAX_VALUE);
        return lbl;
    }

    // ── Map ───────────────────────────────────────────────────────

    private void loadMapImage() {
        try {
            String url = mapService.buildStaticMapUrl(fromLat, fromLon, toLat, toLon);
            Image mapImage = new Image(url, true);
            mapImage.errorProperty().addListener((obs, old, isError) -> {
                if (isError) showMapFallback();
            });
            mapImageView.setImage(mapImage);
            mapImageView.setVisible(true);
            mapFallbackLabel.setVisible(false);
        } catch (Exception ex) {
            showMapFallback();
        }
    }

    private void showMapFallback() {
        mapImageView.setVisible(false);
        mapFallbackLabel.setVisible(true);
        mapFallbackLabel.setText(NavigationService.getBundle().getString("shipment_detail.map_unavailable"));
    }

    private void getCoordinatesFromPostalCode(int postalCode) {
        new Thread(() -> {
            try {
                double[] coords = mapService.fetchCoordinatesForPostalCode(postalCode);
                toLat = coords[0];
                toLon = coords[1];
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Platform.runLater(this::loadMapImage);
            }
        }).start();
    }

    // ── Change status popup ───────────────────────────────────────

    private void showChangeStatusPopup() {
        VBox popup = createPopupRoot();
        popup.setMaxWidth(400);
        popup.setPrefWidth(400);

        Label title = createPopupTitle("Change status of shipment");

        Label stateLabel = createFormLabel("State:");
        ComboBox<String> stateCombo = new ComboBox<>();
        stateCombo.getItems().addAll(
                "NOT READY",
                "READY",
                "BEING DELIVERED",
                "COMPLETED",
                "CANCELED",
                "FAILED"
        );
        stateCombo.setValue(currentStatus);
        stateCombo.setMaxWidth(Double.MAX_VALUE);
        stateCombo.getStyleClass().add("popup-text-field");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(16);
        formGrid.setVgap(14);
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setPrefWidth(100);
        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        formGrid.getColumnConstraints().addAll(labelCol, fieldCol);
        formGrid.add(stateLabel, 0, 0);
        formGrid.add(stateCombo, 1, 0);

        HBox buttons = new HBox(18);
        buttons.setAlignment(Pos.CENTER_LEFT);

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().addAll("popup-button", "popup-secondary-button");
        cancelButton.setPrefSize(140, 42);
        cancelButton.setOnAction(e -> hideModal());

        Button saveButton = new Button("Update status");
        saveButton.getStyleClass().addAll("popup-button", "popup-primary-button");
        saveButton.setPrefSize(160, 42);
        saveButton.setOnAction(e -> {
            String picked = stateCombo.getValue();
            if (picked != null) {
                currentStatus = picked;
                State newState = displayToState(picked);
                if (newState != null) {
                    shipment.setState(newState);
                }
                populateStatusRow();
                // TODO: persist status change via ShipmentDAO
            }
            hideModal();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        buttons.getChildren().addAll(cancelButton, spacer, saveButton);

        popup.getChildren().addAll(title, formGrid, buttons);
        showModal(popup);
    }

    // ── Popup helpers ─────────────────────────────────────────────

    private VBox createPopupRoot() {
        VBox root = new VBox(28);
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

    // ── Actions ───────────────────────────────────────────────────

    @FXML
    private void onCostBreakdown() {
        System.out.println("Cost breakdown for shipment #" + shipment.getShipment_id());
    }

    @FXML
    private void onChangeStatus() {
        showChangeStatusPopup();
    }

    @FXML
    private void onDailySummary() throws java.io.IOException {
        loadScreen(DAILY_COST_SUM);
    }

}
