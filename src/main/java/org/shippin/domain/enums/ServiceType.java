package org.shippin.domain.enums;

import java.util.ResourceBundle;

public enum ServiceType {
    SERVICES,
    PRODUCTS,
    ADDITIONAL_PAYMENTS;

    public String getI18nKey() {
        return "services_management.service_type." + name();
    }
    
    public String getLocalized(ResourceBundle bundle) {
        if (bundle == null) {
            return name();
        }

        try {
            return bundle.getString(getI18nKey());
        } catch (java.util.MissingResourceException e) {
            System.err.println("Missing i18n key: " + getI18nKey());
            return name();
        }
    }
}
