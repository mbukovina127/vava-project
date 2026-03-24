package org.shippin.app.models;

public class PriceListItem {
    private float weight;
    private float volume;
    private float cost;
    private String region;

    public PriceListItem(float weight, float volume, float cost, String region) {
        this.weight = weight;
        this.volume = volume;
        this.cost = cost;
        this.region = region;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
