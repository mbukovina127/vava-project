package org.shippin.controller.utils;

import javafx.scene.control.CheckBox;

public enum ExtraOption {
    SMALL_PACKAGE,
    SHIPMENT,
    ADDITIONAL_FEES,
    ADR,
    DOBIERKA,
    PRIPOISTENIE,
    VRATENIE_EUP,
    PREMIUM,
    FIX,
    PREMIUM_10,
    FIX_10,
    PREMIUM_13,
    FIX_13;

    private CheckBox checkBox;

    public void bind(CheckBox checkBox) {
        this.checkBox = checkBox;
    }

    public boolean isSelected() {
        return checkBox != null && checkBox.isSelected();
    }
}