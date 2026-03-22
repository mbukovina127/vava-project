package org.shippin.domain.formatted;

import org.shippin.domain.Row;
import java.util.LinkedHashMap;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceListRow implements Row {
    private float weight;
    private float volume;
    private LinkedHashMap<String, Float> regions = new LinkedHashMap<>();

    public PriceListRow(float weight, float volume) {
        this.weight = weight;
        this.volume = volume;
    }

    public float getWeight() { return weight; }
    public void setWeight(float weight) { this.weight = weight; }
    public float getVolume() { return volume; }
    public void setVolume(float volume) { this.volume = volume; }
    public LinkedHashMap<String, Float> getRegions() { return regions; }
    public void setRegions(LinkedHashMap<String, Float> regions) { this.regions = regions; }
}