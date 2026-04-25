package org.shippin.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.shippin.controller.utils.ErrorHandler;
import org.shippin.controller.utils.InputValidator;
import org.shippin.controller.utils.NavigationUtilities;
import org.shippin.dto.Screens;

import java.util.List;


public class LoginController {

    @FXML private TextField emailTextField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisible;
    @FXML private Button eyeButton;
    @FXML private Label statusLabelEmail;
    @FXML private Label statusLabelPass;

    private boolean passwordShown = false;

    @FXML private void onTogglePassword()
    {
        if (passwordShown)
        {
            // Switch back to hidden
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
            // Switch to visible
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

        String email = emailTextField.getText();
        String password = passwordShown ? passwordVisible.getText() : passwordField.getText();

        // SYNTACTICAL VERIFICATION OF INPUT
        String emailError = ErrorHandler.validateEmail(email);
        String passwordError = ErrorHandler.validatePassword(password);

        if (!emailError.isEmpty() || !passwordError.isEmpty())
        {
            statusLabelEmail.setText(emailError);
            statusLabelPass.setText(passwordError);
            return;
        }

        // TODO verification function to DB (email,password)
        NavigationUtilities.navigateTo(Screens.HOME);
    }

    //TODO implement maybe
    @FXML private void onForgotPassword()
    {

    }

    @FXML private void onGoToRegister()
    {
        NavigationUtilities.navigateTo(Screens.REGISTER);
    }
}