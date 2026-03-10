# LEGEND

#### *_ID → Primary key or foreign key.

#### dest_psc → destination psc.

#### fuel_payment → additional fuel payment.

#### total_cost → sum of all cost in the order.

#### TIME → timestamp of the order.

#### service_type → type of the service FK to service_ID.

#### service_cost → ? adjusted cost by user - need clarification.

#### service_name → name of the service.

#### default_cost → default cost for the service or percentage of a cost from total_cost.

#### name → name of the user.

#### password → hashed password.

#### email → users email.

#### role → role of the user (admin, basic user, ...).

#### storage_region → psc of the sender region.

#### up_dest_region → upper bound of the set of psc numbers in destination region.

#### down_dest_region → lower bound of the set of psc numbers in destination region.

#### weight → weight of potential package in KG.

#### volume → volume of the potential package in m^3.

#### cost → cost of the package given the weight and volume in €.

#### region_name → name of the region.

![database_structure.png](../assets/database_structure.png)
