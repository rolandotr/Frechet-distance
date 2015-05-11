package util;

import wrappers.GPSFormat;
import wrappers.SimpleFormat;
import wrappers.Trajectory;

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
		if (t.firstTime() > newPoint || t.lastTime() < newPoint){
			throw new RuntimeException("firstime = "+t.firstTime()+
					", lasttime = "+t.lastTime()+", and newPoint = "+newPoint);
		}
		if (t.containsTime(newPoint)) return t.getPoint(newPoint);
		GPSFormat p1 = t.getPoint(newPoint-t.closestTimeLower(newPoint));
		GPSFormat p2 = t.getPoint(newPoint+t.closestTimeGreater(newPoint));
		double frac = ((double)(newPoint - p1.getTime()))/((double)(p2.getTime()-p1.getTime()));
		double newLatitude = p1.getLatitude()+frac*(p2.getLatitude()-p1.getLatitude());
		double newLongitude = p1.getLongitude()+frac*(p2.getLongitude()-p1.getLongitude());
		//final Long newDate = newPoint;
		return new SimpleFormat(newPoint, newLatitude, newLongitude);
	}

	public static GPSFormat centroide(GPSFormat[] pointCluster) {
		double x = 0;
		double y = 0;
		long time = 0;
		for (int i = 0; i < pointCluster.length; i++) {
			x += pointCluster[i].getLatitude();
			y += pointCluster[i].getLongitude();
			time += pointCluster[i].getTime();
		}
		x = x/pointCluster.length;
		y = y/pointCluster.length;
		time = time/pointCluster.length;
		return new SimpleFormat(time, x, y);
	}
	
	public static void main(String[] args) {
		GPSFormat p1 = new SimpleFormat(95, 9371.751469901668, 17995.922925931387); 	
		GPSFormat p2 = new SimpleFormat(96, 9341.322034773306, 18024.872010161613); 	
		GPSFormat p3 = new SimpleFormat(97, 9310.892599644943, 18053.82109439184);
		GPSFormat p4 = new SimpleFormat(98, 9280.46316451658, 18082.770178622064);
		GPSFormat p6 = new SimpleFormat(99, 9273.942571274787, 18088.973553814256);
		GPSFormat p7 = new SimpleFormat(100, 9267.421978032995, 18095.176929006448);
		System.out.println("interpolated = "+Interpolation.interpolate(p1, p7, 96));
	}

}
