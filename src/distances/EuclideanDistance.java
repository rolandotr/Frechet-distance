package distances;

import java.awt.Point;

import wrappers.GPSFormat;
import wrappers.Trajectory;

public class EuclideanDistance extends Distance{


	@Override
	public double distance(Point p1, Point p2){
		return Math.sqrt(Math.pow(p1.getX()-p2.getX(), 2)+Math.pow(p1.getY()-p2.getY(), 2));
	}

	@Override
	public double distance(double x1, double y1, double x2, double y2){
		return Math.sqrt(Math.pow(x1-x2, 2)+Math.pow(y1-y2, 2));
	}

	public double distance(double x1, double y1, double z1, double x2, double y2, double z2){
		return Math.sqrt(Math.pow(x1-x2, 2)+Math.pow(y1-y2, 2)+Math.pow(z1-z2, 2));
	}

	@Override
	public double distance(GPSFormat p1, GPSFormat p2) {
		return Math.sqrt(Math.pow(p1.getLatitude()-p2.getLatitude(), 2)+Math.pow(p1.getLongitude()-p2.getLongitude(), 2));
	}

	@Override
	public String getName() {
		return "euclidean_distance";
	}

}

