package uk.ac.ed.inf.aqmaps;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;

public class WebServer {

    private String serverUrl;
    private static final HttpClient client = HttpClient.newHttpClient();

    public WebServer(String port) {
        this.serverUrl = "http://localhost:" + port + "/";
    }

    private String getDataAt(String url) {
        var request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        HttpResponse<String> response;
        try {
            response = client.send(request, BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            System.out
                .println("Fatal error: Unable to connect to " + url + " .");
            System.exit(1);
        }
        return null;
    }

    public ArrayList<AirQualitySensor> getSensorDataOn(String day, String month, String year) {
        var url = serverUrl + "maps/" + year + "/" + month + "/" + day + "/air-quality-data.json";
        var jsonString = getDataAt(url);
        var aqSensorList = parseAqSensorData(jsonString);
        for (int i = 0; i < aqSensorList.size(); i++) {
            var location = aqSensorList.get(i).getLocation().split("\\.");
            url = serverUrl + "words/" + location[0] + "/" + location[1] + "/" + location[2]
                    + "/details.json";
            jsonString = getDataAt(url);
            var coordinates = parseW3WLocation(jsonString);
            aqSensorList.get(i).setCoordinates(coordinates);
        }
        return aqSensorList;
    }

    private Point parseW3WLocation(String jsonString) {
        var location = new Gson().fromJson(jsonString, W3WLocation.class);
        var coordinates = location.getCoordinates().asPoint();
        return coordinates;
    }

    private ArrayList<AirQualitySensor> parseAqSensorData(String jsonString) {
        Type listType = new TypeToken<ArrayList<AirQualitySensor>>() {}.getType();
        ArrayList<AirQualitySensor> aqSensorList = new Gson().fromJson(jsonString, listType);
        return aqSensorList;
    }

    public List<Feature> getNoFlyZones() {
        var url = serverUrl + "buildings/no-fly-zones.geojson";
        var jsonString = getDataAt(url);
        var featureCollection = FeatureCollection.fromJson(jsonString);
        var noFlyZones = featureCollection.features();
        return noFlyZones;
    }

}
