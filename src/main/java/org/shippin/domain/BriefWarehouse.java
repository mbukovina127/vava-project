package org.shippin.domain;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BriefWarehouse extends CoreWarehouseInfo {
	public BriefWarehouse(int id, String name, String regionName, int postalCode, Coordinates coordinates) {
		super(id, name, regionName, postalCode, coordinates);
  }
  
	public BriefWarehouse(int id, String name, String regionName) {
		super(id, name, regionName, 0);
	}

	public BriefWarehouse(int id, String name, String regionName, int postalCode) {
		super(id, name, regionName, postalCode);
	}
}
