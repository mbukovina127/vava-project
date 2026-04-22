-- SELECT * FROM Postal_code pc
-- JOIN Postal_code_list pcl ON pc.postal_code_ID = pcl.postal_code_ID
-- JOIN Region r ON pcl.region_ID = r.region_ID
-- JOIN Parameter_list pl ON r.region_ID = pl.region_ID
-- JOIN Warehouse w ON r.warehouse_ID = w.warehouse_ID
-- JOIN Shipment s ON s.warehouse_ID = w.warehouse_ID
-- JOIN History h ON s.shipment_ID = h.shipment_ID;
------------------------------------------------------------------------------------------------------
--Additional Service DAO

--GetAll()
SELECT * FROM Service;

--GetAllByID(ID) get all services linked to the shipment id
SELECT * FROM Service s JOIN Service_list sl ON s.service_ID = sl.service_ID
WHERE sl.shipment_ID = 1; --(Temporary ID)

------------------------------------------------------------------------------------------------------
--ShipmentDAO

-- upsert(Shipment)
INSERT INTO Shipment (shipment_ID, user_ID, warehouse_ID, dest_region, fuel_payment, total_cost, created_at, status, is_sp)
VALUES (DEFAULT, 1, 1, 1, 1, 1, DEFAULT, DEFAULT, NULL)
ON CONFLICT (shipment_ID)
DO UPDATE SET
	user_ID = EXCLUDED.user_ID,
	warehouse_ID = EXCLUDED.warehouse_ID,
	dest_region = EXCLUDED.dest_region,
	fuel_payment = EXCLUDED.fuel_payment,
	total_cost = EXCLUDED.total_cost,
	created_at = EXCLUDED.created_at,
	status = EXCLUDED.status,
	is_sp = EXCLUDED.is_sp;

-- will be repeated till all the services are added

INSERT INTO Service_list (list_ID, service_ID, shipment_ID)
VALUES (DEFAULT, 1, 2)
ON CONFLICT (service_ID, shipment_ID)
DO UPDATE SET
	service_ID = EXCLUDED.service_ID,
	shipment_ID = EXCLUDED.shipment_ID;

--upsertSmall(Shipment)
INSERT INTO Shipment(shipment_ID, user_ID, warehouse_ID, dest_region, fuel_payment, total_cost, created_at, status, is_sp)
VALUES (6, 2 ,1 ,1 ,1 ,1, DEFAULT,'Failed',4 ) -- test input
ON CONFLICT (shipment_ID)
DO UPDATE SET
	user_ID = EXCLUDED.user_ID,
	warehouse_ID = EXCLUDED.warehouse_ID,
	dest_region = EXCLUDED.dest_region,
	fuel_payment = EXCLUDED.fuel_payment,
	total_cost = EXCLUDED.total_cost,
	created_at = EXCLUDED.created_at,
	status = EXCLUDED.status,
	is_sp = EXCLUDED.is_sp;

--getByID(ID)
SELECT * FROM Shipment s
JOIN Service_list sl ON s.shipment_ID = sl.shipment_ID
JOIN Service se ON sl.service_ID = se.service_ID
JOIN History h ON s.shipment_id = h.shipment_id
WHERE s.shipment_ID = 1; --(Temporary ID)

--delete(ID)
DELETE FROM Shipment WHERE shipment_ID = 1; --test input // treba cascade?

--getBriefAllRecent(number of days)
SELECT * FROM Shipment s
WHERE s.created_at >= CURRENT_TIMESTAMP - (3 * INTERVAL '1 day'); --3 = test input

--getBriefAllByDate(Date)
SELECT * FROM Shipment s
WHERE s.created_at >= DATE '2026-03-24' --test input
AND s.created_at < DATE '2026-03-25';

--getBriefAllByStatus(Status)
SELECT * FROM Shipment s
WHERE s.status = 'Pending';

------------------------------------------------------------------------------------------------------
-- UserDAO

--insert(f_name,l_name, email, password)
INSERT INTO Users(first_name, last_name, password, email)
VALUES ('Marian', 'Holodej', 'majocasdd', 'Majo@gmail.com');

--delete(ID)
DELETE FROM Users u WHERE u.user_ID = 11; --test input

--getAll()
SELECT * FROM Users u LEFT JOIN Shipment s ON s.user_ID = u.user_ID;

--getByDetails(email, password)
SELECT * FROM Users u LEFT JOIN Shipment s ON u.user_id = s.user_id
WHERE u.email = 'Majo@gmail.com' AND u.password = 'majocasdd';

--getRoleById(ID)
SELECT u.role FROM Users u WHERE u.user_ID = 1;

--changeRoleById(ID, role)
UPDATE Users u  SET role = 0 WHERE u.user_id = 1;

------------------------------------------------------------------------------------------------------
--WarehouseDAO

--upsert(Warehouse)
INSERT INTO Warehouse(warehouse_id, storage_region, warehouse_region_name, price_list_file)
VALUES (1,1,'Bratislava', 'file.txt')
ON CONFLICT (warehouse_id)
DO UPDATE SET
    warehouse_id = EXCLUDED.warehouse_id,
    storage_region = EXCLUDED.storage_region,
    warehouse_region_name = EXCLUDED.warehouse_region_name,
    price_list_file = EXCLUDED.price_list_file;

--getById(ID)
SELECT * FROM Warehouse w JOIN Region r ON w.warehouse_id = r.warehouse_id
JOIN Postal_code_list pcl ON r.region_id = pcl.region_id
JOIN Postal_code pc ON pcl.postal_code_id = pc.postal_code_id
JOIN Parameter_list pl ON pl.region_id = r.region_id
WHERE w.warehouse_id = 1;

--getBriefAll()
SELECT * FROM Warehouse;

--delete(ID)
DELETE FROM Warehouse w WHERE w.warehouse_id = 1;

--getPrice(warehouse_ID, weight, volume, postal_code)
SELECT pl.cost
FROM Region r
JOIN Parameter_list pl
ON pl.region_id = r.region_id
JOIN Postal_code_list pcl
ON pcl.region_id = r.region_id
JOIN Postal_code pc
ON pc.postal_code_id = pcl.postal_code_id
WHERE r.warehouse_id = 2
  AND pl.volume = 0.2
  AND pl.weight = 50
  AND pc.up_bound <= 11000
  AND pc.down_bound >= 11000;

--setPrice(warehouse_ID, weight volume, postal code)
--repeat until filled
INSERT INTO Parameter_list(weight,volume,cost,region_id)
VALUES(1, 1, 1, 1);

--setRegionTable(RegionTable)
INSERT INTO Region(warehouse_id, region_name) VALUES(1, 'Kokotice');

INSERT INTO Parameter_list(region_id, weight, volume, cost)
VALUES(1, 2, 2, 4);

INSERT INTO Postal_code_list(region_id, postal_code_id)
VALUES(1, 1);

INSERT INTO Postal_code(up_bound, down_bound) VALUES(12345, 54321);
--updateCoreInfo(warehouse)
UPDATE Warehouse SET
    storage_region = 10000,
    warehouse_region_name = 'Bratislava',
    latitude = 48.1234,
    longitude = 17.9999,
    price_list_file = 'random'
WHERE warehouse_id = 1;

------------------------------------------------------------------------------------------------------
--SmallPriceListDAO

--get()
SELECT * FROM Sp_price_list;

--set(sp_pl_list)
INSERT INTO Sp_price_list(weight_sp, cost_sp)
VALUES (5, 10);

--getPrice(weight)
SELECT sp.cost_sp FROM sp_price_list sp WHERE sp.weight_sp = 5;
------------------------------------------------------------------------------------------------------