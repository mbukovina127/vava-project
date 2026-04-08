package org.shippin.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Range {
    private int min;
    private int max;

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public Range(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public boolean contains(int n) {
        return false;
    }
}
