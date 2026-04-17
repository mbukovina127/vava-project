package org.shippin.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

public class CostEstimationController implements Initializable {

    // ── Title / Date ─────────────────────────────────────────────────────────
    @FXML private Label     sectionTitleLabel;
    @FXML private TextField dateField;

    // ── Type toggles ─────────────────────────────────────────────────────────
    @FXML private CheckBox chkSmallPackage;
    @FXML private CheckBox chkShipment;

    // ── Postal codes ─────────────────────────────────────────────────────────
    @FXML private ComboBox<String> fromCombo;
    @FXML private TextField        destinationField;

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

        // Additional fees body visibility bound to header checkbox
//        chkAdditionalFees.selectedProperty().addListener((obs, oldVal, selected) -> {
//            // TODO: show/hide fees section if needed
//        });
    }

    // ── Action handlers ──────────────────────────────────────────────────────

    @FXML
    private void onReset() {}
    @FXML
    private void onComputeCost() {}
}
