package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

public class Drone {

    private static final double MOVE_DISTANCE = 0.0003;
    private static final double READING_RANGE = 0.0002;
    private static final Point NW_BOUNDARY_POINT = Point.fromLngLat(-3.192473, 55.946233);
    private static final Point SW_BOUNDARY_POINT = Point.fromLngLat(-3.192473, 55.942617);
    private static final Point NE_BOUNDARY_POINT = Point.fromLngLat(-3.184319, 55.946233);
    private static final Point SE_BOUNDARY_POINT = Point.fromLngLat(-3.184319, 55.942617);

    private Pathfinder pathFinder;
    private ArrayList<AirQualitySensor> unvisitedAqSensorList;
    private Feature confinementArea;
    private ArrayList<AirQualitySensor> visitedAqSensorList;
    private ArrayList<Point> path;
    private String flightLog;
    private String[] moveRecord;
    private int battery = 150;
    private Point coordinates;
    private int moveCounter = 0;
    private boolean canRead = false;

    public Drone(Point startPosition, ArrayList<AirQualitySensor> aqSensorList,
            List<Feature> noFlyZones) {
        this.coordinates = startPosition;
        this.unvisitedAqSensorList = aqSensorList;
        this.flightLog = "";
        this.moveRecord = new String[7];
        this.visitedAqSensorList = new ArrayList<AirQualitySensor>();
        this.path = new ArrayList<Point>();
        createConfinementArea();
        this.pathFinder = new Pathfinder(this, noFlyZones);
    }

    private boolean hasBattery() {
        if (battery > 0) {
            return true;
        }
        return false;
    }

    private void decreaseBattery() {
        battery--;
    }

    public Point getCoordinates() {
        return coordinates;
    }

    public double getReadingRange() {
        return READING_RANGE;
    }

    public double getMoveDistance() {
        return MOVE_DISTANCE;
    }

    public ArrayList<AirQualitySensor> getUnvisitedAqSensorList() {
        return unvisitedAqSensorList;
    }

    public void setUnvisitedAqSensorList(ArrayList<AirQualitySensor> unvisitedAqSensorList){
        this.unvisitedAqSensorList = unvisitedAqSensorList;
    }

    public ArrayList<AirQualitySensor> getVisitedAqSensorList() {
        return visitedAqSensorList;
    }
    
    public ArrayList<Point> getPath(){
        return path;
    }

    public String getFlightLog() {
        return flightLog;
    }

    private void logMoveRecord() {
        var record = String.join(",", moveRecord) + "\n";
        flightLog += record;
    }

    private void createConfinementArea() {
        var confinementAreaPoints = new ArrayList<Point>();
        confinementAreaPoints.add(NW_BOUNDARY_POINT);
        confinementAreaPoints.add(NE_BOUNDARY_POINT);
        confinementAreaPoints.add(SE_BOUNDARY_POINT);
        confinementAreaPoints.add(SW_BOUNDARY_POINT);
        confinementArea = Feature.fromGeometry(Polygon.fromLngLats(List.of(confinementAreaPoints)));
    }

    public Feature getConfinementArea() {
        return confinementArea;
    }

    private void makeReading(AirQualitySensor aqSensor) {
        var dronePosition = getCoordinates();
        var sensorPosition = aqSensor.getCoordinates();
        if (canRead && pathFinder.isClose(dronePosition, sensorPosition)) {
            var sensor = aqSensor;
            visitedAqSensorList.add(aqSensor);
            unvisitedAqSensorList.remove(aqSensor);
            canRead = false;
            moveRecord[6] = sensor.getLocation();
            logMoveRecord();
        } else {
            // Reading failed, try read the sensor at the end
            unvisitedAqSensorList.remove(aqSensor);
            unvisitedAqSensorList.add(aqSensor);
            var sensorLng = sensorPosition.longitude();
            var sensorLat = sensorPosition.latitude();
            System.out.printf("Drone could not make a reading.\n"
                    + "Sensor located at %6f lng %6f lat " + "was moved to the end of queue.\n",
                    sensorLng, sensorLat);
        }
    }

    private boolean move(int degrees) {
        if (hasBattery()) {
            var dronePosition = getCoordinates();
            coordinates = pathFinder.calculateCoordinatesAfterMove(dronePosition, degrees);
            path.add(coordinates);
            decreaseBattery();
            moveCounter++;
            canRead = true;
            return true;
        } else {
            // Move failed drone has no battery
            var dronePosition = getCoordinates();
            var droneLng = dronePosition.longitude();
            var droneLat = dronePosition.latitude();
            System.out.printf("Drone has no battery.\n" + "Drone is located at %6f lng %6f lat.\n",
                    droneLng, droneLat);
        }
        return false;
    }

    private boolean move(ArrayList<Integer> listOfMoves) {
        for (int i = 0; i < listOfMoves.size(); i++) {
            var oldPosition = getCoordinates();
            if (move(listOfMoves.get(i))) {
                var newPosition = getCoordinates();
                moveRecord[0] = String.valueOf(moveCounter);
                moveRecord[1] = String.valueOf(oldPosition.longitude());
                moveRecord[2] = String.valueOf(oldPosition.latitude());
                moveRecord[3] = String.valueOf(listOfMoves.get(i));
                moveRecord[4] = String.valueOf(newPosition.longitude());
                moveRecord[5] = String.valueOf(newPosition.latitude());
                if (i != listOfMoves.size() - 1 || unvisitedAqSensorList.isEmpty()) {
                    moveRecord[6] = null;
                    logMoveRecord();
                }
            } else {
                // Move failed drone has no battery
                return false;
            }
        }
        return true;
    }

    public void collectReadings() {
        pathFinder.findSensorOrder();
        var sensorIndex = 0;
        var start = getCoordinates();
        path.add(start);
        var end = getCoordinates();

        var nextSensorPoint = unvisitedAqSensorList.get(sensorIndex).getCoordinates();

        while (!unvisitedAqSensorList.isEmpty() || battery == 0) {
            var listOfMoves = pathFinder.aStar(start, nextSensorPoint);
            if (move(listOfMoves) == false) {
                break;
            }

            var aqSensor = unvisitedAqSensorList.get(sensorIndex);
            makeReading(aqSensor);
            start = getCoordinates();
            if (!unvisitedAqSensorList.isEmpty()) {
                nextSensorPoint = unvisitedAqSensorList.get(sensorIndex).getCoordinates();
            }
        }

        var currentPosition = getCoordinates();
        var listOfMoves = pathFinder.aStar(currentPosition, end);
        move(listOfMoves);
    }

}
