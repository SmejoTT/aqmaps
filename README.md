# Drone control system and air quality map
**Java 11, HTTP Client, Mapbox GeoJSON SDK, Maven**

This project was a part of university coursework where was needed to programme a software prototype that would fetch input data from the webserver,
use drone to collect sensor readings and output the air quality map in a GeoJSON format.

Example of the air quality map

![preview](https://user-images.githubusercontent.com/47607423/102548237-47545e80-40b2-11eb-9411-4ecbea31f70f.png)

## Drone
- **longitude** and **latitude** is used to determine coordinates
- can move in an arbitrary direction (in multiples of **10** degrees) between **0 - 350** degrees, by convention, **0º** means EAST, **90º** means NORTH, **180º** means WEST,
**270º** means SOUTH
- can make at most **150** moves
- each move is a straight line and has a length of **0.0003** degrees
- must stay in **bounded area** and avoid **no-fly zones**
- must make at least one **move before each reading**
- uses __A* with static weighting__ to find a path between two sensors

## Sensors
- have a **location** in What3Words format
- have a **battery level**
- have a **reading value**
- have a reading range of **0.0002** degrees
        
## Fetching the data
Fetching is done in *WebServer* class by using Java's HTTP Client.
At first, a list of sensors that need to be read is fetched. This list is then parsed to a list of *AirQualitySenor* objects.
Then the information about the What3Words location is fetched from webserver a parsed to longitude and latitude format.
Lastly, the list of no-fly zones is fetched and parsed.

## Drone flight
At the start, the drone receives start position, a list of 33 sensors that need to be visited and a list of no-fly zones. Then the drone utilizes its *Pathfinder* object that orders sensors
in a more optimal order for visiting. After this, the drone starts to visit sensors and collect readings. The drone finds the path between sensors again by using *Pathfinder* object. After visiting the last sensor drone returns to start location.

## Air quality map
Readings collected by the drone are evaluated in *App* class and output as a GeoJSON file that can be visualised e.g. at http://geojson.io/

## Handling of edge cases
- If the drone makes 150 moves it stops at its location and prints it to the console. 
- If the reading fails the involved sensor is moved to the end of the list of unvisited sensors so the drone can try to make reading at the end of the flight.
- If the sensor's battery level is less than 10 then the reading value can not be trusted. The sensor is marked with X.

## Server
-not included

Strucutre:
- buildings/no-fly-zones.geojson
- maps/*YYYY*/*MM*/*DD*/air-quality-data.json
- words/*word1*/*word2*/*word3*/details.json

## Build and run
- Package the project to a .jar by using maven build.
- Run the webserver with the required data
- Run the .jar file with these command-line attributes: day month year startLatitude startLogitude seed port
- day - DD, month - MM, year - YYYY, startLatitude and startLongitude in the bounded area, seed is not used, port - port of the webserver
