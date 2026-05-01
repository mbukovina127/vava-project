package org.shippin.services;

import org.shippin.domain.CoreWarehouseInfo;
import org.shippin.domain.PriceList;
import org.shippin.domain.RegionTable;
import org.shippin.domain.SmallPriceList;
import org.shippin.domain.Warehouse;
import org.shippin.domain.formatted.PriceListFormatted;
import org.shippin.domain.formatted.RegionTableFormatted;
import org.shippin.util.WarehouseConvertor;
import org.shippin.domain.BriefWarehouse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.SQLException;
import java.util.List;

import org.shippin.database.dao.PriceListDAO;
import org.shippin.database.dao.RegionDAO;
import org.shippin.database.dao.WarehouseDAO;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseService {
	
	private WarehouseDAO warehouseDao;
	private PriceListDAO priceListDao;
	private RegionDAO regionDao;
	
	public void replacePriceList(PriceListFormatted priceListFormatted, Warehouse warehouse) {
		PriceList priceList = WarehouseConvertor.convertPriceList(priceListFormatted);
		try {
			priceListDao.deletePriceListByWarehouseID(warehouse.getId());
			priceListDao.insertPriceList(priceList, warehouse.getRegionName());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void replaceRegionTable(RegionTableFormatted regionTableFormatted, Warehouse warehouse) {
		RegionTable regionTable = WarehouseConvertor.convertRegionTable(regionTableFormatted);
		try {
			regionDao.deleteFullRegionTable(warehouse.getId());
			regionDao.insertFullRegionTable(regionTable, warehouse);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateWarehouse(Warehouse warehouse, String name, String regionName) {
		warehouse.setName(name);
		warehouse.setRegionName(regionName);
		try {
			warehouseDao.updateWarehouse(warehouse);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addWarehouse(String name, String regionName, PriceListFormatted priceListFormatted, RegionTableFormatted regionTableFormatted) {
		PriceList priceList = WarehouseConvertor.convertPriceList(priceListFormatted);
		RegionTable regionTable = WarehouseConvertor.convertRegionTable(regionTableFormatted);
		Warehouse warehouse = new Warehouse(name, regionName, priceList, regionTable);
		
		try {
			warehouseDao.insertFullWarehouse(warehouse);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void deleteWarehouse(CoreWarehouseInfo warehouseInfo) {
		try {
			warehouseDao.deleteFullWarehouse(warehouseInfo.getId());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<BriefWarehouse> getBriefWarehouses(CoreWarehouseInfo warehouseInfo) {
		try {
			return warehouseDao.getAllBriefWarehouses();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public Warehouse getWarehouse(BriefWarehouse briefWarehouse) {
		try {
			return warehouseDao.getById(briefWarehouse.getId());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public SmallPriceList getSmallPackagePriceList(SmallPriceList smallPackagePriceList) {
		return priceListDao.get
	}
}
