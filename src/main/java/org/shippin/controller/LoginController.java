package org.shippin.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import lombok.extern.log4j.Log4j2;
import org.shippin.controller.utils.ErrorHandler;
import org.shippin.services.NavigationService;
import org.shippin.domain.User;
import org.shippin.dto.Screens;
import org.shippin.services.UserService;

import java.sql.SQLException;
import java.util.Locale;

@Log4j2
public class LoginController extends AuthController
{
    @FXML private TextField emailTextField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisible;
    @FXML private Button eyeButton;
    @FXML private Label statusLabelEmail;
    @FXML private Label statusLabelPass;
    @FXML private Button langButton;
    @FXML private HBox passwordWrapper;

    private boolean passwordShown = false;

    @FXML
    private void initialize()
    {
        initLangButton(langButton);
        //change password visibility observer
        setEyeIcon(eyeButton, false);
        bindPasswordFocus(passwordWrapper, passwordField, passwordVisible);
    }

    @FXML private void onTogglePassword()
    {
        passwordShown = !passwordShown;
        togglePassword(passwordShown,passwordField,passwordVisible,eyeButton);
    }

    @FXML private void onLogin()
    {
        String email    = emailTextField.getText();
        String password = passwordShown ? passwordVisible.getText() : passwordField.getText();

        String emailError    = ErrorHandler.validateEmail(email);
        String passwordError = ErrorHandler.validatePassword(password);

        if (!emailError.isEmpty() || !passwordError.isEmpty())
        {
            statusLabelPass.setText(passwordError);
            statusLabelEmail.setText(emailError);
            return;
        }

        try {
            User user = UserService.authenticate(email, password);

            if (user == null) {
                statusLabelEmail.setText("Invalid email or password");
                statusLabelPass.setText("");
                return;
            }

            log.info("User logged in: {}", user.getEmail());
            NavigationService.navigateTo(Screens.HOME);

        } catch (SQLException e) {
            log.error("Login DB error", e);
            statusLabelEmail.setText("Login failed, please try again");
        }
    }

    @FXML private void onGoToRegister()
    {
        NavigationService.navigateTo(Screens.REGISTER);
    }

    @FXML private void onToggleLanguage()
    {
       toggleLanguage(Screens.LOGIN);
       log.info("User changed language: {}", NavigationService.getBundle().getLocale().getLanguage());
    }
}
