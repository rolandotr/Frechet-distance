package util;

import trajectory.Trajectory;
import wrappers.GPSFormat;
import wrappers.SimpleFormat;

public class Interpolation {

	/** 08/07/2010 Trujillo Comment
	 * */
	public static GPSFormat interpolate(GPSFormat p1, GPSFormat p2, long newPoint) {
		double frac = ((double)(newPoint - p1.getTime()))/((double)(p2.getTime()-p1.getTime()));
		double newLatitude = p1.getLatitude()+frac*(p2.getLatitude()-p1.getLatitude());
		double newLongitude = p1.getLongitude()+frac*(p2.getLongitude()-p1.getLongitude());
		//final Long newDate = newPoint;
		return new SimpleFormat(newPoint, newLatitude, newLongitude);
	}

	public static GPSFormat interpolate(Trajectory t, long newPoint) {
		if (t.containsTime(newPoint)) return t.getPoint(newPoint);
		GPSFormat p1 = t.getPoint(newPoint-t.closestTimeLower(newPoint));
		GPSFormat p2 = t.getPoint(newPoint+t.closestTimeGreater(newPoint));
		double frac = ((double)(newPoint - p1.getTime()))/((double)(p2.getTime()-p1.getTime()));
		double newLatitude = p1.getLatitude()+frac*(p2.getLatitude()-p1.getLatitude());
		double newLongitude = p1.getLongitude()+frac*(p2.getLongitude()-p1.getLongitude());
		//final Long newDate = newPoint;
		return new SimpleFormat(newPoint, newLatitude, newLongitude);
	}

}
