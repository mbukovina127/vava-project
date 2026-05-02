package org.shippin.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.shippin.controller.utils.CostEstimationInput;
import org.shippin.services.NavigationService;
import org.shippin.controller.utils.ShipmentData;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static org.shippin.dto.Screens.DAILY_COST_SUM;

public class ShipmentDetailController extends BaseController<ShipmentData> implements Initializable {

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

    public record HistoryEntry(String time, String driver, String location, String status) {}

    // Current shipment state — populated from ShipmentData in onData()
    private ShipmentData        shipmentData;
    private CostEstimationInput estimation;
    private String              currentStatus = "COMPLETED";

    // Map coords
    private double fromLat = 48.894;
    private double fromLon = 18.044;
    private double toLat   = 48.974;
    private double toLon   = 19.302;
    private int toLocationPostalCode = 0; //postal code of the destination

    // Placeholder history — replace with real data from backend
    private List<HistoryEntry> history = List.of(
            new HistoryEntry("20:21", "Martin", "Modra",    "COMPLETED"),
            new HistoryEntry("19:00", "Martin", "Sklad BA", "BEING DELIVERED"),
            new HistoryEntry("9:55",  "Emil",   "Sklad BA", "READY"),
            new HistoryEntry("9:21",  "Juraj",  "Sklad BA", "NOT READY")
    );

    // ── BaseController ────────────────────────────────────────────

    @Override
    protected Class<ShipmentData> getDataType() { return ShipmentData.class; }

    @Override
    protected void onData(ShipmentData data) {
        this.shipmentData = data;
        this.estimation   = data.getData();

        populateHeader();
        populateStatusRow();
        populateHistoryGrid();

        loadMapImage();

        if (estimation != null && estimation.destination() != null && !estimation.destination().isEmpty())
        {
            try {
                toLocationPostalCode = Integer.parseInt(estimation.destination().replaceAll("[^0-9]", ""));
                if (toLocationPostalCode > 0) {
                    getCoordinatesFromPostalCode(toLocationPostalCode);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid PSČ: " + estimation.destination());
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {}

    // ── Header ────────────────────────────────────────────────────

    private void populateHeader() {
        titleLabel.setText("Shipment details #" + shipmentData.getShipmentID());

        if (estimation != null) {
            routeLabel.setText(estimation.from() + " - " + estimation.destination());
            distanceLabel.setText("(- km)");
            totalCostLabel.setText("9"); // TODO: computeTotalCost(estimation)
        } else {
            routeLabel.setText("—");
            distanceLabel.setText("");
            totalCostLabel.setText("—");
        }
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

    // ── History grid ──────────────────────────────────────────────

    private void populateHistoryGrid() {
        historyGrid.getChildren().clear();
        for (int i = 0; i < history.size(); i++) {
            HistoryEntry e = history.get(i);
            historyGrid.add(historyCell(e.time(),     "sd-history-time"),   0, i);
            historyGrid.add(historyCell(e.driver(),   "sd-history-cell"),   1, i);
            historyGrid.add(historyCell(e.location(), "sd-history-cell"),   2, i);
            historyGrid.add(historyCell(e.status(),   "sd-history-status"), 3, i);
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
            double centerLat = (fromLat + toLat) / 2.0;
            double centerLon = (fromLon + toLon) / 2.0;

            double distance = calculateDistance(fromLat, fromLon, toLat, toLon);
            int zoom = calculateZoom(distance);

            String apiKey = "AIzaSyAuHM5wJRSqhMhzLQSj_VIpwvamKoaZjrc";
            String url = String.format(
                    "https://maps.googleapis.com/maps/api/staticmap?" +
                            "center=%.4f,%.4f&zoom=%d&size=900x200" +
                            "&markers=color:white|%.4f,%.4f" +
                            "&markers=color:red|%.4f,%.4f" +
                            "&path=color:0xff0000|weight:2|%.4f,%.4f|%.4f,%.4f" +
                            "&key=%s",
                    centerLat, centerLon, zoom,
                    fromLat, fromLon,
                    toLat, toLon,
                    fromLat, fromLon, toLat, toLon,
                    apiKey
            );

            try {
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

    //calculateDistance a calculateZoom aby mapa bola viac zoomnuta ak je kratka vzdialenost, a oddialena ak je daleka, aby sa to zmestilo
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private int calculateZoom(double distance) {
        if (distance < 20) return 11;
        if (distance < 50) return 10;
        if (distance < 100) return 9;
        if (distance < 200) return 8;
        return 7;
    }

    private void showMapFallback() {
        mapImageView.setVisible(false);
        mapFallbackLabel.setVisible(true);
        mapFallbackLabel.setText(NavigationService.getBundle().getString("shipment_detail.map_unavailable"));
    }

    // ── Change status popup ───────────────────────────────────────

    /**
     * Shows "Change status of shipment" modal.
     * Pattern mirrors WarehouseManagementController.showAddWarehousePopup().
     */
    private void showChangeStatusPopup() {
        VBox popup = createPopupRoot();
        popup.setMaxWidth(480);
        popup.setPrefWidth(480);

        Label title = createPopupTitle("Change status of shippment");

        // ── Form grid ─────────────────────────────────────────────
        GridPane formGrid = new GridPane();
        formGrid.setHgap(16);
        formGrid.setVgap(14);

        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setPrefWidth(100);

        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);

        formGrid.getColumnConstraints().addAll(labelCol, fieldCol);

        // Name field
        Label nameLabel = createFormLabel("Name:");
        TextField nameField = createPopupTextField("Value");
        nameField.setText(shipmentData.getEstimationName() != null
                ? shipmentData.getEstimationName() : "");

        // Location field
        Label locationLabel = createFormLabel("Location:");
        TextField locationField = createPopupTextField("Value");
        if (estimation != null) {
            locationField.setText(estimation.destination());
        }

        // State dropdown — values match your existing badge states
        Label stateLabel = createFormLabel("State:");
        ComboBox<String> stateCombo = new ComboBox<>();
        stateCombo.getItems().addAll(
                "NOT READY",
                "READY",
                "BEING DELIVERED",
                "COMPLETED"
        );
        stateCombo.setValue(currentStatus);
        stateCombo.setMaxWidth(Double.MAX_VALUE);
        stateCombo.getStyleClass().add("popup-text-field"); // reuse same border style

        formGrid.add(nameLabel,    0, 0);
        formGrid.add(nameField,    1, 0);
        formGrid.add(locationLabel, 0, 1);
        formGrid.add(locationField, 1, 1);
        formGrid.add(stateLabel,   0, 2);
        formGrid.add(stateCombo,   1, 2);

        // ── Buttons ───────────────────────────────────────────────
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
                populateStatusRow();
                // TODO: persist status change to backend
                System.out.println("Status updated to: " + currentStatus
                        + " | name: " + nameField.getText()
                        + " | location: " + locationField.getText());
            }
            hideModal();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        buttons.getChildren().addAll(cancelButton, spacer, saveButton);

        popup.getChildren().addAll(title, formGrid, buttons);

        showModal(popup);
    }

    // ── Popup helpers (same pattern as WarehouseManagementController) ──

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

    private TextField createPopupTextField(String prompt) {
        TextField textField = new TextField();
        textField.setPromptText(prompt);
        textField.getStyleClass().add("popup-text-field");
        textField.setPrefHeight(38);
        return textField;
    }

    // ── Actions ───────────────────────────────────────────────────

    @FXML
    private void onCostBreakdown() {
        // navigateTo("CostEstimation.fxml", shipmentData);
        System.out.println("Cost breakdown for shipment #" + shipmentData.getShipmentID());
    }

    @FXML
    private void onChangeStatus() {
        showChangeStatusPopup();
    }

    @FXML
    private void onDailySummary() throws IOException {
        loadScreen(DAILY_COST_SUM);
    }

    // ── Setters ───────────────────────────────────────────────────

    public void setFromCoords(double lat, double lon) { fromLat = lat; fromLon = lon; }
    public void setToCoords(double lat, double lon)   { toLat = lat;   toLon = lon; }
    public void setCurrentStatus(String s)            { this.currentStatus = s; }
}