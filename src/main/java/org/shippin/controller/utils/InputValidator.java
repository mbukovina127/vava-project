package org.shippin.controller.utils;

import java.util.regex.Pattern;

public class InputValidator {

    public static boolean isValidLength(String value, int maxLength) {
        return value != null && value.length() <= maxLength;
    }

    public static boolean matches(String value, String regex) {
        if (value == null) return false;
        return value.matches(regex);
    }

    // RFC 5322-compliant email pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    private static final Pattern HAS_LOWERCASE  = Pattern.compile(".*[a-z].*");
    private static final Pattern HAS_UPPERCASE  = Pattern.compile(".*[A-Z].*");
    private static final Pattern HAS_DIGIT      = Pattern.compile(".*\\d.*");
    private static final Pattern HAS_SPECIAL    = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
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