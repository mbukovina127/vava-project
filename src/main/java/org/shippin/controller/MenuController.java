package org.shippin.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MenuController implements Initializable {

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


    // CONTENT
    @FXML private StackPane contentArea;


    // ── Action handlers ──────────────────────────────────────────────────────
    private void loadPage(String path) {
        try {
            var resource = getClass().getResource(path);
            System.out.println("RESOURCE = " + resource);

            Node node = FXMLLoader.load(resource);
            contentArea.getChildren().setAll(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        loadPage("/views/CostEstimation.fxml");
    }

//    @FXML private void onNavHome() {
//        loadPage("/views/Cost_estimation.fxml");
//    }
//    @FXML private void onNavNewShipment() {
//        loadPage("/views/CostEstimation.fxml");
//    }
//    @FXML private void onNavSchedule() {}
//    @FXML private void onNavShipmentOrder() {}
//    @FXML private void onNavCostEstimate() {}


    @FXML private void onProfileClicked() {}
    @FXML private void onSidebarBtn1() {
        setActive(sidebarBtn1);
        loadPage("/views/CostEstimation.fxml");
    }
    @FXML private void onSidebarBtn2() {
        setActive(sidebarBtn2);
        loadPage("/views/Test.fxml");
    }
    @FXML private void onSidebarBtn3() {

    }
    @FXML private void onSidebarBtn4() {}
    @FXML private void onSidebarBtn5() {}
    @FXML private void onSidebarBtn6() {}

    private void setActive(Button activeBtn) {
        // remove active from all
        sidebarBtn1.getStyleClass().remove("sidebar-btn-active");
        sidebarBtn2.getStyleClass().remove("sidebar-btn-active");
        sidebarBtn3.getStyleClass().remove("sidebar-btn-active");
        sidebarBtn4.getStyleClass().remove("sidebar-btn-active");
        sidebarBtn5.getStyleClass().remove("sidebar-btn-active");
        sidebarBtn6.getStyleClass().remove("sidebar-btn-active");

        // add normal class back (optional safety)
        sidebarBtn1.getStyleClass().add("sidebar-btn");
        sidebarBtn2.getStyleClass().add("sidebar-btn");
        sidebarBtn3.getStyleClass().add("sidebar-btn");
        sidebarBtn4.getStyleClass().add("sidebar-btn");
        sidebarBtn5.getStyleClass().add("sidebar-btn");
        sidebarBtn6.getStyleClass().add("sidebar-btn");

        // remove normal from clicked
        activeBtn.getStyleClass().remove("sidebar-btn");

        // set active
        activeBtn.getStyleClass().add("sidebar-btn-active");
    }
}
