package org.shippin.controller.utils;

import org.shippin.dto.RegPattern;

import java.util.regex.Pattern;


public class InputValidator {

    public static boolean isValidLength(String value) {
        return value != null && value.length() > 250;
    }

    public static boolean isValidLength(String value, int maxLength) {
        return value != null && value.length() <= maxLength;
    }

    public static boolean matches(String value, RegPattern pattern) {
        return value != null && pattern.getPattern().matcher(value).matches();
    }
    public static boolean matches(String value, RegPattern pattern, int minLength, int maxLength) {
        return value != null
                && value.length() >= minLength
                && value.length() <= maxLength
                && pattern.getPattern().matcher(value).matches();
    }

    private static final Pattern HAS_LOWERCASE = Pattern.compile(".*[a-z].*");
    private static final Pattern HAS_UPPERCASE = Pattern.compile(".*[A-Z].*");
    private static final Pattern HAS_DIGIT     = Pattern.compile(".*\\d.*");
    private static final Pattern HAS_SPECIAL   = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

    public static boolean isValidEmail(String email) {
        return matches(email, RegPattern.EMAIL);
    }

    public static boolean passwordHasMinLength(String password) {
        return password != null && password.length() >= 8;
    }

    public static boolean passwordHasLowercase(String password) {
        return password != null && HAS_LOWERCASE.matcher(password).matches();
    }

    public static boolean passwordHasUppercase(String password) {
        return password != null && HAS_UPPERCASE.matcher(password).matches();
    }

    public static boolean passwordHasDigit(String password) {
        return password != null && HAS_DIGIT.matcher(password).matches();
    }

    public static boolean passwordHasSpecial(String password) {
        return password != null && HAS_SPECIAL.matcher(password).matches();
    }

    public static boolean isValidPassword(String password) {
        return passwordHasMinLength(password)
            && passwordHasLowercase(password)
            && passwordHasUppercase(password)
            && passwordHasDigit(password)
            && passwordHasSpecial(password);
    }

    public static boolean passwordsMatch(String password, String confirmation) {
        return password != null && password.equals(confirmation);
    }

    public static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}