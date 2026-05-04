package org.shippin.infrastructure.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.shippin.exception.ValidationException;

public class PriceListValidator {

    /**
     * Validates price list rows. Call after the raw strings have been split into
     * per-row (hmotnost, objem, regionPrices) structures, before building domain objects.
     *
     * @param hmotnostList  raw hmotnost strings, one per data row
     * @param objemList     raw objem strings, one per data row
     * @param regionColumns map from zone code → list of raw price strings (one per data row)
     * @throws ValidationException with all found errors if validation fails
     */
    public static void validate(List<String> hmotnostList,
                                List<String> objemList,
                                Map<String, List<String>> regionColumns) throws ValidationException {
        List<String> errors = new ArrayList<>();

        // at least one data row
        if (hmotnostList == null || hmotnostList.isEmpty()) {
            throw new ValidationException(List.of("Price list has no data rows."));
        }

        // at least one zone column present
        if (regionColumns == null || regionColumns.isEmpty()) {
            errors.add("At least one zone column must be present.");
        }

        // every row must have numeric hmotnost and objem
        for (int i = 0; i < hmotnostList.size(); i++) {
            int rowNum = i + 1;
            if (!isNumeric(hmotnostList.get(i))) {
                errors.add("Row " + rowNum + ": Hmotnosť \"" + hmotnostList.get(i) + "\" is not a valid number.");
            }
            if (!isNumeric(objemList.get(i))) {
                errors.add("Row " + rowNum + ": Objem \"" + objemList.get(i) + "\" is not a valid number.");
            }
        }

        // every present zone column must have at least one non-zero entry
        if (regionColumns != null) {
            for (Map.Entry<String, List<String>> entry : regionColumns.entrySet()) {
                String zone = entry.getKey();
                if (zone == null || zone.isBlank()) {
                    errors.add("Zone column has an empty or blank name.");
                    continue;
                }
                boolean hasEntry = entry.getValue().stream()
                        .anyMatch(v -> isNumeric(v) && Float.parseFloat(v.replace(',', '.')) > 0f);
                if (!hasEntry) {
                    errors.add("%validator.zone " + zone + " %validator.only_zero_entries");
                }
            }
        }

        if (!errors.isEmpty()) throw new ValidationException(errors);
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
