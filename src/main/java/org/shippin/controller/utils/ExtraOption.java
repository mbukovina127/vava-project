package org.shippin.controller.utils;

import javafx.scene.control.CheckBox;
import org.shippin.domain.AdditionalService;

import java.util.List;

public enum ExtraOption {
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
    private int serviceId = -1;

    public void bind(CheckBox checkBox) {
        this.checkBox = checkBox;
    }

    public boolean isSelected() {
        return checkBox != null && checkBox.isSelected();
    }

    public int getServiceId() {
        return serviceId;
    }

    public static void initializeServiceIds(List<AdditionalService> services) {
        for (ExtraOption opt : values()) {
            services.stream()
                    .filter(s -> s.getName().equalsIgnoreCase(opt.name().replace("_", " "))
                              || s.getName().equalsIgnoreCase(opt.name()))
                    .findFirst()
                    .ifPresent(s -> opt.serviceId = s.getId());
        }
    }
}
