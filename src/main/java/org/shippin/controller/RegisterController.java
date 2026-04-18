package org.shippin.controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import lombok.extern.log4j.Log4j2;
import org.shippin.controller.utils.InputValidator;
import org.shippin.controller.utils.NavigationUtilities;
import org.shippin.dto.Screens;

import java.util.LinkedHashSet;
import java.util.Set;

@Log4j2
public class RegisterController {

    public TextField firstNameField;
    public TextField lastNameField;
    public TextField emailField;
    public PasswordField passwordField;
    public PasswordField confirmPasswordField;
    public Label statusLabelPass;
    public Label statusLabelEmail;
    public Label statusLabelPassConfirm;
    public Label statusLabelName;

    public void onRegister(ActionEvent actionEvent)
    {
//      log.debug("Registering user");
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // SYNTACTICAL VERIFICATION OF INPUT
        String firstNameError = InputValidator.validateFirstName(firstName);
        String lastNameError = InputValidator.validateLastName(lastName);
        String emailError = InputValidator.validateEmail(email);
        String passwordError = InputValidator.validatePassword(password);
        String confirmPasswordError = InputValidator.comparePasswords(password,confirmPassword);

        if
        (
                !emailError.isEmpty()
                        || !passwordError.isEmpty()
                        || !firstNameError.isEmpty()
                        || !lastNameError.isEmpty()
                        || !confirmPasswordError.isEmpty()
        )
        {
            Set<String> uniqueErrors = new LinkedHashSet<>();

            if (!firstNameError.isEmpty()) uniqueErrors.add(firstNameError);
            if (!lastNameError.isEmpty()) uniqueErrors.add(lastNameError);

            statusLabelName.setText(String.join("\n", uniqueErrors));

            statusLabelEmail.setText(emailError);
            statusLabelPass.setText(passwordError);
            statusLabelPassConfirm.setText(confirmPasswordError);

            return;
        }

        //TODO function that checks if user already exists in DB (first_name,last_name,email)
        NavigationUtilities.navigateTo(Screens.HOME);
//      log.info("User registration failed");

    }

    public void onGoToLogin(ActionEvent actionEvent) {
        NavigationUtilities.navigateTo(Screens.LOGIN);
    }

    public void onShowTerms(ActionEvent actionEvent) {
        log.debug("Showing terms");
    }
}
