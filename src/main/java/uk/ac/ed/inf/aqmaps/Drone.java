package uk.ac.ed.inf.aqmaps;

import java.math.BigDecimal;

import com.mapbox.geojson.Point;

public class Drone {
    
    private static final double EAST_BOUND = -3.184319;
    private static final double WEST_BOUND = -3.192473;
    private static final double NORTH_BOUND = 55.946233;
    private static final double SOUTH_BOUND = 55.942617;
    private static final double MOVE_DISTANCE = 0.0003;
    private static final double READING_RANGE = 0.0002;
    
    private int battery = 150;
    private double longitude;
    private double latitude;
    
    public Drone(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }
    
    public int getBattery() {
        return battery;
    }

    public void decreaseBattery() {
        this.battery--;
    }
    
    public double[] getCoordinates() {
        var coordinates = new double[2];
        coordinates[0]=longitude;
        coordinates[1]= latitude;
        return coordinates;
    }
    
    public double getReadingRange() {
        return READING_RANGE;
    }
    
    public double getMoveDistance() {
        return MOVE_DISTANCE;
    }

    public void move(int degrees) {
        var angle = Math.toRadians(degrees);
        var longStep = Math.cos(angle)*(MOVE_DISTANCE);
        this.longitude = this.longitude+longStep;
        var latStep = Math.sin(angle)*(MOVE_DISTANCE);
        this.latitude = this.latitude+latStep;    
    }
}
