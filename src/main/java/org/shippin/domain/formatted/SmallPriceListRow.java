package org.shippin.domain.formatted;

import org.shippin.domain.Row;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class SmallPriceListRow implements Row {
    private float weight;
    private float cost;
}