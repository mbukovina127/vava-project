package org.shippin.controller;

import javafx.event.ActionEvent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import lombok.extern.log4j.Log4j2;
import org.shippin.controller.utils.NavigationUtilities;
import org.shippin.dto.Screens;

@Log4j2
public class RegisterController {

    public TextField firstNameField;
    public TextField lastNameField;
    public TextField emailField;
    public PasswordField passwordField;
    public PasswordField confirmPasswordField;

    public void onRegister(ActionEvent actionEvent) {
        log.debug("Registering user");
        NavigationUtilities.navigateTo(Screens.HOME);
    }

    public void onGoToLogin(ActionEvent actionEvent) {
        NavigationUtilities.navigateTo(Screens.LOGIN);
    }

    public void onShowTerms(ActionEvent actionEvent) {
        log.debug("Showing terms");
    }
}
