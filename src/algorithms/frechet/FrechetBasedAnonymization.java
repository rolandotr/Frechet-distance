package algorithms.frechet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import algorithms.AnonymizationMethod;

import distances.FrechetDistance;
import distances.FrechetDistanceEuclideanBased;
import distances.Transformation;

import wrappers.Trajectory;

/*Trujillo- May 15, 2013
 * This method is based on the Frechet distance in its simplest variant.*/
public abstract class FrechetBasedAnonymization extends AnonymizationMethod{

	protected FrechetDistance distance;
	
	public FrechetBasedAnonymization(String preffix, FrechetDistance distance) {
		super(preffix, "frechet");
		this.distance = distance;
	}
	
	@Override
	public List<List<Trajectory>> createClusters(int k, List<Trajectory> trajectories){
		//loadTrajectories();
		List<List<Trajectory>> result = new LinkedList<List<Trajectory>>();
		List<Transformation> tmp;
		List<Trajectory> tmp2;
		while (trajectories.size() >= k) {
			tmp = getTransformation(trajectories, k, distance);
			if (tmp == null) continue;
			tmp2 = new LinkedList<Trajectory>();
			tmp2.add(tmp.get(0).t1);
			for (Transformation t : tmp){
				tmp2.add(t.t2);
			}
			result.add(tmp2);
		}
		return result;
	}
	
	/*@Override
	public List<Trajectory> anonymizeAllEfficiently(List<Trajectory> trajectories, int k){
		List<Trajectory> result = new LinkedList<Trajectory>();
		for (List<Transformation> cluster = 
				getTransformation(trajectories, k, distance); 
				cluster != null; cluster = getTransformation(trajectories, k, distance)) {
			for (Trajectory trajectory : anonymizeClusterEfficiently(cluster)) {
				result.add(trajectory);
			}
		}
		return result;
	}*/

	/*Trujillo- May 15, 2013
	 * This is part of the optimization, while the clusters are created the way 
	 * the points are matched is also computed. However, this points can be distorted in several
	 * ways, that is why the method is abstract.*/
	//protected abstract List<Trajectory> anonymizeClusterEfficiently(List<Transformation> cluster);

	protected List<Transformation> getTransformation(
			List<Trajectory> trajectories, int k, FrechetDistance distance) {
		if (trajectories.size() < k) return null;
		Trajectory pivot = getRandomPivotTrajectory(trajectories);		
		List<Transformation> result = new ArrayList<Transformation>();
		double worstDistance = 0;
		Transformation transformation;
		Transformation toRemove = null;
		for (Trajectory trajectory : trajectories) {
			if (trajectory.equals(pivot)) continue;
			transformation = distance.distanceWithTransformationOptimized(pivot, trajectory);
			if (transformation == null){
				continue;
			}
			if (result.size() < k-1){
				result.add(transformation);
				if (transformation.distance > worstDistance) {
					worstDistance = transformation.distance;
					toRemove = transformation;
				}
				continue;
			}
			if (transformation.distance > worstDistance) continue;
			result.remove(toRemove);
			result.add(transformation);
			worstDistance = 0;
			for (Transformation t : result) {
				if (t.distance > worstDistance) {
					worstDistance = t.distance;
					toRemove = t;
				}
			}
		}
		if (!trajectories.remove(pivot)) throw new RuntimeException();
		if (result.size() == k-1){
			for (Transformation t : result) {
				if (!trajectories.remove(t.t2)) throw new RuntimeException();
			}
		}
		else{
			return null;
		}
		return result;
	}

	protected int[] getRelativePositions(List<Transformation> cluster, double[] alpha, int pos) {
		int[] result = new int[cluster.size()];
		int cont = 0;
		double value = alpha[pos];
		for (Transformation t : cluster) {
			int posTmp = 0;
			while (t.alpha[posTmp] < value)
				posTmp++;
			result[cont++] = posTmp;
			//System.out.println("Alpha size = "+t.alpha.length);
			//System.out.println("Beta size = "+t.beta.length);
			//System.out.println("Pos = "+posTmp);
			//System.out.println("value = "+value+" and found "+t.alpha[posTmp]);
		}
		return result;
	}

	protected List<Transformation> createTransformations(List<Trajectory> cluster) {
		List<Transformation> result = new LinkedList<Transformation>();
		Trajectory pivot = null;
		Trajectory tmp = null;
		boolean first = true;
		for (Trajectory t : cluster) {
			if (first) {
				pivot = t;
				first = false;
			}
			else{
				tmp = t;
				Transformation transformation = distance.distanceWithTransformationOptimized(pivot, tmp);
				//System.out.println("Frechet distance between "+pivot+" and "+tmp+" could not be computed");
				result.add(transformation);
			}
		}
		return result;
	}

}
