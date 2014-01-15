package util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import parsers.GPXParser;
import parsers.TxtParser;

import distances.GPSDistance;
import trajectory.SimpleTrajectory;
import trajectory.Trajectory;
import wrappers.GPSFormat;
import wrappers.SimpleFormat;

public class Converter {

	private static final int SCALE = 1;
	public static final double RADIOUS = 6378.8;
	//public static final double RADIOUS = 0;
	
	public static double minutesToDegrees(MinutesCoordinates coord){
		double result = coord.getSec()/60;
		result += coord.getMinutes();
		result /= 60;
		result += coord.getDegrees();
		return result;
	}
	
	/** 21/06/2010 Trujillo Comment
	 * Para convertir a los minutos de los grados segundos
		1. Tome simplemente el número antes del decimal como sus grados.
			Tenemos tan 78°
		2. Ahora reste eso del problema y multiplique la respuesta por 60
			Tenemos tan 78° 27 '
		3. Ahora reste 27 de la respuesta y multipliqúese por 60 otra vez.
		La respuesta final es 78° 27 ' 21,6 " (78 grados 27 minutos y 21,6 segundos)*/
	public static MinutesCoordinates degreesToMinutes(double degrees){
		int intValue = (int)degrees;
		double rest = degrees-intValue;
		int minutes = (int)(rest*60);
		rest = rest*60 - minutes;
		float sec = (float)rest*60;
		return new MinutesCoordinates(intValue, minutes, sec);
	}
	
	/*public static double[] degreesToXYZ(double latitude, double longitude){
		if (latitude == 90) return new double[]{0, 0, 6378.8};
		if (latitude == -90) return new double[]{0, 0, -6378.8};
		double rLat1 = latitude/57.29577951;
		double rLon1 = longitude/57.29577951;
		double y = RADIOUS/Math.sqrt(1 + Math.tan(rLon1)*Math.tan(rLon1) 
				+ Math.tan(rLat1)*Math.tan(rLat1)); 
		double x = y*Math.tan(rLon1);
		double z = y*Math.tan(rLat1);
		if (longitude < -90 || longitude > 90) y = -y;
		//if (longitude < 0 || longitude > 180) x = -x;
		//if (latitude < 0 || latitude > 90) z = -z;
		return new double[]{x*SCALE, y*SCALE, z*SCALE};
	}
	
	public static GPSFormat xyzToDegrees(double[] p){
		double a = p[0]/SCALE;
		double b = p[1]/SCALE;
		double c = p[2]/SCALE;
		if (a == 0 && b == 0 && c == 6378.8) 
			return new SimpleFormat(0, 90, 0);
		if (a == 0 && b == 0 && c == -6378.8) 
			return new SimpleFormat(0, -90, 0);
		double rLat1 = Math.atan(c/b);
		double rLon1 = Math.atan(c/b);
		double latitude = rLat1*57.29577951;
		double longitude = rLon1*57.29577951;
		return new SimpleFormat(0, latitude, longitude);
	}*/
	
	public static double[] degreesToXYZ(double latitude, double longitude){
		double rLat1 = latitude/57.29577951;
		double rLon1 = longitude/57.29577951;
		double x = RADIOUS*Math.cos(rLat1)*Math.cos(rLon1);
		double y = RADIOUS*Math.cos(rLat1)*Math.sin(rLon1);
		double z = RADIOUS*Math.sin(rLat1);
		return new double[]{x,y,z};
	}
	
	public static GPSFormat xyzToDegrees(double[] p){
		double lat = Math.asin(p[2]/RADIOUS);
		double lon = Math.atan2(p[1], p[0]);
		double latitude = lat*57.29577951;
		double longitude = lon*57.29577951;
		return new SimpleFormat(0, latitude, longitude);
	}
	

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith(".txt") && name.startsWith("new")) return true;
				return false;
			}
		};
		File folder = new File("./");
		String[] files = folder.list(filter);
		//cojo cualquiera;
		TreeMap<Long, GPSFormat> tree = TxtParser.parseTxtFile(new File(files[0]));
		Trajectory t = new SimpleTrajectory(files[0], tree);
		checkDegreesConverter(t);
		//checkDegreesConverterSpheric(t);
	}

	public static double[] degreesToXYZ(GPSFormat p) {
		return degreesToXYZ(p.getLatitude(), p.getLongitude());
	}

	public static void checkDegreesConverter(Trajectory t){
		System.out.println("Original trajectory lenght = "+t.spatialLength(new GPSDistance()));
		Trajectory tmp = new SimpleTrajectory("asd");
		for (GPSFormat p : t.points()) {
			double[] coord = Converter.degreesToXYZ(p);
			GPSFormat newP = Converter.xyzToDegrees(coord);
			newP.setTime(p.getTime());
			tmp.addPoint(newP);
		}
		System.out.println("Converted trajectory lenght = "+tmp.spatialLength(new GPSDistance()));
	}
	/*public static void checkDegreesConverterSpheric(Trajectory t){
		System.out.println("Original trajectory lenght = "+t.spatialLength(new GPSDistance()));
		Trajectory tmp = new SimpleTrajectory("asd");
		for (GPSFormat p : t.points()) {
			double[] coord = Converter.degreesToXYZEspheric(p.getLatitude(), p.getLongitude());
			GPSFormat newP = Converter.xyzToDegreesEspheric(coord);
			newP.setTime(p.getTime());
			tmp.addPoint(newP);
		}
		System.out.println("Spheric Converted trajectory lenght = "+tmp.spatialLength(new GPSDistance()));
	}*/
}
