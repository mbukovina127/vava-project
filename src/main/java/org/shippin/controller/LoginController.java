package org.shippin.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.shippin.controller.utils.NavigationUtilities;
import org.shippin.dto.ShippinScreen;


public class LoginController {

    @FXML private TextField emailTextField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    @FXML private void onLogin() {
        NavigationUtilities.navigateTo(ShippinScreen.HOME);
    }

    @FXML private void onForgotPassword() {
    }

    @FXML private void onGoToRegister() {
        NavigationUtilities.navigateTo(ShippinScreen.REGISTER);
    }
}