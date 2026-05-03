package org.shippin.controller.utils;

import org.shippin.dto.RegPattern;
import org.shippin.services.NavigationService;

import java.text.MessageFormat;

import static org.shippin.controller.utils.InputValidator.*;

public class ErrorHandler {
    // TODO: ake budeme kontrolovat limity?

    private ErrorHandler() {}

    private static String msg(String key)
    {
        return NavigationService.getBundle().getString(key);
    }

    public static String validateEmail(String email) {
        if (!isNotBlank(email)) {
            return msg("error.email.required");
        }
        if (!isValidEmail(email)) {
            return msg("error.email.invalid");
        }
        return "";
    }

    public static String validatePassword(String password) {
        if (!isNotBlank(password)) {
            return msg("error.password.required");
        }
        if (!isValidPassword(password)) {
            return msg("error.password.invalid");
        }
        return "";
    }

    public static String validateFirstName(String firstName) {
        if (!isNotBlank(firstName)) {
            return msg("error.surname.required");
        }
        if (!matches(firstName, RegPattern.NAME)) {
            return msg("error.name.invalid");
        }
        return "";
    }

    public static String validateLastName(String lastName) {
        if (!isNotBlank(lastName)) {
            return msg("error.lastname.required");
        }
        if (!matches(lastName, RegPattern.NAME)) {
            return msg("error.name.invalid");
        }
        return "";
    }

    public static String comparePasswords(String password, String repeatedPassword) {
        if (!stringMatch(password, repeatedPassword)) {
            return msg("error.passwords.mismatch");
        }
        return "";
    }

//    public static String validateShipmentType(String shipmentType) {
//        if (!isNotBlank(shipmentType)) {
//            return "Select shipment type.";
//        }
//        return "";
//    }

    public static String validateRequired(String value, String fieldName) {
        if (!isNotBlank(value)) {
            return MessageFormat.format(msg("error.field.required"), fieldName);
        }
        return "";
    }

    public static String validatePositiveDouble(String value, String fieldName) {

        String error = validateRequired(value,fieldName);
        if (!error.isEmpty()){return  error;}

        try {
            double number = Double.parseDouble(value.trim());
            if (number < 0) {
                return MessageFormat.format(msg("error.field.positive"), fieldName);
            }
            return "";
        } catch (NumberFormatException e) {
            return MessageFormat.format(msg("error.field.number"), fieldName);
        }
    }

    public static String validatePercent(String value, String fieldName) {

        String error = validateRequired(value, fieldName);
        if (!error.isEmpty()) {return error;}

        try {
            double number = Double.parseDouble(value.trim());

            if (number < 0 || number > 1) {
                return MessageFormat.format(msg("error.field.percent_range"), fieldName);
            }

            return "";
        } catch (NumberFormatException e) {
            return MessageFormat.format(msg("error.field.number"), fieldName);
        }
    }
}