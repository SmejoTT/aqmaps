package uk.ac.ed.inf.aqmaps;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Iterator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class WebServer {
    
    private String serverUrl;
    private String port;
    private static final HttpClient client = HttpClient.newHttpClient();
    
    
    public WebServer(String port) {
        this.port = port;
        this.serverUrl = "http://localhost:" + port + "/";
    }

    private static String getDataAt(String url) {
        var request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        HttpResponse<String> response;
        try {
            response = client.send(request, BodyHandlers.ofString());
            return response.body();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    public ArrayList<AirQualitySensor> getSensorDataOn(String day, String month, String year) {
        var url = serverUrl+"maps/"+year+"/"+month+"/"+day+"/air-quality-data.json";
        var jsonString = getDataAt(url);
        var aqSensorList = parseAqSensorData(jsonString);
        for (int i = 0; i < aqSensorList.size(); i++) {
            var location = aqSensorList.get(i).getLocation().split("\\.");
            System.out.println(location[0]);
            url = serverUrl+"words/"+location[0]+"/"+location[1]+"/"+location[2]+"/details.json";
            jsonString = getDataAt(url);
            var coordinates = parseW3WLocation(jsonString);
            aqSensorList.get(i).setCoordinates(coordinates);
            }
        return aqSensorList;
    }
    
    
    private static double[] parseW3WLocation(String jsonString) {
        var location = new Gson().fromJson(jsonString, W3WLocation.class);
        var coordinates = location.getCoordinates().toArray();
        return coordinates;
    }
    
    private static ArrayList<AirQualitySensor> parseAqSensorData(String jsonString) {
        Type listType = new TypeToken<ArrayList<AirQualitySensor>>() {}.getType();
        ArrayList<AirQualitySensor> aqSensorList = new Gson().fromJson(jsonString, listType);
        return aqSensorList;
    }
    
}
