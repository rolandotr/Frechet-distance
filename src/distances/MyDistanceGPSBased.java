package distances;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import clustering.MyTrajectoryFormat;

import distances.MyDynamicDistance.Transformation;

import algorithms.generalization.GeneralizedTrajectory;


import trajectory.SimpleTrajectory;
import trajectory.Trajectory;
import util.Syntetic;
import util.Util;
import wrappers.GPSFormat;
import wrappers.GeneralizedPoint;
import wrappers.SimpleFormat;


public class MyDistanceGPSBased extends MyDynamicDistance{

	private int l;
	private Distance distance;
	
	public static void main(String[] args) {
		/*Trajectory t1 = Util.getCompressedTrajectory("real", "new_ojfieza.txt72781");		
		Trajectory t2 = Util.getCompressedTrajectory("real", "new_ektamy.txt32652");
		System.out.println(t1);
		System.out.println(t2);
		Transformation t = distance.getTransformation(t1, t2);	*/	
		//System.out.println(t.toString());
		MyDistanceGPSBased distance = new MyDistanceGPSBased(3);
		
		MyRecursiveDistance d = new MyRecursiveDistance(3) {
			
			@Override
			public double distance(GPSFormat p1, GPSFormat p2) {
				return new GPSDistance().distance(p1, p2);
			}
			
			@Override
			public double distance(double x1, double y1, double x2, double y2) {
				throw new RuntimeException();
			}
			
			@Override
			public double distance(Point p1, Point p2) {
				throw new RuntimeException();
			}
			
			@Override
			public String getName() {
				return "my_recursive_distance";
			}
		};
		
		MyRecursiveDistance.Transformation tran = null;		
		//System.out.println(tran.toString());
		double min = Double.MAX_VALUE;
		Trajectory bestT1 = null;
		Trajectory bestT2 = null;
		Transformation bestDynamicTrans = null;   
		MyRecursiveDistance.Transformation bestRecursiveTrans = null;
		Hashtable<String, Trajectory> dst = MyTrajectoryFormat.loadCompressedTrajecotries("real");
		for (Trajectory tra1 : dst.values()) {
			for (Trajectory tra2 : dst.values()) {
				Transformation dynamicTrans = distance.getTransformation(tra1, tra2);   
				MyRecursiveDistance.Transformation recursiveTrans = d.getTransformation(tra1, tra2);
				if (dynamicTrans.matches.size() != recursiveTrans.matches.size()){
					if (tra1.size() + tra2.size() < min){
						min = tra1.size() + tra2.size();
						bestT1 = tra1;
						bestT2 = tra2;
						bestDynamicTrans = dynamicTrans;
						bestRecursiveTrans = recursiveTrans;
					}
				}
			}
			if (bestT1 != null && bestT2 != null){
				System.out.println("Best so far size = "+bestT1.size()+","+bestT2.size());
				System.out.println(bestT1);
				System.out.println(bestT2);
				System.out.println("Dynamic distance");
				System.out.println(bestDynamicTrans);
				System.out.println("Recursive distance");
				System.out.println(bestRecursiveTrans);
			}
		}
	}
	
	public MyDistanceGPSBased(int l){
		super(l);
		distance = new GPSDistance();
	}

	@Override
	public double distance(Point p1, Point p2) {
		return distance.distance(p1, p2);
	}

	@Override
	public double distance(double x1, double y1, double x2, double y2) {
		return distance.distance(x1, y1, x2, y2);
	}

	@Override
	public double distance(GPSFormat p1, GPSFormat p2) {
		return distance.distance(p1, p2);
	}
	
	@Override
	public String getName() {
		return "my_dynamic_gps_based_distance";
	}

}
