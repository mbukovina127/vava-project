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
    public boolean contains(int n) {
        return false;
    }
}
