DROP TABLE IF EXISTS 
    Parameter,
	Region,
	Parameter_list,
	Postal_code,
	Postal_code_list,
	Warehouse,
	SP_price_list,
	History,
	Shipment,
	Users,
	Service,
	Service_list
CASCADE;

CREATE TABLE Warehouse (
	warehouse_ID SERIAL PRIMARY KEY,
	storage_region INT NOT NULL,
	warehouse_region_name TEXT NOT NULL,
	price_list_file TEXT NOT NULL
);

CREATE TABLE Region (
    region_ID SERIAL PRIMARY KEY,
    warehouse_ID INT NOT NULL REFERENCES Warehouse(warehouse_ID) ON DELETE CASCADE,
    region_name TEXT NOT NULL
);

CREATE TABLE Parameter_list (
    parameter_list_ID SERIAL PRIMARY KEY,
    region_ID INT NOT NULL REFERENCES Region(region_ID) ON DELETE CASCADE,
    weight NUMERIC(10,2) NOT NULL CHECK (weight >= 0),
    volume NUMERIC(10,2) NOT NULL CHECK (volume >= 0),
    cost NUMERIC(10,2) NOT NULL CHECK (cost >= 0)
);

CREATE TABLE Postal_code (
    postal_code_ID SERIAL PRIMARY KEY,
    up_bound INT NOT NULL,
    down_bound INT NOT NULL
);

CREATE TABLE Postal_code_list (
    postal_code_list_ID SERIAL PRIMARY KEY,
    region_ID INT NOT NULL REFERENCES Region(region_ID) ON DELETE CASCADE,
    postal_code_ID INT NOT NULL REFERENCES Postal_code(postal_code_ID) ON DELETE CASCADE
);

CREATE TABLE SP_price_list (
    sp_price_list_ID SERIAL PRIMARY KEY,
    weight_sp NUMERIC(10,2) NOT NULL CHECK (weight_sp >= 0),
    cost_sp NUMERIC(10,2) NOT NULL CHECK (cost_sp >= 0)
);

CREATE TABLE Users (
    user_ID SERIAL PRIMARY KEY,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    password TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    role INT NOT NULL DEFAULT 0
);

CREATE TABLE Shipment (
    shipment_ID SERIAL PRIMARY KEY,
    user_ID INT NOT NULL REFERENCES Users(user_ID) ON DELETE CASCADE,
    warehouse_ID INT NOT NULL REFERENCES Warehouse(warehouse_ID) ON DELETE CASCADE,
    dest_region INT NOT NULL,
    fuel_payment NUMERIC(10,2) NOT NULL DEFAULT 0,
    total_cost NUMERIC(10,2) NOT NULL DEFAULT 0,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status TEXT NOT NULL DEFAULT 'NOT_READY' CHECK (status in ('NOT_READY', 'READY', 'CANCELED', 'BEING_DELIVERED', 'DELIVERED', 'FAILED')),
    sp_ID INT REFERENCES SP_price_list(sp_price_list_ID) ON DELETE SET NULL
);

CREATE TABLE History (
    history_ID SERIAL PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    state TEXT NOT NULL CHECK (state in ('NOT_READY', 'READY', 'CANCELED', 'BEING_DELIVERED', 'DELIVERED', 'FAILED')),
    shipment_ID INT NOT NULL REFERENCES Shipment(shipment_ID) ON DELETE CASCADE
);

CREATE TABLE Service (
    service_ID SERIAL PRIMARY KEY,
    service_name TEXT NOT NULL,
    default_cost NUMERIC(10,2) NOT NULL DEFAULT 0,
    cost_modificator NUMERIC(10,2) NOT NULL DEFAULT 1
);

CREATE TABLE Service_list (
    list_ID SERIAL PRIMARY KEY,
    service_ID INT NOT NULL REFERENCES Service(service_ID) ON DELETE CASCADE,
    shipment_ID INT NOT NULL REFERENCES Shipment(shipment_ID) ON DELETE CASCADE
);