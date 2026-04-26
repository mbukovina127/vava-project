package org.shippin.dto;

public enum Screens {
    LOGIN, REGISTER, HOME, COST_ESTIMATION,COST_BREAKDOWN, USER_MANAGEMENT, DAILY_COST,DAILY_COST_SUM, WAREHOUSE_MANAGEMENT, EDIT_WAREHOUSE, SMALL_PRICE_LIST_VIEW;

    /**
     * @param screen
     * @return String path to FXML file
     */
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
            case SMALL_PRICE_LIST_VIEW -> "/views/SmallPriceListView.fxml";
        };
    }
}
