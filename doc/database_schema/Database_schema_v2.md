# Legend

*_ID → Primary key or foreign key.

Database users:

- Pre_login → One view just for the user table.
- Guest → Only one are two possible views and that's it.
- User → Can make a shipment, but cant change data in the other tables accept shipping and only add to the shipment table.
- Admin → Can access and update all the tables.

![VAVA_database.png](../assets/VAVA_database_v3.png)

## Shipment
* dest_psc → destination psc.
* fuel_payment → additional fuel payment.
* total_cost → sum of all cost in the order.
* sent_at → timestamp of the time when the shipment was sent.
* status → tracks state of the shipment (Canceled, Failed, Delivered, In_Progress).

## Service
* service_name → name of the service.
* cost_modificator → percentage of the total_cost without services used only if higher than default cost.
* default_cost → minimal cost for the service if the case of insufficient funds from the cost_modificator.

## History
* timestamp → time and date of the logged state.
* state →  same as status but in the past.

## User
* first_name → first name of the user.
* last_name → last name of the user.
* password → hashed password.
* email → users email.
* role → role of the user (admin, user, guest).

## Warehouse
* storage_region → psc of the sender region.
* warehouse_region_name → name of the region in which the warehouse is located (Bratislava, Košice,....).
* price_list_file → path to the price_list file.

## SP_price_list
* weight_sp → weight of a small package.
* cost_sp → cost of the small package according to the weight.

## Region
* region_name → name of the destination region (BA1, ZA,...).

## Postal_code
* up_bound → upper bound of possible postal codes.
* down_bound → down bound of possible postal codes.

# Parameters_list
* weight → weight of potential package in KG.
* volume → volume of the potential package in m^3.
* cost → cost of the package given the weight and volume in €.