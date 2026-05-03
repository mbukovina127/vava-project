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
            System.out.println("DEBUG: Loaded " + shipments.size() + " shipments");
            log.info("✅ Loaded {} shipments", shipments.size());
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

            // ===== HARDCODED TEST PSČ =====
            int testWarehousePsc = 80000;
            if (s.getShipment_id() == 159) testWarehousePsc = 4001;
            if (s.getShipment_id() == 158) testWarehousePsc = 94500;

            try {
                double[] coords = mapService.fetchCoordinatesForPostalCode(testWarehousePsc);
                fromLat = coords[0];
                fromLon = coords[1];
                System.out.println("DEBUG: TEST PSC " + testWarehousePsc + " -> lat=" + fromLat + ", lon=" + fromLon);
            } catch (Exception e) {
                System.out.println("ERROR loading warehouse coords: " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println("DEBUG: About to load dest for shipment " + s.getShipment_id());

            if (s.getDest_region() > 0) {
                try {
                    System.out.println("DEBUG: Fetching dest coords for PSC " + s.getDest_region());
                    double[] coords = mapService.fetchCoordinatesForPostalCode(s.getDest_region());
                    double toLat = coords[0];
                    double toLon = coords[1];

                    System.out.println("DEBUG: Shipment #" + s.getShipment_id() + " dest OK");

                    if (markerCount > 0) markers.append("&");
                    markers.append(String.format("markers=color:green|%.4f,%.4f", fromLat, fromLon));
                    markers.append(String.format("&markers=color:red|%.4f,%.4f", toLat, toLon));
                    paths.append(String.format("&path=color:0x0000ff|weight:1|%.4f,%.4f|%.4f,%.4f",
                            fromLat, fromLon, toLat, toLon));

                    markerCount++;
                } catch (Exception e) {
                    System.out.println("ERROR loading dest coords: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("DEBUG: Shipment #" + s.getShipment_id() + " has no dest_region!");
            }
        }

        System.out.println("🗺️ Final markers count: " + markerCount);

        String url = String.format(
                "https://maps.googleapis.com/maps/api/staticmap?" +
                        "size=1000x600&" +
                        "%s%s" +
                        "&key=%s",
                markers.toString(),
                paths.toString(),
                "AIzaSyAuHM5wJRSqhMhzLQSj_VIpwvamKoaZjrc"
        );

        System.out.println("🔗 Map URL: " + url);
        return url;
    }

    private void showMapFallback() {
        mapImageView.setVisible(false);
        mapFallbackLabel.setVisible(true);
        mapFallbackLabel.setText("Map unavailable or no shipments");
    }
}