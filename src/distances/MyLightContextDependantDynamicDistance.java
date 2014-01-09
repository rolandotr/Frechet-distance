package distances;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import distances.MyDynamicDistance.Transformation;


import wrappers.GPSFormat;
import wrappers.GeneralizedPoint;
import wrappers.SimpleFormat;
import wrappers.SimpleTrajectory;
import wrappers.Trajectory;

public abstract class MyLightContextDependantDynamicDistance extends MyDynamicDistance{

	public static final int MAXIMUM_DISTANCE = 20;
	public static final long MAXIMUM_TIME = 60*60*24;
	
	protected int noiseSize;
	
	public MyLightContextDependantDynamicDistance(int noiseSize){
		super(noiseSize);
	}
	
	@Override
	public double distance(Trajectory t1, Trajectory t2){
		Transformation t = getTransformation(t1, t2);
		long maximum = 0;
		for (GPSFormat p1 : t.matches.keySet()) {
			for (GPSFormat p2 : t.matches.get(p1)) {
				long temp = Math.abs(p2.getTime()-p1.getTime());
				if (temp > maximum) maximum = temp;
			}
		}
		int size = 0;		
		for (GPSFormat key : t.matches.keySet()) {
			size += t.matches.get(key).size();
		}
		double spatialCost = t.cost/(size*MAXIMUM_DISTANCE);
		double temporalCost = maximum/MAXIMUM_TIME;
		//return Math.max(spatialCost, temporalCost);
		return (spatialCost+1)*(temporalCost+1);
	}
	

}
