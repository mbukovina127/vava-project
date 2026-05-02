package org.shippin.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.shippin.controller.utils.CostEstimationInput;
import org.shippin.controller.utils.ErrorHandler;
import org.shippin.controller.utils.ExtraOption;

import org.shippin.app.FromCoordsDataGetter;

import org.shippin.controller.MapPickerController;
import org.shippin.app.FromCoordsDataGetter;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static java.lang.Double.parseDouble;
import static org.shippin.dto.Screens.COST_BREAKDOWN;

public class CostEstimationController extends BaseController<Void> implements Initializable {

    // Title + Date
    @FXML private Label     sectionTitleLabel;
    @FXML private TextField dateField;

    // Type toggles (small or shipment)
    @FXML private ToggleGroup shipmentTypeGroup;
    @FXML private RadioButton rbSmallPackage;
    @FXML private RadioButton rbShipment;

    // Postal codes
    @FXML private ComboBox<String> fromCombo;
    @FXML private TextField destinationField;

    // Size row
    @FXML private TextField weightField;
    @FXML private TextField volumeField;

    // Coefficients row
    @FXML private TextField fuelSurchargeField;
    @FXML private TextField tollField;

    // Additional fees
    @FXML private CheckBox chkAdditionalFees;
    @FXML private CheckBox chkADR;
    @FXML private CheckBox chkDobierka;
    @FXML private CheckBox chkPripoistenie;
    @FXML private CheckBox chkVratenieEUP;

    // Type toggles (Time of delivery)
    @FXML private ToggleGroup deliveryTypeGroup;
    @FXML private RadioButton rbExpress;
    @FXML private RadioButton rbClassic;
    @FXML private RadioButton rbEconomy;

    // Additional fees — Produkty pre ZBS
    @FXML private CheckBox chkPremium;
    @FXML private CheckBox chkFIX;
    @FXML private CheckBox chkPremium10;
    @FXML private CheckBox chkFIX10;
    @FXML private CheckBox chkPremium13;
    @FXML private CheckBox chkFIX13;

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

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        rbShipment.setSelected(true);
        rbClassic.setSelected(true);
        //TODO: nacitat mena skladov z db (strings)
        fromCombo.getItems().addAll("Sklad BA", "Sklad KE", "Sklad PO");
        fromCombo.setValue(fromCombo.getItems().getFirst());
        //TODO: nacitat poskytovane doplnkove sluzby z db (strings) a asi zotriedit do skupin
        initializeOptions();
    }

    public void initializeOptions()
    {
        ExtraOption.ADDITIONAL_FEES.bind(chkAdditionalFees);
        ExtraOption.ADR.bind(chkADR);
        ExtraOption.DOBIERKA.bind(chkDobierka);
        ExtraOption.PRIPOISTENIE.bind(chkPripoistenie);
        ExtraOption.VRATENIE_EUP.bind(chkVratenieEUP);

        ExtraOption.PREMIUM.bind(chkPremium);
        ExtraOption.FIX.bind(chkFIX);
        ExtraOption.PREMIUM_10.bind(chkPremium10);
        ExtraOption.FIX_10.bind(chkFIX10);
        ExtraOption.PREMIUM_13.bind(chkPremium13);
        ExtraOption.FIX_13.bind(chkFIX13);
    }


    // GETTERS

    public List<ExtraOption> getSelectedOptions() {
        return Arrays.stream(ExtraOption.values())
                .filter(ExtraOption::isSelected)
                .toList();
    }

    public String getDate() {
        return dateField.getText();
    }

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
        dateField.clear();
        fromCombo.getSelectionModel().selectFirst();
        destinationField.clear();
        weightField.clear();
        volumeField.clear();
        fuelSurchargeField.clear();
        tollField.clear();

        rbShipment.setSelected(true);
        rbClassic.setSelected(true);

        chkAdditionalFees.setSelected(true);
        chkADR.setSelected(false);
        chkDobierka.setSelected(false);
        chkPripoistenie.setSelected(false);
        chkVratenieEUP.setSelected(false);
        chkPremium.setSelected(false);
        chkFIX.setSelected(false);
        chkPremium10.setSelected(false);
        chkFIX10.setSelected(false);
        chkPremium13.setSelected(false);
        chkFIX13.setSelected(false);
    }

    private String getSelectedText(ToggleGroup group)
    {
        return group.getSelectedToggle() != null
                ? ((RadioButton) group.getSelectedToggle()).getText()
                : "";
    }

    @FXML
    private void onComputeCost() throws IOException
    {
        // zistime typ sluzby
        String shipment = getSelectedText(shipmentTypeGroup);
        // zistime rychlost dorucenia
        String deliveryTime = getSelectedText(deliveryTypeGroup);

        //TODO:asi calendar view a errors
        String date = dateField.getText().trim();
        //TODO:pridat mapu alebo aky vstup?
        String destination = destinationField.getText().trim();

        String weightText = weightField.getText().trim();
        String volumeText = volumeField.getText().trim();
        String fuelSurchargeText = fuelSurchargeField.getText().trim();
        String tollText = tollField.getText().trim();

        String toggleError = ErrorHandler.validateShipmentType(shipment);
       // String dateError = ErrorHandler.validateRequired(date, "Date");
        String destinationError = ErrorHandler.validateRequired(destination, "Destination");
        String weightError = ErrorHandler.validatePositiveDouble(weightText, "Weight");
        String volumeError = ErrorHandler.validatePositiveDouble(volumeText, "Volume");

        String fuelSurchargeError = ErrorHandler.validatePositiveDouble(fuelSurchargeText, "Fuel surcharge");
        String tollError = ErrorHandler.validatePositiveDouble(tollText, "Toll");

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

        // send loaded user data to next screen
        CostEstimationInput input = new CostEstimationInput(
                getDate(),
                getFrom(),
                getDestination(),
                getWeight(),
                getVolume(),
                getFuelSurcharge(),
                getToll(),
                shipment,
                deliveryTime,
                getSelectedOptions()
        );
        loadScreen(COST_BREAKDOWN,input);
    }

    @FXML
    public void onTestMap() {
        FromCoordsDataGetter.setCallback(postalCode -> {
            destinationField.setText(postalCode);
        });
        MapPickerController.open();
    }

}
