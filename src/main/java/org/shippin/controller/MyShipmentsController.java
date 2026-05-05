package org.shippin.controller;

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
import javafx.scene.layout.VBox;
import org.shippin.domain.Shipment;
import org.shippin.domain.User;
import org.shippin.domain.enums.State;
import org.shippin.dto.Screens;
import org.shippin.controller.utils.GenericPopup;
import org.shippin.services.NavigationService;
import org.shippin.services.ShipmentService;
import org.shippin.services.UserService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Log4j2
public class MyShipmentsController extends BaseController<User> implements Initializable {
    private record ShipmentEntry(
            int    shipmentId,
            String time,
            int    destRegion,
            float  fuelPayment,
            double totalCost,
            State  state
    ) {}

    @FXML private Label       lblTitle;
    @FXML private VBox        shipmentList;
    @FXML private Label       lblTotal;
    @FXML private TextField   searchField;
    @FXML private ComboBox<String> sortCombo;
    @FXML private Button      btnAddShipment;
    @FXML private Button      btnShowOnMap;


    private User viewedUser;
    private List<Shipment> rawShipments = new ArrayList<>();
    private List<ShipmentEntry> entries = new ArrayList<>();

    private enum SortType { TIME, COST, STATE }

    private final ShipmentService shipmentService = new ShipmentService();

    @Override
    protected Class<User> getDataType() {
        return User.class;
    }

    @Override
    protected void onData(User user) {
        viewedUser = (user != null) ? user : UserService.getUser();
        loadFromService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ResourceBundle bundle = NavigationService.getBundle();

        sortCombo.getItems().addAll(
                bundle.getString("my_shipments.sort_time"),
                bundle.getString("my_shipments.sort_cost"),
                bundle.getString("my_shipments.sort_state")
        );
        sortCombo.getSelectionModel().selectFirst();

        searchField.textProperty().addListener((obs, old, val) -> applyFilterAndSort());
        sortCombo.valueProperty().addListener((obs, old, val) -> applyFilterAndSort());
    }

    @FXML
    private void handleAddShipment() throws IOException {
        log.info("Add shipment for user " + viewedUser.getId());
        loadScreen(Screens.COST_ESTIMATION);
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

    private void loadFromService() {
        updateTitle();
        try {
            List<Shipment> shipments = shipmentService.getShipmentsByUser(viewedUser.getId());
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
            log.error("Failed to load shipments for user #{}", viewedUser.getId(), e);
        }
    }

    private void updateTitle() {
        String titleKey = NavigationService.getBundle().getString("my_shipments.title");
        lblTitle.setText(viewedUser.getFullUserName() + " – " + titleKey);
    }

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

    private HBox buildRow(ShipmentEntry entry) {
        HBox row = new HBox(16);
        row.getStyleClass().add("dcd-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setCursor(javafx.scene.Cursor.HAND);
        row.setOnMouseClicked(e -> handleEdit(entry));

        VBox leftBox = new VBox(2);
        Label lblId   = new Label("#" + entry.shipmentId());
        lblId.getStyleClass().add("dcd-row-id");
        Label lblTime = new Label(entry.time());
        lblTime.getStyleClass().add("dcd-row-time");
        leftBox.getChildren().addAll(lblId, lblTime);

        VBox centerBox = new VBox(2);
        HBox.setHgrow(centerBox, Priority.ALWAYS);
        Label lblDest = new Label("Dest. region: " + entry.destRegion());
        lblDest.getStyleClass().add("dcd-row-desc");
        Label lblFuel = new Label("Fuel surcharge: " + formatCost(entry.fuelPayment()));
        lblFuel.getStyleClass().add("dcd-row-sub");
        centerBox.getChildren().addAll(lblDest, lblFuel);

        VBox rightBox = new VBox(2);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        Label lblState = new Label(entry.state().toString());
        lblState.getStyleClass().addAll("dcd-row-state", stateStyleClass(entry.state()));
        Label lblAmount = new Label(formatCost(entry.totalCost()));
        lblAmount.getStyleClass().add("dcd-row-amount");
        rightBox.getChildren().addAll(lblState, lblAmount);

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
            case NOT_READY       -> "dcd-state-not-ready";
            case READY_FOR_DELIVERY -> "dcd-state-ready";
            case BEING_DELIVERED -> "dcd-state-transit";
            case DELIVERED       -> "dcd-state-delivered";
            case CANCELED        -> "dcd-state-cancelled";
            case FAILED          -> "dcd-state-failed";
            default              -> "";
        };
    }

    private void handleEdit(ShipmentEntry entry) {
        try {
            Shipment shipment = shipmentService.getDao().getShipmentById(entry.shipmentId());
            loadScreen(Screens.SHIPMENT_DETAIL, shipment);
        } catch (SQLException | java.io.IOException e) {
            log.error("Failed to open shipment #{}", entry.shipmentId(), e);
        }
    }

    private void handleDelete(ShipmentEntry entry) {
        entries.remove(entry);
        try {
            shipmentService.deleteShipmentByID(entry.shipmentId);
        } catch (SQLException e) {
            log.error("Failed to delete shipment #{}", entry.shipmentId(), e);
        }
        applyFilterAndSort();
    }

    private static String formatCost(double cost) {
        if (cost == Math.floor(cost)) {
            return String.format(Locale.ROOT, "%.0f€", cost);
        }
        return String.format(Locale.ROOT, "%.2f€", cost).replace('.', ',');
    }
    private static String formatCost(float cost) { return formatCost((double) cost); }


}
