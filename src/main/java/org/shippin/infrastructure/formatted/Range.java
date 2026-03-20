package org.shippin.infrastructure.formatted;

public class Range {
    private int min;
    private int max;

    public Range() {
        this.min = 0;
        this.max = 0;
    }

    public Range(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min must be <= max");
        }
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
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
