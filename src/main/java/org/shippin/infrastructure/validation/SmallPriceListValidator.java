package org.shippin.infrastructure.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.shippin.exception.ValidationException;

import static org.shippin.infrastructure.validation.ValidationMessages.msg;

public class SmallPriceListValidator {

    public static void validate(String headerCol0,
                                String headerCol1,
                                List<String> weightDescs,
                                List<String> priceStrs,
                                ResourceBundle resources) throws ValidationException {
        List<String> errors = new ArrayList<>();

        if (isNullOrBlank(headerCol0) || isNumeric(headerCol0)) {
            errors.add(msg("validator.small_price_list.header_first_invalid",
                    headerCol0));
        }

        if (isNullOrBlank(headerCol1) || isNumeric(headerCol1)) {
            errors.add(msg("validator.small_price_list.header_second_invalid",
                    headerCol1));
        }

        if (weightDescs == null || weightDescs.isEmpty()) {
            errors.add(msg("validator.small_price_list.no_data_rows"));
            throw new ValidationException(errors);
        }

        for (int i = 0; i < weightDescs.size(); i++) {
            int rowNum = i + 2;
            String col0 = weightDescs.get(i);
            String col1 = priceStrs.get(i);

            if (isNullOrBlank(col0)) {
                errors.add(msg("validator.small_price_list.empty_weight_description",
                        rowNum));
            }

            if (isNullOrBlank(col1)) {
                errors.add(msg("validator.small_price_list.missing_price",
                        rowNum));
            } else if (!isNumeric(col1)) {
                errors.add(msg("validator.small_price_list.invalid_price",
                        rowNum,
                        col1));
            } else {
                float price = Float.parseFloat(col1.replace(',', '.'));

                if (price <= 0f) {
                    errors.add(msg("validator.small_price_list.price_must_be_positive",
                            rowNum,
                            price));
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private static boolean isNullOrBlank(String s) {
        return s == null || s.isBlank();
    }

    private static boolean isNumeric(String s) {
        if (s == null || s.isBlank()) return false;

        try {
            Float.parseFloat(s.replace(',', '.'));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
