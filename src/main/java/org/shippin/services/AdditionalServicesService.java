package org.shippin.services;

import lombok.extern.log4j.Log4j2;
import org.shippin.database.dao.AdditionalServiceDAO;
import org.shippin.domain.AdditionalService;

import java.sql.SQLException;
import java.util.List;

@Log4j2
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
        List<AdditionalService> services = additionalServiceDAO.getAllServices();
        log.debug("Fetched {} additional services", services.size());
        return services;
    }

    public AdditionalService getServiceById(int id) throws SQLException {
        AdditionalService service = additionalServiceDAO.getServiceById(id);
        if (service == null) log.warn("Additional service #{} not found", id);
        return service;
    }

    public int createService(AdditionalService service) throws SQLException {
        int id = additionalServiceDAO.insertService(service);
        log.info("Created additional service '{}' with id #{}", service.getName(), id);
        return id;
    }

    public boolean updateService(AdditionalService service) throws SQLException {
        boolean updated = additionalServiceDAO.updateService(service);
        if (updated) log.info("Updated additional service #{}", service.getId());
        else log.warn("Update had no effect for additional service #{}", service.getId());
        return updated;
    }

    public boolean deleteService(int id) throws SQLException {
        boolean deleted = additionalServiceDAO.deleteService(id);
        if (deleted) log.info("Deleted additional service #{}", id);
        else log.warn("Delete had no effect for additional service #{}", id);
        return deleted;
    }
}