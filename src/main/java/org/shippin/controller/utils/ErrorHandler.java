package org.shippin.controller.utils;

import org.shippin.dto.RegPattern;
import org.shippin.services.NavigationService;
import java.text.MessageFormat;
import static org.shippin.controller.utils.InputValidator.*;

public class ErrorHandler {

    private ErrorHandler() {}

    private static final int STANDARD_MAX_LENGTH = 50;

    public static String msg(String key)
    {
        return NavigationService.getBundle().getString(key);
    }

    public static String validateEmail(String email)
    {
        if (!isNotBlank(email))
        {
            return msg("error.field.required");
        }
        if (email.length() > STANDARD_MAX_LENGTH)
        {
            return MessageFormat.format(msg("error.field.maxlength"), STANDARD_MAX_LENGTH);
        }
        if (!isValidEmail(email))
        {
            return msg("error.email.invalid");
        }
        return "";
    }

    public static String validatePassword(String password)
    {
        if (!isNotBlank(password)) {
            return msg("error.field.required");
        }
        if (password.length() > STANDARD_MAX_LENGTH) {
            return MessageFormat.format(msg("error.field.maxlength"), STANDARD_MAX_LENGTH);
        }
        if (!isValidPassword(password)) {
            return msg("error.password.invalid");
        }
        return "";
    }

    public static String validateFirstName(String firstName)
    {
        if (!isNotBlank(firstName)) {
            return msg("error.surname.required");
        }
        if (firstName.length() > STANDARD_MAX_LENGTH) {
            return MessageFormat.format(msg("error.field.maxlength"), STANDARD_MAX_LENGTH);
        }
        if (!matches(firstName, RegPattern.NAME)) {
            return msg("error.name.invalid");
        }
        return "";
    }

    public static String validateLastName(String lastName)
    {
        if (!isNotBlank(lastName)) {
            return msg("error.lastname.required");
        }
        if (lastName.length() > STANDARD_MAX_LENGTH) {
            return MessageFormat.format(msg("error.field.maxlength"), STANDARD_MAX_LENGTH);
        }
        if (!matches(lastName, RegPattern.NAME)) {
            return msg("error.name.invalid");
        }
        return "";
    }

    public static String comparePasswords(String password, String repeatedPassword) {
        if (!stringMatch(password, repeatedPassword))
        {
            return msg("error.passwords.mismatch");
        }
        return "";
    }

    public static String validateRequired(String value)
    {
        if (!isNotBlank(value)) {
            return msg("error.field.required");
        }
        return "";
    }

    public static String validateMaxLength(String value, String fieldName, int maxLength)
    {
        if (value != null && value.length() > maxLength)
        {
            return MessageFormat.format(msg("error.field.maxlength"), maxLength);
        }
        return "";
    }

    public static String validatePositiveDouble(String value) {
        String error = validateRequired(value);
        if (!error.isEmpty()) return error;
        try {
            double number = Double.parseDouble(value.trim());
            if (number < 0) {
                return msg("error.field.positive");
            }
            else if (!matches(value, RegPattern.POSITIVE_DOUBLE))
            {
                return msg("error.field.format.double");
            }
            return "";
        } catch (NumberFormatException e) {
            return msg("error.field.number");
        }
    }

    public static String validatePercent(String value)
    {
        String error = validateRequired(value);
        if (!error.isEmpty()) return error;
        try {
            double number = Double.parseDouble(value.trim());
            if (number < 0 || number > 1)
            {
                return msg("error.field.percent_range");
            }
            else if (!matches(value, RegPattern.PERCENT))
            {
                return msg("error.field.format.percent");
            }
            return "";
        } catch (NumberFormatException e) {
            return msg("error.field.number");
        }
    }
}