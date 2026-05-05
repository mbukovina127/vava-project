package org.shippin.controller.utils;

import org.shippin.dto.RegPattern;

import java.util.regex.Pattern;


public class InputValidator {


    // BASIC

    public static boolean isValidLength(String value) {
        return value != null && value.length() > 250;
    }

    public static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    public static boolean isValidLength(String value, int maxLength) {
        return value != null && value.length() <= maxLength;
    }
    public static boolean isValidMinLength(String password, int minLength) {
        return password != null && password.length() >= minLength;
    }
    public static boolean isLength(String password, int length) {
        return password != null && password.length() == length;
    }
    public static boolean stringMatch(String str1, String str2) {
        return str1 != null && str1.equals(str2);
    }


//    public static boolean matches(String value, RegPattern pattern, int minLength, int maxLength) {
//        return value != null
//                && value.length() >= minLength
//                && value.length() <= maxLength
//                && pattern.getPattern().matcher(value).matches();
//    }


    // REGEX

    public static boolean hasLowercase(String password) {
        return password != null && HAS_LOWERCASE.matcher(password).matches();
    }

    public static boolean hasUppercase(String password) {
        return password != null && HAS_UPPERCASE.matcher(password).matches();
    }

    public static boolean hasDigit(String password) {
        return password != null && HAS_DIGIT.matcher(password).matches();
    }

    public static boolean hasSpecial(String password) {
        return password != null && HAS_SPECIAL.matcher(password).matches();
    }
    
    public static boolean isPostalCode(String text) {
    	return text != null && InputValidator.isInteger(text.replaceAll("\\s+", "")) 
		    && InputValidator.isLength(text.replaceAll("\\s+", ""), 5);
	}
    
    public static boolean isInteger(String text) {
        return IS_DIGIT.matcher(text).matches();
    }

    private static final Pattern IS_DIGIT = Pattern.compile("[0-9]+");
    private static final Pattern HAS_LOWERCASE = Pattern.compile(".*[a-z].*");
    private static final Pattern HAS_UPPERCASE = Pattern.compile(".*[A-Z].*");
    private static final Pattern HAS_DIGIT     = Pattern.compile(".*\\d.*");
    private static final Pattern HAS_SPECIAL   = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

    public static boolean matches(String value, RegPattern pattern) {
        return value != null && pattern.getPattern().matcher(value).matches();
    }

    public static boolean isValidEmail(String email) {
        return matches(email, RegPattern.EMAIL);
    }

    public static boolean isValidPassword(String password) {
        return
            isValidMinLength(password,8)
            && hasDigit(password)
            && hasSpecial(password)
            && hasUppercase(password)
            && hasLowercase(password);
    }
}