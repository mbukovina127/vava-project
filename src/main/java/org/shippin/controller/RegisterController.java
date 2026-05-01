package org.shippin.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import lombok.extern.log4j.Log4j2;
import org.shippin.controller.utils.ErrorHandler;
import org.shippin.services.NavigationService;
import org.shippin.controller.utils.PasswordUtils;
import org.shippin.database.DBConnector;
import org.shippin.database.dao.UserDAO;
import org.shippin.domain.User;
import org.shippin.domain.enums.Role;
import org.shippin.dto.Screens;
import org.shippin.services.UserService;

import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

@Log4j2
public class RegisterController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField passwordFieldRep;
    @FXML private Label statusLabelPass;
    @FXML private Label statusLabelEmail;
    @FXML private Label statusLabelPassConfirm;
    @FXML private Label statusLabelName;
    @FXML private TextField passwordVisible;
    @FXML private TextField passwordVisibleRep;
    @FXML private Button eyeButton;
    @FXML private Button eyeButtonRep;
    public Button langButton;

    private boolean passwordShown    = false;

    @FXML
    public void initialize() {
        langButton.setText(NavigationService.getBundle().getLocale().getLanguage().equals("sk") ? "EN" : "SK");
    }
    private boolean passwordShownRep = false;


    // helpers

    //SHOW/HIDE PASSWORD---
    private void togglePassword
    (
            boolean shown,
            PasswordField hidden,
            TextField visible,
            Button eye
    )
    {
        if (shown)
        {
            // show
            visible.setText(hidden.getText());
            visible.setManaged(true);
            visible.setVisible(true);
            hidden.setManaged(false);
            hidden.setVisible(false);
            eye.setText("🙈");
        }
        else
        {
            // hide
            hidden.setText(visible.getText());
            hidden.setManaged(true);
            hidden.setVisible(true);
            visible.setManaged(false);
            visible.setVisible(false);
            eye.setText("👁");
        }
    }

    @FXML
    private void onTogglePassword()
    {
        passwordShown = !passwordShown;
        togglePassword(passwordShown, passwordField, passwordVisible, eyeButton);
    }

    @FXML
    private void onTogglePasswordRep()
    {
        passwordShownRep = !passwordShownRep;
        togglePassword(passwordShownRep, passwordFieldRep, passwordVisibleRep, eyeButtonRep);
    }

    private String getPassword()    { return passwordShown    ? passwordVisible.getText()    : passwordField.getText(); }
    private String getPasswordRep() { return passwordShownRep ? passwordVisibleRep.getText() : passwordFieldRep.getText(); }

    // action handlers

    public void onRegister(ActionEvent actionEvent)
    {
        String firstName      = firstNameField.getText();
        String lastName       = lastNameField.getText();
        String email          = emailField.getText();
        String password       = getPassword();
        String confirmPassword = getPasswordRep();

        String firstNameError       = ErrorHandler.validateFirstName(firstName);
        String lastNameError        = ErrorHandler.validateLastName(lastName);
        String emailError           = ErrorHandler.validateEmail(email);
        String passwordError        = ErrorHandler.validatePassword(password);
        String confirmPasswordError = ErrorHandler.comparePasswords(password, confirmPassword);

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
            UserDAO userDAO = new UserDAO(DBConnector.getInstance().getConnection());

            if (userDAO.findByEmail(email) != null) {
                statusLabelEmail.setText("Email already in use");
                return;
            }

            User newUser = new User(firstName, lastName, email, Role.USER);
            newUser.setPassword(PasswordUtils.hash(password));
            userDAO.insert(newUser);

            UserService.login(userDAO.findByEmail(email));
            NavigationService.navigateTo(Screens.HOME);

        } catch (SQLException e) {
            log.error("Registration DB error", e);
            statusLabelEmail.setText("Registration failed, please try again");
        }
    }

    public void onGoToLogin(ActionEvent actionEvent)
    {
        NavigationService.navigateTo(Screens.LOGIN);
    }

    @FXML
    public void onToggleLanguage()
    {
        Locale next = NavigationService.getBundle().getLocale().getLanguage().equals("sk")
                ? Locale.ENGLISH
                : new Locale("sk");
        NavigationService.setLocale(next);
        NavigationService.navigateTo(Screens.REGISTER);
    }
}