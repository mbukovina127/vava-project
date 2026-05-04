package org.shippin.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import lombok.extern.log4j.Log4j2;
import org.shippin.domain.Shipment;
import org.shippin.services.MapService;
import org.shippin.services.NavigationService;
import org.shippin.services.ShipmentService;
import org.shippin.domain.enums.State;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Log4j2
public class MapOfShipmentsController extends BaseController<List<Shipment>> implements Initializable {

    @FXML private StackPane mapContainer;
    @FXML private ImageView mapImageView;
    @FXML private javafx.scene.control.Label mapFallbackLabel;

    private final MapService mapService = new MapService();
    private final ShipmentService shipmentService = new ShipmentService();

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
            loadAllShipments();
        }
        loadMapImage();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("MapOfShipmentsController initialized");
        loadAllShipments();
    }

    private void loadAllShipments() {
        try {
            shipments = shipmentService.getAllShipments();

            //ukáže len ongoing shipments
            shipments = shipments.stream()
                    .filter(s -> s.getState() != State.DELIVERED &&
                            s.getState() != State.FAILED)
                    .toList();

            System.out.println("DEBUG: Loaded " + shipments.size() + " ongoing shipments");
            log.info("✅ Loaded {} ongoing shipments", shipments.size());
            // ...
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            log.error("❌ Failed to load shipments", e);
            showMapFallback();
        }
    }


    private void loadMapImage() {
        if (shipments.isEmpty()) {
            showMapFallback();
            return;
        }

        try {
            // Zostav všetky trasy do jedného URL
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
        StringBuilder paths = new StringBuilder();

        int markerCount = 0;
        for (Shipment s : shipments) {

            double fromLat = 48.7;
            double fromLon = 19.15;

            if (s.getStartCoordinate() != null) {
                fromLat = s.getStartCoordinate().getX();
                fromLon = s.getStartCoordinate().getY();


                /*
                // FILTER NA Slovensko + okolie
                if (fromLat < 47.0|| fromLat > 50.5 || fromLon < 14.0 || fromLon > 23.5) {
                    log.warn("Warehouse outside region: {}, {}", fromLat, fromLon);
                    continue;
                }*/
            }

            if (s.getDest_region() > 0) {
                try {
                    double[] coords = mapService.fetchCoordinatesForPostalCode(s.getDest_region());
                    double toLat = coords[0];
                    double toLon = coords[1];

                    //RANDOM OFFSET
                    double offsetLat = (Math.random() - 0.5) * 0.06;  // ±3km na sever/juh
                    double offsetLon = (Math.random() - 0.5) * 0.06;  // ±3km na východ/západ
                    toLat += offsetLat;
                    toLon += offsetLon;

                    /*
                    // FILTER NA destináciu!
                    if (toLat < 47.0 || toLat > 50.5 || toLon < 14.0 || toLon > 23.5) {
                        log.warn("Destination outside region: {}, {}", toLat, toLon);
                        continue;
                    }*/

                    if (markerCount > 0) markers.append("&");
                    markers.append(String.format("markers=color:green|%.4f,%.4f", fromLat, fromLon));
                    markers.append(String.format("&markers=color:red|%.4f,%.4f", toLat, toLon));
                    paths.append(String.format("&path=color:0x0000ff|weight:1|%.4f,%.4f|%.4f,%.4f",
                            fromLat, fromLon, toLat, toLon));

                    markerCount++;
                } catch (Exception e) {
                    System.out.println("ERROR: " + e.getMessage());
                }
            }
        }

        String url = String.format(
                "https://maps.googleapis.com/maps/api/staticmap?" +
                        "size=1000x600&" +
                        "%s%s" +
                        "&key=%s",
                markers.toString(),
                paths.toString(),
                "AIzaSyAuHM5wJRSqhMhzLQSj_VIpwvamKoaZjrc"
        );

        return url;
    }

    private void showMapFallback() {
        mapImageView.setVisible(false);
        mapFallbackLabel.setVisible(true);
        mapFallbackLabel.setText("Map unavailable or no shipments");
    }
}