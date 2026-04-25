package org.shippin.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import lombok.extern.log4j.Log4j2;
import org.shippin.controller.utils.ErrorHandler;
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
    public PasswordField passwordFieldRep;
    public Label statusLabelPass;
    public Label statusLabelEmail;
    public Label statusLabelPassConfirm;
    public Label statusLabelName;
    public TextField passwordVisible;
    public TextField passwordVisibleRep;
    public Button eyeButton;
    public Button eyeButtonRep;

    private boolean passwordShown    = false;
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

        // TODO: check if user already exists in DB
        NavigationUtilities.navigateTo(Screens.HOME);
    }

    public void onGoToLogin(ActionEvent actionEvent)
    {
        NavigationUtilities.navigateTo(Screens.LOGIN);
    }
}