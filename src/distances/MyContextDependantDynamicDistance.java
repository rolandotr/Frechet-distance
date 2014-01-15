package distances;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;


import trajectory.SimpleTrajectory;
import trajectory.Trajectory;
import wrappers.GPSFormat;
import wrappers.GeneralizedPoint;
import wrappers.SimpleFormat;

public abstract class MyContextDependantDynamicDistance extends MyDynamicDistance{

	public double maxDistance = 0;
	public double maxTime = 0;
	
	protected int noiseSize;
	
	Hashtable<String, Hashtable<String, Transformation>> distance;
	
	public MyContextDependantDynamicDistance(int noiseSize, Collection<Trajectory> trajectories){
		super(noiseSize);
		double tmpSpace, tmpTime;
		Transformation t;
		distance = new Hashtable<String, Hashtable<String,Transformation>>();
		for (Trajectory t1 : trajectories) {
			Hashtable<String, Transformation> newTable = new Hashtable<String, Transformation>();
			for (Trajectory t2 : trajectories) {
				t = getTransformation(t1, t2);
				tmpSpace = t.cost;
				tmpTime = Math.max(Math.abs(t2.firstTime()-t1.firstTime()), Math.abs(t2.lastTime()-t1.lastTime()));
				if (tmpSpace > maxDistance) maxDistance = tmpSpace;
				if (tmpTime > maxTime) maxTime = tmpTime;
				newTable.put(t2.getIdentifier(), t);
			}
			distance.put(t1.getIdentifier(), newTable);
		}
	}
	
	@Override
	public double distance(Trajectory t1, Trajectory t2){
		Transformation t = distance.get(t1.getIdentifier()).get(t2.getIdentifier());
		double spatialCost = t.cost/maxDistance;
		double temporalCost = Math.max(Math.abs(t2.firstTime()-t1.firstTime()), Math.abs(t2.lastTime()-t1.lastTime()))/maxTime;
		//return Math.max(spatialCost, temporalCost);
		return (spatialCost+1)*(temporalCost+1);
	}
	

}
