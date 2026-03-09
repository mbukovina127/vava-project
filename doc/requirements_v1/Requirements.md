## **Business requirements:**

1. Decrease time and effort necessary to create a shipment cost estimation.
2. Provide a secure way to store shipment data, which is not reported as difficult to use by the users.
3. Reduce the amount of steps with a non-negligible error risk in shipment cost estimation.
4. Provide a way to, with minimal effort, check and store the shipment cost estimation process.
5. Provide a reliable way to track shipments in real time.
6. Include the ability to export price lists to CSV format.





## **User requirements:**

1. An unauthorized guest must be able to register and log in.
2. The user must be able to enter shipment details and compute shipment cost estimate.
3. The user must be able to view the breakdown of how the system computed the shipment cost estimate.
4. The administrator must be able to enter a new price list.
5. The power user must be able to view archived price lists.
6. The administrator must be able to edit and delete archived price lists.
7. The user must be able to view the shipment constraints.
8. The administrator must be able to edit the shipment constraints.
9. The power user must be able to export a price list to an CSV format.
10. The administrator must be able to view all of the ongoing shipments and their exact positions.





## **Functional requirements:**

1. The system will allow guests to register and log in. A role-based access control will be enforced with the following roles:

   1. User: Basic permissions for viewing data and entering shipment details to compute shipment cost estimates.
   2. Power user: Heightened permissions. On top of basic permissions, power user is able to view archived price lists and export them to an CSV format.
   3. Administrator: Highest permissions. On top of heightened permissions, administrator is able to edit, add and remove price lists freely, edit shipment constraints and view the map of all currently ongoing shipments and their positions.

2. The system will provide authorized users with the ability to enter shipment details (while validating and rejecting invalid entries):

   1. Primary attributes: postal code, weight, volume.
   2. Coefficients: fuel surcharge coefficient, toll.
   3. Additional fees: SMS fees, ...

3. The system will perform automated cost estimation (when being prompted by authorized users) computed using primary attributes, coefficients and additional fees.
4. The system will, after being prompted by an authorized user to estimate shipment costs, display a complete breakdown of how the estimate was computed. Breakdown will include every step of the way, including price list selection.
5. The system will provide authorized administrators with the ability to upload a new price list. The system will store it as a new archived price list.
6. The system will provide authorized power user with the ability to view archived price lists.
7. The system will provide authorized administrators with the ability to edit and delete archived price lists.
8. The system will provide authorized users with the ability to view shipment constraints.
9. The system will provide authorized administrators with the ability to edit shipment constraints.
10. The system will provide authorized power user with the ability to export price lists into an CSV format.
11. The system will be able to track shipments in real time.
12. The system will provide authorized administrators with the ability to display a map of all shipments and their current positions.





## **Non-functional requirements:**

1. The system has to be Windows 11 compatible.
2. The development of the project will have a development pipeline with automated tests:

   1. Line coverage: 80%.
   2. Branch coverage: 80%.

3. The system will meet the following accessibility criteria:

   1. An individual familiar with the current shipment price estimation process can be taught how to perform all of the designed use cases in their most basic form using the new system in under 15 minutes, and afterwards be able to complete all of them in under 20 minutes without any guidance. Test will be performed on a single test subject.
   2. An individual who is familiar with the current shipment prices estimation process and has been instructed on how to use it can recall how to perform all of the use cases even after not interacting or being instructed about the system in any way for more than 24 hours and perform them all in under 20 minutes without any guidance. Test will be performed on a single test subject.
