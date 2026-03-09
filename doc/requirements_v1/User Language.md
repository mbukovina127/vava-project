**Shipment (zásielka)** => A package being transported from point A to point B. The word itself can refer to the package being delivered, or the journey itself, depending on context. When talking about the journey, it has a cost associated with it (which is often influenced by the attributes of the package). The core goal of the program is to estimate the costs of a shipment before it is finalised.



**Shipment details (vlastnosti zásielky/ údaje o zásielke)** => Variables directly connected to a shipment. Generally, they affect the shipment costs. Obtained from user input.



**Fuel surcharge coefficient (koeficient palivového príplatku)** => A coefficient. Helps estimate the current fuel costs based on the fluctuation in fuel prices compared to a constant. Both the constant and the coefficient for the current month are issued by an official trusted third party source. A shipment detail.



**Toll (mýto)** => The tax for using a specific road. One of the shipment details.



**SMS fee (SMS príplatok)** => An extra fee for sending an SMS to the user about the incoming delivery. A shipment detail.



**Price list (cenník)** => A list of shipment prices in a specific region. Changes in variables like weight and volume can disproportionately affect the shipment prices based on the region. Separate price lists address these differences.



**Primary attributes (primárne údaje)** => Shipment details whose effects on the shipment prices are thoroughly documented using extensive archived prices. Shipment specific. Postal code, weight and volume.



**Archived prices (archivované ceny)** => Stored values of prices from previous months. Divided based on primary attributes. Used for aid in future estimations. Divided into price lists.



**Coefficients (koeficienty)** => Multipliers of the intermediate results derived from primary attributes. Update every month (month specific). Fuel surcharge coefficient and toll.



**Additional fees/ surcharges (dodatočné poplatky/ príplatky)** => Shipment details. Unlike primary fees and coefficients, their effects are included by simple addition. Shipment specific. SMS fees and other additional fees.



**Final price (konečná cena)** => Not necessarily the real cost of the shipment, only the final computed estimate. The core program product.



**Shipment constraints** => The edge constraints of shipment details. Contains the maximum and minimum allowed values for each shipment detail. Viewable by any user and configurable by the administrator.

