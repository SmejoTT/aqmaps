package uk.ac.ed.inf.aqmaps;


public class App {
    
    private static WebServer webServer;
    private static Drone drone;
    
    
    public static void main(String[] args) {
        
        var day = args[0];
        var month = args[1];
        var year = args[2];
        
        var startLatitude = Double.parseDouble(args[3]);
        var startLongitude = Double.parseDouble(args[4]);
        
        var seed = Integer.parseInt(args[5]);
        var port = args[6];
        
        var mapUrl =String.format
                ("http://localhost:%s/maps/%s/%s/%s/air-quality-data.json",port,year,month,day);
        
        var noFlyZonesUrl = String.format
                ("http://localhost:%s/buildings/no-fly-zones.json",port);
        
        //ArrayList<AirQualitySensor> aqSensorList = parseAqSensorData(response);
        
        webServer = new WebServer(port);
        webServer.getSensorDataOn(day, month, year);
                
    }
}
