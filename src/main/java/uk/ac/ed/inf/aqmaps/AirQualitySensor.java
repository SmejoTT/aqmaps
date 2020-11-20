package uk.ac.ed.inf.aqmaps;

public class AirQualitySensor {
    private String location;
    private double battery;
    private String reading;
    private double[] coordinates;
    public double[] getCoordinates() {
        return coordinates;
    }
    public void setCoordinates(double[] coordinates) {
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
