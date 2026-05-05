import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.shippin.controller.CostEstimationController;
import org.shippin.domain.AdditionalService;
import org.shippin.domain.enums.ServiceType;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

class CostEstimationControllerTest {

    @BeforeAll
    static void startJavaFx() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException alreadyStarted) {
            latch.countDown();
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    void gettersShouldReturnValuesFromInputFields() throws Exception {
        runOnFxThread(() -> {
            CostEstimationController controller = createControllerWithFields();

            getComboBox(controller, "fromCombo").setValue("Warehouse A");
            getTextField(controller, "destinationField").setText("917 01");
            getTextField(controller, "weightField").setText("12.5");
            getTextField(controller, "volumeField").setText("2.5");
            getTextField(controller, "fuelSurchargeField").setText("5");
            getTextField(controller, "tollField").setText("3");

            assertEquals("Warehouse A", controller.getFrom());
            assertEquals("917 01", controller.getDestination());
            assertEquals(12.5, controller.getWeight(), 0.001);
            assertEquals(2.5, controller.getVolume(), 0.001);
            assertEquals(5.0, controller.getFuelSurcharge(), 0.001);
            assertEquals(3.0, controller.getToll(), 0.001);
        });
    }

    @Test
    void buildServiceUIShouldCreateControlsForAllServiceTypes() throws Exception {
        runOnFxThread(() -> {
            CostEstimationController controller = createControllerWithFields();

            List<AdditionalService> services = createServices();

            invokePrivateMethod(controller, "buildServiceUI", new Class<?>[]{List.class}, services);

            VBox productsContainer = getVBox(controller, "productsContainer");
            VBox servicesContainer = getVBox(controller, "servicesContainer");
            VBox paymentsContainer = getVBox(controller, "paymentsContainer");

            assertTrue(productsContainer.getChildren().size() >= 2);
            assertTrue(servicesContainer.getChildren().size() >= 2);
            assertTrue(paymentsContainer.getChildren().size() >= 2);

            assertTrue(containsRadioButton(productsContainer));
            assertTrue(containsCheckBox(servicesContainer));
            assertTrue(containsRadioButton(paymentsContainer));
        });
    }

    @Test
    void getSelectedServiceIdsShouldReturnSelectedRadioAndCheckboxes() throws Exception {
        runOnFxThread(() -> {
            CostEstimationController controller = createControllerWithFields();

            invokePrivateMethod(controller, "buildServiceUI", new Class<?>[]{List.class}, createServices());

            VBox servicesContainer = getVBox(controller, "servicesContainer");
            VBox paymentsContainer = getVBox(controller, "paymentsContainer");

            selectFirstCheckBox(servicesContainer);
            selectFirstRadioButton(paymentsContainer);

            Object result = invokePrivateMethod(controller, "getSelectedServiceIds", new Class<?>[]{});

            assertInstanceOf(List.class, result);

            List<?> ids = (List<?>) result;

            assertTrue(ids.contains(1));
            assertTrue(ids.contains(2));
            assertTrue(ids.contains(3));
        });
    }

    @Test
    void onResetShouldClearInputFieldsErrorsAndCheckboxes() throws Exception {
        runOnFxThread(() -> {
            CostEstimationController controller = createControllerWithFields();

            invokePrivateMethod(controller, "buildServiceUI", new Class<?>[]{List.class}, createServices());

            getComboBox(controller, "fromCombo").getItems().addAll("Warehouse A", "Warehouse B");
            getComboBox(controller, "fromCombo").setValue("Warehouse B");

            getTextField(controller, "destinationField").setText("91701");
            getTextField(controller, "weightField").setText("10");
            getTextField(controller, "volumeField").setText("2");
            getTextField(controller, "fuelSurchargeField").setText("5");
            getTextField(controller, "tollField").setText("3");

            getLabel(controller, "statusLabelDestination").setText("destination error");
            getLabel(controller, "statusLabelWeight").setText("weight error");
            getLabel(controller, "statusLabelVolume").setText("volume error");
            getLabel(controller, "statusLabelFuel").setText("fuel error");
            getLabel(controller, "statusLabelToll").setText("toll error");

            selectFirstCheckBox(getVBox(controller, "servicesContainer"));
            selectFirstRadioButton(getVBox(controller, "paymentsContainer"));

            invokePrivateMethod(controller, "onReset", new Class<?>[]{});

            assertEquals("", getTextField(controller, "destinationField").getText());
            assertEquals("", getTextField(controller, "weightField").getText());
            assertEquals("", getTextField(controller, "volumeField").getText());
            assertEquals("", getTextField(controller, "fuelSurchargeField").getText());
            assertEquals("", getTextField(controller, "tollField").getText());

            assertEquals("", getLabel(controller, "statusLabelDestination").getText());
            assertEquals("", getLabel(controller, "statusLabelWeight").getText());
            assertEquals("", getLabel(controller, "statusLabelVolume").getText());
            assertEquals("", getLabel(controller, "statusLabelFuel").getText());
            assertEquals("", getLabel(controller, "statusLabelToll").getText());

            assertFalse(anyCheckBoxSelected(getVBox(controller, "servicesContainer")));
            assertFalse(anyRadioButtonSelected(getVBox(controller, "paymentsContainer")));
        });
    }

    @Test
    void onComputeCostShouldShowValidationErrorsForInvalidInputs() throws Exception {
        runOnFxThread(() -> {
            CostEstimationController controller = createControllerWithFields();

            getTextField(controller, "destinationField").setText("");
            getTextField(controller, "weightField").setText("-10");
            getTextField(controller, "volumeField").setText("-2");
            getTextField(controller, "fuelSurchargeField").setText("101");
            getTextField(controller, "tollField").setText("-1");

            assertDoesNotThrow(() ->
                    invokePrivateMethod(controller, "onComputeCost", new Class<?>[]{})
            );

            assertFalse(getLabel(controller, "statusLabelDestination").getText().isEmpty());
            assertFalse(getLabel(controller, "statusLabelWeight").getText().isEmpty());
            assertFalse(getLabel(controller, "statusLabelVolume").getText().isEmpty());
            assertFalse(getLabel(controller, "statusLabelFuel").getText().isEmpty());
            assertFalse(getLabel(controller, "statusLabelToll").getText().isEmpty());
        });
    }

    private static CostEstimationController createControllerWithFields() throws Exception {
        CostEstimationController controller = new CostEstimationController();

        ComboBox<String> fromCombo = new ComboBox<>();
        fromCombo.getItems().addAll("Warehouse A", "Warehouse B");
        fromCombo.setValue("Warehouse A");

        setField(controller, "fromCombo", fromCombo);
        setField(controller, "destinationField", new TextField());
        setField(controller, "weightField", new TextField());
        setField(controller, "volumeField", new TextField());
        setField(controller, "fuelSurchargeField", new TextField());
        setField(controller, "tollField", new TextField());

        setField(controller, "toBox", new HBox());
        setField(controller, "productsContainer", new VBox());
        setField(controller, "servicesContainer", new VBox());
        setField(controller, "paymentsContainer", new VBox());

        setField(controller, "statusLabelDestination", new Label());
        setField(controller, "statusLabelWeight", new Label());
        setField(controller, "statusLabelVolume", new Label());
        setField(controller, "statusLabelDate", new Label());
        setField(controller, "statusLabelFuel", new Label());
        setField(controller, "statusLabelToll", new Label());

        setField(controller, "warehouseList", new ArrayList<>());

        return controller;
    }

    private static List<AdditionalService> createServices() {
        return List.of(
                new AdditionalService(
                        1,
                        "Base service",
                        5f,
                        1f,
                        ServiceType.SERVICES,
                        "Base service description",
                        "Base service description",
                        "Base service"
                ),
                new AdditionalService(
                        2,
                        "Additional payment",
                        3f,
                        1f,
                        ServiceType.ADDITIONAL_PAYMENTS,
                        "Additional payment description",
                        "Additional payment description",
                        "Additional payment"
                ),
                new AdditionalService(
                        3,
                        "Product",
                        2f,
                        1f,
                        ServiceType.PRODUCTS,
                        "Product description",
                        "Product description",
                        "Product"
                )
        );
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = CostEstimationController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Object getField(Object target, String fieldName) throws Exception {
        Field field = CostEstimationController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private static Object invokePrivateMethod(
            Object target,
            String methodName,
            Class<?>[] parameterTypes,
            Object... args
    ) throws Exception {
        Method method = CostEstimationController.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    @SuppressWarnings("unchecked")
    private static ComboBox<String> getComboBox(Object target, String fieldName) throws Exception {
        return (ComboBox<String>) getField(target, fieldName);
    }

    private static TextField getTextField(Object target, String fieldName) throws Exception {
        return (TextField) getField(target, fieldName);
    }

    private static Label getLabel(Object target, String fieldName) throws Exception {
        return (Label) getField(target, fieldName);
    }

    private static VBox getVBox(Object target, String fieldName) throws Exception {
        return (VBox) getField(target, fieldName);
    }

    private static boolean containsRadioButton(VBox container) {
        for (Node node : container.getChildren()) {
            if (node instanceof HBox row) {
                for (Node rowNode : row.getChildren()) {
                    if (rowNode instanceof RadioButton) {
                        return true;
                    }
                }
            }

            if (node instanceof RadioButton) {
                return true;
            }
        }

        return false;
    }

    private static boolean containsCheckBox(VBox container) {
        for (Node node : container.getChildren()) {
            if (node instanceof CheckBox) {
                return true;
            }
        }

        return false;
    }

    private static void selectFirstRadioButton(VBox container) {
        for (Node node : container.getChildren()) {
            if (node instanceof HBox row) {
                for (Node rowNode : row.getChildren()) {
                    if (rowNode instanceof RadioButton rb) {
                        rb.setSelected(true);
                        return;
                    }
                }
            }
            if (node instanceof RadioButton rb) {
                rb.setSelected(true);
                return;
            }
        }
    }

    private static boolean anyRadioButtonSelected(VBox container) {
        for (Node node : container.getChildren()) {
            if (node instanceof HBox row) {
                for (Node rowNode : row.getChildren()) {
                    if (rowNode instanceof RadioButton rb && rb.isSelected()) {
                        return true;
                    }
                }
            }
            if (node instanceof RadioButton rb && rb.isSelected()) {
                return true;
            }
        }
        return false;
    }

    private static void selectFirstCheckBox(VBox container) {
        for (Node node : container.getChildren()) {
            if (node instanceof CheckBox checkBox) {
                checkBox.setSelected(true);
                return;
            }
        }
    }

    private static boolean anyCheckBoxSelected(VBox container) {
        for (Node node : container.getChildren()) {
            if (node instanceof CheckBox checkBox && checkBox.isSelected()) {
                return true;
            }
        }

        return false;
    }

    private static void runOnFxThread(ThrowingRunnable action) throws Exception {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable throwable) {
                error.set(throwable);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        Throwable throwable = error.get();

        if (throwable instanceof Exception exception) {
            throw exception;
        }

        if (throwable instanceof Error errorThrown) {
            throw errorThrown;
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}