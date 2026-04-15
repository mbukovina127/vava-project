package org.shippin.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class CostEstimationController {

    // ── Top nav bar ──────────────────────────────────────────────────────────
    @FXML private HBox      topNavBar;
    @FXML private ImageView brandLogoImageView;
    @FXML private Label     appNameLabel;
    @FXML private Hyperlink navLink1;
    @FXML private Hyperlink navLink2;
    @FXML private Hyperlink navLink3;
    @FXML private Hyperlink navLink4;
    @FXML private Hyperlink navLink5;
    @FXML private Label     currentDateLabel;
    @FXML private Button    profileButton;

    // ── Left sidebar ─────────────────────────────────────────────────────────
    @FXML private VBox   leftSidebar;
    @FXML private Button sidebarBtn1;
    @FXML private Button sidebarBtn2;
    @FXML private Button sidebarBtn3;
    @FXML private Button sidebarBtn4;
    @FXML private Button sidebarBtn5;
    @FXML private Button sidebarBtn6;

    // ── Content header ───────────────────────────────────────────────────────
    @FXML private Label     sectionTitleLabel;
    @FXML private TextField searchField;

    // ── Shipment details (left column) ───────────────────────────────────────
    @FXML private Label              originLabel;
    @FXML private TextField          originField;
    @FXML private Label              destinationLabel;
    @FXML private TextField          destinationField;
    @FXML private Label              shipmentTypeLabel;
    @FXML private ComboBox<String>   shipmentTypeCombo;

    // ── Cargo details (right column) ─────────────────────────────────────────
    @FXML private Label              weightLabel;
    @FXML private TextField          weightField;
    @FXML private Label              volumeLabel;
    @FXML private TextField          volumeField;
    @FXML private Label              cargoTypeLabel;
    @FXML private ComboBox<String>   cargoTypeCombo;

    // ── Address details grid ─────────────────────────────────────────────────
    @FXML private CheckBox  chkRow1;
    @FXML private TextField addRow1;
    @FXML private TextField addrDetailRow1;
    @FXML private TextField addrRow1;

    @FXML private CheckBox  chkRow2;
    @FXML private TextField addRow2;
    @FXML private TextField addrDetailRow2;
    @FXML private TextField addrRow2;

    @FXML private CheckBox  chkRow3;
    @FXML private TextField addRow3;
    @FXML private TextField addrDetailRow3;
    @FXML private TextField addrRow3;

    @FXML private CheckBox  chkRow4;
    @FXML private TextField addRow4;
    @FXML private TextField addrDetailRow4;
    @FXML private TextField addrRow4;

    // ── Result / status ──────────────────────────────────────────────────────
    @FXML private Label  statusLabel;
    @FXML private HBox   resultBox;
    @FXML private Label  estimatedCostLabel;
    @FXML private Button resetButton;
    @FXML private Button computeButton;

    // ── Nav handlers ─────────────────────────────────────────────────────────
    @FXML private void onNavHome() {}
    @FXML private void onNavNewShipment() {}
    @FXML private void onNavSchedule() {}
    @FXML private void onNavShipmentOrder() {}
    @FXML private void onNavCostEstimate() {}
    @FXML private void onProfileClicked() {}

    // ── Sidebar handlers ─────────────────────────────────────────────────────
    @FXML private void onSidebarBtn1() {}
    @FXML private void onSidebarBtn2() {}
    @FXML private void onSidebarBtn3() {}
    @FXML private void onSidebarBtn4() {}
    @FXML private void onSidebarBtn5() {}
    @FXML private void onSidebarBtn6() {}

    // ── Action handlers ──────────────────────────────────────────────────────
    @FXML
    private void onReset() {

    }

    @FXML
    private void onComputeCost() {

    }
}
