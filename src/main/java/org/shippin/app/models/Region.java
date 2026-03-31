package org.shippin.app.models;

import java.util.ArrayList;
import java.util.List;

public class Region {
    private String code;
    private ArrayList<Integer> psc;

    public Region(String code) {
        this.code = code;
        this.psc = new ArrayList<>();
    }

    public void addZoneRange(String range){
        String[] parts;
        parts = range.split("-");
        int start = Integer.valueOf(parts[0]);
        int end = Integer.valueOf(parts[1]);
        for (int i = start; i <= end; i++) {
            this.psc.add(i);
        }
    }

    public void addPsc(String Psc){
        this.psc.add(Integer.valueOf(Psc));
    }

    public List<Integer> getPscList(){
        return new ArrayList<>(psc);
    }

    public boolean isInRange(int psc){
        return this.psc.contains(psc);
    }

    public boolean isInRange(String psc){
        return this.psc.contains(Integer.valueOf(psc));
    }



    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
