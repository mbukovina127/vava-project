package org.shippin.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceListEntry implements EntityEntry {
    private int id;
    private float weight;
    private float volume;
    private float cost;
    private String zone;

    public PriceListEntry(int id, float weight, float volume, float cost, String zone) {
        this.id = id;
        this.weight = weight;
        this.volume = volume;
        this.cost = cost;
        this.zone = zone;
    }

    public int getId() {
        return id;
    }

    public float getWeight() {
        return weight;
    }

    public float getVolume() {
        return volume;
    }

    public String getZone() {
        return zone;
    }

    public float getCost() {
        return cost;
    }
}
