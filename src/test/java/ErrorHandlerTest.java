import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shippin.controller.utils.ErrorHandler;
import org.shippin.services.NavigationService;

public class ErrorHandlerTest {

    @BeforeEach
    void setUp() {
        NavigationService.setLocale(Locale.ENGLISH);
    }

    @AfterEach
    void cleanUp() {
        NavigationService.setLocale(Locale.ENGLISH);
    }

    @Test
    void validateEmailReturnsErrorForBlankEmail() {
        assertEquals("Field is required.", ErrorHandler.validateEmail(null));
        assertEquals("Field is required.", ErrorHandler.validateEmail(""));
        assertEquals("Field is required.", ErrorHandler.validateEmail("   "));
    }

    @Test
    void validateEmailReturnsErrorForInvalidEmail() {
        assertEquals(
                "Email format is invalid. Example: test@gmail.com.",
                ErrorHandler.validateEmail("invalid-email")
        );
    }

    @Test
    void validateEmailReturnsEmptyStringForValidEmail() {
        assertEquals("", ErrorHandler.validateEmail("test@gmail.com"));
    }

    @Test
    void validatePasswordReturnsErrorForBlankPassword() {
        assertEquals("Field is required.", ErrorHandler.validatePassword(null));
        assertEquals("Field is required.", ErrorHandler.validatePassword(""));
        assertEquals("Field is required.", ErrorHandler.validatePassword("   "));
    }

    @Test
    void validatePasswordReturnsErrorForWeakPassword() {
        assertEquals(
                "Password must be at least 8 characters long and include uppercase, lowercase, a number, and a special character.",
                ErrorHandler.validatePassword("password")
        );
    }

    @Test
    void validatePasswordReturnsEmptyStringForValidPassword() {
        assertEquals("", ErrorHandler.validatePassword("Password1!"));
    }

    @Test
    void validateFirstNameReturnsErrorForBlankName() {
        assertEquals("Name is required.", ErrorHandler.validateFirstName(null));
        assertEquals("Name is required.", ErrorHandler.validateFirstName(""));
    }

    @Test
    void validateFirstNameReturnsErrorForInvalidName() {
        assertEquals(
                "Name and Surname can contain only letters, spaces, hyphens, or apostrophes between words.",
                ErrorHandler.validateFirstName("John123")
        );
    }

    @Test
    void validateFirstNameReturnsEmptyStringForValidName() {
        assertEquals("", ErrorHandler.validateFirstName("John"));
        assertEquals("", ErrorHandler.validateFirstName("Anna-Maria"));
        assertEquals("", ErrorHandler.validateFirstName("O'Connor"));
    }

    @Test
    void validateLastNameReturnsErrorForBlankLastName() {
        assertEquals("Last name is required.", ErrorHandler.validateLastName(null));
        assertEquals("Last name is required.", ErrorHandler.validateLastName(""));
    }

    @Test
    void validateLastNameReturnsErrorForInvalidLastName() {
        assertEquals(
                "Name and Surname can contain only letters, spaces, hyphens, or apostrophes between words.",
                ErrorHandler.validateLastName("Smith123")
        );
    }

    @Test
    void validateLastNameReturnsEmptyStringForValidLastName() {
        assertEquals("", ErrorHandler.validateLastName("Smith"));
        assertEquals("", ErrorHandler.validateLastName("Novakova"));
    }

    @Test
    void comparePasswordsReturnsErrorWhenPasswordsDoNotMatch() {
        assertEquals(
                "Passwords do not match.",
                ErrorHandler.comparePasswords("Password1!", "Different1!")
        );
    }

    @Test
    void comparePasswordsReturnsEmptyStringWhenPasswordsMatch() {
        assertEquals("", ErrorHandler.comparePasswords("Password1!", "Password1!"));
    }

    @Test
    void validateRequiredReturnsErrorForBlankValue() {
        assertEquals("Field is required.", ErrorHandler.validateRequired(null));
        assertEquals("Field is required.", ErrorHandler.validateRequired(""));
        assertEquals("Field is required.", ErrorHandler.validateRequired("   "));
    }

    @Test
    void validateRequiredReturnsEmptyStringForFilledValue() {
        assertEquals("", ErrorHandler.validateRequired("Bratislava"));
    }

    @Test
    void validatePositiveDoubleReturnsErrorForBlankValue() {
        assertEquals("Field is required.", ErrorHandler.validatePositiveDouble(null));
        assertEquals("Field is required.", ErrorHandler.validatePositiveDouble(""));
    }

    @Test
    void validatePositiveDoubleReturnsErrorForNegativeValue() {
        assertEquals(
                "Input must be greater than 0.",
                ErrorHandler.validatePositiveDouble("-1.5")
        );
    }

    @Test
    void validatePositiveDoubleReturnsErrorForNonNumericValue() {
        assertEquals(
                "Input must be a number.",
                ErrorHandler.validatePositiveDouble("abc")
        );
    }

    @Test
    void validatePositiveDoubleReturnsEmptyStringForValidValue() {
        assertEquals("", ErrorHandler.validatePositiveDouble("0"));
        assertEquals("", ErrorHandler.validatePositiveDouble("12.5"));
    }

    @Test
    void validatePercentReturnsErrorForBlankValue() {
        assertEquals("Field is required.", ErrorHandler.validatePercent(null));
        assertEquals("Field is required.", ErrorHandler.validatePercent(""));
    }

    @Test
    void validatePercentReturnsErrorForValueOutsideRange() {
        assertEquals(
                "Input must be between 0.0 - 1.0",
                ErrorHandler.validatePercent("1.5")
        );
    }

    @Test
    void validatePercentReturnsEmptyStringForValidValue() {
        assertEquals("", ErrorHandler.validatePercent("0"));
        assertEquals("", ErrorHandler.validatePercent("0.75"));
        assertEquals("", ErrorHandler.validatePercent("1"));
    }

    @Test
    void validatePostalCodeReturnsErrorForBlankValue() {
        assertEquals("Field is required.", ErrorHandler.validatePostalCode(null));
        assertEquals("Field is required.", ErrorHandler.validatePostalCode(""));
    }

    @Test
    void validatePostalCodeReturnsErrorForInvalidValue() {
        assertEquals(
                "Invalid postal code format. Example: 080 01",
                ErrorHandler.validatePostalCode("abc")
        );
    }

    @Test
    void validatePostalCodeReturnsEmptyStringForValidValue() {
        assertEquals("", ErrorHandler.validatePostalCode("08001"));
        assertEquals("", ErrorHandler.validatePostalCode("080 01"));
    }
}