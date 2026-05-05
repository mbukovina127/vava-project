package org.shippin.infrastructure.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.shippin.exception.ValidationException;

import static org.shippin.infrastructure.validation.ValidationMessages.msg;

public class PriceListValidator {

    public static void validate(List<String> hmotnostList,
                                List<String> objemList,
                                Map<String, List<String>> regionColumns,
                                ResourceBundle resources) throws ValidationException {
        List<String> errors = new ArrayList<>();

        if (hmotnostList == null || hmotnostList.isEmpty()) {
            throw new ValidationException(List.of(
                    msg("validator.price_list.no_data_rows")
            ));
        }

        if (regionColumns == null || regionColumns.isEmpty()) {
            errors.add(msg("validator.price_list.no_zone_columns"));
        }

        for (int i = 0; i < hmotnostList.size(); i++) {
            int rowNum = i + 1;

            if (!isNumeric(hmotnostList.get(i))) {
                errors.add(msg("validator.price_list.invalid_weight",
                        rowNum,
                        hmotnostList.get(i)));
            }

            if (!isNumeric(objemList.get(i))) {
                errors.add(msg("validator.price_list.invalid_volume",
                        rowNum,
                        objemList.get(i)));
            }
        }

        if (regionColumns != null) {
            for (Map.Entry<String, List<String>> entry : regionColumns.entrySet()) {
                String zone = entry.getKey();

                if (zone == null || zone.isBlank()) {
                    errors.add(msg("validator.price_list.blank_zone_name"));
                    continue;
                }

                boolean hasEntry = entry.getValue().stream()
                        .anyMatch(v -> isNumeric(v) && Float.parseFloat(v.replace(',', '.')) > 0f);

                if (!hasEntry) {
                    errors.add(msg("validator.price_list.zone_only_zero_entries",
                            zone));
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private static boolean isNumeric(String value) {
        if (value == null || value.isBlank()) return false;

        try {
            Float.parseFloat(value.replace(',', '.'));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
