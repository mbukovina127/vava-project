package org.shippin.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import lombok.extern.log4j.Log4j2;
import org.shippin.domain.Shipment;
import org.shippin.domain.ShipmentHistory;
import org.shippin.domain.enums.State;
import org.shippin.dto.Screens;
import org.shippin.services.MapService;
import org.shippin.services.NavigationService;
import org.shippin.services.ShipmentService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static org.shippin.dto.Screens.DAILY_COST_SUM;


@Log4j2
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

    private record HistoryEntry(String time, String status, String actor) {}

    private final ShipmentService shipmentService = new ShipmentService();
    private final MapService      mapService      = new MapService();

    private Shipment shipment;
    private State    currentState  = null;
    private String   currentStatus = "UNKNOWN";

    // Map coords
    private double fromLat = 48.894;
    private double fromLon = 18.044;
    private double toLat   = 48.974;
    private double toLon   = 19.302;

    private List<HistoryEntry> history = List.of();

    // BaseController

    @Override
    protected Class<Shipment> getDataType() { return Shipment.class; }
    @Override
    protected void onData(Shipment data) {
        if (data == null) {
            log.warn("ShipmentDetailController received null data");
            return;
        }
        this.shipment      = data;
        this.currentState  = data.getState();
        this.currentStatus = stateToDisplay(data.getState());

        if (data.getStartCoordinate() != null) {
            fromLat = data.getStartCoordinate().getX();
            fromLon = data.getStartCoordinate().getY();
            log.debug("Using DB coords: {}, {}", fromLat, fromLon);
        } else {
            log.debug("DEBUG: startCoordinate is NULL! Using fallback");
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

    // Header

    private void populateHeader() {
        titleLabel.setText("Shipment number: #" + shipment.getShipment_id());

        String dest = shipment.getDest_region() > 0
                ? String.format("%05d", shipment.getDest_region()) : "—";

        String route_text = "Unknown";
        try {
            String from = shipment.getWarehouse().getName();
            route_text = from + " → " + dest;
        } catch (NullPointerException ex) {
            log.error("Shipment has unknown warehouse");
        }
        routeLabel.setText(route_text);
        double distance = calculateDistance(fromLat, fromLon, toLat, toLon);
        distanceLabel.setText(String.format("(%.2f km)", distance));
        totalCostLabel.setText(String.format("%.2f €", shipment.getTotalCost()));
    }

    // Status row

    private void populateStatusRow() {
        currentStatus = stateToDisplay(currentState);
        statusBadge.setText(currentStatus);
        statusBadge.getStyleClass().removeIf(c -> c.startsWith("sd-badge-"));
        statusBadge.getStyleClass().add(badgeStyleClass(currentState));
    }

    private String badgeStyleClass(State state) {
        if (state == null) return "sd-badge-default";
        return switch (state) {
            case DELIVERED          -> "sd-badge-completed";
            case BEING_DELIVERED    -> "sd-badge-delivering";
            case READY_FOR_DELIVERY -> "sd-badge-ready";
            case NOT_READY          -> "sd-badge-not-ready";
            default                 -> "sd-badge-default";
        };
    }

    private String stateToDisplay(State state) {
        if (state == null) return bundle().getString("shipment_detail.state_unknown");
        return switch (state) {
            case NOT_READY -> bundle().getString("shipment_detail.state_not_ready");
            case READY_FOR_DELIVERY -> bundle().getString("shipment_detail.state_ready_for_delivery");
            case BEING_DELIVERED -> bundle().getString("shipment_detail.state_being_delivered");
            case DELIVERED -> bundle().getString("shipment_detail.state_completed");
            case CANCELED -> bundle().getString("shipment_detail.state_canceled");
            case FAILED -> bundle().getString("shipment_detail.state_failed");
        };
    }

    private State displayToState(String display, List<State> candidates) {
        return candidates.stream()
                .filter(s -> stateToDisplay(s).equalsIgnoreCase(display))
                .findFirst()
                .orElse(null);
    }

    private ResourceBundle bundle() { return NavigationService.getBundle(); }

    // History grid

    private void loadHistoryAsync() {
        new Thread(() -> {
            try {
                List<ShipmentHistory> raw = shipmentService.getShipmentHistory(shipment.getShipment_id());
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
                List<HistoryEntry> entries = raw.stream()
                        .map(h -> new HistoryEntry(
                                h.getTimestamp().toLocalDateTime().format(fmt),
                                stateToDisplay(h.getState()),
                                h.getUserName() != null ? h.getUserName() : "—"
                        ))
                        .toList()
                        .reversed();

                Platform.runLater(() -> {
                    history = entries;
                    populateHistoryGrid();
                });
            } catch (Exception e) {
                log.error("Failed to load shipment history for #{}", shipment.getShipment_id(), e);
            }
        }).start();
    }

    private void populateHistoryGrid() {
        historyGrid.getChildren().clear();
        for (int i = 0; i < history.size(); i++) {
            HistoryEntry e = history.get(i);
            historyGrid.add(historyCell(e.time(),   "sd-history-time"),   0, i);
            historyGrid.add(historyCell(e.status(), "sd-history-status"), 1, i);
            historyGrid.add(historyCell(e.actor(),  "sd-history-actor"),  2, i);
        }
    }

    private Label historyCell(String text, String... styleClasses) {
        Label lbl = new Label(text);
        lbl.getStyleClass().addAll(styleClasses);
        lbl.setAlignment(Pos.CENTER_LEFT);
        lbl.setMinWidth(Region.USE_PREF_SIZE);
        lbl.setMaxWidth(Double.MAX_VALUE);
        return lbl;
    }

    // Map

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

                double distance = calculateDistance(fromLat, fromLon, toLat, toLon);
                Platform.runLater(() -> {
                    distanceLabel.setText(String.format("(%.2f km)", distance));
                    loadMapImage();
                });
            } catch (Exception e) {
                log.error("Failed to fetch coordinates for postal code {}", postalCode, e);
                Platform.runLater(this::loadMapImage);  // ← len ak je error
            }
        }).start();
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private void showChangeStatusPopup() {
        VBox popup = createPopupRoot();
        popup.setMaxWidth(400);
        popup.setPrefWidth(400);
        popup.getStyleClass().add("sd-popup-container");

        Label title = createPopupTitle(bundle().getString("shipment_detail.popup_title"));

        List<State> states = currentState != null ? currentState.allowedTransitions() : List.of();
        Label stateLabel = createFormLabel(bundle().getString("shipment_detail.state"));
        ComboBox<String> stateCombo = new ComboBox<>();
        stateCombo.getItems().addAll(states.stream().map(this::stateToDisplay).toList());
        stateCombo.setMaxWidth(Double.MAX_VALUE);
        stateCombo.getStyleClass().add("popup-text-field");
        if (states.isEmpty()) {
            stateCombo.setPromptText("No transitions available");
            stateCombo.setDisable(true);
        } else {
            stateCombo.getSelectionModel().selectFirst();
        }

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

        Button cancelButton = new Button(bundle().getString("shipment_detail.popup_cancel"));
        cancelButton.getStyleClass().addAll("secondary-button");
        cancelButton.setPrefSize(140, 42);
        cancelButton.setOnAction(e -> hideModal());

        Button saveButton = new Button(bundle().getString("shipment_detail.popup_save"));
        saveButton.getStyleClass().addAll("tertiary-button");
        saveButton.setPrefSize(160, 42);
        saveButton.setOnAction(e -> {
            String picked = stateCombo.getValue();
            if (picked != null) {
                State newState = displayToState(picked, states);
                if (newState != null && newState != shipment.getState()) {
                    try {
                        shipmentService.updateShipmentState(shipment, newState);
                        currentState = newState;
                    } catch (SQLException ex) {
                        log.error("Failed to update shipment {}", shipment.getShipment_id(), ex);
                        new Alert(Alert.AlertType.ERROR, "Failed to update shipment!\n - " + ex.getMessage()).showAndWait();
                    }
                }
                populateStatusRow();
                loadHistoryAsync();
            }
            hideModal();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        buttons.getChildren().addAll(cancelButton, spacer, saveButton);

        popup.getChildren().addAll(title, formGrid, buttons);
        showModal(popup);
    }

    //  Popup helpers

    private VBox createPopupRoot() {
        VBox root = new VBox(28);
        root.setAlignment(Pos.TOP_LEFT);
        root.setPadding(new Insets(24));
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

    // Actions

    @FXML
    private void onCostBreakdown() {
        log.info("Cost breakdown requested for shipment #{}", shipment.getShipment_id());
        try {
            loadScreen(Screens.COST_BREAKDOWN, shipment);
        } catch (IOException e) {
            log.error("When trying to change screens with data {}", shipment, e);
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void onChangeStatus() {
        showChangeStatusPopup();
    }

    @FXML
    private void onDailySummary() throws java.io.IOException {
        try {
            LocalDate date = shipment.getCreated_at().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            System.out.println(date);
            loadScreen(DAILY_COST_SUM, date);
        } catch (Exception ex) {
            log.error("Exception probably caused by shipment={} createdat={}", shipment.getShipment_id(), shipment.getCreated_at(), ex);
        }
    }

}
