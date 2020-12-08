package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

public class Pathfinder {

    private Drone drone;
    private List<Feature>forbidenAreas;
    
    private ArrayList<Point> openSet;
    private HashMap<Point, Point> cameFromPoint;
    private HashMap<Point, Integer> cameFromDegrees;
    private HashMap<Point, Double> gScore;
    private HashMap<Point, Double> fScore;

    public Pathfinder(Drone drone, List<Feature> noFlyZones) {
        this.drone = drone;
        this.forbidenAreas = new ArrayList<Feature>();
        this.forbidenAreas.addAll(noFlyZones);
        this.forbidenAreas.add(drone.getConfinementArea());
    }


    private ArrayList<Integer> reconstructPath(Point current) {
        var moves = new ArrayList<Integer>();
        moves.add(cameFromDegrees.get(current));
        while (cameFromPoint.containsKey(current)) {
            current = cameFromPoint.get(current);
            if (cameFromDegrees.get(current) != null) {
                var move = cameFromDegrees.get(current);
                moves.add(move);
            }
        }
        Collections.reverse(moves);
        return moves;
    }

    public ArrayList<Integer> aStar(Point start, Point goal) {
        // list of expanded points that may be re-expanded
        openSet = new ArrayList<Point>();
        openSet.add(start);
        
        cameFromPoint = new HashMap<Point, Point>();
        cameFromDegrees = new HashMap<Point, Integer>();

        var moveDistance = drone.getMoveDistance();
        // gScore.get(n) is the shortest distance in which we can get from start to n
        gScore = new HashMap<Point, Double>();
        gScore.put(start, 0.0);

        var epsilon = 1.6;
        // fScore.get(n) is an estimation of how long the path through n to goal will be
        fScore = new HashMap<Point, Double>();
        fScore.put(start,epsilon * h(start, goal) + gScore.get(start));

        while (!openSet.isEmpty()) {
            var current = findMinHeuristicPoint(goal);

            if (isClose(current, goal) && gScore.size() > 1) {
                return reconstructPath(current);
            }

            openSet.remove(current);
            var neighbours = expand(current);
            var neighboursKeys = new ArrayList<Point>(neighbours.keySet());
            for (int i = 0; i < neighboursKeys.size(); i++) {
                var neighbour = neighboursKeys.get(i);
                var preScore = gScore.get(current) + moveDistance;
                if (gScore.get(neighbour) == null) {
                    cameFromPoint.put(neighbour, current);
                    cameFromDegrees.put(neighbour, neighbours.get(neighbour));
                    gScore.put(neighbour, preScore);
                    var fScoreValue = gScore.get(neighbour) + epsilon * h(neighbour, goal);
                    fScore.put(neighbour, fScoreValue);
                    if (!openSet.contains(neighbour)) {
                        openSet.add(neighbour);
                    }
                } else if (preScore < gScore.get(neighbour)) {
                    cameFromPoint.replace(neighbour, current);
                    cameFromDegrees.replace(neighbour, neighbours.get(neighbour));
                    gScore.replace(neighbour, preScore);
                    var fScoreValue = gScore.get(neighbour) + epsilon * h(neighbour, goal);
                    fScore.replace(neighbour, fScoreValue);
                    if (!openSet.contains(neighbour)) {
                        openSet.add(neighbour);
                    }
                }
            }
        }
        return null;
    }

    private Point findMinHeuristicPoint(Point goal) {
        var min = fScore.get(openSet.get(0));
        var minPoint = openSet.get(0);
        if (openSet.size() == 1) {
            return minPoint;
        }
        for (int i = 1; i < openSet.size(); i++) {
            var f = fScore.get(openSet.get(i));
            if (f < min) {
                min = f;
                minPoint = openSet.get(i);
            }
        }
        return minPoint;
    }

    private double h(Point position, Point goal) {
        // heuristic function that uses Euclidean distance
        var positionCoordinates = position.coordinates();
        var goalCoordinates = goal.coordinates();
        var xDiff = goalCoordinates.get(0) - positionCoordinates.get(0);
        var yDiff = goalCoordinates.get(1) - positionCoordinates.get(1);
        var distance = Math.sqrt(Math.pow(xDiff, 2.0) + Math.pow(yDiff, 2.0));
        return distance;
    }

    public Point calculateCoordinatesAfterMove(Point position, int degrees) {
        var moveDistance = drone.getMoveDistance();
        var angle = Math.toRadians(degrees);
        var longStep = Math.cos(angle) * (moveDistance);
        var nextLongitude = position.longitude() + longStep;
        var latStep = Math.sin(angle) * (moveDistance);
        var nextLatitude = position.latitude() + latStep;
        var nextPosistion = Point.fromLngLat(nextLongitude, nextLatitude);
        return nextPosistion;
    }

    private HashMap<Point, Integer> expand(Point position) {
        var neighbours = new HashMap<Point, Integer>();
        for (int degrees = 0; degrees < 360; degrees += 10) {
            var nextPosition = calculateCoordinatesAfterMove(position, degrees);
            if (isLegalMove(position, nextPosition)) {
                neighbours.put(nextPosition, degrees);
            }
        }
        return neighbours;
    }

    private boolean isLegalMove(Point position, Point nextPosition) {
        var currentPositionCoordinates = position.coordinates();
        var nextPositionCoordinates = nextPosition.coordinates();
        var startX = currentPositionCoordinates.get(0);
        var startY = currentPositionCoordinates.get(1);
        var endX = nextPositionCoordinates.get(0);
        var endY = nextPositionCoordinates.get(1);
        
        var moveLine = new Line2D.Double(startX, startY, endX, endY);
        for (int i = 0; i < forbidenAreas.size(); i++) {
            var zone = (Polygon) forbidenAreas.get(i).geometry();
            var zoneCoordiantes = zone.coordinates().get(0);
            for (int j = 0; j < zoneCoordiantes.size(); j++) {
                var startLine = zoneCoordiantes.get(j).coordinates();
                var startLineX = startLine.get(0);
                var startLineY = startLine.get(1);
                var endLine = zoneCoordiantes.get((j + 1) % zoneCoordiantes.size()).coordinates();
                var endLineX = endLine.get(0);
                var endLineY = endLine.get(1);
                var areaBoundaryLine = new Line2D.Double(startLineX, startLineY, endLineX, endLineY);
                if (moveLine.intersectsLine(areaBoundaryLine)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isClose(Point p1, Point p2) {
        var readingRange = drone.getReadingRange();
        var moveDistance = drone.getMoveDistance();

        if (drone.getUnvisitedAqSensorList().isEmpty()) {
            if (h(p1, p2) < moveDistance) {
                return true;
            }
        }
        
        if (h(p1, p2) < readingRange) {
            return true;
        }
        
        return false;
    }

    public void findSensorOrder() {
        var aqSensorList = new ArrayList<AirQualitySensor>(drone.getUnvisitedAqSensorList());
        var greedyOrder = findGreedyOrder(aqSensorList);
        var optimizedOrder = swapHeuristic(greedyOrder);
        drone.setUnvisitedAqSensorList(optimizedOrder);
    }

    private ArrayList<AirQualitySensor> findGreedyOrder(ArrayList<AirQualitySensor> aqSensorList) {
        var position = drone.getCoordinates();
        var greedyOrder = new ArrayList<AirQualitySensor>();
        while (!aqSensorList.isEmpty()) {
            var closestSensor = findClosestSensor(position, aqSensorList);
            greedyOrder.add(closestSensor);
            aqSensorList.remove(closestSensor);
            position = closestSensor.getCoordinates();
        }
        return greedyOrder;
    }

    private AirQualitySensor findClosestSensor(Point position, ArrayList<AirQualitySensor> sensorList) {
        var aqSnesorPoint = sensorList.get(0).getCoordinates();
        var minDistance = h(position, aqSnesorPoint);
        var closestSensor = sensorList.get(0);
        for (int i = 1; i < sensorList.size(); i++) {
            aqSnesorPoint = sensorList.get(i).getCoordinates();
            var distance = h(position, aqSnesorPoint);
            if (distance < minDistance) {
                minDistance = distance;
                closestSensor = sensorList.get(i);
            }
        }
        return closestSensor;
    }

    private ArrayList<AirQualitySensor> swapHeuristic(ArrayList<AirQualitySensor> aqSensorList) {
        var better = true;
        while (better) {
            better = false;
            for (int i = 0; i < aqSensorList.size() - 1; i++) {
                var oldCost = pathCost(aqSensorList);
                var sensor1 = aqSensorList.get(i);
                var sensor2 = aqSensorList.get(i + 1);
                aqSensorList.set(i, sensor2);
                aqSensorList.set(i + 1, sensor1);
                var newCost = pathCost(aqSensorList); 
                if (newCost < oldCost) {
                    better = true;
                } else {
                    aqSensorList.set(i, sensor1);
                    aqSensorList.set(i + 1, sensor2);
                }
            }
        }
        return aqSensorList;
    }

    private double pathCost(ArrayList<AirQualitySensor> sensorList) {
        var cost = 0.0;
        for (int i = 0; i < sensorList.size() - 1; i++) {
            var sensor1 = sensorList.get(i).getCoordinates();
            var sensor2 = sensorList.get(i + 1).getCoordinates();
            cost += h(sensor1, sensor2);
        }
        cost+= costFromStart(sensorList);
        cost+= costToStart(sensorList);
        return cost;
    }

    private double costFromStart(ArrayList<AirQualitySensor> sensorList) {
        var firstSensorPoint = sensorList.get(0).getCoordinates();
        var dronePosition = drone.getCoordinates();
        return h(dronePosition, firstSensorPoint);
    }

    private double costToStart(ArrayList<AirQualitySensor> sensorList) {
        var lastSensorPoint = sensorList.get(sensorList.size() - 1).getCoordinates();
        var dronePosition = drone.getCoordinates();
        return h(lastSensorPoint, dronePosition);
    }

}
