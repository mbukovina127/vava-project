package org.shippin.infrastructure.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.shippin.exception.ValidationException;

public class RegionTableValidator {

    private static final Pattern RANGE_PATTERN = Pattern.compile("^\\d{5}-\\d{5}$");

    /**
     * Validates region data. Call once the raw string ranges have been collected,
     * before converting to domain objects.
     *
     * @param regionMap  region code → list of raw range strings (e.g. "81000-85999")
     * @throws ValidationException with all found errors if validation fails
     */
    public static void validate(Map<String, List<String>> regionMap) throws ValidationException {
        List<String> errors = new ArrayList<>();

        if (regionMap == null || regionMap.isEmpty()) {
            throw new ValidationException(List.of("Region table is empty — no data found."));
        }

        for (Map.Entry<String, List<String>> entry : regionMap.entrySet()) {
            String zone = entry.getKey();
            if (zone == null || zone.isBlank()) {
                errors.add("A zone has an empty or blank name.");
                continue;
            }
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                errors.add("Zone " + zone + " is present but has no entries.");
                continue;
            }
            for (String range : entry.getValue()) {
                if (!RANGE_PATTERN.matcher(range).matches()) {
                    errors.add("Invalid range in zone " + zone
                            + ": \"" + range + "\" — expected XXXXX-YYYYY.");
                }
            }
        }

        if (!errors.isEmpty()) throw new ValidationException(errors);
    }
}
