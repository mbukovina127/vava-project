package org.shippin.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.shippin.controller.utils.ErrorHandler;

import java.net.URL;
import java.util.*;

public class UserManagementController extends BaseController<Void> implements Initializable {

    public Label statusLabelPassConfirm;
    public Label statusLabelPass;
    public Label statusLabelEmail;
    public Label statusLabelName;

    // ── Roles ─────────────────────────────────────────────────────────────────
    public enum Role { USER, POWER, ADMIN }
    public record UserEntry(String fullName, Role role) {}

    // ── FXML — page ───────────────────────────────────────────────────────────
    @FXML private Button addUserButton;
    @FXML private VBox   userListContainer;

    // ── FXML — overlay + dialog card ─────────────────────────────────────────
    @FXML private Region       dimOverlay;
    @FXML private VBox         dialogCard;
    @FXML private TextField    nameField;
    @FXML private TextField    surnameField;
    @FXML private TextField    emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField repeatPasswordField;
    @FXML private Label        statusLabel;

    // ── State ─────────────────────────────────────────────────────────────────
    private final List<UserEntry> users = new ArrayList<>();

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO: replace with real DB/service call
        users.addAll(List.of(
                new UserEntry("Roman Mrkva",       Role.USER),
                new UserEntry("Adam Kaleráb",      Role.POWER),
                new UserEntry("Ronnie O'Sullivan",  Role.ADMIN)
        ));
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
        Label nameLabel = new Label(user.fullName());
        nameLabel.getStyleClass().add("um-user-name");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        ToggleGroup group = new ToggleGroup();
        ToggleButton btnUser  = makeRoleButton("user",  Role.USER,  user.role(), group);
        ToggleButton btnPower = makeRoleButton("power", Role.POWER, user.role(), group);
        ToggleButton btnAdmin = makeRoleButton("admin", Role.ADMIN, user.role(), group);

        btnUser .getStyleClass().add("um-role-left");
        btnPower.getStyleClass().add("um-role-mid");
        btnAdmin.getStyleClass().add("um-role-right");

        HBox roleBox = new HBox(btnUser, btnPower, btnAdmin);
        roleBox.setAlignment(Pos.CENTER);

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

        HBox row = new HBox(nameLabel, roleBox, deleteBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("um-row");
        HBox.setMargin(deleteBtn, new Insets(0, 0, 0, 12));
        return row;
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
                case USER  -> "um-active-user";
                case POWER -> "um-active-power";
                case ADMIN -> "um-active-admin";
            });
        }
    }

    // ── Dialog open / close ───────────────────────────────────────────────────

    /** Called by the "Add new user" button — shows the overlay + dialog card. */
    @FXML
    private void onAddUser() {
        clearDialogFields();
        dimOverlay.setVisible(true);
        dialogCard.setVisible(true);
    }

    /** Cancel button or clicking the dim overlay closes the dialog. */
    @FXML
    private void onCancel() {
        hideDialog();
    }

    @FXML
    private void onOverlayClicked() {
        hideDialog();
    }

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
        statusLabel.setText("");
        statusLabel.getStyleClass().removeAll("dialog-status-error", "dialog-status-ok");
    }

    // ── Dialog confirm ────────────────────────────────────────────────────────

    @FXML
    private void onConfirmAddUser() {
        String name     = nameField.getText().trim();
        String surname  = surnameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = passwordField.getText();
        String repeat   = repeatPasswordField.getText();

        // SYNTACTICAL VERIFICATION OF INPUT
        String firstNameError = ErrorHandler.validateFirstName(name);
        String lastNameError = ErrorHandler.validateLastName(surname);
        String emailError = ErrorHandler.validateEmail(email);
        String passwordError = ErrorHandler.validatePassword(password);
        String confirmPasswordError = ErrorHandler.comparePasswords(password,repeat);

        if
        (
                !emailError.isEmpty()
                        || !passwordError.isEmpty()
                        || !firstNameError.isEmpty()
                        || !lastNameError.isEmpty()
                        || !confirmPasswordError.isEmpty()
        ) {
            Set<String> uniqueErrors = new LinkedHashSet<>();

            if (!firstNameError.isEmpty()) uniqueErrors.add(firstNameError);
            if (!lastNameError.isEmpty()) uniqueErrors.add(lastNameError);

            statusLabelName.setText(String.join("\n", uniqueErrors));
            statusLabelName.getStyleClass().removeAll("dialog-status-ok");
            statusLabelName.getStyleClass().add("dialog-status-error");

            statusLabelEmail.setText(emailError);
            statusLabelEmail.getStyleClass().removeAll("dialog-status-ok");
            statusLabelEmail.getStyleClass().add("dialog-status-error");
            statusLabelPass.setText(passwordError);
            statusLabelPass.getStyleClass().removeAll("dialog-status-ok");
            statusLabelPass.getStyleClass().add("dialog-status-error");
            statusLabelPassConfirm.setText(confirmPasswordError);
            statusLabelPassConfirm.getStyleClass().removeAll("dialog-status-ok");
            statusLabelPassConfirm.getStyleClass().add("dialog-status-error");

            return;
        }
        // TODO: persist to DB / service here
        users.add(new UserEntry(name + " " + surname, Role.USER));
        populateList();
        hideDialog();
    }

//    private void showError(String message) {
//        statusLabel.setText(message);

//    }

    // ── Row actions ───────────────────────────────────────────────────────────

    private void onDeleteUser(UserEntry user) {
        // TODO: confirm + delete from DB
        users.remove(user);
        populateList();
    }

    private void onRoleChanged(UserEntry user, Role newRole) {
        // TODO: persist role change to DB / service
        System.out.printf("Role changed: %s → %s%n", user.fullName(), newRole);
    }
}