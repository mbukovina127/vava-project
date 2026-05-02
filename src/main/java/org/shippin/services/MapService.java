package org.shippin.services;

public class MapService {

    private static final String API_KEY = "AIzaSyAuHM5wJRSqhMhzLQSj_VIpwvamKoaZjrc";

    public String buildStaticMapUrl(double fromLat, double fromLon, double toLat, double toLon) {
        double centerLat = (fromLat + toLat) / 2.0;
        double centerLon = (fromLon + toLon) / 2.0;
        int zoom = calculateZoom(calculateDistance(fromLat, fromLon, toLat, toLon));

        return String.format(
                "https://maps.googleapis.com/maps/api/staticmap?" +
                        "center=%.4f,%.4f&zoom=%d&size=900x200" +
                        "&markers=color:white|%.4f,%.4f" +
                        "&markers=color:red|%.4f,%.4f" +
                        "&path=color:0xff0000|weight:2|%.4f,%.4f|%.4f,%.4f" +
                        "&key=%s",
                centerLat, centerLon, zoom,
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
            throw new IllegalArgumentException("No geocoding results for postal code: " + postalCode);
        }

        com.google.gson.JsonObject location = json.getAsJsonArray("results")
                .get(0).getAsJsonObject()
                .getAsJsonObject("geometry")
                .getAsJsonObject("location");

        return new double[]{ location.get("lat").getAsDouble(), location.get("lng").getAsDouble() };
    }

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R    = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a    = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public int calculateZoom(double distance) {
        if (distance < 20)  return 11;
        if (distance < 50)  return 10;
        if (distance < 100) return 9;
        if (distance < 200) return 8;
        return 7;
    }
}
