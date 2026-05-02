package org.shippin.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import lombok.extern.log4j.Log4j2;
import org.shippin.services.NavigationService;
import org.shippin.domain.User;
import org.shippin.dto.Screens;
import org.shippin.services.UserService;

import java.sql.SQLException;
import java.util.Locale;

@Log4j2
public class LoginController {

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
    private void initialize() {
        langButton.setText(NavigationService.getBundle().getLocale().getLanguage().equals("sk") ? "EN" : "SK");
        // password wrapper focus efekt
        passwordField.focusedProperty().addListener((obs, old, isFocused) -> {
            if (isFocused) passwordWrapper.getStyleClass().add("password-wrapper-focused");
            else passwordWrapper.getStyleClass().remove("password-wrapper-focused");
        });

        passwordVisible.focusedProperty().addListener((obs, old, isFocused) -> {
            if (isFocused) passwordWrapper.getStyleClass().add("password-wrapper-focused");
            else passwordWrapper.getStyleClass().remove("password-wrapper-focused");
        });
    }

    @FXML private void onTogglePassword()
    {
        if (passwordShown)
        {
            passwordField.setText(passwordVisible.getText());
            passwordField.setManaged(true);
            passwordField.setVisible(true);
            passwordVisible.setManaged(false);
            passwordVisible.setVisible(false);
            eyeButton.setText("👁");
            passwordShown = false;
        }
        else
        {
            passwordVisible.setText(passwordField.getText());
            passwordVisible.setManaged(true);
            passwordVisible.setVisible(true);
            passwordField.setManaged(false);
            passwordField.setVisible(false);
            eyeButton.setText("🙈");
            passwordShown = true;
        }
    }

    @FXML private void onLogin()
    {
        String email    = emailTextField.getText();
        String password = passwordShown ? passwordVisible.getText() : passwordField.getText();

//        String emailError    = ErrorHandler.validateEmail(email);
//        String passwordError = ErrorHandler.validatePassword(password);

//        if (!emailError.isEmpty() || !passwordError.isEmpty())
//        {
//            statusLabelPass.setText(passwordError);
//            return;
//        }

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
        Locale next = NavigationService.getBundle().getLocale().getLanguage().equals("sk")
                ? Locale.ENGLISH
                : new Locale("sk");
        NavigationService.setLocale(next);
        NavigationService.navigateTo(Screens.LOGIN);
    }
}
