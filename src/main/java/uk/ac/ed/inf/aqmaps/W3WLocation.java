package uk.ac.ed.inf.aqmaps;

public class W3WLocation {
    private Coordinates coordinates;
    
    public Coordinates getCoordinates(){
        return coordinates;
    }
    
    public static class Coordinates{
        private double lng;
        private double lat;
        
        public double[] toArray() {
            var coordinates = new double[2];
            coordinates[0]=lng;
            coordinates[1]=lat;
            return coordinates;
        }
    }
    
}
