package org.shippin.controller.utils;

import org.shippin.dto.RegPattern;

import static org.shippin.controller.utils.InputValidator.*;

public class ErrorHandler {
    // TODO: ake budeme kontrolovat limity?

    private ErrorHandler() {
        // utility class
    }

    public static String validateEmail(String email) {
        if (!isNotBlank(email)) {
            return "Email is required.";
        }
        if (!isValidEmail(email)) {
            return "Email format is invalid. Example: test@gmail.com";
        }
        return "";
    }

    public static String validatePassword(String password) {
        if (!isNotBlank(password)) {
            return "Password is required.";
        }
        if (!isValidPassword(password)) {
            return "Password must be at least 8 characters long and include uppercase, lowercase, a number, and a special character.";
        }
        return "";
    }

    public static String validateFirstName(String firstName) {
        if (!isNotBlank(firstName)) {
            return "Name is required.";
        }
        if (!matches(firstName, RegPattern.NAME)) {
            return "Name and Surname can contain only letters, spaces, hyphens, or apostrophes between words.";
        }
        return "";
    }

    public static String validateLastName(String lastName) {
        if (!isNotBlank(lastName)) {
            return "Last name is required.";
        }
        if (!matches(lastName, RegPattern.NAME)) {
            return "Name and Surname can contain only letters, spaces, hyphens, or apostrophes between words.";
        }
        return "";
    }

    public static String comparePasswords(String password, String repeatedPassword) {
        if (!stringMatch(password, repeatedPassword)) {
            return "Passwords do not match.";
        }
        return "";
    }

    public static String validateShipmentType(String shipmentType) {
        if (!isNotBlank(shipmentType)) {
            return "Select shipment type.";
        }
        return "";
    }

    public static String validateRequired(String value, String fieldName) {
        if (!isNotBlank(value)) {
            return fieldName + " is required.";
        }
        return "";
    }

    public static String validatePositiveDouble(String value, String fieldName) {
        if (!isNotBlank(value)) {
            return fieldName + " is required.";
        }

        try {
            double number = Double.parseDouble(value.trim());
            if (number < 0) {
                return fieldName + " must be greater than or equal to 0.";
            }
            return "";
        } catch (NumberFormatException e) {
            return fieldName + " must be a number.";
        }
    }

    public static String validatePositiveInteger(String value, String fieldName) {
        if (!isNotBlank(value)) {
            return fieldName + " is required.";
        }

        try {
            int number = Integer.parseInt(value.trim());
            if (number < 0) {
                return fieldName + " must be greater than or equal to 0.";
            }
            return "";
        } catch (NumberFormatException e) {
            return fieldName + " must be an integer.";
        }
    }
}