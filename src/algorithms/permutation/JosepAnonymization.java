package algorithms.permutation;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import algorithms.AnonymizationMethod;

import distances.Distance;
import distances.FrechetDistance;
import distances.FrechetDistanceEuclideanBased;
import distances.Transformation;

import util.Timer;
import wrappers.Trajectory;

/*Trujillo- May 15, 2013
 * This method is based on the Frechet distance in its simplest variant.*/
public abstract class JosepAnonymization extends AnonymizationMethod{

	protected Distance distance;
	
	public JosepAnonymization(String preffix, Distance distance) {
		super(preffix, "josep");
		this.distance = distance;
	}
	
	@Override
	public List<List<Trajectory>> createClusters(int k, List<Trajectory> trajectories){
		//loadTrajectories();
		List<List<Trajectory>> result = new LinkedList<List<Trajectory>>();
		while (trajectories.size() >= k){
			Trajectory centroide = findCentroide(trajectories);
			Trajectory pivote = findCloserTrajectoryFrom(centroide, trajectories, distance);
			Hashtable<Trajectory, Double> partialResult = new Hashtable<Trajectory, Double>();
			double worstDistance = -1;
			Trajectory toRemove = null;			
			for (Trajectory trajectory : trajectories) {
				if (pivote.getIdentifier().equals(trajectory.getIdentifier())) continue;
				Double transformation = distance.distance(pivote, trajectory);
				if (Double.isNaN(transformation) || Double.isInfinite(transformation)){
					throw new RuntimeException("Distance between pivote "+pivote.getIdentifier()+
							" and trajectory "+trajectory.getIdentifier()+" is "+transformation);
				}
				if (partialResult.size() < k-1){
					partialResult.put(trajectory, transformation);
					if (transformation > worstDistance) {
						worstDistance = transformation;
						toRemove = trajectory;
					}
				}
				else{
					if (transformation > worstDistance) {
						continue;
					}
					partialResult.remove(toRemove);
					partialResult.put(trajectory, transformation);
					worstDistance = 0;
					for (Trajectory t : partialResult.keySet()) {
						Double value = partialResult.get(t);
						if (value > worstDistance) {
							worstDistance = value;
							toRemove = t;
						}
					}
				}
			}
			List<Trajectory> finalCluster = new LinkedList<Trajectory>();
			if (partialResult.size() == k-1){
				//es porque el cluster es bueno, por tanto borramos las trajectorias.
				finalCluster.add(pivote);
				for (Trajectory t : partialResult.keySet()) {
					if (!trajectories.remove(t)) 
						throw new RuntimeException("Could not remove from dst the trajectory "+t);
					finalCluster.add(t);
				}
				result.add(finalCluster);
			}
			if (!trajectories.remove(pivote)) {
				throw new RuntimeException("The pivote ="+pivote.getIdentifier()+" does not appear");
			}
		}
		return result;
	}
	
	/*@Override
	public List<Trajectory> anonymizeAllEfficiently(List<Trajectory> trajectories, int k){
		return anonymizeAll(k);
	}*/


}
