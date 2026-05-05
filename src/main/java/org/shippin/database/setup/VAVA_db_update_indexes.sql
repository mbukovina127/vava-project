SELECT setval(pg_get_serial_sequence('Service', 'service_id'), coalesce(max(service_id), 1)) FROM Service;

SELECT setval(pg_get_serial_sequence('Warehouse', 'warehouse_id'), coalesce(max(warehouse_id), 1)) FROM Warehouse;

SELECT setval(pg_get_serial_sequence('Region', 'region_id'), coalesce(max(region_id), 1)) FROM Region;

SELECT setval(pg_get_serial_sequence('Parameter_list', 'parameter_list_id'), coalesce(max(parameter_list_id), 1)) FROM Parameter_list;

SELECT setval(pg_get_serial_sequence('Postal_code', 'postal_code_id'), coalesce(max(postal_code_id), 1)) FROM Postal_code;

SELECT setval(pg_get_serial_sequence('Postal_code_list', 'postal_code_list_id'), coalesce(max(postal_code_list_id), 1)) FROM Postal_code_list;

SELECT setval(pg_get_serial_sequence('SP_price_list', 'sp_price_list_id'), coalesce(max(sp_price_list_id), 1)) FROM SP_price_list;

SELECT setval(pg_get_serial_sequence('Users', 'user_id'), coalesce(max(user_id), 1)) FROM Users;

SELECT setval(pg_get_serial_sequence('Shipment', 'shipment_id'), coalesce(max(shipment_id), 1)) FROM Shipment;

SELECT setval(pg_get_serial_sequence('History', 'history_id'), coalesce(max(history_id), 1)) FROM History;

SELECT setval(pg_get_serial_sequence('Service_list', 'list_id'), coalesce(max(list_id), 1)) FROM Service_list;