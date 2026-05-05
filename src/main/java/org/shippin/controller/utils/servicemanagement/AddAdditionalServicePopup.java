package org.shippin.controller.utils.servicemanagement;

import java.util.ResourceBundle;

import org.shippin.controller.ServicesManagementController;
import org.shippin.controller.utils.InputValidator;
import org.shippin.controller.utils.ServicesManagementPopup;
import org.shippin.domain.AdditionalService;
import org.shippin.domain.enums.ServiceType;
import javafx.util.StringConverter;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class AddAdditionalServicePopup extends ServicesManagementPopup {

    public AddAdditionalServicePopup(ResourceBundle resources) {
        super(resources);
    }

    public void show(ServicesManagementController controller) {
        VBox popup = createPopupRoot();
        popup.getStyleClass().addAll("service-popup", "service-add-popup");
        popup.setMaxWidth(720);
        popup.setPrefWidth(720);

        Label title = createPopupTitle(t("%services_management.add_popup.title"));

        GridPane form = new GridPane();
        form.getStyleClass().add("popup-form");
        form.setHgap(16);
        form.setVgap(0);
        form.setMaxWidth(Double.MAX_VALUE);

        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setMinWidth(170);
        labelColumn.setPrefWidth(170);

        ColumnConstraints inputColumn = new ColumnConstraints();
        inputColumn.setHgrow(Priority.ALWAYS);

        form.getColumnConstraints().addAll(labelColumn, inputColumn);

        // --- Slovak name ---
        Label nameSkLabel = createFormLabel(t("%services_management.add_popup.name_sk"));
        TextField nameSkField = createTextField(t("%services_management.add_popup.name_sk_prompt"));
        Label nameSkError = createErrorLabel();

        // --- English name ---
        Label nameEnLabel = createFormLabel(t("%services_management.add_popup.name_en"));
        TextField nameEnField = createTextField(t("%services_management.add_popup.name_en_prompt"));
        Label nameEnError = createErrorLabel();

        // --- Slovak description ---
        Label descriptionSkLabel = createFormLabel(t("%services_management.add_popup.description_sk"));
        TextArea descriptionSkArea = createTextArea(t("%services_management.add_popup.description_sk_prompt"));
        Label descriptionSkError = createErrorLabel();

        // --- English description ---
        Label descriptionEnLabel = createFormLabel(t("%services_management.add_popup.description_en"));
        TextArea descriptionEnArea = createTextArea(t("%services_management.add_popup.description_en_prompt"));
        Label descriptionEnError = createErrorLabel();

        // --- Service type ---
        Label serviceTypeLabel = createFormLabel(t("%services_management.add_popup.service_type"));
        ComboBox<ServiceType> serviceTypeComboBox = createServiceTypeComboBox();
        serviceTypeComboBox.setPromptText(t("%services_management.add_popup.type_prompt"));
        serviceTypeComboBox.setConverter(new StringConverter<ServiceType>() {
            @Override
            public String toString(ServiceType serviceType) {
                return serviceType == null ? "" : serviceType.getLocalized(resources);
            }

            @Override
            public ServiceType fromString(String string) {
                return null;
            }
        });
        Label serviceTypeError = createErrorLabel();

        // --- Default cost ---
        Label defaultCostLabel = createFormLabel(t("%services_management.add_popup.default_cost"));
        TextField defaultCostField = createTextField("0.00");
        Label defaultCostError = createErrorLabel();

        // --- Cost modifier ---
        Label costModifierLabel = createFormLabel(t("%services_management.add_popup.cost_modifier"));
        TextField costModifierField = createTextField("1.00");
        Label costModifierError = createErrorLabel();

        // Clear errors when user edits fields
        nameSkField.textProperty().addListener((obs, old, val) -> nameSkError.setText(""));
        nameEnField.textProperty().addListener((obs, old, val) -> nameEnError.setText(""));
        descriptionSkArea.textProperty().addListener((obs, old, val) -> descriptionSkError.setText(""));
        descriptionEnArea.textProperty().addListener((obs, old, val) -> descriptionEnError.setText(""));
        serviceTypeComboBox.valueProperty().addListener((obs, old, val) -> serviceTypeError.setText(""));
        defaultCostField.textProperty().addListener((obs, old, val) -> defaultCostError.setText(""));
        costModifierField.textProperty().addListener((obs, old, val) -> costModifierError.setText(""));

        int row = 0;

        form.add(nameSkLabel, 0, row);
        form.add(nameSkField, 1, row++);
        form.add(nameSkError, 1, row++);

        form.add(nameEnLabel, 0, row);
        form.add(nameEnField, 1, row++);
        form.add(nameEnError, 1, row++);

        form.add(descriptionSkLabel, 0, row);
        form.add(descriptionSkArea, 1, row++);
        form.add(descriptionSkError, 1, row++);

        form.add(descriptionEnLabel, 0, row);
        form.add(descriptionEnArea, 1, row++);
        form.add(descriptionEnError, 1, row++);

        form.add(serviceTypeLabel, 0, row);
        form.add(serviceTypeComboBox, 1, row++);
        form.add(serviceTypeError, 1, row++);

        form.add(defaultCostLabel, 0, row);
        form.add(defaultCostField, 1, row++);
        form.add(defaultCostError, 1, row++);

        form.add(costModifierLabel, 0, row);
        form.add(costModifierField, 1, row++);
        form.add(costModifierError, 1, row++);

        HBox buttons = new HBox(18);
        buttons.setAlignment(Pos.CENTER_LEFT);

        Button cancelButton = new Button(t("%services_management.button.cancel"));
        cancelButton.getStyleClass().addAll("popup-button", "popup-secondary-button");
        cancelButton.setPrefSize(160, 42);
        cancelButton.setOnAction(e -> controller.hideModal());

        Button addButton = new Button(t("%services_management.add_popup.button_confirm"));
        addButton.getStyleClass().addAll("popup-button", "popup-primary-button");
        addButton.setPrefSize(160, 42);
        addButton.setOnAction(e -> handleAddAdditionalServiceSaved(
                controller,
                nameSkField.getText(),
                nameEnField.getText(),
                descriptionSkArea.getText(),
                descriptionEnArea.getText(),
                serviceTypeComboBox.getValue(),
                defaultCostField.getText(),
                costModifierField.getText(),
                nameSkError,
                nameEnError,
                descriptionSkError,
                descriptionEnError,
                serviceTypeError,
                defaultCostError,
                costModifierError
        ));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        buttons.getChildren().addAll(cancelButton, spacer, addButton);

        popup.getChildren().addAll(title, form, buttons);

        controller.showModal(popup);
    }

    private void handleAddAdditionalServiceSaved(
            ServicesManagementController controller,
            String nameSk,
            String nameEn,
            String descriptionSk,
            String descriptionEn,
            ServiceType serviceType,
            String defaultCostString,
            String costModifierString,
            Label nameSkError,
            Label nameEnError,
            Label descriptionSkError,
            Label descriptionEnError,
            Label serviceTypeError,
            Label defaultCostError,
            Label costModifierError
    ) {
        clearErrors(
                nameSkError,
                nameEnError,
                descriptionSkError,
                descriptionEnError,
                serviceTypeError,
                defaultCostError,
                costModifierError
        );

        boolean valid = true;

        String trimmedNameSk = clean(nameSk);
        String trimmedNameEn = clean(nameEn);
        String trimmedDescriptionSk = clean(descriptionSk);
        String trimmedDescriptionEn = clean(descriptionEn);

        if (!InputValidator.isNotBlank(trimmedNameSk)) {
            nameSkError.setText(t("%services_management.add_popup.error_name_sk_required"));
            valid = false;
        } else if (!InputValidator.isValidLength(trimmedNameSk, 80)) {
            nameSkError.setText(t("%services_management.add_popup.error_name_too_long"));
            valid = false;
        }

        if (!InputValidator.isNotBlank(trimmedNameEn)) {
            nameEnError.setText(t("%services_management.add_popup.error_name_en_required"));
            valid = false;
        } else if (!InputValidator.isValidLength(trimmedNameEn, 80)) {
            nameEnError.setText(t("%services_management.add_popup.error_name_too_long"));
            valid = false;
        }

        if (!InputValidator.isNotBlank(trimmedDescriptionSk)) {
            descriptionSkError.setText(t("%services_management.add_popup.error_description_sk_required"));
            valid = false;
        } else if (!InputValidator.isValidLength(trimmedDescriptionSk, 255)) {
            descriptionSkError.setText(t("%services_management.add_popup.error_description_too_long"));
            valid = false;
        }

        if (!InputValidator.isNotBlank(trimmedDescriptionEn)) {
            descriptionEnError.setText(t("%services_management.add_popup.error_description_en_required"));
            valid = false;
        } else if (!InputValidator.isValidLength(trimmedDescriptionEn, 255)) {
            descriptionEnError.setText(t("%services_management.add_popup.error_description_too_long"));
            valid = false;
        }

        if (serviceType == null) {
            serviceTypeError.setText(t("%services_management.add_popup.error_type_required"));
            valid = false;
        }

        Float defaultCost = parseNullableFloat(defaultCostString);
        if (!InputValidator.isNotBlank(defaultCostString)) {
            defaultCostError.setText(t("%services_management.add_popup.error_default_cost_required"));
            valid = false;
        } else if (defaultCost == null) {
            defaultCostError.setText(t("%services_management.add_popup.error_invalid_number"));
            valid = false;
        } else if (defaultCost < 0) {
            defaultCostError.setText(t("%services_management.add_popup.error_default_cost_negative"));
            valid = false;
        }

        Float costModifier = parseNullableFloat(costModifierString);
        if (!InputValidator.isNotBlank(costModifierString)) {
            costModifierError.setText(t("%services_management.add_popup.error_cost_modifier_required"));
            valid = false;
        } else if (costModifier == null) {
            costModifierError.setText(t("%services_management.add_popup.error_invalid_number"));
            valid = false;
        } else if (costModifier <= 0) {
            costModifierError.setText(t("%services_management.add_popup.error_cost_modifier_positive"));
            valid = false;
        }

        if (!valid) {
            return;
        }

        AdditionalService service = new AdditionalService();
        service.setName(trimmedNameSk);
        service.setName_en(trimmedNameEn);
        service.setDescription(trimmedDescriptionSk);
        service.setDescription_en(trimmedDescriptionEn);
        service.setServiceType(serviceType);
        service.setDefaultCost(defaultCost);
        service.setCostModifier(costModifier);

        controller.createAdditionalService(service);
        controller.hideModal();
    }

    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("popup-form-label");
        label.setMinWidth(170);
        label.setPrefWidth(170);
        label.setWrapText(false);
        return label;
    }

    private TextField createTextField(String promptText) {
        TextField textField = new TextField();
        textField.setPromptText(promptText);
        textField.getStyleClass().add("popup-input");
        textField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(textField, Priority.ALWAYS);
        return textField;
    }

    private TextArea createTextArea(String promptText) {
        TextArea textArea = new TextArea();
        textArea.setPromptText(promptText);
        textArea.getStyleClass().add("popup-input");
        textArea.setPrefRowCount(3);
        textArea.setPrefHeight(86);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        return textArea;
    }

    private ComboBox<ServiceType> createServiceTypeComboBox() {
        ComboBox<ServiceType> comboBox = new ComboBox<>();
        comboBox.getItems().setAll(ServiceType.values());
        comboBox.getStyleClass().add("popup-input");
        comboBox.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(comboBox, Priority.ALWAYS);
        return comboBox;
    }

    private Label createErrorLabel() {
        Label label = new Label("");
        label.getStyleClass().add("status-label");
        label.setManaged(false);
        label.setWrapText(true);
        label.textProperty().addListener((obs, old, val) -> label.setManaged(val != null && !val.isEmpty()));
        return label;
    }

    private void clearErrors(Label... labels) {
        for (Label label : labels) {
            label.setText("");
        }
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private Float parseNullableFloat(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Float.parseFloat(value.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}