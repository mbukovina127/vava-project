package org.shippin.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import org.shippin.controller.utils.servicemanagement.AddAdditionalServicePopup;
import org.shippin.controller.utils.servicemanagement.DeleteAdditionalServicePopup;
import org.shippin.controller.utils.GenericPopup;
import org.shippin.domain.AdditionalService;
import org.shippin.domain.BriefWarehouse;
import org.shippin.services.AdditionalServicesService;

import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class ServicesManagementController extends BaseController<Void> implements Initializable {

    @FXML
    private ListView<AdditionalService> servicesListView;

    @FXML
    private Button addServiceButton;

    private ResourceBundle bundle;

    private final DecimalFormat moneyFormat = new DecimalFormat("0.00€");

	private ResourceBundle resources;

	private AdditionalServicesService additionalServicesService;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	    this.resources = resources;
	    this.bundle = resources != null
	            ? resources
	            : ResourceBundle.getBundle("i18n.messages", Locale.getDefault());

	    AdditionalServiceCell.loadDeleteIcon();

	    servicesListView.setCellFactory(listView -> new AdditionalServiceCell());
	    this.additionalServicesService = AdditionalServicesService.getInstance();

	    addServiceButton.setOnAction(event -> handleAddNewService());

	    try {
	        servicesListView.getItems().setAll(
	                AdditionalServicesService.getInstance().getAllServices()
	        );
	    } catch (SQLException e) {
	        e.printStackTrace();
	        new GenericPopup(this.resources)
	                .showOkPopup(this, "%generic.failed_to_fetch", "%generic.database_problem");
	    }
	}

	private void handleAddNewService() {
	    new AddAdditionalServicePopup(resources).show(this);
	}
    
	public void createAdditionalService(AdditionalService service) {
	    if (service == null) {
	        return;
	    }

	    try {
	        AdditionalServicesService.getInstance().createService(service);

	    } catch (SQLException e) {
	        e.printStackTrace();
	        new GenericPopup(this.resources)
	                .showOkPopup(this,
	                        "%generic.failed_to_insert",
	                        "%generic.database_problem");
	    }
	    
	    try {
        	servicesListView.getItems().setAll(additionalServicesService.getAllServices());
		} catch (SQLException e) {
			e.printStackTrace();
			new GenericPopup(this.resources).showOkPopup(this, "%generic.failed_to_fetch", "%generic.database_problem");
		}
	}
	
    public void deleteAdditionalService(AdditionalService service) {
        if (service == null) {
            return;
        }

        try {
            additionalServicesService.deleteService(service.getId());

        } catch (SQLException e) {
            e.printStackTrace();
            new GenericPopup(this.resources)
                    .showOkPopup(this,
                            "%generic.failed_to_delete",
                            "%generic.database_problem");
        }
        
        try {
        	servicesListView.getItems().setAll(additionalServicesService.getAllServices());
		} catch (SQLException e) {
			e.printStackTrace();
			new GenericPopup(this.resources).showOkPopup(this, "%generic.failed_to_fetch", "%generic.database_problem");
		}
    }

    private class AdditionalServiceCell extends ListCell<AdditionalService> {
    	
    	private static Image loadImage(String path) {
            URL resource = AdditionalServiceCell.class.getResource(path);
            if (resource == null) {
                throw new IllegalStateException("Missing icon resource: " + path);
            }
            return new Image(resource.toExternalForm());
        }
    	
    	private static Image deleteIcon;
    	
    	public static void loadDeleteIcon() {
    		AdditionalServiceCell.deleteIcon = loadImage("/icons/png-dark/delete_black.png");
    	}

    	private Button createTableIconButton(Image image,
		                double imageWidth,
		                double imageHeight,
		                double buttonWidth,
		                double buttonHeight) {
			ImageView imageView = new ImageView(image);
			imageView.setFitWidth(imageWidth);
			imageView.setFitHeight(imageHeight);
			imageView.setPreserveRatio(true);
			
			Button button = new Button();
			button.setGraphic(imageView);
			button.setPrefWidth(buttonWidth);
			button.setPrefHeight(buttonHeight);
			button.getStyleClass().add("table-icon-button");
			button.setAlignment(Pos.CENTER);
			
			return button;
		}
    	
    	@Override
    	protected void updateItem(AdditionalService service, boolean empty) {
    	    super.updateItem(service, empty);

    	    if (empty || service == null) {
    	        setGraphic(null);
    	        setText(null);
    	        return;
    	    }

    	    HBox row = new HBox();
    	    row.getStyleClass().add("service-row");
    	    row.prefWidthProperty().bind(servicesListView.widthProperty().subtract(35));

    	    VBox textBox = new VBox(4);
    	    textBox.getStyleClass().add("service-text-box");

    	    Label nameLabel = new Label(getLocalizedServiceName(service));
    	    nameLabel.getStyleClass().add("service-name");

    	    Label descriptionLabel = new Label(getLocalizedServiceDescription(service));
    	    descriptionLabel.getStyleClass().add("service-description");

    	    textBox.getChildren().addAll(nameLabel, descriptionLabel);

    	    Region spacer = new Region();
    	    HBox.setHgrow(spacer, Priority.ALWAYS);

    	    Label typeBadge = new Label(service.getServiceType() == null
    	            ? bundle.getString("services_management.no_type")
    	            : service.getServiceType().name());

    	    typeBadge.getStyleClass().add("service-type-badge");

    	    VBox priceBox = new VBox(2);
    	    priceBox.getStyleClass().add("service-price-box");

    	    Label defaultCostLabel = new Label(
    	            bundle.getString("services_management.default_cost") + " " +
    	                    moneyFormat.format(service.getDefaultCost())
    	    );

    	    Label modifierLabel = new Label(
    	            bundle.getString("services_management.cost_modifier") + " " +
    	                    service.getCostModifier()
    	    );

    	    defaultCostLabel.getStyleClass().add("service-price");
    	    modifierLabel.getStyleClass().add("service-modifier");

    	    priceBox.getChildren().addAll(defaultCostLabel, modifierLabel);

    	    Button deleteButton = createTableIconButton(
    	    		AdditionalServiceCell.deleteIcon,
    	    		 22.0, 22.0, 32.0, 32.0);
    	    
    	    deleteButton.getStyleClass().add("delete-button");
    	    deleteButton.setOnAction(event ->
		            new DeleteAdditionalServicePopup(resources).show(ServicesManagementController.this, service)
		    );

    	    HBox rightSide = new HBox(28);
    	    rightSide.getStyleClass().add("service-right-side");
    	    rightSide.getChildren().addAll(typeBadge, priceBox, deleteButton);

    	    row.getChildren().addAll(textBox, spacer, rightSide);

    	    setGraphic(row);
    	    setText(null);
    	}
    	
    	private boolean isEnglish() {
    	    return bundle != null
    	            && bundle.getLocale() != null
    	            && "en".equalsIgnoreCase(bundle.getLocale().getLanguage());
    	}

    	private String getLocalizedServiceName(AdditionalService service) {
    	    if (isEnglish()) {
    	        return service.getName_en() != null && !service.getName_en().isBlank()
    	                ? service.getName_en()
    	                : service.getName();
    	    }

    	    return service.getName();
    	}

    	private String getLocalizedServiceDescription(AdditionalService service) {
    	    if (isEnglish()) {
    	        return service.getDescription_en() != null && !service.getDescription_en().isBlank()
    	                ? service.getDescription_en()
    	                : service.getDescription();
    	    }

    	    return service.getDescription();
    	}
    }
}