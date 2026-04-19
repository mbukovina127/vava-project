package org.shippin.dto;

public enum Screens {
    LOGIN, REGISTER, HOME, COST_ESTIMATION,COST_BREAKDOWN, USER_MANAGEMENT;

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
        };
    }
}
