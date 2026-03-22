package org.shippin.util;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
public class Range {
    private int min;
    private int max;

    public Range(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min must be <= max");
        }
        this.min = min;
        this.max = max;
    }

    public boolean contains(int value) {
        return value >= min && value <= max;
    }

    @Override
    public String toString() {
        return min + "-" + max;
    }
}
