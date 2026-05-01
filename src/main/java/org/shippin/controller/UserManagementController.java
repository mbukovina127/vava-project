package org.shippin.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.extern.log4j.Log4j2;
import org.shippin.controller.utils.ErrorHandler;
import org.shippin.services.NavigationService;
import org.shippin.controller.utils.PasswordUtils;
import org.shippin.domain.User;
import org.shippin.domain.enums.Role;
import org.shippin.services.UserService;

import java.net.URL;
import java.sql.SQLException;
import java.util.*;

@Log4j2
public class UserManagementController extends BaseController<Void> implements Initializable {

    private record UserEntry(int id, String fullName, Role role) {}

    // ── FXML — page ───────────────────────────────────────────────────────────
    @FXML private Button addUserButton;
    @FXML private VBox   userListContainer;

    // ── FXML — overlay + dialog card ─────────────────────────────────────────
    @FXML private Region        dimOverlay;
    @FXML private VBox          dialogCard;
    @FXML private TextField     nameField;
    @FXML private TextField     surnameField;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField repeatPasswordField;
    @FXML private Label         statusLabel;
    @FXML private Label         statusLabelName;
    @FXML private Label         statusLabelEmail;
    @FXML private Label         statusLabelPass;
    @FXML private Label         statusLabelPassConfirm;

    // ── State ─────────────────────────────────────────────────────────────────
    private final List<UserEntry> users = new ArrayList<>();

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            for (User u : UserService.getAllUsers()) {
                users.add(new UserEntry(u.getId(), u.getFirstName() + " " + u.getLastName(), u.getRole()));
            }
        } catch (SQLException e) {
            log.error("Failed to load users", e);
        }
        populateList();
    }

    @Override
    protected Class<Void> getDataType() { return Void.class; }

    // ── List builder ──────────────────────────────────────────────────────────
    private void populateList() {
        userListContainer.getChildren().clear();
        for (UserEntry user : users) {
            userListContainer.getChildren().add(buildUserRow(user));
        }
    }

    private HBox buildUserRow(UserEntry user) {
        // Left side — user name
        Label nameLabel = new Label(user.fullName());
        nameLabel.getStyleClass().add("um-user-name");

        // Right side — role toggles + delete
        ToggleGroup group = new ToggleGroup();
        ToggleButton btnUser  = makeRoleButton("user",  Role.USER,       user.role(), group);
        ToggleButton btnPower = makeRoleButton("power", Role.POWER_USER, user.role(), group);
        ToggleButton btnAdmin = makeRoleButton("admin", Role.ADMIN,      user.role(), group);

        btnUser .getStyleClass().add("um-role-left");
        btnPower.getStyleClass().add("um-role-mid");
        btnAdmin.getStyleClass().add("um-role-right");

        group.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) { oldT.setSelected(true); return; }
            Role selected = (Role) newT.getUserData();
            onRoleChanged(user, selected);
            applyActiveStyle(btnUser,  selected);
            applyActiveStyle(btnPower, selected);
            applyActiveStyle(btnAdmin, selected);
        });

        Button deleteBtn = new Button("🗑");
        deleteBtn.getStyleClass().add("um-btn-delete");
        deleteBtn.setOnAction(e -> onDeleteUser(user));

        HBox controls = new HBox(12, btnUser, btnPower, btnAdmin, deleteBtn);
        controls.setAlignment(Pos.CENTER_RIGHT);

        // Flexible spacer pushes controls to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Inner HBox: name | spacer | controls
        HBox inner = new HBox(nameLabel, spacer, controls);
        inner.setAlignment(Pos.CENTER_LEFT);
        inner.getStyleClass().add("um-row");
        inner.setPadding(new Insets(10, 16, 10, 16));

        // Outer HBox: gray background, full width
        HBox outer = new HBox(inner);
        HBox.setHgrow(inner, Priority.ALWAYS);
        outer.getStyleClass().add("um-row-outer");
        outer.setMaxWidth(Double.MAX_VALUE);

        return outer;
    }

    private ToggleButton makeRoleButton(String text, Role role, Role currentRole, ToggleGroup group) {
        ToggleButton btn = new ToggleButton(text);
        btn.setToggleGroup(group);
        btn.setUserData(role);
        btn.setSelected(role == currentRole);
        btn.getStyleClass().add("um-role-btn");
        applyActiveStyle(btn, currentRole);
        return btn;
    }

    private void applyActiveStyle(ToggleButton btn, Role activeRole) {
        btn.getStyleClass().removeAll("um-active-user", "um-active-power", "um-active-admin");
        if ((Role) btn.getUserData() == activeRole) {
            btn.getStyleClass().add(switch (activeRole) {
                case USER       -> "um-active-user";
                case POWER_USER -> "um-active-power";
                case ADMIN      -> "um-active-admin";
            });
        }
    }

    // ── Dialog open / close ───────────────────────────────────────────────────

    @FXML
    private void onAddUser() {
        clearDialogFields();
        dimOverlay.setVisible(true);
        dialogCard.setVisible(true);
    }

    @FXML
    private void onCancel() { hideDialog(); }

    @FXML
    private void onOverlayClicked() { hideDialog(); }

    private void hideDialog() {
        dimOverlay.setVisible(false);
        dialogCard.setVisible(false);
        clearDialogFields();
    }

    private void clearDialogFields() {
        nameField.clear();
        surnameField.clear();
        emailField.clear();
        passwordField.clear();
        repeatPasswordField.clear();
        if (statusLabel != null) {
            statusLabel.setText("");
            statusLabel.getStyleClass().removeAll("dialog-status-error", "dialog-status-ok");
        }
        statusLabelName.setText("");
        statusLabelEmail.setText("");
        statusLabelPass.setText("");
        statusLabelPassConfirm.setText("");
    }

    // ── Dialog confirm ────────────────────────────────────────────────────────

    @FXML
    private void onConfirmAddUser() {
        String name     = nameField.getText().trim();
        String surname  = surnameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = passwordField.getText();
        String repeat   = repeatPasswordField.getText();

        String firstNameError       = ErrorHandler.validateFirstName(name);
        String lastNameError        = ErrorHandler.validateLastName(surname);
        String emailError           = ErrorHandler.validateEmail(email);
        String passwordError        = ErrorHandler.validatePassword(password);
        String confirmPasswordError = ErrorHandler.comparePasswords(password, repeat);

        if (!emailError.isEmpty() || !passwordError.isEmpty()
                || !firstNameError.isEmpty() || !lastNameError.isEmpty()
                || !confirmPasswordError.isEmpty()) {

            Set<String> nameErrors = new LinkedHashSet<>();
            if (!firstNameError.isEmpty()) nameErrors.add(firstNameError);
            if (!lastNameError.isEmpty())  nameErrors.add(lastNameError);

            statusLabelName.setText(String.join("\n", nameErrors));
            statusLabelEmail.setText(emailError);
            statusLabelPass.setText(passwordError);
            statusLabelPassConfirm.setText(confirmPasswordError);
            return;
        }

        try {
            if (UserService.findByEmail(email) != null) {
                statusLabelEmail.setText(NavigationService.getBundle().getString("user_management.email_in_use"));
                return;
            }

            User newUser = new User(name, surname, email, Role.USER);
            newUser.setPassword(PasswordUtils.hash(password));
            UserService.register(newUser);

            User saved = UserService.findByEmail(email);
            users.add(new UserEntry(saved.getId(), name + " " + surname, Role.USER));
            populateList();
            hideDialog();

        } catch (SQLException e) {
            log.error("Add user failed", e);
            statusLabelEmail.setText(NavigationService.getBundle().getString("user_management.add_failed"));
        }
    }

    // ── Row actions ───────────────────────────────────────────────────────────

    private void onDeleteUser(UserEntry user) {
        try {
            UserService.deleteUser(user.id());
            users.remove(user);
            log.info("Deleted user. User_id: {}", user.id);
            populateList();
        } catch (SQLException e) {
            log.error("Delete user failed", e);
        }
    }

    private void onRoleChanged(UserEntry user, Role newRole) {
        try {
            UserService.updateRole(user.id(), newRole);
            log.info("Changed role of user_id={}, to new role={}", user.id, newRole);
        } catch (SQLException e) {
            log.error("Role update failed for user {}", user.id(), e);
        }
    }
}
