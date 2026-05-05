package org.shippin.database.dao;

import org.shippin.domain.AdditionalService;
import org.shippin.domain.enums.ServiceType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdditionalServiceDAO extends BaseDAO {

    private static AdditionalServiceDAO instance;

    private static final String INSERT_SQL = """
            INSERT INTO service(
                service_name,
                service_name_en,
                default_cost,
                cost_modificator,
                description,
                description_en,
                service_type
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE_SQL = """
            UPDATE service
            SET service_name = ?,
                service_name_en = ?,
                default_cost = ?,
                cost_modificator = ?,
                description = ?,
                description_en = ?,
                service_type = ?
            WHERE service_id = ?
            """;

    private static final String DELETE_SQL = """
            DELETE FROM service 
            WHERE service_id = ?
            """;

    private static final String SELECT_ALL_SQL = """
            SELECT service_id,
                   service_name,
                   service_name_en,
                   default_cost,
                   cost_modificator,
                   description,
                   description_en,
                   service_type
            FROM service
            """;

    private static final String SELECT_BY_ID_SQL = """
            SELECT service_id,
                   service_name,
                   service_name_en,
                   default_cost,
                   cost_modificator,
                   description,
                   description_en,
                   service_type
            FROM service
            WHERE service_id = ?
            """;

    private AdditionalServiceDAO() {
        super();
    }

    public static AdditionalServiceDAO getInstance() {
        if (instance == null) {
            instance = new AdditionalServiceDAO();
        }

        return instance;
    }

    public List<AdditionalService> getAllServices() throws SQLException {
        List<AdditionalService> services = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(SELECT_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                services.add(readService(resultSet));
            }
        }

        return services;
    }

    public AdditionalService getServiceById(int serviceId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID_SQL)) {
            statement.setInt(1, serviceId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return readService(resultSet);
                }
            }
        }

        return null;
    }

    public int insertService(AdditionalService service) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                INSERT_SQL,
                Statement.RETURN_GENERATED_KEYS
        )) {
            statement.setString(1, service.getName());
            statement.setString(2, service.getName_en());
            statement.setFloat(3, service.getDefaultCost());
            statement.setFloat(4, service.getCostModifier());
            statement.setString(5, service.getDescription());
            statement.setString(6, service.getDescription_en());
            statement.setString(7, service.getServiceType() == null ? null : service.getServiceType().name());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    service.setId(generatedId);
                    return generatedId;
                }
            }
        }

        return -1;
    }

    public boolean updateService(AdditionalService service) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, service.getName());
            statement.setString(2, service.getName_en());
            statement.setFloat(3, service.getDefaultCost());
            statement.setFloat(4, service.getCostModifier());
            statement.setString(5, service.getDescription());
            statement.setString(6, service.getDescription_en());
            statement.setString(7, service.getServiceType() == null ? null : service.getServiceType().name());
            statement.setInt(8, service.getId());

            int affectedRows = statement.executeUpdate();

            return affectedRows > 0;
        }
    }

    public boolean deleteService(int serviceId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, serviceId);

            int affectedRows = statement.executeUpdate();

            return affectedRows > 0;
        }
    }

    private AdditionalService readService(ResultSet resultSet) throws SQLException {
        AdditionalService service = new AdditionalService();

        service.setId(resultSet.getInt("service_id"));
        service.setName(resultSet.getString("service_name"));
        service.setName_en(resultSet.getString("service_name_en"));
        service.setDefaultCost(resultSet.getFloat("default_cost"));
        service.setCostModifier(resultSet.getFloat("cost_modificator"));
        service.setDescription(resultSet.getString("description"));
        service.setDescription_en(resultSet.getString("description_en"));
        service.setServiceType(readServiceType(resultSet.getString("service_type")));

        return service;
    }

    private ServiceType readServiceType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return ServiceType.valueOf(value);
    }
}