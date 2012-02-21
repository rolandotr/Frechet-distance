package distances;

import java.awt.Point;

import util.CartesianCoordinates;
import util.Converter;
import util.MinutesCoordinates;
import wrappers.GPSFormat;

public class GPSDistance extends Distance{

	public double distance(MinutesCoordinates lat1, MinutesCoordinates lon1, 
			MinutesCoordinates lat2, MinutesCoordinates lon2){
		return distance(Converter.minutesToDegrees(lat1), Converter.minutesToDegrees(lon1), 
				Converter.minutesToDegrees(lat2), Converter.minutesToDegrees(lon2));
	}

	@Override
	public double distance(double lat1, double lon1, double lat2, double lon2){
		/** 21/06/2010 Trujillo Comment
		 * primero debemos obtener las coordenadas cartesianas*/
		/*CartesianCoordinates p1 = cartesianCord(lat1, lon1);
		CartesianCoordinates p2 = cartesianCord(lat2, lon2);
		return Math.sqrt(Math.pow(p1.lat-p2.lat, 2)+Math.pow(p1.lon-p2.lon, 2));*/
		//return Math.sqrt(Math.pow(69.1*(lat1-lat2), 2)+Math.pow(53*(lon1-lon2),2));
		//para convertir en radianes se multiplica por 57.29577951
		double rLat1 = lat1/57.29577951;
		double rLon1 = lon1/57.29577951;
		double rLat2 = lat2/57.29577951;
		double rLon2 = lon2/57.29577951;
		//el radio de la esfera es mas o menos 6,378.8 kilometers
		double argument = Math.sin(rLat1)*Math.sin(rLat2)+ 
		Math.cos(rLat1)*Math.cos(rLat2)*Math.cos(rLon2-rLon1);
		if (argument > 1) argument = 1;
		if (argument < -1) argument = -1;
		
		return 6378.8 * Math.acos(argument);
	}

	protected CartesianCoordinates cartesianCord(double lat1, double lon1) {
		/** 21/06/2010 Trujillo Comment
		 * La tierra por la latitud tiene un semi diametro de 10000 Km, lo que quiere decir
		 * que los 90 grados son 10000*/
		double lat = lat1*10000/(double)90;
		/** 21/06/2010 Trujillo Comment
		 * mientras que en la longitud 360 grados se reparten 40030 kilometros*/
		double lon = lat1*40030/(double)360;
		return new CartesianCoordinates(lat, lon);
	}

	public boolean isInRadius(double latCenter, double lonCenter, double lat, double lon, double radius){
		return  (latCenter - radius <= lat && latCenter+radius >= lat && lonCenter - radius <= lon && lonCenter+radius >= lon);
	}
	
	@Override
	public double distance(GPSFormat p1, GPSFormat p2) {
		return distance(p1.getLatitude(), p1.getLongitude(), p2.getLatitude(), p2.getLongitude());
	}

	@Override
	public double distance(Point p1, Point p2) {
		throw new RuntimeException("no se debio llamar este metod, se debe usar siempre con las" +
				"coordenadas GPS");
	}

}
