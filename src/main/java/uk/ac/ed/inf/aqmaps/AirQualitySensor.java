package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Point;

public class AirQualitySensor {
    private String location;
    private double battery;
    private String reading;
    private Point coordinates;
    public Point getCoordinates() {
        return coordinates;
    }
    public void setCoordinates(Point coordinates) {
        this.coordinates = coordinates;
    }
    public String getLocation() {
        return location;
    }
    public double getBattery() {
        return battery;
    }
    public String getReading() {
        return reading;
    }
}
