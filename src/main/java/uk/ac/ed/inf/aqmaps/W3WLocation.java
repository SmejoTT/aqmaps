package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Point;

public class W3WLocation {
    private Coordinates coordinates;
    
    public Coordinates getCoordinates(){
        return coordinates;
    }
    
    public static class Coordinates{
        private double lng;
        private double lat;
        
        public Point asPoint() {
            var coordinates = Point.fromLngLat(lng, lat);
            return coordinates;
        }
    }
    
}
