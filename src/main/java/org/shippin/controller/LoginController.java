package org.shippin.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.shippin.controller.utils.InputValidator;
import org.shippin.controller.utils.NavigationUtilities;
import org.shippin.dto.Screens;

import java.util.List;


public class LoginController {

    @FXML private TextField emailTextField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    @FXML private void onLogin()
    {

        String email = emailTextField.getText();
        String password = passwordField.getText();

        // SYNTACTICAL VERIFICATION OF INPUT
        List<String> errors = InputValidator.validateLogin(email, password);

        if (!errors.isEmpty()) {
            statusLabel.setText(String.join("\n", errors));
            return;
        }

        // TODO verification function to DB (email,password)
        NavigationUtilities.navigateTo(Screens.HOME);
    }

    @FXML private void onForgotPassword() {}

    @FXML private void onGoToRegister()
    {
        NavigationUtilities.navigateTo(Screens.REGISTER);
    }
}