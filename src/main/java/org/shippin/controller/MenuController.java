package org.shippin.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import lombok.extern.log4j.Log4j2;
import org.shippin.dto.Screens;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

@Log4j2
public class MenuController implements Initializable {
    private record NavItem(Screens screen, String name, String icon_light, String icon_dark) {}

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
    private static final List<NavItem> NAV_ITEMS = List.of(
            new NavItem(Screens.COST_ESTIMATION, "Cost Estimation", "/icons/png-light/plus_white.png", "/icons/png-dark/plus_black.png"),
            new NavItem(Screens.LOGIN, "LOGIN", "/icons/png-light/admin_white.png", "/icons/png-dark/admin_black.png"), //FIXME testing menu item
            new NavItem(null, "Home", "", "")
    );


    // CONTENT
    @FXML private StackPane contentArea;


    // ── Action handlers ──────────────────────────────────────────────────────
    private void loadPage(Screens screen) {
        // TODO place for privilage verification
        try {
            var resource = getClass().getResource(Screens.resolveScreen(screen));
            Node node = FXMLLoader.load(resource);
            contentArea.getChildren().setAll(node);
            log.debug("Loaded screen: {}, into the main container", screen);
        } catch (IOException e) {
            log.error("Failed to load screen: {}", screen, e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        List<Button> buttons = new ArrayList<>();

        for (int i = 0; i < NAV_ITEMS.size(); i++) {
            NavItem item = NAV_ITEMS.get(i);
            Button btn = new Button();
            boolean isFirst = i == 0;
            btn.getStyleClass().add(isFirst ? "sidebar-btn-active" : "sidebar-btn");

            ImageView icon = null;
            if (!item.icon_dark().isEmpty()) {
                String initialIcon = isFirst ? item.icon_light() : item.icon_dark();
                var stream = getClass().getResourceAsStream(initialIcon);
                if (stream != null) {
                    icon = new ImageView(new Image(stream));
                    icon.setFitWidth(40);
                    icon.setFitHeight(40);
                    icon.setPreserveRatio(true);
                    btn.setGraphic(icon);
                } else {
                    log.warn("Icon for menu item: {} - not found: {}", item, initialIcon);
                }
            }

            final ImageView finalIcon = icon;
            VBox.setMargin(btn, new Insets(10, 5, 0, 5));
            buttons.add(btn);

            btn.setOnAction(e -> {
                if (item.screen() == null) {
                    log.error("Cannot load null screen, (btn= {}, item= {})", btn, item);
                    return;
                }
                for (int j = 0; j < buttons.size(); j++) {
                    Button b = buttons.get(j);
                    b.getStyleClass().setAll("sidebar-btn");
                    NavItem ni = NAV_ITEMS.get(j);
                    if (!ni.icon_dark().isEmpty()) {
                        var s = getClass().getResourceAsStream(ni.icon_dark());
                        if (s != null) ((ImageView) b.getGraphic()).setImage(new Image(s));
                    }
                }
                btn.getStyleClass().setAll("sidebar-btn-active");
                if (finalIcon != null) {
                    var s = getClass().getResourceAsStream(item.icon_light());
                    if (s != null) finalIcon.setImage(new Image(s));
                }
                loadPage(item.screen());
            });

            leftSidebar.getChildren().add(btn);
        }

        loadPage(NAV_ITEMS.get(0).screen());
    }

    @FXML private void onProfileClicked() {}

    private void setActive(Button activeBtn) {
        // remove active from all
//        sidebarBtn1.getStyleClass().remove("sidebar-btn-active");
//        sidebarBtn2.getStyleClass().remove("sidebar-btn-active");
//        sidebarBtn3.getStyleClass().remove("sidebar-btn-active");
//        sidebarBtn4.getStyleClass().remove("sidebar-btn-active");
//        sidebarBtn5.getStyleClass().remove("sidebar-btn-active");
//        sidebarBtn6.getStyleClass().remove("sidebar-btn-active");
//
//        // add normal class back (optional safety)
//        sidebarBtn1.getStyleClass().add("sidebar-btn");
//        sidebarBtn2.getStyleClass().add("sidebar-btn");
//        sidebarBtn3.getStyleClass().add("sidebar-btn");
//        sidebarBtn4.getStyleClass().add("sidebar-btn");
//        sidebarBtn5.getStyleClass().add("sidebar-btn");
//        sidebarBtn6.getStyleClass().add("sidebar-btn");

        // remove normal from clicked
        activeBtn.getStyleClass().remove("sidebar-btn");

        // set active
        activeBtn.getStyleClass().add("sidebar-btn-active");
    }
}
