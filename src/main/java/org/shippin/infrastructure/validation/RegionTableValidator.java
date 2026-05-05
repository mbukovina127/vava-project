package org.shippin.infrastructure.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.shippin.exception.ValidationException;

import static org.shippin.infrastructure.validation.ValidationMessages.msg;

public class RegionTableValidator {

    private static final Pattern RANGE_PATTERN = Pattern.compile("^\\d{5}-\\d{5}$");

    public static void validate(Map<String, List<String>> regionMap,
                                ResourceBundle resources) throws ValidationException {
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
