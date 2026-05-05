package org.shippin.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import lombok.extern.log4j.Log4j2;
import org.shippin.domain.Shipment;
import org.shippin.domain.enums.State;
import org.shippin.services.MapService;
import org.shippin.services.NavigationService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Log4j2
public class MapOfShipmentsController extends BaseController<List<Shipment>> implements Initializable {

    // ── pin colors — single source of truth for map URL and legend ─────────
    static final String COLOR_ORIGIN          = "#00AA00";
    static final String COLOR_NOT_READY       = "#FF5555";
    static final String COLOR_READY           = "#3366FF";
    static final String COLOR_BEING_DELIVERED = "#FF9900";
    static final String COLOR_CANCELED        = "#999999";

    @FXML private StackPane mapContainer;
    @FXML private ImageView mapImageView;
    @FXML private Label     mapFallbackLabel;
    @FXML private HBox      legendRow;

    private final MapService mapService = new MapService();

    private List<Shipment> shipments = List.of();

    @Override
    protected Class<List<Shipment>> getDataType() {
        return (Class<List<Shipment>>) (Class<?>) List.class;
    }

    @Override
    protected void onData(List<Shipment> data) {
        if (data != null) {
            this.shipments = data;
        } else {
            filterShipments();
        }
        loadMapImage();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("MapOfShipmentsController initialized");
        buildLegend();
        filterShipments();
    }

    private void filterShipments() {
        try {
            shipments = shipments.stream()
                    .filter(s -> s.getState() != State.DELIVERED && s.getState() != State.FAILED)
                    .toList();
            log.info("Loaded {} ongoing shipments", shipments.size());
        } catch (Exception e) {
            log.error("Failed to filter shipments", e);
            showMapFallback();
        }
    }

    private void loadMapImage() {
        if (shipments.isEmpty()) {
            showMapFallback();
            return;
        }
        try {
            String url = buildMultipleShipmentsUrl();
            Image mapImage = new Image(url, true);
            mapImage.errorProperty().addListener((obs, old, isError) -> {
                if (isError) showMapFallback();
            });
            mapImageView.setImage(mapImage);
            mapImageView.setVisible(true);
            mapFallbackLabel.setVisible(false);
        } catch (Exception ex) {
            log.error("Failed to load map", ex);
            showMapFallback();
        }
    }

    private String buildMultipleShipmentsUrl() {
        StringBuilder markers = new StringBuilder();
        StringBuilder paths   = new StringBuilder();
        int markerCount = 0;

        for (Shipment s : shipments) {
            double fromLat = 48.7;
            double fromLon = 19.15;

            if (s.getStartCoordinate() != null) {
                fromLat = s.getStartCoordinate().getX();
                fromLon = s.getStartCoordinate().getY();
            }

            if (s.getDest_region() > 0) {
                try {
                    double[] coords = mapService.fetchCoordinatesForPostalCode(s.getDest_region());
                    double toLat = coords[0] + (Math.random() - 0.5) * 0.06;
                    double toLon = coords[1] + (Math.random() - 0.5) * 0.06;

                    if (markerCount > 0) markers.append("&");
                    markers.append(String.format("markers=color:%s|%.4f,%.4f",
                            toMapColor(COLOR_ORIGIN), fromLat, fromLon));

                    String destColor = switch (s.getState()) {
                        case NOT_READY          -> toMapColor(COLOR_NOT_READY);
                        case READY_FOR_DELIVERY -> toMapColor(COLOR_READY);
                        case BEING_DELIVERED    -> toMapColor(COLOR_BEING_DELIVERED);
                        case CANCELED           -> toMapColor(COLOR_CANCELED);
                        default                 -> toMapColor(COLOR_NOT_READY);
                    };

                    markers.append(String.format("&markers=color:%s|%.4f,%.4f", destColor, toLat, toLon));
                    paths.append(String.format("&path=color:0x0000ff|weight:2|%.4f,%.4f|%.4f,%.4f",
                            fromLat, fromLon, toLat, toLon));

                    markerCount++;
                } catch (Exception e) {
                    log.error("Failed to resolve coordinates for shipment {}", s.getShipment_id(), e);
                }
            }
        }

        return String.format(
                "https://maps.googleapis.com/maps/api/staticmap?size=1000x300&%s%s&key=%s",
                markers, paths,
                "AIzaSyAuHM5wJRSqhMhzLQSj_VIpwvamKoaZjrc");
    }

    private void buildLegend() {
        ResourceBundle bundle = NavigationService.getBundle();

        record LegendEntry(String color, String key) {}
        List<LegendEntry> entries = List.of(
                new LegendEntry(COLOR_ORIGIN,          "map_of_shipments.legend.origin"),
                new LegendEntry(COLOR_NOT_READY,       "map_of_shipments.legend.not_ready"),
                new LegendEntry(COLOR_READY,           "map_of_shipments.legend.ready_for_delivery"),
                new LegendEntry(COLOR_BEING_DELIVERED, "map_of_shipments.legend.being_delivered"),
                new LegendEntry(COLOR_CANCELED,        "map_of_shipments.legend.canceled")
        );

        legendRow.getChildren().clear();
        for (LegendEntry e : entries) {
            legendRow.getChildren().add(buildLegendItem(e.color(), bundle.getString(e.key())));
        }
    }

    private HBox buildLegendItem(String color, String text) {
        Region dot = new Region();
        dot.setPrefSize(14, 14);
        dot.setMinSize(14, 14);
        dot.setMaxSize(14, 14);
        dot.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 7;");

        Label label = new Label(text);
        label.getStyleClass().add("map-legend-label");

        HBox item = new HBox(6, dot, label);
        item.getStyleClass().add("map-legend-item");
        item.setAlignment(Pos.CENTER_LEFT);
        return item;
    }

    private static String toMapColor(String hex) {
        return "0x" + hex.substring(1);
    }

    private void showMapFallback() {
        mapImageView.setVisible(false);
        mapFallbackLabel.setVisible(true);
    }
}
