import org.junit.jupiter.api.Test;
import org.shippin.controller.utils.ErrorHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ErrorHandlerTest {

    @Test
    void validateEmailReturnsErrorForBlankEmail() {
        assertEquals("Email is required.", ErrorHandler.validateEmail(null));
        assertEquals("Email is required.", ErrorHandler.validateEmail(""));
        assertEquals("Email is required.", ErrorHandler.validateEmail("   "));
    }

    @Test
    void validateEmailReturnsErrorForInvalidEmail() {
        assertEquals(
                "Email format is invalid. Example: test@gmail.com",
                ErrorHandler.validateEmail("invalid-email")
        );
    }

    @Test
    void validateEmailReturnsEmptyStringForValidEmail() {
        assertEquals("", ErrorHandler.validateEmail("test@gmail.com"));
    }

    @Test
    void validatePasswordReturnsErrorForBlankPassword() {
        assertEquals("Password is required.", ErrorHandler.validatePassword(null));
        assertEquals("Password is required.", ErrorHandler.validatePassword(""));
        assertEquals("Password is required.", ErrorHandler.validatePassword("   "));
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
    void validateShipmentTypeReturnsErrorForBlankValue() {
        assertEquals("Select shipment type.", ErrorHandler.validateShipmentType(null));
        assertEquals("Select shipment type.", ErrorHandler.validateShipmentType(""));
    }

    @Test
    void validateShipmentTypeReturnsEmptyStringForSelectedValue() {
        assertEquals("", ErrorHandler.validateShipmentType("Package"));
    }

    @Test
    void validateRequiredReturnsErrorForBlankValue() {
        assertEquals("Destination is required.", ErrorHandler.validateRequired(null, "Destination"));
        assertEquals("Destination is required.", ErrorHandler.validateRequired("", "Destination"));
    }

    @Test
    void validateRequiredReturnsEmptyStringForFilledValue() {
        assertEquals("", ErrorHandler.validateRequired("Bratislava", "Destination"));
    }

    @Test
    void validatePositiveDoubleReturnsErrorForBlankValue() {
        assertEquals("Weight is required.", ErrorHandler.validatePositiveDouble(null, "Weight"));
        assertEquals("Weight is required.", ErrorHandler.validatePositiveDouble("", "Weight"));
    }

    @Test
    void validatePositiveDoubleReturnsErrorForNegativeValue() {
        assertEquals(
                "Weight must be greater than or equal to 0.",
                ErrorHandler.validatePositiveDouble("-1.5", "Weight")
        );
    }

    @Test
    void validatePositiveDoubleReturnsErrorForNonNumericValue() {
        assertEquals(
                "Weight must be a number.",
                ErrorHandler.validatePositiveDouble("abc", "Weight")
        );
    }

    @Test
    void validatePositiveDoubleReturnsEmptyStringForValidValue() {
        assertEquals("", ErrorHandler.validatePositiveDouble("0", "Weight"));
        assertEquals("", ErrorHandler.validatePositiveDouble("12.5", "Weight"));
        assertEquals("", ErrorHandler.validatePositiveDouble(" 12.5 ", "Weight"));
    }

    @Test
    void validatePositiveIntegerReturnsErrorForBlankValue() {
        assertEquals("Volume is required.", ErrorHandler.validatePositiveInteger(null, "Volume"));
        assertEquals("Volume is required.", ErrorHandler.validatePositiveInteger("", "Volume"));
    }

    @Test
    void validatePositiveIntegerReturnsErrorForNegativeValue() {
        assertEquals(
                "Volume must be greater than or equal to 0.",
                ErrorHandler.validatePositiveInteger("-1", "Volume")
        );
    }

    @Test
    void validatePositiveIntegerReturnsErrorForDecimalValue() {
        assertEquals(
                "Volume must be an integer.",
                ErrorHandler.validatePositiveInteger("12.5", "Volume")
        );
    }

    @Test
    void validatePositiveIntegerReturnsErrorForNonNumericValue() {
        assertEquals(
                "Volume must be an integer.",
                ErrorHandler.validatePositiveInteger("abc", "Volume")
        );
    }

    @Test
    void validatePositiveIntegerReturnsEmptyStringForValidValue() {
        assertEquals("", ErrorHandler.validatePositiveInteger("0", "Volume"));
        assertEquals("", ErrorHandler.validatePositiveInteger("12", "Volume"));
        assertEquals("", ErrorHandler.validatePositiveInteger(" 12 ", "Volume"));
    }
}