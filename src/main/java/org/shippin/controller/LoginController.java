package org.shippin.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import org.shippin.controller.utils.NavigationUtilities;
import org.shippin.dto.Screens;


public class LoginController {

    @FXML private TextField emailTextField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    @FXML private void onLogin() {
        NavigationUtilities.navigateTo(Screens.HOME);
    }

    @FXML private void onForgotPassword() {
    }

    @FXML private void onGoToRegister() {
        NavigationUtilities.navigateTo(Screens.REGISTER);
    }
}