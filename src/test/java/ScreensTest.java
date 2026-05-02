import org.junit.jupiter.api.Test;
import org.shippin.domain.enums.Role;
import org.shippin.dto.Screens;

import static org.junit.jupiter.api.Assertions.*;

public class ScreensTest {

    @Test
    void publicScreensAreAccessibleWithoutRole() {
        assertTrue(Screens.LOGIN.isAccessibleBy(null));
        assertTrue(Screens.REGISTER.isAccessibleBy(null));
    }

    @Test
    void userCanAccessUserScreens() {
        assertTrue(Screens.HOME.isAccessibleBy(Role.USER));
        assertTrue(Screens.COST_ESTIMATION.isAccessibleBy(Role.USER));
        assertTrue(Screens.COST_BREAKDOWN.isAccessibleBy(Role.USER));
        assertTrue(Screens.SHIPMENT_DETAIL.isAccessibleBy(Role.USER));
        assertTrue(Screens.SMALL_PRICE_LIST_VIEW.isAccessibleBy(Role.USER));
    }

    @Test
    void userCannotAccessPowerUserOrAdminScreens() {
        assertFalse(Screens.DAILY_COST.isAccessibleBy(Role.USER));
        assertFalse(Screens.WAREHOUSE_MANAGEMENT.isAccessibleBy(Role.USER));
        assertFalse(Screens.USER_MANAGEMENT.isAccessibleBy(Role.USER));
    }

    @Test
    void powerUserCanAccessPowerUserScreensButNotAdminScreen() {
        assertTrue(Screens.DAILY_COST.isAccessibleBy(Role.POWER_USER));
        assertTrue(Screens.DAILY_COST_SUM.isAccessibleBy(Role.POWER_USER));
        assertTrue(Screens.WAREHOUSE_MANAGEMENT.isAccessibleBy(Role.POWER_USER));
        assertTrue(Screens.EDIT_WAREHOUSE.isAccessibleBy(Role.POWER_USER));

        assertFalse(Screens.USER_MANAGEMENT.isAccessibleBy(Role.POWER_USER));
    }

    @Test
    void adminCanAccessAllScreens() {
        for (Screens screen : Screens.values()) {
            assertTrue(screen.isAccessibleBy(Role.ADMIN));
        }
    }

    @Test
    void resolveScreenReturnsCorrectFxmlPaths() {
        assertEquals("/views/Login.fxml", Screens.resolveScreen(Screens.LOGIN));
        assertEquals("/views/Register.fxml", Screens.resolveScreen(Screens.REGISTER));
        assertEquals("/views/Menu.fxml", Screens.resolveScreen(Screens.HOME));
        assertEquals("/views/CostEstimation.fxml", Screens.resolveScreen(Screens.COST_ESTIMATION));
        assertEquals("/views/CostBreakdown.fxml", Screens.resolveScreen(Screens.COST_BREAKDOWN));
        assertEquals("/views/UserManagement.fxml", Screens.resolveScreen(Screens.USER_MANAGEMENT));
        assertEquals("/views/DailyCostsSummaries.fxml", Screens.resolveScreen(Screens.DAILY_COST));
        assertEquals("/views/DailyCostsSummaryDetail.fxml", Screens.resolveScreen(Screens.DAILY_COST_SUM));
        assertEquals("/views/WarehouseManagement.fxml", Screens.resolveScreen(Screens.WAREHOUSE_MANAGEMENT));
        assertEquals("/views/EditWarehouse.fxml", Screens.resolveScreen(Screens.EDIT_WAREHOUSE));
        assertEquals("/views/ShipmentDetail.fxml", Screens.resolveScreen(Screens.SHIPMENT_DETAIL));
        assertEquals("/views/SmallPriceListView.fxml", Screens.resolveScreen(Screens.SMALL_PRICE_LIST_VIEW));
    }
}