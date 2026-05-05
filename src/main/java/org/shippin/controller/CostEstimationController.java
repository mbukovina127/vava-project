package org.shippin.controller;

import lombok.extern.log4j.Log4j2;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.shippin.controller.utils.ErrorHandler;
import org.shippin.controller.utils.GenericPopup;
import org.shippin.controller.utils.RegionsPopup;
import org.shippin.domain.AdditionalService;
import org.shippin.domain.BriefWarehouse;
import org.shippin.domain.Shipment;
import org.shippin.services.ShipmentService;

import org.shippin.services.NavigationService;
import org.shippin.services.WarehouseService;
import org.shippin.util.FromCoordsDataGetter;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

import static java.lang.Double.parseDouble;
import static org.shippin.dto.Screens.COST_BREAKDOWN;

@Log4j2
public class CostEstimationController extends BaseController<Void> implements Initializable, StatePreservable {

    private record FormState(
        String from, String destination,
        String weight, String volume, String fuel, String toll,
        Set<Integer> checkedServiceIds, Integer selectedProductId
    ) {}

    @Override
    public Object captureState() {
        Set<Integer> checkedIds = new LinkedHashSet<>();
        for (Map.Entry<CheckBox, AdditionalService> e : serviceCheckBoxes.entrySet()) {
            if (e.getKey().isSelected()) checkedIds.add(e.getValue().getId());
        }
        Integer selectedProductId = null;
        Toggle t = productsToggleGroup.getSelectedToggle();
        if (t instanceof RadioButton rb && productRadioButtons.containsKey(rb)) {
            selectedProductId = productRadioButtons.get(rb).getId();
        }
        return new FormState(
            fromCombo.getValue(), destinationField.getText(),
            weightField.getText(), volumeField.getText(),
            fuelSurchargeField.getText(), tollField.getText(),
            checkedIds, selectedProductId
        );
    }

    @Override
    public void restoreState(Object rawState) {
        if (!(rawState instanceof FormState s)) return;
        if (s.from() != null) fromCombo.setValue(s.from());
        destinationField.setText(s.destination());
        weightField.setText(s.weight());
        volumeField.setText(s.volume());
        fuelSurchargeField.setText(s.fuel());
        tollField.setText(s.toll());
        for (Map.Entry<CheckBox, AdditionalService> e : serviceCheckBoxes.entrySet()) {
            e.getKey().setSelected(s.checkedServiceIds().contains(e.getValue().getId()));
        }
        if (s.selectedProductId() != null) {
            for (Map.Entry<RadioButton, AdditionalService> e : productRadioButtons.entrySet()) {
                if (e.getValue().getId() == s.selectedProductId()) {
                    productsToggleGroup.selectToggle(e.getKey());
                    break;
                }
            }
        }
    }

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
    @FXML private Button viewRegionsButton;

    private final ToggleGroup productsToggleGroup = new ToggleGroup();
    private final Map<CheckBox, AdditionalService> serviceCheckBoxes = new LinkedHashMap<>();
    private final Map<RadioButton, AdditionalService> productRadioButtons = new LinkedHashMap<>();
    private final ToggleGroup paymentsToggleGroup = new ToggleGroup();
    private final Map<RadioButton, AdditionalService> paymentRadioButtons = new LinkedHashMap<>();
    private ResourceBundle resources;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
    	this.resources = resources;
        try {
            WarehouseService wService = new WarehouseService();
            warehouseList = wService.getBriefWarehouses();
            for (BriefWarehouse bw : warehouseList) {
                fromCombo.getItems().add(bw.getName());
            }
            fromCombo.setValue(fromCombo.getItems().getFirst());

            ShipmentService sService = new ShipmentService();
            List<AdditionalService> allServices = sService.getAllServices();
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
        ResourceBundle bundle = NavigationService.getBundle();
        boolean isEnglish = bundle.getLocale().getLanguage().equals("en");

        productsContainer.getChildren().add(createGroupLabel(bundle.getString("cost_estimation.zbs_products")));
        servicesContainer.getChildren().add(createGroupLabel(bundle.getString("cost_estimation.procured_services")));
        paymentsContainer.getChildren().add(createGroupLabel(bundle.getString("cost_estimation.surcharges")));

        for (AdditionalService service : allServices)
        {
            String serviceName = isEnglish && service.getName_en() != null && !service.getName_en().isBlank()
                    ? service.getName_en() : service.getName();
            String serviceDesc = isEnglish && service.getDescription_en() != null && !service.getDescription_en().isBlank()
                    ? service.getDescription_en() : service.getDescription();

            switch (service.getServiceType())
            {
                case SERVICES ->
                {
                    RadioButton rb = new RadioButton(serviceName);
                    rb.setToggleGroup(productsToggleGroup);
                    rb.setMnemonicParsing(false);

                    HBox row = new HBox(8);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.getStyleClass().add("ce-product-row");
                    row.getChildren().add(rb);

                    if (serviceDesc != null && !serviceDesc.isBlank())
                    {
                        Label desc = new Label(serviceDesc);
                        desc.getStyleClass().add("ce-product-desc");
                        desc.setWrapText(true);
                        row.getChildren().add(desc);
                    }

                    productsContainer.getChildren().add(row);
                    productRadioButtons.put(rb, service);
                }
                case ADDITIONAL_PAYMENTS ->
                {
                    CheckBox cb = new CheckBox(serviceName);
                    cb.getStyleClass().add("ce-check");
                    if (serviceDesc != null && !serviceDesc.isBlank())
                    {
                        cb.setTooltip(new Tooltip(serviceDesc));
                    }
                    servicesContainer.getChildren().add(cb);
                    serviceCheckBoxes.put(cb, service);
                }
                case PRODUCTS ->
                {
                    RadioButton rb = new RadioButton(serviceName);
                    rb.setToggleGroup(paymentsToggleGroup);
                    rb.setMnemonicParsing(false);
                    rb.getStyleClass().add("ce-radio");

                    HBox row = new HBox(8);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.getStyleClass().add("ce-product-row");
                    row.getChildren().add(rb);

                    if (serviceDesc != null && !serviceDesc.isBlank())
                    {
                        Label desc = new Label(serviceDesc);
                        desc.getStyleClass().add("ce-product-desc");
                        desc.setWrapText(true);
                        row.getChildren().add(desc);
                    }

                    paymentsContainer.getChildren().add(row);
                    paymentRadioButtons.put(rb, service);
                }
            }
        }

        if (!productsToggleGroup.getToggles().isEmpty())
        {
            productsToggleGroup.selectToggle(productsToggleGroup.getToggles().getFirst());
        }

        if (!paymentsToggleGroup.getToggles().isEmpty())
        {
            paymentsToggleGroup.selectToggle(null);
        }
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

        Toggle selectedPayment = paymentsToggleGroup.getSelectedToggle();
        if (selectedPayment instanceof RadioButton rb && paymentRadioButtons.containsKey(rb))
        {
            ids.add(paymentRadioButtons.get(rb).getId());
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
        if (!paymentsToggleGroup.getToggles().isEmpty())
        {
            paymentsToggleGroup.selectToggle(null);
        }
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
            log.error("Cost estimation failed for postal code {}", destPostalCode, e);
        	new GenericPopup(this.resources).showOkPopup(this, "%generic.failed_to_fetch", "%generic.database_problem");
            return;
        } catch (IllegalArgumentException e) {
            log.warn("Cost estimation rejected: {}", e.getMessage());
            new GenericPopup(this.resources).showOkPopup(this, "%generic.failed_to_fetch", "%generic.map.postal_code_not_in_warehouse");
            return;
        }

        log.info("Cost estimated: dest={}, total={}", destPostalCode, computedShipment.getTotalCost());
        loadScreen(COST_BREAKDOWN, computedShipment);
    }

    @FXML
    private void onViewRegions() {
        String selectedName = fromCombo.getValue();
        BriefWarehouse selected = warehouseList.stream()
                .filter(w -> w.getName().equals(selectedName))
                .findFirst()
                .orElse(null);
        if (selected == null) return;
        try {
            WarehouseService wService = WarehouseService.getInstance();
            var regionTable = wService.getWarehouseFormatted(selected).getRegionTable();
            new RegionsPopup(this.resources).show(this, selected, regionTable);
        } catch (SQLException e) {
            log.error("Failed to load region table for warehouse '{}'", selectedName, e);
            new GenericPopup(this.resources).showOkPopup(this, "%generic.failed_to_fetch", "%generic.database_problem");
        }
    }

    @FXML
    public void onTestMap() {
        FromCoordsDataGetter.setCallback(postalCode -> {
            destinationField.setText(postalCode);
        });
        MapPickerController.open();
    }
}
