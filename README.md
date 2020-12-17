# Drone control system and air quality map
**(Java 11, HTTP Client, Mapbox GeoJSON SDK, Maven)**

This project was a part of university coursework where was needed to programme a software prototype that would fetch input data from the webserver,
use drone to collect sensor readings and output the air quality map.

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
        
## Fetching of the data
Fetching of data is done in *WebServer* class by using Java's HTTP Client.
At first, is fetched list of sensors that need to be read. This list is then parsed to list of *AirQualitySenor* objects.
Then the information about the What3Words location is fetched from webserver a parsed to longitude and latitude format.
Lastly, the list of no-fly zones is fetched and parsed.

## Drone flight
At the start, the drone receives start position, list of 33 sensors that need to be visited and a list of no-fly zones. Then the drone utilizes its Pathfinder object that orders sensors
in a more optimal order for visiting. After this, the drone starts to visit sensors and collect readings, after visiting the last sensor drone returns to start location.

## Air quality map
Readings collected by the drone are evaluated in *App* class and output as a GeoJSON file that can be visualised e.g. at http://geojson.io/

## Handling of edge cases
- If the drone makes 150 moves it stops at its location and prints it to the console. 
- If the reading fails the involved sensor is moved to the end of the list of unvisited sensors so the drone can try to make reading at the end of the flight.

**Project can be packaged to a .jar by using maven build.**
