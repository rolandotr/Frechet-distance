package distances;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;


import util.Geometry;
import distances.Distance;
import distances.EuclideanDistance;
import wrappers.GPSFormat;
import wrappers.SimpleFormat;
import wrappers.SimpleTrajectory;
import wrappers.Trajectory;

public class FrechetDistanceEuclideanBased extends FrechetDistance{

	
	public FrechetDistanceEuclideanBased(){
		this(-1);
	}	
	
	public FrechetDistanceEuclideanBased(int percentage){
		super(percentage);
		distance = new EuclideanDistance();
	}	
	
	@Override
	protected void getProjectionValues(Trajectory t1, Trajectory t2, TreeMap<Double, Double> result) {
		double d;
		double d1;
		double d2;
		for (GPSFormat p : t1.values()){
			GPSFormat p1 = null;
			GPSFormat p2 = null;
			for (GPSFormat tmp : t2.values()){
				p2 = tmp;
				if (p1 == null){
					p1 = tmp;
					continue;
				}
				GPSFormat proyection = Geometry.computeProyectionIfEexist2D(p, p1, p2);
				if (proyection == null) {
					//en este caso, la proyeccion no esta dentro del segmento
					//entonces cogemos el punto mas cercano a p.
					d1 = distance.distance(p, p1);
					d2 = distance.distance(p, p2);
					d = (d1> d2)?d2:d1;
				}
				else{
					d = distance.distance(proyection, p);
				}
				result.put(d, d);
			}
		}
		for (GPSFormat p : t2.values()){
			GPSFormat p1 = null;
			GPSFormat p2 = null;
			for (GPSFormat tmp : t1.values()){
				p2 = tmp;
				if (p1 == null){
					p1 = tmp;
					continue;
				}
				GPSFormat proyection = Geometry.computeProyectionIfEexist2D(p, p1, p2);
				if (proyection == null) {
					//en este caso, la proyeccion no esta dentro del segmento
					//entonces cogemos el punto mas cercano a p.
					d1 = distance.distance(p, p1);
					d2 = distance.distance(p, p2);
					d = (d1> d2)?d2:d1;
				}
				else{
					d = distance.distance(proyection, p);
				}
				result.put(d, d);
			}
		}
	}

	@Override
	protected void getMediatrizValues(Trajectory t1, Trajectory t2, TreeMap<Double, Double> result) {
		GPSFormat firstP1 = null;
		GPSFormat firstP2 = null;
		double d;
		for (GPSFormat p : t1.values()){
			firstP2 = p;
			if (firstP1 == null){
				firstP1 = p;
				continue;
			}
			GPSFormat p1 = null;
			GPSFormat p2 = null;
			for (GPSFormat tmp : t2.values()){
				p2 = tmp;
				if (p1 == null){
					p1 = tmp;
					continue;
				}
				double[] line = Geometry.computeMediatriz2D(p1, p2);
				GPSFormat intersection = Geometry.computeIntersectionIfExist2D(line, firstP1, firstP2);
				if (intersection != null){
					d = distance.distance(intersection, p1);
					result.put(d, d);
				}
				line = Geometry.computeMediatriz2D(firstP1, firstP2);
				intersection = Geometry.computeIntersectionIfExist2D(line, p1, p2);
				if (intersection != null){
					d = distance.distance(intersection, firstP1);
					//System.out.println("Mediatriz distance = "+d);
					result.put(d, d);
				}
			}
		}
		/*for (GPSFormat p : t2.values()){
			GPSFormat p1 = null;
			GPSFormat p2 = null;
			for (GPSFormat tmp : t1.values()){
				p2 = tmp;
				if (p1 == null){
					p1 = tmp;
					continue;
				}
				GPSFormat proyection = Geometry.computeProyectionIfEexist2D(p, p1, p2);
				if (proyection != null){
					d = distance.distance(proyection, p);
					result.put(d, d);
				}
			}
		}*/
	}
	
	@Override
	protected double[] computesTimesOfIntersectionWithElipse(long t1, long t2, GPSFormat p1, 
			GPSFormat p2, GPSFormat p, double epsilon){
		double ax = p1.getLatitude();
		double ay = p1.getLongitude();
		double bx = p2.getLatitude()-ax;
		double by = p2.getLongitude()-ay;
		double b = 2*(bx*(ax-p.getLatitude())+by*(ay-p.getLongitude()));
		double a = bx*bx + by*by;
		double c = Math.pow(ax-p.getLatitude(), 2)+Math.pow(ay-p.getLongitude(), 2)-Math.pow(epsilon, 2);
		double D = b*b-4*a*c;
		if (D < 0){
			//en este caso es que no hay solucion
			return null;
		}
		if (a == 0){
			//en este caso es p1 == p2
			//por tanto hay lo que hay que hace es solo calcular la distance entre p y p1
			if (distance(p1, p) <= epsilon) return new double[]{t1, t2};
			else return null;
		}
		double z1, z2;
		/*if (a == 0 && b == 0) {
			return null;
		}
		if (a == 0){
			z1 = -c/b;
			z2 = z1;
		}*/
		z1 = (-b - Math.sqrt(D))/(2*a);
		z2 = (-b + Math.sqrt(D))/(2*a);
		double result1 = z1*(t2-t1)+t1;
		double result2 = z2*(t2-t1)+t1;
		if (result1 > Math.max(t1, t2) || result2 < Math.min(t1, t2)){
			return null;
		}
		if (result1 < Math.min(t1, t2)) result1 = Math.min(t1, t2);
		if (result2 > Math.max(t1, t2)) result2 = Math.max(t1, t2);
		return new double[]{result1, result2};
	}

	@Override
	public String getName() {
		return "frechet_euclidean_based";
	}

}
