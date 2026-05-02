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
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import lombok.extern.log4j.Log4j2;
import org.shippin.services.NavigationService;
import org.shippin.controller.utils.AuthUtils;
import org.shippin.domain.enums.Role;
import org.shippin.dto.Screens;
import org.shippin.services.UserService;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.shippin.controller.utils.ShipmentData;
import org.shippin.controller.utils.CostEstimationInput;

@Log4j2
public class MenuController implements Initializable {
    private record NavItem(Screens screen, String name, String icon_light, String icon_dark) {}

    //Top nav bar
    @FXML private HBox      topNavBar;
    @FXML private ImageView brandLogoImageView;
    @FXML private Label     appNameLabel;
    @FXML private Hyperlink navLink1;
    @FXML private Hyperlink navLink2;
    @FXML private Hyperlink navLink3;
    @FXML private Hyperlink navLink4;
    @FXML private Hyperlink navLink5;
    @FXML private Label     UserNameLabel;
    @FXML private Button    profileButton;
    @FXML private Button    langButton;
    @FXML private StackPane modalOverlay;
    @FXML private VBox modalContentHolder;

    //Left sidebar
    @FXML private VBox   leftSidebar;
    private static final List<NavItem> NAV_ITEMS = List.of(
            new NavItem(Screens.COST_ESTIMATION, "Cost Estimation", "/icons/png-light/plus_white.png", "/icons/png-dark/plus_black.png"),
            new NavItem(Screens.USER_MANAGEMENT, "User Management", "/icons/png-light/admin_white.png", "/icons/png-dark/admin_black.png"), //FIXME testing menu item
            new NavItem(Screens.DAILY_COST, "Daily Costs", "/icons/png-light/calendar_white.png", "/icons/png-dark/calendar_black.png"), //FIXME testing menu item
            new NavItem(Screens.DAILY_COST_SUM, "Daily Costs Summary", "/icons/png-light/list_white.png", "/icons/png-dark/list_black.png"), //FIXME testing menu item
            new NavItem(Screens.WAREHOUSE_MANAGEMENT, "Warehouse Management", "/icons/png-light/edit_white.png", "/icons/png-dark/edit_black.png") //FIXME testing menu item
//            new NavItem(null, "Home", "", "")

    );


    // CONTENT
    @FXML private StackPane contentArea;
    private Screens currentScreen;
    private Object  currentData;

    public void showOverlay(javafx.scene.Node content) {
    	if (content instanceof Region region) {
            region.setMaxHeight(Region.USE_PREF_SIZE);
            region.setMaxWidth(Region.USE_PREF_SIZE);
        }
        StackPane.setAlignment(content, Pos.CENTER);
    	modalOverlay.getChildren().setAll(content);
    	modalOverlay.setManaged(true);
        modalOverlay.setVisible(true);
    }

    public void hideOverlay() {
        modalOverlay.setVisible(false);
        modalOverlay.setManaged(false);
        modalOverlay.getChildren().clear();
    }

    // package-private — len BaseController to vidí
    void loadScreen(Screens screen, Object data) {
        if (!screen.isAccessibleBy(UserService.getRole())) {
            log.warn("Access denied to screen: {} (current role: {})", screen, UserService.getRole());
            return;
        }
        currentScreen = screen;
        currentData   = data;
        try {
            URL fxmlUrl = getClass().getResource(Screens.resolveScreen(screen));
            if (fxmlUrl == null) {
                throw new IllegalStateException("FXML not found: " + Screens.resolveScreen(screen));
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.setResources(NavigationService.getBundle());
            Node node = loader.load();

            Object ctrl = loader.getController();

            if (ctrl instanceof BaseController<?> bc) {
                bc.setMenuController(this);
            }

            if (ctrl instanceof Navigatable nav) {
                nav.onNavigatedTo(data);
            }

            contentArea.getChildren().setAll(node);

        } catch (Exception e) {
            log.error("Failed to load screen: {}", screen, e);
            throw new RuntimeException("Failed to load screen: " + screen, e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        langButton.setText(NavigationService.getBundle().getLocale().getLanguage().equals("sk") ? "EN" : "SK");

        //TODO dat meno usera zo session (get string)
        //UserNameLabel.setText(username);

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

            if (item.screen() != null) {
                Role required = item.screen().getRequiredRole();
                if (required != null) AuthUtils.guard(btn, required);
            }

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
                loadScreen(item.screen(),null);
            });

            leftSidebar.getChildren().add(btn);
        }


//        //..TOTO ZMAZAT OD TADE
//        Button testBtn = new Button();
//        testBtn.getStyleClass().add("sidebar-btn");
//
//        var testStream = getClass().getResourceAsStream("/icons/png-dark/plus_black.png");
//        if (testStream != null) {
//            ImageView testIcon = new ImageView(new Image(testStream));
//            testIcon.setFitWidth(40);
//            testIcon.setFitHeight(40);
//            testIcon.setPreserveRatio(true);
//            testBtn.setGraphic(testIcon);
//        }
//
//        VBox.setMargin(testBtn, new Insets(10, 5, 0, 5));
//
//        testBtn.setOnAction(e -> {
//            System.out.println("TEST BUTTON CLICKED!");
//            try {
//                URL fxmlUrl = getClass().getResource("/views/ShipmentDetail.fxml");
//                FXMLLoader loader = new FXMLLoader(fxmlUrl);
//                loader.setResources(NavigationService.getBundle());
//                Node node = loader.load();
//
//                Object ctrl = loader.getController();
//                if (ctrl instanceof BaseController<?> bc) {
//                    bc.setMenuController(this);
//                }
//
//                if (ctrl instanceof ShipmentDetailController sdc) {
//                    CostEstimationInput est = new CostEstimationInput(
//                            "2026-04-30",           // date
//                            "Bratislava",           // from
//                            "09301",                // destination ← PSČ KOŠICE!
//                            54.46,                  // weight
//                            210,                    // volume
//                            5.0,                    // fuelSurcharge
//                            2.0,                    // toll
//                            "TEST",                 // shipment_opt
//                            "TEST",                 // delivery_opt
//                            List.of()               // options
//                    );
//                    ShipmentData dummyData = new ShipmentData(est, "Test", 321);
//                    sdc.onData(dummyData);
//                }
//
//                contentArea.getChildren().setAll(node);
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//        });
//
//        leftSidebar.getChildren().add(testBtn);
//        //PO TADE

        loadScreen(NAV_ITEMS.getFirst().screen(),null);
    }

    @FXML private void onProfileClicked() {}

    @FXML
    private void onToggleLanguage() {
        Locale next = NavigationService.getBundle().getLocale().getLanguage().equals("sk")
                ? Locale.ENGLISH
                : new Locale("sk");
        NavigationService.setLocale(next);
        langButton.setText(next.getLanguage().equals("sk") ? "EN" : "SK");
        if (currentScreen != null) {
            loadScreen(currentScreen, currentData);
        }
    }

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
