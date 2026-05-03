package org.shippin.controller.utils;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import org.shippin.controller.BaseController;
import org.shippin.controller.WarehouseManagementController;
import org.shippin.domain.BriefWarehouse;
import org.shippin.domain.formatted.PriceListFormatted;
import org.shippin.domain.formatted.RegionTableFormatted;
import org.shippin.services.WarehouseParsingService;
import org.shippin.services.WarehouseService;
import org.shippin.util.WarehouseConvertor;
import org.shippin.util.io.FilePicker;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import lombok.AllArgsConstructor;

public class WarehouseManagementPopup extends GenericPopup {
	public WarehouseManagementPopup(ResourceBundle resources) {
		super(resources);
	}
	
	 protected Image loadImage(String path) {
	        URL resource = getClass().getResource(path);
	        if (resource == null) {
	            throw new IllegalStateException("Missing icon resource: " + path);
	        }
	        return new Image(resource.toExternalForm());
	    }
	
	 protected Button createUploadButton(String text) {
	    Image importIcon = loadImage("/icons/png-dark/import_black.png");

	    ImageView iconView = new ImageView(importIcon);
	    iconView.setFitWidth(18);
	    iconView.setFitHeight(18);
	    iconView.setPreserveRatio(true);

	    Label label = new Label(text);
	    label.getStyleClass().add("upload-button-text");

	    HBox content = new HBox(12, iconView, label);
	    content.setAlignment(Pos.CENTER_LEFT);

	    Button button = new Button();
	    button.setGraphic(content);
	    button.setAlignment(Pos.CENTER_LEFT);
	    button.setMaxWidth(Double.MAX_VALUE);
	    button.setPrefHeight(40);
	    button.getStyleClass().addAll("popup-button", "upload-button");
	    button.setUserData(label);

	    return button;
	}
}
