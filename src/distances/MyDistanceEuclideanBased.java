package distances;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import algorithms.generalization.GeneralizedTrajectory;


import trajectory.SimpleTrajectory;
import trajectory.Trajectory;
import wrappers.GPSFormat;
import wrappers.GeneralizedPoint;
import wrappers.SimpleFormat;


public class MyDistanceEuclideanBased extends MyDynamicDistance{

	private int l;
	private Distance distance;

	public MyDistanceEuclideanBased(int l){
		super(l);
		distance = new EuclideanDistance();
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
		return "my_dynamic_euclidean_based_distance";
	}

}
