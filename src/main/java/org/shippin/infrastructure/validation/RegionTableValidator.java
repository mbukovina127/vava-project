package org.shippin.infrastructure.validation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;

import org.shippin.exception.ValidationException;

import static org.shippin.infrastructure.validation.ValidationMessages.msg;

public class RegionTableValidator {

    private static final Pattern RANGE_PATTERN = Pattern.compile("^\\d{5}-\\d{5}$");

    public static void validate(Map<String, List<String>> regionMap,
                                List<String> regionCodesInOrder,
                                ResourceBundle resources) throws ValidationException {
        // 1. Check for duplicate region codes
        Set<String> seen = new LinkedHashSet<>();
        for (String code : regionCodesInOrder) {
            if (!seen.add(code)) {
                throw new ValidationException(List.of(
                        msg("csv.price_list.duplicate_zone_column", code)
                ));
            }
        }

        List<String> errors = new ArrayList<>();

        if (regionMap == null || regionMap.isEmpty()) {
            throw new ValidationException(List.of(
                    msg("validator.region_table.empty")
            ));
        }

        for (Map.Entry<String, List<String>> entry : regionMap.entrySet()) {
            String zone = entry.getKey();

            if (zone == null || zone.isBlank()) {
                errors.add(msg("validator.region_table.blank_zone_name"));
                continue;
            }

            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                errors.add(msg("validator.region_table.zone_no_entries",
                        zone));
                continue;
            }

            for (String range : entry.getValue()) {
                if (!RANGE_PATTERN.matcher(range).matches()) {
                    errors.add(msg("validator.region_table.invalid_range",
                            zone,
                            range));
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
