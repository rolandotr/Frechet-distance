package distances;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import clustering.Cluster.PointInsideTrajectory;

import trajectory.Trajectory;
import util.CartesianCoordinates;
import util.Converter;
import wrappers.GPSFormat;

public abstract class DistanceOld {

	public abstract double distance(Point p1, Point p2);

	public abstract double distance(double x1, double y1, double x2, double y2);

	public abstract double distance(GPSFormat p1, GPSFormat p2);

	/** 17/08/2010 Trujillo Comment
	 * Computa para cada punto la distancia entre ellos y entonces devuelve el promedio*/
	public double intraClusterAverageDistance(
			List<PointInsideTrajectory> cluster) {
		double distance = 0;
		for (PointInsideTrajectory p1 : cluster){
			for (PointInsideTrajectory p2 : cluster){
				distance+= distance(p1.p, p2.p);
			}
		}
		return distance/(cluster.size()*2);
	}
	
	public double intraTrajectoryClusterAverageDistance(
			List<Trajectory> cluster) {
		double distance = 0;
		for (Trajectory p1 : cluster){
			System.out.println(p1);
			for (Trajectory p2 : cluster){
				distance+= distance(p1, p2);
			}
		}
		return distance/(cluster.size()*2);
	}

	public double distance(TreeMap<Long, GPSFormat> t1,
			TreeMap<Long, GPSFormat> t2) {
		double distance = 0;
		int cont = 0;
		List<GPSFormat> l1 = new ArrayList<GPSFormat>();
		List<GPSFormat> l2 = new ArrayList<GPSFormat>();
		for (long time : t1.keySet()){
			l1.add(t1.get(time));
		}
		for (long time : t2.keySet()){
			l2.add(t2.get(time));
		}
		for (int i = 0; i < Math.min(l1.size(), l2.size()); i++){
			distance += distance(l1.get(i), l2.get(i));
			cont++;
		}
		if (cont == 0) return Double.MAX_VALUE;
		return distance/cont;
	}

	public double length(Trajectory t) {
		GPSFormat first = null;
		GPSFormat second = null;
		double result = 0;
		for (long time : t.times()){
			if (first == null) {
				first = t.getPoint(time);
				continue;
			}
			second = t.getPoint(time);
			result += distance(first, second);
			first = second;
		}
		return result; 
	}

	public double distance(Trajectory t1, Trajectory t2){
		double distance = 0;
		int cont = 0;
		List<GPSFormat> l1 = new ArrayList<GPSFormat>();
		List<GPSFormat> l2 = new ArrayList<GPSFormat>();
		for (long time : t1.times()){
			l1.add(t1.getPoint(time));
		}
		for (long time : t2.times()){
			l2.add(t2.getPoint(time));
		}
		for (int i = 0; i < Math.min(l1.size(), l2.size()); i++){
			distance += distance(l1.get(i), l2.get(i));
			cont++;
		}
		if (cont == 0) return Double.MAX_VALUE;
		return distance/cont;		
	}

	public double distance(double[] p1, GPSFormat p2) {
		return distance(Converter.xyzToDegrees(p1), p2);
	}


}
