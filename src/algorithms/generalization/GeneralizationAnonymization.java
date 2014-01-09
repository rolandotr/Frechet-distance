package algorithms.generalization;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import algorithms.AnonymizationMethod;

import distances.Distance;
import distances.FrechetDistance;
import distances.FrechetDistanceEuclideanBased;
import distances.GPSDistance;
import distances.LogCostDistance;
import distances.LogCostDistance.Transformation;

import util.Timer;
import wrappers.GeneralizedPoint;
import wrappers.Trajectory;

/*Trujillo- May 15, 2013
 * This method is based on the Frechet distance in its simplest variant.*/
public abstract class GeneralizationAnonymization extends AnonymizationMethod{

	protected LogCostDistance distance;
	
	public GeneralizationAnonymization(String preffix, LogCostDistance distance) {
		super(preffix, "generalization");
		this.distance = distance;
	}
	
	@Override
	public List<List<Trajectory>> createClusters(int k, List<Trajectory> trajectories){
		//loadTrajectories();
		List<List<Trajectory>> result = new LinkedList<List<Trajectory>>();
		for (List<Trajectory> cluster = getCluster(k, trajectories); 
				cluster != null; cluster = getCluster(k, trajectories)) {
				result.add(cluster);
		}
		return result;
	}
	
	protected List<Trajectory> getCluster(int k, List<Trajectory> trajectories){
		if (trajectories.size() < k) return null;
		List<Trajectory> result = new LinkedList<Trajectory>();
		Trajectory tmp = getRandomPivotTrajectory(trajectories);
		result.add(tmp);
		trajectories.remove(tmp);
		GeneralizedTrajectory representative = LogCostDistance.generalizeAtomicTrajectory(tmp);
		for (int i = 1; i < k; i++) {
			Trajectory toBeAdded = findClosestTrajectory(representative, trajectories);
			representative = anonymize(representative, 
					LogCostDistance.generalizeAtomicTrajectory(toBeAdded));
			result.add(toBeAdded);
			trajectories.remove(toBeAdded);
		}
double avg = 0;
for (Trajectory t1 : result) {
	for (Trajectory t2 : result) {
		avg += this.distance.distance(t1, t2);
	}
}
System.out.println("Intra cluster distance is "+avg);
System.out.println("Avg Euclidean distance is "+util.Distance.intraClusterAverageDistance(result, new GPSDistance()));

		return result;
	}
	
	/*@Override
	public List<Trajectory> anonymizeAllEfficiently(List<Trajectory> trajectories, int k){
		return anonymizeAll(k);
	}*/

	private Trajectory findClosestTrajectory(GeneralizedTrajectory representative,
			List<Trajectory> trajectories) {
		double min = Double.MAX_VALUE;
		Trajectory minTrajectory = null;
		for (Trajectory trajectory : trajectories) {
			Transformation cost = distance.logCostDistance(LogCostDistance.generalizeAtomicTrajectory(trajectory), 
					representative); 
			if (cost.cost < min){
				min = cost.cost;
				minTrajectory = trajectory;
			}
		}
		return minTrajectory;
	}
	
	/***Trujillo- Feb 18, 2013
	 * Dos trajectories se anonymizan cogiendo los matching points 
	 * y cogiendo el bounding box.  
	 */
	private GeneralizedTrajectory anonymize(GeneralizedTrajectory t1, 
			GeneralizedTrajectory t2) {
		Transformation transf = distance.logCostDistance(t1, t2);
		GeneralizedTrajectory result = new GeneralizedTrajectory(t1.getIdentifier()+"-"+
				t2.getIdentifier());
		GeneralizedPoint p1;
		GeneralizedPoint p2;
		GeneralizedPoint tmp;
		for (int i = 0; i < transf.matchesForT1.length; i++) {
			p1 = transf.matchesForT1[i];
			p2 = transf.matchesForT2[i];
			tmp = mergePoints(p1, p2);
			result.addPoint(tmp);
		}
		return result;
	}

	private GeneralizedPoint mergePoints(GeneralizedPoint p1,
			GeneralizedPoint p2) {
		double minTime = (p1.t1 < p2.t1)?p1.t1:p2.t1;
		double maxTime = (p1.t2 > p2.t2)?p1.t2:p2.t2;
		double minX = (p1.x1 < p2.x1)?p1.x1:p2.x1;
		double maxX = (p1.x2 > p2.x2)?p1.x2:p2.x2;
		double minY = (p1.y1 < p2.y1)?p1.y1:p2.y1;
		double maxY = (p1.y2 > p2.y2)?p1.y2:p2.y2;
		return new GeneralizedPoint(minTime, maxTime, minX, maxX, minY, maxY);
	}

}
