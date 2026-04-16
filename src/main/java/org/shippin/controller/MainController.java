package org.shippin.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * MainController
 * ══════════════════════════════════════════════════════════════
 * Owns MainScene.fxml — the outer shell that contains:
 *   • Top navigation bar
 *   • Left sidebar  (Component_3.svg)
 *   • Centre StackPane (#contentPane) — inner view holder
 *
 * Navigation model
 * ────────────────
 * Each sidebar button and each top-nav button calls loadView().
 * loadView() replaces the single child of #contentPane with the
 * newly loaded FXML node.  The old node is discarded; its
 * controller is garbage-collected.
 *
 * Active-button highlighting
 * ──────────────────────────
 * setActiveButton() applies inline dark style to the chosen
 * sidebar button and resets all others to white, mirroring the
 * SVG where the active icon has fill=#5D3F25.
 * ══════════════════════════════════════════════════════════════
 */
public class MainController implements Initializable {

    // ── Top nav bar ────────────────────────────────────────────
    @FXML private Label   currentDateLabel;
    @FXML private Button  profileButton;

    @FXML private Button  navBtnHome;
    @FXML private Button  navBtnNewShipment;
    @FXML private Button  navBtnSchedule;
    @FXML private Button  navBtnShipmentOrder;
    @FXML private Button  navBtnCostEstimate;

    // ── Sidebar buttons ────────────────────────────────────────
    @FXML private Button  sidebarBtn1;   // Add / New shipment
    @FXML private Button  sidebarBtn2;   // Checklist / Orders
    @FXML private Button  sidebarBtn3;   // Calendar / Schedule
    @FXML private Button  sidebarBtn4;   // Map pin / Locations
    @FXML private Button  sidebarBtn5;   // Wrench / Tools
    @FXML private Button  sidebarBtn6;   // User / Profile

    // ── Centre content pane ────────────────────────────────────
    @FXML private StackPane contentPane;
    @FXML private Label     placeholderLabel;

    // ── Style constants (match SVG colours) ────────────────────
    private static final String STYLE_ACTIVE =
        "-fx-background-color: #5D3F25;" +
        "-fx-background-radius: 9;" +
        "-fx-border-color: #000000;" +
        "-fx-border-width: 2;" +
        "-fx-border-radius: 9;" +
        "-fx-cursor: hand;";

    private static final String STYLE_INACTIVE =
        "-fx-background-color: #FFFFFF;" +
        "-fx-background-radius: 9;" +
        "-fx-border-color: #000000;" +
        "-fx-border-width: 2;" +
        "-fx-border-radius: 9;" +
        "-fx-cursor: hand;";

    // ── FXML view paths ────────────────────────────────────────
    private static final String VIEW_COST_ESTIMATION  = "/com/app/view/Cost_estimation.fxml";
    private static final String VIEW_NEW_SHIPMENT     = "/com/app/view/NewShipment.fxml";
    private static final String VIEW_SCHEDULE         = "/com/app/view/Schedule.fxml";
    private static final String VIEW_SHIPMENT_ORDER   = "/com/app/view/ShipmentOrder.fxml";
    private static final String VIEW_HOME             = "/com/app/view/Home.fxml";
    private static final String VIEW_PROFILE          = "/com/app/view/Profile.fxml";

    private List<Button> sidebarButtons;

    // ══════════════════════════════════════════════════════════
    // Initialise
    // ══════════════════════════════════════════════════════════
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Collect sidebar buttons for easy iteration
        sidebarButtons = List.of(
            sidebarBtn1, sidebarBtn2, sidebarBtn3,
            sidebarBtn4, sidebarBtn5, sidebarBtn6
        );

        // Show today's date in the top bar
        currentDateLabel.setText(
            LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
        );

        // Load the default view (Cost Estimation matches the example SVG)
        loadView(VIEW_COST_ESTIMATION);
        setActiveButton(sidebarBtn1);
    }

    // ══════════════════════════════════════════════════════════
    // View-switching helper
    // ══════════════════════════════════════════════════════════

    /**
     * Loads an FXML file and places it as the sole child of
     * #contentPane, replacing whatever was there before.
     *
     * @param fxmlPath classpath-relative path to the FXML file
     */
    public void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(fxmlPath)
            );
            Node view = loader.load();
            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            // Optionally show an error label inside contentPane
            Label err = new Label("Failed to load view: " + fxmlPath);
            err.setStyle("-fx-text-fill: #CC0000; -fx-font-size: 14px;");
            contentPane.getChildren().setAll(err);
        }
    }

    // ══════════════════════════════════════════════════════════
    // Active-button highlight
    // ══════════════════════════════════════════════════════════

    /**
     * Marks one sidebar button as active (dark fill) and resets
     * all others to the inactive (white) style.
     */
    private void setActiveButton(Button active) {
        for (Button btn : sidebarButtons) {
            btn.setStyle(btn == active ? STYLE_ACTIVE : STYLE_INACTIVE);
        }
    }

    // ══════════════════════════════════════════════════════════
    // TOP NAV handlers
    // ══════════════════════════════════════════════════════════

    @FXML
    private void onNavHome() {
        loadView(VIEW_HOME);
        setActiveButton(null); // no sidebar button active for Home
    }

    @FXML
    private void onNavNewShipment() {
        loadView(VIEW_NEW_SHIPMENT);
        setActiveButton(sidebarBtn1);
    }

    @FXML
    private void onNavSchedule() {
        loadView(VIEW_SCHEDULE);
        setActiveButton(sidebarBtn3);
    }

    @FXML
    private void onNavShipmentOrder() {
        loadView(VIEW_SHIPMENT_ORDER);
        setActiveButton(sidebarBtn2);
    }

    @FXML
    private void onNavCostEstimate() {
        loadView(VIEW_COST_ESTIMATION);
        setActiveButton(sidebarBtn1);
    }

    @FXML
    private void onProfileClicked() {
        loadView(VIEW_PROFILE);
        setActiveButton(sidebarBtn6);
    }

    // ══════════════════════════════════════════════════════════
    // SIDEBAR handlers
    // ══════════════════════════════════════════════════════════

    /** Button 1 — Add / New Shipment (plus-circle icon, SVG y=66) */
    @FXML
    private void onSidebarNew() {
        loadView(VIEW_NEW_SHIPMENT);
        setActiveButton(sidebarBtn1);
    }

    /** Button 2 — Checklist / Orders (SVG y=166) */
    @FXML
    private void onSidebarChecklist() {
        loadView(VIEW_SHIPMENT_ORDER);
        setActiveButton(sidebarBtn2);
    }

    /** Button 3 — Calendar / Schedule (SVG y=266) */
    @FXML
    private void onSidebarCalendar() {
        loadView(VIEW_SCHEDULE);
        setActiveButton(sidebarBtn3);
    }

    /** Button 4 — Map pin / Locations (SVG y=366) */
    @FXML
    private void onSidebarMap() {
        loadView(VIEW_COST_ESTIMATION);   // replace with Map view when ready
        setActiveButton(sidebarBtn4);
    }

    /** Button 5 — Wrench / Tools (SVG y=466) */
    @FXML
    private void onSidebarTools() {
        loadView(VIEW_COST_ESTIMATION);   // replace with Tools view when ready
        setActiveButton(sidebarBtn5);
    }

    /** Button 6 — User / Profile (SVG y=566) */
    @FXML
    private void onSidebarProfile() {
        loadView(VIEW_PROFILE);
        setActiveButton(sidebarBtn6);
    }
}
