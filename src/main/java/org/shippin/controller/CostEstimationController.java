package org.shippin.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.shippin.controller.utils.ErrorHandler;
import org.shippin.database.dao.ShipmentDAO;
import org.shippin.database.dao.WarehouseDAO;
import org.shippin.domain.AdditionalService;
import org.shippin.domain.BriefWarehouse;
import org.shippin.domain.Shipment;
import org.shippin.domain.enums.ServiceType;
import org.shippin.services.ShipmentService;
import org.shippin.database.dao.ShipmentDAO;
import org.shippin.database.dao.WarehouseDAO;
import org.shippin.domain.BriefWarehouse;
import org.shippin.domain.Shipment;
import org.shippin.services.ShipmentService;

import org.shippin.app.FromCoordsDataGetter;
import org.shippin.controller.MapPickerController;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.SQLException;
import java.util.Date;
import java.util.*;

import static java.lang.Double.parseDouble;
import static org.shippin.dto.Screens.COST_BREAKDOWN;

public class CostEstimationController extends BaseController<Void> implements Initializable {

    // Title + Date
    @FXML private Label     sectionTitleLabel;
    @FXML private TextField dateField;

    private List<BriefWarehouse> warehouseList;

    // Postal codes
    @FXML private ComboBox<String> fromCombo;
    @FXML private TextField destinationField;
    @FXML private HBox toBox;

    // Size row
    @FXML private TextField weightField;
    @FXML private TextField volumeField;

    // Coefficients row
    @FXML private TextField fuelSurchargeField;
    @FXML private TextField tollField;

    // Dynamic service containers
    @FXML private VBox productsContainer;
    @FXML private VBox servicesContainer;
    @FXML private VBox paymentsContainer;

    // ERRORS (labels)
    @FXML private Label  statusLabelDestination;
    @FXML private Label  statusLabelWeight;
    @FXML private Label  statusLabelVolume;
    @FXML private Label  statusLabelDate;
    @FXML private Label statusLabelFuel;
    @FXML private Label statusLabelToll;

    // Buttons
    @FXML private Button resetButton;
    @FXML private Button computeButton;

    private final ToggleGroup productsToggleGroup = new ToggleGroup();
    private final Map<CheckBox, AdditionalService> serviceCheckBoxes = new LinkedHashMap<>();
    private final Map<RadioButton, AdditionalService> productRadioButtons = new LinkedHashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        try {
            WarehouseDAO warehouseDAO = WarehouseDAO.getInstance();
            warehouseList = warehouseDAO.getAllBriefWarehouses();
            for (BriefWarehouse bw : warehouseList) {
                fromCombo.getItems().add(bw.getName());
            }
            fromCombo.setValue(fromCombo.getItems().getFirst());

            ShipmentDAO shipmentDAO = ShipmentDAO.getInstance();
            List<AdditionalService> allServices = shipmentDAO.getSAllServices();
            buildServiceUI(allServices);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        destinationField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                toBox.getStyleClass().add("ce-to-box-focused");
            } else {
                toBox.getStyleClass().remove("ce-to-box-focused");
            }
        });
    }

    private void buildServiceUI(List<AdditionalService> allServices)
    {
        productsContainer.getChildren().add(createGroupLabel("Produkty pre ZBS:"));
        servicesContainer.getChildren().add(createGroupLabel("Obstarávané služby:"));
        paymentsContainer.getChildren().add(createGroupLabel("Príplatky:"));

        for (AdditionalService service : allServices)
        {
            switch (service.getServiceType())
            {
                case SERVICES ->
                {
                    RadioButton rb = new RadioButton(service.getName());
                    rb.setToggleGroup(productsToggleGroup);
                    rb.setMnemonicParsing(false);

                    HBox row = new HBox(8);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.getStyleClass().add("ce-product-row");
                    row.getChildren().add(rb);

                    if (service.getDescription() != null && !service.getDescription().isBlank())
                    {
                        Label desc = new Label(service.getDescription());
                        desc.getStyleClass().add("ce-product-desc");
                        desc.setWrapText(true);
                        row.getChildren().add(desc);
                    }

                    productsContainer.getChildren().add(row);
                    productRadioButtons.put(rb, service);
                }
                case ADDITIONAL_PAYMENTS ->
                {
                    CheckBox cb = new CheckBox(service.getName());
                    cb.getStyleClass().add("ce-check");
                    if (service.getDescription() != null && !service.getDescription().isBlank())
                    {
                        cb.setTooltip(new Tooltip(service.getDescription()));
                    }
                    servicesContainer.getChildren().add(cb);
                    serviceCheckBoxes.put(cb, service);
                }
                case PRODUCTS ->
                {
                    CheckBox cb = new CheckBox(service.getName());
                    cb.getStyleClass().add("ce-check");
                    if (service.getDescription() != null && !service.getDescription().isBlank())
                    {
                        cb.setTooltip(new Tooltip(service.getDescription()));
                    }
                    paymentsContainer.getChildren().add(cb);
                    serviceCheckBoxes.put(cb, service);
                }
            }
        }

        productsToggleGroup.selectToggle(productsToggleGroup.getToggles().getFirst());
    }

    private Label createGroupLabel(String text)
    {
        Label label = new Label(text);
        label.getStyleClass().add("ce-fees-group-label");
        return label;
    }

    private List<Integer> getSelectedServiceIds()
    {
        List<Integer> ids = new ArrayList<>();

        Toggle selectedProduct = productsToggleGroup.getSelectedToggle();
        if (selectedProduct instanceof RadioButton rb && productRadioButtons.containsKey(rb))
        {
            ids.add(productRadioButtons.get(rb).getId());
        }

        for (Map.Entry<CheckBox, AdditionalService> entry : serviceCheckBoxes.entrySet())
        {
            if (entry.getKey().isSelected())
            {
                ids.add(entry.getValue().getId());
            }
        }

        return ids;
    }


    // GETTERS

    public String getFrom() {
        return fromCombo.getValue();
    }

    public String getDestination() {
        return destinationField.getText();
    }

    public double getWeight() {
        return parseDouble(weightField.getText());
    }

    public double getVolume() {
        return parseDouble(volumeField.getText());
    }

    public double getFuelSurcharge() {
        return parseDouble(fuelSurchargeField.getText());
    }

    public double getToll() {
        return parseDouble(tollField.getText());
    }

    // Action handlers

    @FXML
    private void onReset()
    {
        fromCombo.getSelectionModel().selectFirst();
        destinationField.clear();
        weightField.clear();
        volumeField.clear();
        fuelSurchargeField.clear();
        tollField.clear();

        productsToggleGroup.selectToggle(productsToggleGroup.getToggles().getFirst());
        for (CheckBox cb : serviceCheckBoxes.keySet())
        {
            cb.setSelected(false);
        }

        statusLabelDestination.setText("");
        statusLabelWeight.setText("");
        statusLabelVolume.setText("");
        statusLabelFuel.setText("");
        statusLabelToll.setText("");
    }
    @FXML
    private void onComputeCost() throws IOException
    {
        String destination = destinationField.getText().trim();

        String weightText = weightField.getText().trim();
        String volumeText = volumeField.getText().trim();
        String fuelSurchargeText = fuelSurchargeField.getText().trim();
        String tollText = tollField.getText().trim();

        String destinationError = ErrorHandler.validatePostalCode(destination);
        String weightError = ErrorHandler.validatePositiveDouble(weightText);
        String volumeError = ErrorHandler.validatePositiveDouble(volumeText);

        String fuelSurchargeError = ErrorHandler.validatePercent(fuelSurchargeText);
        String tollError = ErrorHandler.validatePercent(tollText);

        // SYNTACTICAL INPUT CHECKING

        if
        (
                !destinationError.isEmpty()
                || !weightError.isEmpty()
                || !volumeError.isEmpty()
                || !fuelSurchargeError.isEmpty()
                || !tollError.isEmpty()

        )
        {
            statusLabelDestination.setText(destinationError);
            statusLabelVolume.setText(volumeError);
            statusLabelWeight.setText(weightError);
            statusLabelToll.setText(tollError);
            statusLabelFuel.setText(fuelSurchargeError);
            return;
        }

        int warehouseId = warehouseList.stream()
                .filter(w -> w.getName().equals(getFrom()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Warehouse not found: " + getFrom()))
                .getId();

        List<Integer> serviceIds = getSelectedServiceIds();

        int destPostalCode;
        try {
            destPostalCode = Integer.parseInt(destination.replaceAll("\\s", ""));
        } catch (NumberFormatException e) {
            statusLabelDestination.setText("Destination must be a valid postal code");
            return;
        }

        Shipment computedShipment;
        try {
            ShipmentService service = new ShipmentService();
            computedShipment = service.createShipment(
                    null,
                    new Date(),
                    destPostalCode,
                    (float) getFuelSurcharge(),
                    (float) getToll(),
                    (float) getWeight(),
                    (float) getVolume(),
                    warehouseId,
                    serviceIds
            );
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Could not compute shipping cost: " + e.getMessage()).showAndWait();
            return;
        } catch (IllegalArgumentException e) {
            new Alert(Alert.AlertType.ERROR, "Invalid input: " + e.getMessage()).showAndWait();
            return;
        }

        loadScreen(COST_BREAKDOWN, computedShipment);
    }

    @FXML
    public void onTestMap() {
        FromCoordsDataGetter.setCallback(postalCode -> {
            destinationField.setText(postalCode);
        });
        MapPickerController.open();
    }
}
