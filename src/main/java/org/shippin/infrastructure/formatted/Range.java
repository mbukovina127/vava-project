package org.shippin.infrastructure.formatted;

public class Range {
    private float min;
    private float max;

    public Range() {}

    public Range(float min, float max) {
        this.min = min;
        this.max = max;
    }

    public float getMin() { return min; }
    public void setMin(float min) { this.min = min; }
    public float getMax() { return max; }
    public void setMax(float max) { this.max = max; }
}
