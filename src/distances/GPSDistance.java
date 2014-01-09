package distances;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import clustering.Cluster.PointInsideTrajectory;
import util.CartesianCoordinates;
import util.Converter;
import util.Interpolation;
import util.MinutesCoordinates;
import wrappers.GPSFormat;
import wrappers.SimpleFormat;

public class GPSDistance extends Distance{

	public static void main(String[] args) {
		
		System.out.println(new Date(1077671565l));
		
		GPSDistance d = new GPSDistance();
		//EuclideanDistance d = new EuclideanDistance();
		//System.out.println(d.distance(37.74827,-122.40827, 37.74827,-122.40827));
		MinutesCoordinates vilaSecaLatitude = new MinutesCoordinates(41, 6, 41.59f);
		MinutesCoordinates vilaSecaLongitude = new MinutesCoordinates(1, 8, 36.49f);

		MinutesCoordinates tarragonaLatitude = new MinutesCoordinates(41, 7, 8.25f);
		MinutesCoordinates tarragonaLongitude = new MinutesCoordinates(1, 14, 41.59f);

		MinutesCoordinates midPointLatitude = new MinutesCoordinates(41, 6, 58.76f);
		MinutesCoordinates midPointLongitude = new MinutesCoordinates(1, 11, 46.98f);
		
		/*System.out.println("Distance from Tarragon to Vilsa Seca is : "+
				d.distance(tarragonaLatitude, tarragonaLongitude, vilaSecaLatitude, vilaSecaLongitude));
		
		System.out.println("Distance from Tarragon to Mid Point is : "+
				d.distance(tarragonaLatitude, tarragonaLongitude, midPointLatitude, midPointLongitude));
		
		System.out.println("Distance from MidPoint to Tarragona is : "+
				d.distance(midPointLatitude, midPointLongitude, vilaSecaLatitude, vilaSecaLongitude));*/
		
		double vilaSecaLatitudeD = Converter.minutesToDegrees(vilaSecaLatitude);
		double vilaSecaLongitudeD = Converter.minutesToDegrees(vilaSecaLongitude);
		
		System.out.println("Vila seca ["+vilaSecaLatitudeD+", "+vilaSecaLongitudeD);

		double tarragonaLatitudeD = Converter.minutesToDegrees(tarragonaLatitude);
		double tarragonaLongitudeD = Converter.minutesToDegrees(tarragonaLongitude);

		System.out.println("Tarragona ["+tarragonaLatitudeD+", "+tarragonaLongitudeD);

		double midPointLatitudeD = Converter.minutesToDegrees(midPointLatitude);
		double midPointLongitudeD = Converter.minutesToDegrees(midPointLongitude);
		
		System.out.println("Mid Point ["+midPointLatitudeD+", "+midPointLongitudeD);

		System.out.println("Distance from Tarragon to Vilsa Seca is : "+
				d.distance(tarragonaLatitudeD, tarragonaLongitudeD, vilaSecaLatitudeD, vilaSecaLongitudeD));
		
		System.out.println("Distance from Tarragon to Mid Point is : "+
				d.distance(tarragonaLatitudeD, tarragonaLongitudeD, midPointLatitudeD, midPointLongitudeD));
		
		System.out.println("Distance from MidPoint to Vilaseca is : "+
				d.distance(midPointLatitudeD, midPointLongitudeD, vilaSecaLatitudeD, vilaSecaLongitudeD));
		
		GPSFormat midPoint = Interpolation.interpolate(
				new SimpleFormat(0, tarragonaLatitudeD, tarragonaLongitudeD), 
				new SimpleFormat(2, vilaSecaLatitudeD, vilaSecaLongitudeD),
				1);
		
		System.out.println("Compute mid point : "+midPoint.toString());
		System.out.println("Distance from MidPoint to Tarragona is : "+
				d.distance(
						midPoint.getLatitude(), 
						midPoint.getLongitude(), 
						tarragonaLatitudeD, 
						tarragonaLongitudeD
						));
		
		double[] vilaSecaXYZ = Converter.degreesToXYZ(vilaSecaLatitudeD, vilaSecaLongitudeD);
		
		System.out.println("Vila seca XYZ ["+vilaSecaXYZ[0]+", "
				+vilaSecaXYZ[1]+", "+vilaSecaXYZ[2]);

		double[] tarragonaXYZ = Converter.degreesToXYZ(tarragonaLatitudeD, tarragonaLongitudeD);

		System.out.println("tarragona XYZ ["+tarragonaXYZ[0]+", "
				+tarragonaXYZ[1]+", "+tarragonaXYZ[2]);

		double[] midPointXYZ = Converter.degreesToXYZ(midPointLatitudeD, midPointLongitudeD);
		
		System.out.println("midPoint XYZ ["+midPointXYZ[0]+", "
				+midPointXYZ[1]+", "+midPointXYZ[2]);

		System.out.println("Distance from Tarragon to Vilsa Seca is : "+
				d.distance(tarragonaXYZ, vilaSecaXYZ));
		
		System.out.println("Distance from Tarragon to Mid Point is : "+
				d.distance(tarragonaXYZ, midPointXYZ));
		
		System.out.println("Distance from MidPoint to Vilaseca is : "+
				d.distance(midPointXYZ, vilaSecaXYZ));
		
	}
	
	
	private double distance(double[] p1, double[] p2) {
		return Math.sqrt(Math.pow(p1[0]-p2[0], 2)
				+Math.pow(p1[1]-p2[1], 2)+Math.pow(p1[1]-p2[1], 2));
	}


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
		
		//esta es la forma de vedad
		/*double rLat1 = lat1/57.29577951;
		double rLon1 = lon1/57.29577951;
		double rLat2 = lat2/57.29577951;
		double rLon2 = lon2/57.29577951;
		//el radio de la esfera es mas o menos 6,378.8 kilometers
		double argument = Math.sin(rLat1)*Math.sin(rLat2)+ 
		Math.cos(rLat1)*Math.cos(rLat2)*Math.cos(rLon2-rLon1);
		if (argument > 1) argument = 1;
		if (argument < -1) argument = -1;*/
		//pero yo voy a coger la forma sencilla.
		//double[] p1 = Converter.degreesToXYZ(lat1, lon1);
		//double[] p2 = Converter.degreesToXYZ(lat2, lon2);
		double[] p1 = Converter.degreesToXYZ(lat1, lon1);
		double[] p2 = Converter.degreesToXYZ(lat2, lon2);
		return distance(p1, p2);
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
	
	public double gpsIntraClusterAverageDistance(
			List<PointInsideTrajectory> cluster) {
		double distance = 0;
		for (PointInsideTrajectory p1 : cluster){
			for (PointInsideTrajectory p2 : cluster){
				distance+= distance(p1.p, p2.p);
			}
		}
		return distance/(cluster.size()*(cluster.size()-1));
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
