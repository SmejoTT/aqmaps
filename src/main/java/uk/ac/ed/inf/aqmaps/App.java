package uk.ac.ed.inf.aqmaps;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

public class App {
    
    private static final String GREEN = "#00ff00";
    private static final String MEDIUM_GREEN = "#40ff00";
    private static final String LIGHT_GREEN = "#80ff00";
    private static final String LIME_GREEN = "#c0ff00";
    private static final String GOLD = "#ffc000";
    private static final String ORANGE = "#ff8000";
    private static final String RED_ORANGE = "#ff4000";
    private static final String RED = "#ff0000";
    private static final String BLACK = "#000000";
    private static final String GRAY = "#aaaaaa";
    
    private static final String LIGHTHOUSE = "lighthouse";
    private static final String DANGER = "danger";
    private static final String CROSS = "cross";
    
    
    private static WebServer webServer;
    private static Drone drone;
    private static List<Feature> noFlyZones;
    private static ArrayList<AirQualitySensor> aqSensorList;
    
    
    private static void outputFlighpath(String day, String month, String year) {
        var flightLog = drone.getFlightLog();
        var fileName = String.format("flightpath-%s-%s-%s.txt",day,month,year);
        writeToFile(fileName, flightLog);
    }
    
    private static void outputReadings(String day, String month, String year) {
        var sensorData = proccessReadings();
        var dronePath = Feature.fromGeometry(LineString.fromLngLats(drone.getPath()));
        var listOfFeatures = sensorData;
        listOfFeatures.add(dronePath);
        var featureCollection = FeatureCollection.fromFeatures(listOfFeatures);
        var geojson = featureCollection.toJson();
        var fileName = String.format("readings-%s-%s-%s.geojson",day,month,year);
        writeToFile(fileName, geojson);
    }
    
    private static ArrayList<Feature> proccessReadings() {
        var visitedSensors = drone.getVisitedAqSensorList();
        var unvisitedSensors = drone.getUnvisitedAqSensorList();
        var listOfFeatures = new ArrayList<Feature>();
        for (int i = 0; i < visitedSensors.size(); i++) {
            var sensor = visitedSensors.get(i);
            var markerFeature = Feature.fromGeometry(sensor.getCoordinates());
            markerFeature.addStringProperty("location", sensor.getLocation());
            if (sensor.getBattery() >= 10) {
                var evaluation = evaluateReading(sensor.getReading());
                markerFeature.addStringProperty("rgb-string", evaluation[0]);
                markerFeature.addStringProperty("marker-color", evaluation[0]);
                markerFeature.addStringProperty("marker-symbol", evaluation[1]);
                listOfFeatures.add(markerFeature);
            }else {
                markerFeature.addStringProperty("rgb-string", BLACK);
                markerFeature.addStringProperty("marker-color", BLACK);
                markerFeature.addStringProperty("marker-symbol", CROSS);
                listOfFeatures.add(markerFeature);
            }
        }
        
        for (int i = 0; i < unvisitedSensors.size(); i++) {
            var sensor = visitedSensors.get(i);
            var markerFeature = Feature.fromGeometry(sensor.getCoordinates());
            markerFeature.addStringProperty("location", sensor.getLocation());
            markerFeature.addStringProperty("rgb-string", GRAY);
            markerFeature.addStringProperty("marker-color", GRAY);
            listOfFeatures.add(markerFeature);
        }
        return listOfFeatures;
      
    }
    
    private static String[] evaluateReading(String readingValueString) {
        var readingValue = Double.parseDouble(readingValueString);
        var evaluation = new String[2];
        if ((readingValue >= 0) && (readingValue < 32) ) {
            evaluation[0] = GREEN;
            evaluation[1] = LIGHTHOUSE;
            return evaluation;
        }
        
        if ((readingValue >= 32) && (readingValue < 64) ) {
            evaluation[0] = MEDIUM_GREEN;
            evaluation[1] = LIGHTHOUSE;
            return evaluation;
        }
        
        if ((readingValue >= 64) && (readingValue < 96) ) {
            evaluation[0] = LIGHT_GREEN;
            evaluation[1] = LIGHTHOUSE;
            return evaluation;
        }
        
        if ((readingValue >= 96) && (readingValue < 128) ) {
            evaluation[0] = LIME_GREEN;
            evaluation[1] = LIGHTHOUSE;
            return evaluation;
        }
        
        if ((readingValue >= 128) && (readingValue < 160) ) {
            evaluation[0] = GOLD;
            evaluation[1] = DANGER;
            return evaluation;
        }
        
        if ((readingValue >= 160) && (readingValue < 192) ) {
            evaluation[0] = ORANGE;
            evaluation[1] = DANGER;
            return evaluation;
        }
        
        if ((readingValue >= 192) && (readingValue < 224) ) {
            evaluation[0] = RED_ORANGE;
            evaluation[1] = DANGER;
            return evaluation;
        }
        
        if ((readingValue >= 224) && (readingValue < 256) ) {
            evaluation[0] = RED;
            evaluation[1] = DANGER;
            return evaluation;
        }
        return null;
    }
    
    private static void writeToFile(String fileName, String content) {
        try {
            var file = new File(fileName);
            file.createNewFile();
            var writer = new FileWriter(fileName);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            System.out.println("An error has occurred.");
            e.printStackTrace();
        }   
        
    }
    
    public static void main(String[] args) {
       
        var day = args[0];
        var month = args[1];
        var year = args[2];
        
        var startLatitude = Double.parseDouble(args[3]);
        var startLongitude = Double.parseDouble(args[4]);
        
        var seed = Integer.parseInt(args[5]);
        var port = args[6];
        
        webServer = new WebServer(port);
        aqSensorList = webServer.getSensorDataOn(day, month, year);
        noFlyZones = webServer.getNoFlyZones();
        var startPosition = Point.fromLngLat(startLongitude, startLatitude);
        drone = new Drone(startPosition,aqSensorList,noFlyZones);
        drone.collectReadings();
        outputReadings(day, month, year);
        outputFlighpath(day, month, year);
    }
}
