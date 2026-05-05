package org.shippin.infrastructure.validation;

import java.util.ArrayList;
import java.util.List;

import org.shippin.exception.ValidationException;

public class SmallPriceListValidator {

    /**
     * Validates small price list rows. Call after splitting lines but before
     * building domain objects.
     *
     * @param headerCol0  first header cell (expected: "Hmotnosť")
     * @param headerCol1  second header cell (expected: "Cena")
     * @param weightDescs raw weight description strings, one per data row
     * @param priceStrs   raw price strings, one per data row
     * @throws ValidationException with all found errors if validation fails
     */
    public static void validate(String headerCol0, String headerCol1,
                                List<String> weightDescs, List<String> priceStrs) throws ValidationException {
        List<String> errors = new ArrayList<>();

        // header cells must be non-empty strings (not numbers)
        if (isNullOrBlank(headerCol0) || isNumeric(headerCol0)) {
            errors.add("Header row: first column must be a non-numeric string, got \""
                    + headerCol0 + "\".");
        }
        if (isNullOrBlank(headerCol1) || isNumeric(headerCol1)) {
            errors.add("Header row: second column must be a non-numeric string, got \""
                    + headerCol1 + "\".");
        }

        // at least one data row
        if (weightDescs == null || weightDescs.isEmpty()) {
            errors.add("Small price list must have at least one data row.");
            if (!errors.isEmpty()) throw new ValidationException(errors);
        }

        // per-row checks
        for (int i = 0; i < weightDescs.size(); i++) {
            int rowNum = i + 2; // 1-based + 1 for header
            String col0 = weightDescs.get(i);
            String col1 = priceStrs.get(i);

            if (isNullOrBlank(col0)) {
                errors.add("Row " + rowNum + ": weight description (column 1) is empty.");
            }

            if (isNullOrBlank(col1)) {
                errors.add("Row " + rowNum + ": price (column 2) is missing.");
            } else if (!isNumeric(col1)) {
                errors.add("Row " + rowNum + ": price \"" + col1 + "\" is not a valid number.");
            } else {
                float price = Float.parseFloat(col1.replace(',', '.'));
                if (price <= 0f) {
                    errors.add("Row " + rowNum + ": price must be > 0, got " + price + ".");
                }
            }
        }

        if (!errors.isEmpty()) throw new ValidationException(errors);
    }

    private static boolean isNullOrBlank(String s) {
        return s == null || s.isBlank();
    }

    private static boolean isNumeric(String s) {
        try {
            Float.parseFloat(s.replace(',', '.'));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
