import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.shippin.controller.utils.InputValidator;

import static org.junit.jupiter.api.Assertions.*;

public class InputValidatorTest {

    // --- isValidLength ---

    @Property
    void lengthAboveMaxFails(@ForAll @StringLength(min = 251, max = 1000) String s) {
        assertFalse(InputValidator.isValidLength(s, 250));
    }

    @Property
    void lengthAtOrBelowMaxPasses(@ForAll @StringLength(min = 0, max = 250) String s) {
        assertTrue(InputValidator.isValidLength(s, 250));
    }

    @Example
    void nullFailsLengthCheck() {
        assertFalse(InputValidator.isValidLength(null, 250));
    }

    // --- isNotBlank ---

    @Example
    void nullIsBlank() {
        assertFalse(InputValidator.isNotBlank(null));
        assertFalse(InputValidator.isNotBlank(""));
        assertFalse(InputValidator.isNotBlank("   "));
        assertFalse(InputValidator.isNotBlank("\t\n"));
    }

    @Property
    void alphaStringIsNotBlank(@ForAll @AlphaChars @StringLength(min = 1) String s) {
        assertTrue(InputValidator.isNotBlank(s));
    }

    // --- passwordHasMinLength ---

    @Property
    void shortPasswordFailsMinLength(@ForAll @StringLength(min = 1, max = 7) String s) {
        assertFalse(InputValidator.passwordHasMinLength(s));
    }

    @Property
    void longEnoughPasswordPassesMinLength(@ForAll @StringLength(min = 8, max = 100) String s) {
        assertTrue(InputValidator.passwordHasMinLength(s));
    }

    @Example
    void nullFailsMinLength() {
        assertFalse(InputValidator.passwordHasMinLength(null));
    }

    // --- passwordHasLowercase ---

    @Property
    void uppercaseOnlyFailsLowercaseCheck(@ForAll @CharRange(from = 'A', to = 'Z') @StringLength(min = 1) String s) {
        assertFalse(InputValidator.passwordHasLowercase(s));
    }

    @Property
    void stringWithOnlyLowercasePasses(@ForAll @CharRange(from = 'a', to = 'z') @StringLength(min = 1) String s) {
        assertTrue(InputValidator.passwordHasLowercase(s));
    }

    @Example
    void nullFailsLowercaseCheck() {
        assertFalse(InputValidator.passwordHasLowercase(null));
    }

    // --- passwordHasUppercase ---

    @Property
    void lowercaseOnlyFailsUppercaseCheck(@ForAll @CharRange(from = 'a', to = 'z') @StringLength(min = 1) String s) {
        assertFalse(InputValidator.passwordHasUppercase(s));
    }

    @Property
    void stringWithOnlyUppercasePasses(@ForAll @CharRange(from = 'A', to = 'Z') @StringLength(min = 1) String s) {
        assertTrue(InputValidator.passwordHasUppercase(s));
    }

    @Example
    void nullFailsUppercaseCheck() {
        assertFalse(InputValidator.passwordHasUppercase(null));
    }

    // --- passwordHasDigit ---

    @Property
    void alphaOnlyFailsDigitCheck(@ForAll @AlphaChars @StringLength(min = 1) String s) {
        assertFalse(InputValidator.passwordHasDigit(s));
    }

    @Property
    void digitsOnlyPassesDigitCheck(@ForAll @NumericChars @StringLength(min = 1) String s) {
        assertTrue(InputValidator.passwordHasDigit(s));
    }

    @Example
    void nullFailsDigitCheck() {
        assertFalse(InputValidator.passwordHasDigit(null));
    }

    // --- passwordHasSpecial ---

    @Property
    void alphaOnlyFailsSpecialCheck(@ForAll @AlphaChars @StringLength(min = 1) String s) {
        assertFalse(InputValidator.passwordHasSpecial(s));
    }

    @Example
    void knownSpecialCharactersPass() {
        for (char c : "!@#$%^&*()_+-=[]{};':\"\\|,.<>/?".toCharArray()) {
            assertTrue(InputValidator.passwordHasSpecial("abc" + c), "Expected special char to pass: " + c);
        }
    }

    @Example
    void nullFailsSpecialCheck() {
        assertFalse(InputValidator.passwordHasSpecial(null));
    }

    // --- isValidPassword (combined) ---

    @Example
    void validPasswordPasses() {
        assertTrue(InputValidator.isValidPassword("Password1!"));
        assertTrue(InputValidator.isValidPassword("Str0ng@Pass"));
    }

    @Example
    void passwordMissingUppercaseFails() {
        assertFalse(InputValidator.isValidPassword("password1!"));
    }

    @Example
    void passwordMissingLowercaseFails() {
        assertFalse(InputValidator.isValidPassword("PASSWORD1!"));
    }

    @Example
    void passwordMissingDigitFails() {
        assertFalse(InputValidator.isValidPassword("Password!!"));
    }

    @Example
    void passwordMissingSpecialFails() {
        assertFalse(InputValidator.isValidPassword("Password1"));
    }

    @Example
    void passwordTooShortFails() {
        assertFalse(InputValidator.isValidPassword("Pa1!"));
    }

    @Example
    void nullPasswordFails() {
        assertFalse(InputValidator.isValidPassword(null));
    }

    // --- passwordsMatch ---

    @Property
    void sameStringAlwaysMatches(@ForAll String s) {
        assertTrue(InputValidator.passwordsMatch(s, s));
    }

    @Property
    void differentStringsDoNotMatch(@ForAll String a, @ForAll String b) {
        Assume.that(!a.equals(b));
        assertFalse(InputValidator.passwordsMatch(a, b));
    }

    @Example
    void nullPasswordDoesNotMatch() {
        assertFalse(InputValidator.passwordsMatch(null, "password"));
    }

    // --- isValidEmail ---

    @Example
    void validEmailsPass() {
        assertTrue(InputValidator.isValidEmail("user@example.com"));
        assertTrue(InputValidator.isValidEmail("user.name+tag@sub.domain.org"));
        assertTrue(InputValidator.isValidEmail("x@y.co"));
    }

    @Example
    void invalidEmailsFail() {
        assertFalse(InputValidator.isValidEmail("userexample.com"));
        assertFalse(InputValidator.isValidEmail("user@"));
        assertFalse(InputValidator.isValidEmail("@domain.com"));
        assertFalse(InputValidator.isValidEmail("user@domain"));
    }

    @Example
    void nullEmailFails() {
        assertFalse(InputValidator.isValidEmail(null));
    }

    @Property
    void pureAlphaStringFailsEmailCheck(@ForAll @AlphaChars @StringLength(min = 1, max = 50) String s) {
        assertFalse(InputValidator.isValidEmail(s));
    }
}
