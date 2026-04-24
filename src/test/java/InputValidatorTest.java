import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.shippin.controller.utils.InputValidator;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class InputValidatorTest {

    private static final Random RANDOM = new Random(25);

    // isValidLength

    @RepeatedTest(20)
    void lengthAboveMaxFails() {
        String value = randomString(randomInt(251, 1000));

        assertFalse(InputValidator.isValidLength(value, 250));
    }

    @RepeatedTest(20)
    void lengthAtOrBelowMaxPasses() {
        String value = randomString(randomInt(0, 250));

        assertTrue(InputValidator.isValidLength(value, 250));
    }

    @Test
    void nullFailsLengthCheck() {
        assertFalse(InputValidator.isValidLength(null, 250));
    }

    // isNotBlank

    @Test
    void nullAndBlankValuesAreBlank() {
        assertFalse(InputValidator.isNotBlank(null));
        assertFalse(InputValidator.isNotBlank(""));
        assertFalse(InputValidator.isNotBlank("   "));
        assertFalse(InputValidator.isNotBlank("\t\n"));
    }

    @RepeatedTest(20)
    void alphaStringIsNotBlank() {
        String value = randomAlphaString(randomInt(1, 100));

        assertTrue(InputValidator.isNotBlank(value));
    }

    // passwordHasMinLength

    @RepeatedTest(20)
    void shortPasswordFailsMinLength() {
        String password = randomString(randomInt(1, 7));

        assertFalse(InputValidator.passwordHasMinLength(password));
    }

    @RepeatedTest(20)
    void longEnoughPasswordPassesMinLength() {
        String password = randomString(randomInt(8, 100));

        assertTrue(InputValidator.passwordHasMinLength(password));
    }

    @Test
    void nullFailsMinLength() {
        assertFalse(InputValidator.passwordHasMinLength(null));
    }

    // passwordHasLowercase

    @RepeatedTest(20)
    void uppercaseOnlyFailsLowercaseCheck() {
        String password = randomUppercaseString(randomInt(1, 100));

        assertFalse(InputValidator.passwordHasLowercase(password));
    }

    @RepeatedTest(20)
    void stringWithOnlyLowercasePassesLowercaseCheck() {
        String password = randomLowercaseString(randomInt(1, 100));

        assertTrue(InputValidator.passwordHasLowercase(password));
    }

    @Test
    void nullFailsLowercaseCheck() {
        assertFalse(InputValidator.passwordHasLowercase(null));
    }

    // passwordHasUppercase

    @RepeatedTest(20)
    void lowercaseOnlyFailsUppercaseCheck() {
        String password = randomLowercaseString(randomInt(1, 100));

        assertFalse(InputValidator.passwordHasUppercase(password));
    }

    @RepeatedTest(20)
    void stringWithOnlyUppercasePassesUppercaseCheck() {
        String password = randomUppercaseString(randomInt(1, 100));

        assertTrue(InputValidator.passwordHasUppercase(password));
    }

    @Test
    void nullFailsUppercaseCheck() {
        assertFalse(InputValidator.passwordHasUppercase(null));
    }

    // passwordHasDigit

    @RepeatedTest(20)
    void alphaOnlyFailsDigitCheck() {
        String password = randomAlphaString(randomInt(1, 100));

        assertFalse(InputValidator.passwordHasDigit(password));
    }

    @RepeatedTest(20)
    void digitsOnlyPassesDigitCheck() {
        String password = randomDigitString(randomInt(1, 100));

        assertTrue(InputValidator.passwordHasDigit(password));
    }

    @Test
    void nullFailsDigitCheck() {
        assertFalse(InputValidator.passwordHasDigit(null));
    }

    // passwordHasSpecial

    @RepeatedTest(20)
    void alphaOnlyFailsSpecialCheck() {
        String password = randomAlphaString(randomInt(1, 100));

        assertFalse(InputValidator.passwordHasSpecial(password));
    }

    @Test
    void knownSpecialCharactersPass() {
        char[] specialCharacters = "!@#$%^&*()_+-=[]{};':\"\\|,.<>/?".toCharArray();

        for (char character : specialCharacters) {
            assertTrue(
                    InputValidator.passwordHasSpecial("abc" + character),
                    "Expected special char to pass: " + character
            );
        }
    }

    @Test
    void nullFailsSpecialCheck() {
        assertFalse(InputValidator.passwordHasSpecial(null));
    }

    // isValidPassword

    @Test
    void validPasswordPasses() {
        assertTrue(InputValidator.isValidPassword("Password1!"));
        assertTrue(InputValidator.isValidPassword("Str0ng@Pass"));
    }

    @Test
    void passwordMissingUppercaseFails() {
        assertFalse(InputValidator.isValidPassword("password1!"));
    }

    @Test
    void passwordMissingLowercaseFails() {
        assertFalse(InputValidator.isValidPassword("PASSWORD1!"));
    }

    @Test
    void passwordMissingDigitFails() {
        assertFalse(InputValidator.isValidPassword("Password!!"));
    }

    @Test
    void passwordMissingSpecialFails() {
        assertFalse(InputValidator.isValidPassword("Password1"));
    }

    @Test
    void passwordTooShortFails() {
        assertFalse(InputValidator.isValidPassword("Pa1!"));
    }

    @Test
    void nullPasswordFails() {
        assertFalse(InputValidator.isValidPassword(null));
    }

    // passwordsMatch

    @RepeatedTest(20)
    void sameStringAlwaysMatches() {
        String password = randomString(randomInt(0, 100));

        assertTrue(InputValidator.passwordsMatch(password, password));
    }

    @RepeatedTest(20)
    void differentStringsDoNotMatch() {
        String firstPassword = "A" + randomString(randomInt(1, 100));
        String secondPassword = "B" + randomString(randomInt(1, 100));

        assertFalse(InputValidator.passwordsMatch(firstPassword, secondPassword));
    }

    @Test
    void nullPasswordDoesNotMatch() {
        assertFalse(InputValidator.passwordsMatch(null, "password"));
    }

    // isValidEmail

    @Test
    void validEmailsPass() {
        assertTrue(InputValidator.isValidEmail("user@example.com"));
        assertTrue(InputValidator.isValidEmail("user.name+tag@sub.domain.org"));
        assertTrue(InputValidator.isValidEmail("x@y.co"));
    }

    @Test
    void invalidEmailsFail() {
        assertFalse(InputValidator.isValidEmail("userexample.com"));
        assertFalse(InputValidator.isValidEmail("user@"));
        assertFalse(InputValidator.isValidEmail("@domain.com"));
        assertFalse(InputValidator.isValidEmail("user@domain"));
    }

    @Test
    void nullEmailFails() {
        assertFalse(InputValidator.isValidEmail(null));
    }

    @RepeatedTest(20)
    void pureAlphaStringFailsEmailCheck() {
        String value = randomAlphaString(randomInt(1, 50));

        assertFalse(InputValidator.isValidEmail(value));
    }

    // helper methods

    private static int randomInt(int min, int max) {
        return RANDOM.nextInt(max - min + 1) + min;
    }

    private static String randomString(int length) {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        return randomFromCharacters(length, characters);
    }

    private static String randomAlphaString(int length) {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        return randomFromCharacters(length, characters);
    }

    private static String randomLowercaseString(int length) {
        String characters = "abcdefghijklmnopqrstuvwxyz";
        return randomFromCharacters(length, characters);
    }

    private static String randomUppercaseString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        return randomFromCharacters(length, characters);
    }

    private static String randomDigitString(int length) {
        String characters = "0123456789";
        return randomFromCharacters(length, characters);
    }

    private static String randomFromCharacters(int length, String characters) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(characters.length());
            result.append(characters.charAt(index));
        }

        return result.toString();
    }
}