package org.shippin.controller.utils;

import java.util.ResourceBundle;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public abstract class ServicesManagementPopup {

    protected final ResourceBundle resources;

    public ServicesManagementPopup(ResourceBundle resources) {
        this.resources = resources;
    }

    protected String t(String key) {
        if (key == null) {
            return "";
        }

        if (key.startsWith("%")) {
            return resources.getString(key.substring(1));
        }

        return key;
    }

    protected VBox createPopupRoot() {
        VBox popup = new VBox(22);
        popup.getStyleClass().add("popup-root");
        popup.setPadding(new Insets(28));
        return popup;
    }

    protected Label createPopupTitle(String text) {
        Label title = new Label(text);
        title.getStyleClass().add("popup-title");
        title.setWrapText(true);
        return title;
    }
}