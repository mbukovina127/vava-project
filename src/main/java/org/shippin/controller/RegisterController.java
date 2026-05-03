package org.shippin.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import lombok.extern.log4j.Log4j2;
import org.shippin.controller.utils.ErrorHandler;
import org.shippin.services.NavigationService;
import org.shippin.domain.User;
import org.shippin.domain.enums.Role;
import org.shippin.dto.Screens;
import org.shippin.services.UserService;

import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import static org.shippin.controller.utils.ErrorHandler.msg;

@Log4j2
public class RegisterController extends AuthController {

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

    @FXML private HBox passwordWrapper;
    @FXML private HBox passwordWrapperRep;

    private boolean passwordShown = false;
    private boolean passwordShownRep = false;

    private String getPassword()    { return passwordShown    ? passwordVisible.getText()    : passwordField.getText(); }
    private String getPasswordRep() { return passwordShownRep ? passwordVisibleRep.getText() : passwordFieldRep.getText(); }


    @FXML
    public void initialize()
    {
        initLangButton(langButton);
        //change password visibility observer
        setEyeIcon(eyeButton,false);
        setEyeIcon(eyeButtonRep,false);
        bindPasswordFocus(passwordWrapper, passwordField, passwordVisible);
        bindPasswordFocus(passwordWrapperRep, passwordFieldRep, passwordVisibleRep);
    }

    // action handlers

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
            if (UserService.findByEmail(email) != null)
            {
                statusLabelEmail.setText(msg("error.register.invalid"));
                return;
            }

            User newUser = new User(firstName, lastName, email, Role.USER);
            UserService.register(newUser, password);

            UserService.login(UserService.findByEmail(email));
            NavigationService.navigateTo(Screens.HOME);

        } catch (SQLException e) {
            log.error("Registration DB error", e);
            statusLabelEmail.setText(msg("error.database.invalid.register"));
        }
    }

    public void onGoToLogin(ActionEvent actionEvent)
    {
        NavigationService.navigateTo(Screens.LOGIN);
    }

    @FXML
    public void onToggleLanguage()
    {
       toggleLanguage(Screens.REGISTER);
       log.info("User changed language: {}", NavigationService.getBundle().getLocale().getLanguage());
    }
}