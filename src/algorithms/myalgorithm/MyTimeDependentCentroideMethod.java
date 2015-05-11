package algorithms.myalgorithm;

import java.util.LinkedList;
import java.util.List;



import wrappers.GPSFormat;
import wrappers.GeneralizedPoint;
import wrappers.SimpleFormat;
import wrappers.SimpleTrajectory;
import wrappers.Trajectory;
import distances.MyDynamicDistance;
import distances.MyDynamicDistance.Transformation;

/*Trujillo- May 15, 2013
 * The purpose of this class is to define the distortion to the centroide (average)*/
public class MyTimeDependentCentroideMethod extends MyAnonymization{

	public MyTimeDependentCentroideMethod(String preffix, MyDynamicDistance distance) {
		super(preffix, distance);
	}

	@Override
	public List<Trajectory> anonymizeCluster(List<Trajectory> cluster) {
		Trajectory trajectory = anonymizeClusterToOneTrajectory(cluster);
		Trajectory tmp;
		List<Trajectory> result = new LinkedList<Trajectory>();
		for (Trajectory t : cluster) {
			tmp = (Trajectory)trajectory.clone();
			tmp.setIdentifier(t.getIdentifier());
			result.add(tmp);
		}
		return result;
	}
	
	private Trajectory anonymizeClusterToOneTrajectory(List<Trajectory> cluster) {
		Trajectory minimum = findBestTrajectory(cluster);
		Trajectory result = minimum;
		cluster.remove(minimum);
		for (Trajectory trajectory : cluster) {
			result = anonymize(result, trajectory);
		}
		cluster.add(minimum);
		return result;
	}
	

	private Trajectory findBestTrajectory(List<Trajectory> cluster) {
		double min = Double.MAX_VALUE;
		double trans = 0;
		Trajectory best = null;
		for (Trajectory t1 : cluster) {
			double totalCost = 0;
			for (Trajectory t2 : cluster) {
				trans = distance.distance(t1,t2);
				totalCost += trans;
			}
			if (totalCost < min){
				min = totalCost;
				best = t1;
			}
		}
		return best;
	}
	
	@Override
	public String toString() {
		return "my-centroide";
	}

	@Override
	public String getName() {
		return "MC";
		//return "generalization-centroide";
	}

}
