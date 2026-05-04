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
import java.util.*;

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
            new NavItem(Screens.DAILY_COST_SUM, "My Shipments", "/icons/png-light/list_white.png", "/icons/png-dark/list_black.png"),
            new NavItem(Screens.USER_MANAGEMENT, "User Management", "/icons/png-light/admin_white.png", "/icons/png-dark/admin_black.png"), //FIXME testing menu item
            new NavItem(Screens.DAILY_COST, "Daily Costs", "/icons/png-light/calendar_white.png", "/icons/png-dark/calendar_black.png"), //FIXME testing menu item
            new NavItem(Screens.WAREHOUSE_MANAGEMENT, "Warehouse Management", "/icons/png-light/edit_white.png", "/icons/png-dark/edit_black.png"), //FIXME testing menu item
            new NavItem(Screens.MAP_OF_SHIPMENTS, "Map", "/icons/png-light/edit_white.png", "/icons/png-dark/edit_black.png") //MAX TLACITKO, na test, ak to nema byt tu presunut inde
//            new NavItem(null, "Home", "", "")

    );


    // CONTENT
    @FXML private StackPane contentArea;
    private Screens currentScreen;
    private Object  currentData;
    private List<Button> buttons = new ArrayList<>();


    private void setProfilePicture()
    {
        ImageView profileIcon = new ImageView(new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/icons/png-dark/user.png"))
        ));
        profileIcon.setFitHeight(48);
        profileIcon.setFitWidth(48);
        profileIcon.setPreserveRatio(true);
        profileIcon.setSmooth(true);
        profileButton.setGraphic(profileIcon);
        profileButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

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
        setProfilePicture();
        UserNameLabel.setText(UserService.getUser().getFullUserName());



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

    private void setActive(Button activeBtn)
    {
        // remove normal from clicked
        activeBtn.getStyleClass().remove("sidebar-btn");

        // set active
        activeBtn.getStyleClass().add("sidebar-btn-active");
    }

    /*
     mark active on navbar
     */
    public void setActive(Screens screen) {
        for (int i = 0; i < buttons.size(); i++) {
            Button b = buttons.get(i);
            NavItem item = NAV_ITEMS.get(i);

            b.getStyleClass().setAll("sidebar-btn");

            if (!item.icon_dark().isEmpty()) {
                var s = getClass().getResourceAsStream(item.icon_dark());
                if (s != null) ((ImageView) b.getGraphic()).setImage(new Image(s));
            }

            if (item.screen() == screen) {
                b.getStyleClass().setAll("sidebar-btn-active");

                if (b.getGraphic() instanceof ImageView iv) {
                    var s = getClass().getResourceAsStream(item.icon_light());
                    if (s != null) iv.setImage(new Image(s));
                }
            }
        }
    }
}
