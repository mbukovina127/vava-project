package org.shippin.domain;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BriefWarehouse extends CoreWarehouseInfo {
	public BriefWarehouse(int id, String name, String regionName, int postalCode) {
		super(id, name, regionName, postalCode);
	}
}
