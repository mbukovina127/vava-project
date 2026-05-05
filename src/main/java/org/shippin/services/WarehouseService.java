package org.shippin.services;

import org.shippin.domain.CoreWarehouseInfo;
import org.shippin.domain.PriceList;
import org.shippin.domain.RegionTable;
import org.shippin.domain.SmallPriceList;
import org.shippin.domain.Warehouse;
import org.shippin.domain.formatted.PriceListFormatted;
import org.shippin.domain.formatted.RegionTableFormatted;
import org.shippin.domain.formatted.SmallPriceListFormatted;
import org.shippin.domain.formatted.WarehouseFormatted;
import org.shippin.exception.IncompatibleTablesException;
import org.shippin.util.WarehouseConvertor;
import org.shippin.domain.BriefWarehouse;
import org.shippin.domain.Coordinates;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.sql.SQLException;
import java.util.List;

import org.shippin.database.dao.PriceListDAO;
import org.shippin.database.dao.RegionDAO;
import org.shippin.database.dao.WarehouseDAO;

@Log4j2
@Data
@AllArgsConstructor
public class WarehouseService {
	
	private static WarehouseService instance;
	
	private WarehouseDAO warehouseDao;
	private PriceListDAO priceListDao;
	private RegionDAO regionDao;
	
	public WarehouseService() {
		this.warehouseDao = WarehouseDAO.getInstance();
		this.priceListDao = PriceListDAO.getInstance();
		this.regionDao = RegionDAO.getInstance();
	}
	
	public WarehouseFormatted getWarehouseFormatted(BriefWarehouse briefWarehouse) throws SQLException {
		return WarehouseConvertor.toWarehouseFormatted(this.getWarehouse(briefWarehouse));
	}
	
	public static WarehouseService getInstance() {
		if (instance == null) {
            instance = new WarehouseService();
        }
        return instance;
    }
	
	public void updateWarehouse(BriefWarehouse briefWarehouse, String name, String regionName, int postalCode) throws Exception {
		Warehouse warehouse = this.getWarehouse(briefWarehouse);
		warehouse.setName(name);
		warehouse.setRegionName(regionName);
		warehouse.setPostalCode(postalCode);

		double[] coordinatesDouble = MapService.getInstance().fetchCoordinatesForPostalCode(postalCode);
		Coordinates coordinates = new Coordinates(coordinatesDouble[0], coordinatesDouble[1]);

		warehouse.setCoord(coordinates);

		warehouseDao.updateWarehouse(warehouse);
		log.info("Updated warehouse #{} — name='{}', region='{}', postalCode={}", warehouse.getId(), name, regionName, postalCode);
	}
	
	public void addWarehouse(String name, String regionName, int postalCode, PriceListFormatted priceListFormatted, RegionTableFormatted regionTableFormatted) throws Exception {
		PriceList priceList = WarehouseConvertor.convertPriceList(priceListFormatted);
		RegionTable regionTable = WarehouseConvertor.convertRegionTable(regionTableFormatted);
		
		boolean compatibleTables = WarehouseParsingService.getInstance()
				.checkTableCompatibility(priceList, regionTable);
		
		double[] coordinatesDouble = MapService.getInstance().fetchCoordinatesForPostalCode(postalCode);
		Coordinates coordinates = new Coordinates(coordinatesDouble[0], coordinatesDouble[1]);
		
		if (!compatibleTables) {
			throw new IncompatibleTablesException();
		}
		
		Warehouse warehouse = new Warehouse(name, regionName, priceList, regionTable, postalCode, coordinates);
		warehouseDao.insertFullWarehouse(warehouse);
		log.info("Added new warehouse '{}' in region '{}', postalCode={}", name, regionName, postalCode);
	}
	
	public void deleteWarehouse(CoreWarehouseInfo warehouseInfo) throws SQLException {
		warehouseDao.deleteFullWarehouse(warehouseInfo.getId());
		log.info("Deleted warehouse #{}", warehouseInfo.getId());
	}
	
	public List<BriefWarehouse> getBriefWarehouses() throws SQLException {
		return warehouseDao.getAllBriefWarehouses();
	}
	
	public Warehouse getWarehouse(BriefWarehouse briefWarehouse) throws SQLException {
		return warehouseDao.getById(briefWarehouse.getId());
	}
	
	public SmallPriceList getSmallPriceList() throws SQLException {
		return priceListDao.getSmallPriceList();
	}
	
	public SmallPriceListFormatted getSmallPriceListFormatted() throws SQLException {
		return WarehouseConvertor.toSmallPriceListFormatted(this.getSmallPriceList());
	}
	
	public void setSmallPriceList(SmallPriceList smallPriceList) throws SQLException {
		priceListDao.setAutoCommit(false);
		try {
			priceListDao.deleteSmallPriceList();
			priceListDao.insertSmallPriceList(smallPriceList);
			priceListDao.commit();
			log.info("Small price list updated ({} entries)", smallPriceList.getEntries().size());
		} catch (SQLException e) {
			log.error("Failed to update small price list", e);
			priceListDao.rollback();
		} finally {
			priceListDao.setAutoCommit(true);
		}
	}

	public void setSmallPriceListFormatted(SmallPriceListFormatted smallPriceListFormatted) throws SQLException {
		SmallPriceList smallPriceList = WarehouseConvertor.toSmallPriceList(smallPriceListFormatted);
		this.setSmallPriceList(smallPriceList);
	}
	
	public void replaceTables(PriceListFormatted priceListFormatted, RegionTableFormatted regionTableFormatted, Warehouse warehouse) throws SQLException, IncompatibleTablesException {
		PriceList priceList = WarehouseConvertor.convertPriceList(priceListFormatted);
		RegionTable regionTable = WarehouseConvertor.convertRegionTable(regionTableFormatted);
		
		boolean compatibleTables = WarehouseParsingService.getInstance()
				.checkTableCompatibility(priceList, regionTable);
		
		if (!compatibleTables) {
			throw new IncompatibleTablesException();
		}
		
		priceListDao.setAutoCommit(false);
		regionDao.setAutoCommit(false);
		try {
			priceListDao.deletePriceListByWarehouseID(warehouse.getId());
			regionDao.deleteFullRegionTable(warehouse.getId());
			
			priceListDao.insertPriceList(priceList, warehouse.getId());
			regionDao.insertFullRegionTable(regionTable, warehouse);
			
			priceListDao.commit();
			regionDao.commit();
			log.info("Replaced price list and region table for warehouse #{}", warehouse.getId());
		} catch (SQLException e) {
			log.error("Failed to replace tables for warehouse #{}", warehouse.getId(), e);
			priceListDao.rollback();
			regionDao.rollback();
		} finally {
			priceListDao.setAutoCommit(true);
			regionDao.setAutoCommit(true);
		}
	}
	
}
