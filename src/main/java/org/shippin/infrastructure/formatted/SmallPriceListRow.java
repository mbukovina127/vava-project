package org.shippin.infrastructure.formatted;

import org.shippin.infrastructure.table.Row;

public class SmallPriceListRow implements Row {
    private float weight;
    private float cost;

    public SmallPriceListRow() {}

    public SmallPriceListRow(float weight, float cost) {
        this.weight = weight;
        this.cost = cost;
    }

    public float getWeight() { return weight; }
    public void setWeight(float weight) { this.weight = weight; }
    public float getCost() { return cost; }
    public void setCost(float cost) { this.cost = cost; }
}