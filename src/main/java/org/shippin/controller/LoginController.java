package org.shippin.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import lombok.extern.log4j.Log4j2;
import org.shippin.controller.utils.ErrorHandler;
import org.shippin.controller.utils.NavigationUtilities;
import org.shippin.controller.utils.PasswordUtils;
import org.shippin.database.DBConnector;
import org.shippin.database.dao.UserDAO;
import org.shippin.domain.User;
import org.shippin.dto.Screens;
import org.shippin.session.Session;

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

    private boolean passwordShown = false;

    @FXML
    private void initialize() {
        langButton.setText(NavigationUtilities.getBundle().getLocale().getLanguage().equals("sk") ? "EN" : "SK");
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
            UserDAO userDAO = new UserDAO(DBConnector.getInstance().getConnection());
            User user = userDAO.authenticate(email, PasswordUtils.hash(password));

            if (user == null) {
                statusLabelEmail.setText("Invalid email or password");
                statusLabelPass.setText("");
                return;
            }

            Session.login(user);
            log.info("User logged in: {}", user.getEmail());
            NavigationUtilities.navigateTo(Screens.HOME);

        } catch (SQLException e) {
            log.error("Login DB error", e);
            statusLabelEmail.setText("Login failed, please try again");
        }
    }

    @FXML private void onGoToRegister()
    {
        NavigationUtilities.navigateTo(Screens.REGISTER);
    }

    @FXML private void onToggleLanguage()
    {
        Locale next = NavigationUtilities.getBundle().getLocale().getLanguage().equals("sk")
                ? Locale.ENGLISH
                : new Locale("sk");
        NavigationUtilities.setLocale(next);
        NavigationUtilities.navigateTo(Screens.LOGIN);
    }
}
