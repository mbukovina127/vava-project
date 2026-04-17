package org.shippin.controller;

import javafx.event.ActionEvent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import lombok.extern.log4j.Log4j2;
import org.shippin.controller.utils.InputValidator;
import org.shippin.controller.utils.NavigationUtilities;
import org.shippin.dto.Screens;
import org.shippin.dto.RegPattern;

@Log4j2
public class RegisterController {

    public TextField firstNameField;
    public TextField lastNameField;
    public TextField emailField;
    public PasswordField passwordField;
    public PasswordField confirmPasswordField;

    public void onRegister(ActionEvent actionEvent) {
//        log.debug("Registering user");
//        String firstName = firstNameField.getText();
//        String lastName = lastNameField.getText();
//        String email = emailField.getText();
//        String password = passwordField.getText();
//        String confirmPassword = confirmPasswordField.getText();
//
//        if (InputValidator.matches(firstName, RegPattern.NAME)
//        && InputValidator.matches(lastName, RegPattern.NAME)
//        && InputValidator.isValidEmail(email)
//        && InputValidator.isValidPassword(password)
//        && InputValidator.passwordsMatch(password, confirmPassword)) {
//            log.info("User registered");
            //TODO function that checks if user already exists
            NavigationUtilities.navigateTo(Screens.HOME);
//        }
//        log.info("User registration failed");

    }

    public void onGoToLogin(ActionEvent actionEvent) {
        NavigationUtilities.navigateTo(Screens.LOGIN);
    }

    public void onShowTerms(ActionEvent actionEvent) {
        log.debug("Showing terms");
    }
}
