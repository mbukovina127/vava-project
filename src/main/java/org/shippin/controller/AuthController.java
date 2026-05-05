package org.shippin.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.shippin.services.NavigationService;
import org.shippin.dto.Screens;

import java.util.Locale;
import java.util.Objects;

public abstract class AuthController {

    //Language toggle
    protected void initLangButton(Button langButton) {
        langButton.setText(
                NavigationService.getBundle().getLocale().getLanguage().equals("sk") ? "EN" : "SK"
        );
    }

    protected void toggleLanguage(Screens currentScreen) {
        if (this instanceof StatePreservable sp) {
            NavigationService.setPreservedState(sp.captureState());
        }
        Locale next = NavigationService.getBundle().getLocale().getLanguage().equals("sk")
                ? Locale.ENGLISH
                : new Locale("sk");
        NavigationService.setLocale(next);
        NavigationService.navigateTo(currentScreen);
    }

    //Password wrapper focus
    protected void bindPasswordFocus(HBox wrapper, TextField field1, TextField field2) {
        field1.focusedProperty().addListener((obs, old, isFocused) -> {
            if (isFocused) wrapper.getStyleClass().add("password-wrapper-focused");
            else wrapper.getStyleClass().remove("password-wrapper-focused");
        });
        field2.focusedProperty().addListener((obs, old, isFocused) -> {
            if (isFocused) wrapper.getStyleClass().add("password-wrapper-focused");
            else wrapper.getStyleClass().remove("password-wrapper-focused");
        });
    }

    //Password show/hide
    protected void togglePassword(boolean shown, PasswordField hidden,
                                  TextField visible, Button eye) {
        if (shown)
        {
            visible.setText(hidden.getText());
            visible.setManaged(true);
            visible.setVisible(true);
            hidden.setManaged(false);
            hidden.setVisible(false);
//            eye.setText("◎");
        } else
        {
            hidden.setText(visible.getText());
            hidden.setManaged(true);
            hidden.setVisible(true);
            visible.setManaged(false);
            visible.setVisible(false);
//            eye.setText("◉");
        }
        setEyeIcon(eye,shown);
    }

    //true - eye
    //false - eye_crossed
    protected void setEyeIcon(Button eye, boolean shown) {
        ImageView icon = new ImageView();
        icon.setFitHeight(16);
        icon.setFitWidth(16);
        icon.setPreserveRatio(true);
        icon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(
                shown ? "/icons/png-dark/eye.png" : "/icons/png-dark/eye_crossed.png"
        ))));
        eye.setGraphic(icon);
        eye.setText("");
    }
}