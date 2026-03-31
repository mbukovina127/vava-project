package org.shippin.util;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class NumberUtils {
    public static  float parseFloat(String s) {
        if (s == null || s.trim().isEmpty()) return 0f;
        String normalized = s.trim().replace(',', '.').replaceAll("[^0-9.\\-]", "");
        try {
            return Float.parseFloat(normalized);
        } catch (NumberFormatException e) {
            return 0f;
        }
    }

    public static  String formatFloat(float f) {
        String s = String.format("%.2f", f);
        return s.replace('.', ',');
    }
}
