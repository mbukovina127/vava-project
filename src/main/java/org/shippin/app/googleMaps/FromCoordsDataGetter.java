package org.shippin.app;

import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public class FromCoordsDataGetter {

    private static final String API_KEY = "AIzaSyAuHM5wJRSqhMhzLQSj_VIpwvamKoaZjrc";

    public static void reverse(double lat, double lng) {
    	try {

    	    String url = "https://maps.googleapis.com/maps/api/geocode/json"
    	            + "?latlng=" + lat + "," + lng
    	            + "&key=" + API_KEY;

    	    HttpClient client = HttpClient.newHttpClient();
    	    HttpRequest request = HttpRequest.newBuilder()
    	            .uri(URI.create(url))
    	            .GET()
    	            .build();

    	    HttpResponse<String> response =
    	            client.send(request, HttpResponse.BodyHandlers.ofString());

    	    if (response.statusCode() != 200) {
    	        System.out.println("HTTP error: " + response.statusCode());
    	        return;
    	    }

    	    Gson gson = new Gson();
    	    JsonObject root = gson.fromJson(response.body(), JsonObject.class);

    	    JsonArray results = root.getAsJsonArray("results");
    	    if (results == null || results.size() == 0) {
    	        System.out.println("No results");
    	        return;
    	    }

    	    JsonArray compsFinal = null;
    	    String postal = "N/A";

    	    for (JsonElement resEl : results) {

    	        JsonObject res = resEl.getAsJsonObject();
    	        JsonArray comps = res.getAsJsonArray("address_components");

    	        if (comps == null) continue;

    	        for (JsonElement el : comps) {
    	            JsonObject comp = el.getAsJsonObject();
    	            JsonArray types = comp.getAsJsonArray("types");

    	            for (JsonElement t : types) {
    	                if (t.getAsString().equals("postal_code")) {
    	                    postal = comp.get("long_name").getAsString();
    	                    compsFinal = comps;
    	                    break;
    	                }
    	            }
    	            if (!postal.equals("N/A")) break;
    	        }
    	        if (!postal.equals("N/A")) break;
    	    }

    	    if (compsFinal == null) {
    	        System.out.println("No address components");
    	        return;
    	    }

    	    String city = "N/A";
    	    String district = "N/A";
    	    String region = "N/A";
    	    String country = "N/A";
    	    String street = "N/A";
    	    String streetNumber = "N/A";

    	    for (JsonElement el : compsFinal) {
    	        JsonObject comp = el.getAsJsonObject();
    	        String longName = comp.get("long_name").getAsString();
    	        JsonArray types = comp.getAsJsonArray("types");

    	        for (JsonElement t : types) {
    	            String type = t.getAsString();

    	            if (type.equals("locality") || type.equals("postal_town") || type.equals("sublocality")) city = longName;
    	            if (type.equals("administrative_area_level_2")) district = longName;
    	            if (type.equals("administrative_area_level_1")) region = longName;
    	            if (type.equals("country")) country = longName;
    	            if (type.equals("route")) street = longName;
    	            if (type.equals("street_number")) streetNumber = longName;
    	        }
    	    }

    	    System.out.println("PSC: " + postal);
    	    System.out.println("Mesto: " + city);
    	    System.out.println("Okres: " + district);
    	    System.out.println("Kraj: " + region);
    	    System.out.println("Stat: " + country);
    	    System.out.println("Ulica: " + street);
    	    System.out.println("Cislo: " + streetNumber);

    	} catch (Exception e) {
    	    e.printStackTrace();
    	}

    	}
    }




























