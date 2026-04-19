package org.shippin.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.shippin.controller.utils.CostEstimationInput;
import org.shippin.controller.utils.ExtraOption;
import org.shippin.controller.utils.NavigationUtilities;
import org.shippin.dto.Screens;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static java.lang.Double.parseDouble;
import static org.shippin.dto.Screens.COST_BREAKDOWN;

public class CostEstimationController extends BaseController<Void> implements Initializable {

    // ── Title / Date ─────────────────────────────────────────────────────────
    @FXML private Label     sectionTitleLabel;
    @FXML private TextField dateField;

    // ── Type toggles ─────────────────────────────────────────────────────────
    @FXML private CheckBox chkSmallPackage;
    @FXML private CheckBox chkShipment;

    // ── Postal codes ─────────────────────────────────────────────────────────
    @FXML private ComboBox<String> fromCombo;
    @FXML private TextField destinationField;

    // ── Size row ─────────────────────────────────────────────────────────────
    @FXML private TextField weightField;
    @FXML private TextField volumeField;

    // ── Coefficients row ─────────────────────────────────────────────────────
    @FXML private TextField fuelSurchargeField;
    @FXML private TextField tollField;

    // ── Additional fees — Obstarávané služby ─────────────────────────────────
    @FXML private CheckBox chkAdditionalFees;
    @FXML private CheckBox chkADR;
    @FXML private CheckBox chkDobierka;
    @FXML private CheckBox chkPripoistenie;
    @FXML private CheckBox chkVratenieEUP;

    // ── Additional fees — Produkty pre ZBS ───────────────────────────────────
    @FXML private CheckBox chkPremium;
    @FXML private CheckBox chkFIX;
    @FXML private CheckBox chkPremium10;
    @FXML private CheckBox chkFIX10;
    @FXML private CheckBox chkPremium13;
    @FXML private CheckBox chkFIX13;

    // ── Result / status ──────────────────────────────────────────────────────
    @FXML private Label  statusLabel;
    @FXML private HBox   resultBox;
    @FXML private Label  estimatedCostLabel;

    // ── Buttons ──────────────────────────────────────────────────────────────
    @FXML private Button resetButton;
    @FXML private Button computeButton;



    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Populate From combo
        fromCombo.getItems().addAll("Sklad BA", "Sklad KE", "Sklad PO");
        fromCombo.setValue("Sklad BA");
        initializeOptions();


        // Additional fees body visibility bound to header checkbox
//        chkAdditionalFees.selectedProperty().addListener((obs, oldVal, selected) -> {
//            // TODO: show/hide fees section if needed
//        });
    }

    public void initializeOptions()
    {
        ExtraOption.SMALL_PACKAGE.bind(chkSmallPackage);
        ExtraOption.SHIPMENT.bind(chkShipment);

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

    // ── Action handlers ──────────────────────────────────────────────────────

    @FXML
    private void onReset() {}
    @FXML
    private void onComputeCost() throws IOException
    {
        CostEstimationInput input = new CostEstimationInput(
                getDate(),
                getFrom(),
                getDestination(),
                getWeight(),
                getVolume(),
                getFuelSurcharge(),
                getToll(),
                getSelectedOptions()
        );
        loadScreen(COST_BREAKDOWN,input);
    }

}
