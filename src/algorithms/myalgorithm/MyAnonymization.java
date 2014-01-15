package algorithms.myalgorithm;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import clustering.Cluster;
import clustering.Cluster.PointInsideTrajectory;

import algorithms.DistanceBasedAnonymizationMethod;

import distances.EuclideanDistance;
import distances.GPSDistance;
import distances.MyDynamicDistance;
import distances.MyDynamicDistance.Transformation;

import util.Distance;
import util.Print;
import util.Statistics;
import wrappers.GPSFormat;
import wrappers.SimpleFormat;
import wrappers.SimpleTrajectory;
import wrappers.Trajectory;

/*Trujillo- May 15, 2013
 * This method is based on the Frechet distance in its simplest variant.*/
public abstract class MyAnonymization extends DistanceBasedAnonymizationMethod{

	protected MyDynamicDistance distance;
	
	public MyAnonymization(String preffix, MyDynamicDistance distance) {
		super(preffix+"myone/", distance);
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
		Trajectory representative = tmp;
		for (int i = 1; i < k; i++) {
			Trajectory toBeAdded = findClosestTrajectory(representative, trajectories);
			representative = anonymize(toBeAdded, representative);
			result.add(toBeAdded);
			trajectories.remove(toBeAdded);
		}		
/*Print.printList(result, "The cluster found is : ");
String preffix = null;
List<Trajectory> bestCluster = new LinkedList<Trajectory>();
for (Trajectory trajectory : trajectories) {
	if (preffix == null){
		preffix = trajectory.getIdentifier().substring(0, trajectory.getIdentifier().length()-2);
		bestCluster.add(trajectory);
	}
	else{
		String nextPreffix = trajectory.getIdentifier().substring(0, trajectory.getIdentifier().length()-2);
		if (preffix.equals(nextPreffix)){
			bestCluster.add(trajectory);
		}
	}
}
Print.printList(bestCluster, "However, this is really the best cluster");
System.out.println("The distances are:");*/
double avg = 0;
for (Trajectory t1 : result) {
	for (Trajectory t2 : result) {
		avg += this.distance.distance(t1, t2);
	}
}
System.out.println("Intra cluster distance is "+avg/(result.size()*(result.size()-1)));
System.out.println("Avg Euclidean distance is "+Distance.intraClusterAverageDistance(result, new GPSDistance()));

		return result;
	}
	
	/*@Override
	public List<Trajectory> anonymizeAllEfficiently(List<Trajectory> trajectories, int k){
		return anonymizeAll(k);
	}*/

	private Trajectory findClosestTrajectory(Trajectory pivot,
			List<Trajectory> trajectories) {
		double min = Double.MAX_VALUE;
		Trajectory minTrajectory = null;
		for (Trajectory trajectory : trajectories) {
			double cost = distance.distance(pivot,trajectory);
			//System.out.println("Distance between "+pivot+" and "+trajectory+" = "+cost);
			if (cost < min){
				min = cost;
				minTrajectory = trajectory;
			}
		}
		return minTrajectory;
	}
	
	/***Trujillo- Feb 18, 2013
	 * Dos trajectories se anonymizan cogiendo los matching points 
	 * y cogiendo el bounding box.  
	 */
	protected Trajectory anonymize(Trajectory t1, 
			Trajectory t2) {
		Hashtable<GPSFormat, List<GPSFormat>> transf = distance.getMatches(t1, t2);
		Trajectory result = new SimpleTrajectory(t1.getIdentifier()+"-"+
				t2.getIdentifier());
		GPSFormat tmp;
		for (GPSFormat p1 : transf.keySet()) {
			for (GPSFormat p2 : transf.get(p1)) {
				tmp = mergePoints(p1, p2);
				result.addPoint(tmp);
			}
		}
		return result;
	}

	private GPSFormat mergePoints(GPSFormat p1,
			GPSFormat p2) {
		return new SimpleFormat((p1.getTime()+p2.getTime())/2, (p1.getLatitude()+p2.getLatitude())/2, (p1.getLongitude()+p1.getLongitude())/2);
	}

}
