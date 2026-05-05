package org.shippin.services;

import org.shippin.database.dao.AdditionalServiceDAO;
import org.shippin.database.dao.ShipmentDAO;
import org.shippin.domain.AdditionalService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdditionalServicesService {
	
	private static AdditionalServicesService instance;
	private AdditionalServiceDAO additionalServiceDAO;
	
	private AdditionalServicesService() {
        this.additionalServiceDAO = AdditionalServiceDAO.getInstance();
    }
	
	public static AdditionalServicesService getInstance() {
    	if (instance == null) {
    		instance = new AdditionalServicesService();
    	}
    	
		return instance;
    }
    
    public List<AdditionalService> getAllServices() throws SQLException {
        return additionalServiceDAO.getAllServices();
    }

    public AdditionalService getServiceById(int id) throws SQLException {
        return additionalServiceDAO.getServiceById(id);
    }

    public int createService(AdditionalService service) throws SQLException {
        return additionalServiceDAO.insertService(service);
    }

    public boolean updateService(AdditionalService service) throws SQLException {
        return additionalServiceDAO.updateService(service);
    }

    public boolean deleteService(int id) throws SQLException {
        return additionalServiceDAO.deleteService(id);
    }
}