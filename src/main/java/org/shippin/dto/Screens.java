package org.shippin.dto;

import org.shippin.domain.enums.Role;

public enum Screens {
    LOGIN(null),
    REGISTER(null),
    HOME(Role.USER),
    COST_ESTIMATION(Role.USER),
    COST_BREAKDOWN(Role.USER),
    SHIPMENT_DETAIL(Role.USER),
    SMALL_PRICE_LIST_VIEW(Role.USER),
    DAILY_COST(Role.POWER_USER),
    DAILY_COST_SUM(Role.POWER_USER),
    WAREHOUSE_MANAGEMENT(Role.POWER_USER),
    EDIT_WAREHOUSE(Role.POWER_USER),
    USER_MANAGEMENT(Role.ADMIN),
    MY_SHIPMENTS(Role.USER),
    MAP_OF_SHIPMENTS(Role.ADMIN);

    private final Role requiredRole;

    Screens(Role requiredRole) {
        this.requiredRole = requiredRole;
    }

    public Role getRequiredRole() {
        return requiredRole;
    }

    public boolean isAccessibleBy(Role role) {
        return requiredRole == null || (role != null && role.ordinal() >= requiredRole.ordinal());
    }

    public static String resolveScreen(Screens screen) {
        return switch (screen) {
            case LOGIN -> "/views/Login.fxml";
            case REGISTER -> "/views/Register.fxml";
            case HOME -> "/views/Menu.fxml";
            case COST_ESTIMATION -> "/views/CostEstimation.fxml";
            case COST_BREAKDOWN -> "/views/CostBreakdown.fxml";
            case USER_MANAGEMENT -> "/views/UserManagement.fxml";
            case DAILY_COST -> "/views/DailyCostsSummaries.fxml";
            case DAILY_COST_SUM -> "/views/DailyCostsSummaryDetail.fxml";
            case WAREHOUSE_MANAGEMENT -> "/views/WarehouseManagement.fxml";
            case EDIT_WAREHOUSE -> "/views/EditWarehouse.fxml";
            case SHIPMENT_DETAIL -> "/views/ShipmentDetail.fxml";
            case SMALL_PRICE_LIST_VIEW -> "/views/SmallPriceListView.fxml";
            case MY_SHIPMENTS -> "/views/MyShipments.fxml";
            case MAP_OF_SHIPMENTS -> "/views/MapOfShipments.fxml";
        };
    }
}
