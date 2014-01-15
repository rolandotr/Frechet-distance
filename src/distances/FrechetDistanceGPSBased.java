package distances;

import java.awt.Point;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import clustering.MyTrajectoryFormat;


import util.Converter;
import util.Geometry;
import distances.Distance;
import distances.EuclideanDistance;
import wrappers.GPSFormat;
import wrappers.SimpleFormat;
import wrappers.SimpleTrajectory;
import wrappers.Trajectory;

public class FrechetDistanceGPSBased extends FrechetDistance{

	
	public FrechetDistanceGPSBased(){
		this(-1);
	}	
	
	/*public FrechetDistanceGPSBased(int percentage){
		super(percentage);
		distance = new GPSDistance();
	}*/	
	
	public FrechetDistanceGPSBased(double compressThreshold){
		super(-1);
		this.compressThreshold = compressThreshold;
		distance = new GPSDistance();
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
				//double[] proyection = Geometry.computeProyectionIfEexistGPS(p, p1, p2);//OJO: This should be actived in practice
				double[] proyection = null;
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
				double[] proyection = Geometry.computeProyectionIfEexistGPS(p, p1, p2);
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
				double[] intersection = Geometry.computeMediatrizIntersectionGPS(p1, 
						p2, firstP1, firstP2);
				if (intersection != null){
					d = distance.distance(intersection, p1);
					result.put(d, d);
				}
				intersection = Geometry.computeMediatrizIntersectionGPS(firstP1, firstP2, 
						p1, p2);
				if (intersection != null){
					d = distance.distance(intersection, firstP1);
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
		double[] point1 = Converter.degreesToXYZ(p1);
		double[] point2 = Converter.degreesToXYZ(p2);
		double[] point = Converter.degreesToXYZ(p);
		
		EuclideanDistance distance = new EuclideanDistance();
		
		/*System.out.println("point1 vs point2 = " + distance.distance(point1[0], point1[1], point1[2], point2[0], point2[1], point2[2]));
		System.out.println("point1 vs p = " + distance.distance(point1[0], point1[1], point1[2], point[0], point[1], point[2]));
		System.out.println("point2 vs p = " + distance.distance(point2[0], point2[1], point2[2], point[0], point[1], point[2]));*/
		
		double a1 = point1[0];
		double b1 = point1[1];
		double a2 = point2[0];
		double b2 = point2[1];
		double a = point[0];
		double b = point[1];
		//double l = Math.pow((a2-a1), 2)+Math.pow((b2-b1), 2)+Math.pow((c2-c1), 2);
		//double m = 2*((a2-a1)*(a1-a) + (b2-b1)*(b1-b) + (c2-c1)*(c1-c));
		//double n = Math.pow((a-a1), 2)+Math.pow((b-b1), 2)+Math.pow((c-c1), 2) - epsilon*epsilon;
		double l = Math.pow((a2-a1), 2)+Math.pow((b2-b1), 2);
		double m = 2*((a2-a1)*(a1-a) + (b2-b1)*(b1-b));
		double n = Math.pow((a-a1), 2)+Math.pow((b-b1), 2) - epsilon*epsilon;
		
		double D = m*m-4*l*n;
		if (D < 0){
			//en este caso es que no hay solucion
			//System.out.println("No way to find the distance for epsiolobn = "+epsilon);
			return null;
		}
		if (l == 0){
			//en este caso es p1 == p2
			//por tanto hay lo que hay que hace es solo calcular la distance entre p y p1
			if (distance(p1, p) <= epsilon) return new double[]{t1, t2};
			else return null;
		}

		double z1 = (-m - Math.sqrt(D))/(2*l);
		double z2 = (-m + Math.sqrt(D))/(2*l);
		double result1 = z1*(t2-t1)+t1;
		double result2 = z2*(t2-t1)+t1;
		if (result1 > Math.max(t1, t2) || result2 < Math.min(t1, t2)){
			return null;
		}
		if (result1 < Math.min(t1, t2)) result1 = Math.min(t1, t2);
		if (result2 > Math.max(t1, t2)) result2 = Math.max(t1, t2);
		//System.out.println("l = "+l);
		//System.out.println("result1 = "+result1+", result2 = "+result2);
		return new double[]{result1, result2};		
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		System.setOut(new PrintStream("checking_frechet.txt"));
		Hashtable<String, Trajectory> dst = MyTrajectoryFormat.loadCompressedTrajecotries("real");
		FrechetDistanceGPSBased distance = new FrechetDistanceGPSBased();
		for (Trajectory t1 : dst.values()) {
			for (Trajectory t2 : dst.values()) {
				double d = distance.distance(t1, t2);
				if (d > 100) System.out.println(t1.getIdentifier() + " <----> " +t2.getIdentifier()+" = "+d);
			}			
		}
	}
	
	@Override
	public String getName() {
		return "frechet_distance_based";
	}


}
