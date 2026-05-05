package org.shippin.infrastructure.validation;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.shippin.services.NavigationService;

final class ValidationMessages {

    private ValidationMessages() {}

    static String msg(String key, Object... args) {
        ResourceBundle bundle = NavigationService.getBundle();

        if (bundle == null) {
            return key;
        }

        String realKey = key.startsWith("%") ? key.substring(1) : key;

        try {
            String pattern = bundle.getString(realKey);
            return new MessageFormat(pattern, bundle.getLocale()).format(args);
        } catch (MissingResourceException e) {
            System.err.println("Missing i18n key: " + realKey);
            return key;
        }
    }
}