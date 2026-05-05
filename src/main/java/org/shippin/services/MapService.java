package org.shippin.services;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@NoArgsConstructor
public class MapService {

    private static final String API_KEY = "AIzaSyAuHM5wJRSqhMhzLQSj_VIpwvamKoaZjrc";
    static MapService instance;
    
    public static MapService getInstance() {
    	if (instance == null) {
    		instance = new MapService();
    	}
    	
		return instance;
    }

    public String buildStaticMapUrl(double fromLat, double fromLon, double toLat, double toLon) {
        return String.format(
                "https://maps.googleapis.com/maps/api/staticmap?" +
                        "size=900x200" +
                        "&markers=color:white|%.4f,%.4f" +
                        "&markers=color:red|%.4f,%.4f" +
                        "&path=color:0xff0000|weight:2|%.4f,%.4f|%.4f,%.4f" +
                        "&key=%s",
                fromLat, fromLon,
                toLat, toLon,
                fromLat, fromLon, toLat, toLon,
                API_KEY
        );
    }

    /**
     * Fetches [lat, lon] for a Slovak postal code via the Geocoding API.
     * Blocking — call from a background thread.
     */
    public double[] fetchCoordinatesForPostalCode(int postalCode) throws Exception {
        log.debug("Fetching coordinates for postal code {}", postalCode);
        String psc = String.format("%05d", postalCode);
        String url = "https://maps.googleapis.com/maps/api/geocode/json?address="
                + java.net.URLEncoder.encode(psc + " Slovakia", "UTF-8")
                + "&key=" + API_KEY;

        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .GET()
                .build();

        java.net.http.HttpResponse<String> response = client.send(request,
                java.net.http.HttpResponse.BodyHandlers.ofString());

        com.google.gson.JsonObject json = com.google.gson.JsonParser
                .parseString(response.body()).getAsJsonObject();

        if (!json.has("results") || json.getAsJsonArray("results").isEmpty()) {
            log.warn("No geocoding results for postal code {}", postalCode);
            throw new IllegalArgumentException("No geocoding results for postal code: " + postalCode);
        }

        com.google.gson.JsonObject location = json.getAsJsonArray("results")
                .get(0).getAsJsonObject()
                .getAsJsonObject("geometry")
                .getAsJsonObject("location");

        double lat = location.get("lat").getAsDouble();
        double lng = location.get("lng").getAsDouble();
        log.info("Resolved postal code {} to [{}, {}]", postalCode, lat, lng);
        return new double[]{lat, lng};
    }
}
