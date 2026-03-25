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
SELECT * FROM Service s JOIN Service_list sl ON s.service_ID = sl.service_ID;

--GetAllByID(ID)
SELECT * FROM Service s JOIN Service_list sl ON s.service_ID = sl.service_ID
WHERE s.service_ID = 1; --(Temporary ID)

------------------------------------------------------------------------------------------------------
--ShipmentDAO

-- upsert(Shipment)
INSERT INTO Shipment (shipment_ID, user_ID, warehouse_ID, dest_region, fuel_payment, total_cost, sent_at, status, sp_ID)
VALUES (DEFAULT, 1, 1, 1, 1, 1, DEFAULT, DEFAULT, NULL)
ON CONFLICT (shipment_ID)
DO UPDATE SET
	user_ID = EXCLUDED.user_ID,
	warehouse_ID = EXCLUDED.warehouse_ID,
	dest_region = EXCLUDED.dest_region,
	fuel_payment = EXCLUDED.fuel_payment,
	total_cost = EXCLUDED.total_cost,
	sent_at = EXCLUDED.sent_at,
	status = EXCLUDED.status,
	sp_ID = EXCLUDED.sp_ID;

-- will be repeated till all the services are added

INSERT INTO Service_list (list_ID, service_ID, shipment_ID)
VALUES (DEFAULT, 1, 2)
ON CONFLICT (service_ID, shipment_ID)
DO UPDATE SET
	service_ID = EXCLUDED.service_ID,
	shipment_ID = EXCLUDED.shipment_ID;

--upsertSmall(Shipment)
INSERT INTO Shipment(shipment_ID, user_ID, warehouse_ID, dest_region, fuel_payment, total_cost, sent_at, status, sp_ID)
VALUES (6, 2 ,1 ,1 ,1 ,1, DEFAULT,'Failed',4 ) -- test input
ON CONFLICT (shipment_ID)
DO UPDATE SET
	user_ID = EXCLUDED.user_ID,
	warehouse_ID = EXCLUDED.warehouse_ID,
	dest_region = EXCLUDED.dest_region,
	fuel_payment = EXCLUDED.fuel_payment,
	total_cost = EXCLUDED.total_cost,
	sent_at = EXCLUDED.sent_at,
	status = EXCLUDED.status,
	sp_ID = EXCLUDED.sp_ID;

--getByID(ID)
SELECT s.*, se.service_name FROM Shipment s
JOIN Service_list sl ON s.shipment_ID = sl.shipment_ID
JOIN Service se ON sl.service_ID = se.service_ID
WHERE s.shipment_ID = 1; --(Temporary ID)

--delete(ID)
DELETE FROM Shipment WHERE shipment_ID = 1; --test input

--getBriefAllRecent(number of days)
SELECT * FROM Shipment s
WHERE s.sent_at >= CURRENT_TIMESTAMP - (3 * INTERVAL '1 day'); --3 = test input

--getBriefAllByDate(Date)
SELECT * FROM Shipment s
WHERE s.sent_at >= DATE '2026-03-24' --test input
AND s.sent_at < DATE '2026-03-25';

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
--updateCoreInfo(warehouse)
--getConstraints(ID)
SELECT *
FROM information_schema.table_constraints
WHERE table_name = 'warehouse';
------------------------------------------------------------------------------------------------------
--SmallPriceListDAO

--get()
SELECT * FROM Sp_price_list;

--set(sp_pl_list)
INSERT INTO Sp_price_list(weight_sp, cost_sp)
VALUES (5, 10);

--getPrice(weight)
SELECT sp.cost_sp FROM sp_price_list sp WHERE sp.weight_sp = 5;

--getConstraints()

SELECT *
FROM information_schema.table_constraints
WHERE table_name = 'sp_price_list';
------------------------------------------------------------------------------------------------------