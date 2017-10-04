package algorithms;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import clustering.MyTrajectoryFormat;
import distances.Distance;

import trajectory.TrajectoryDatasetLoader;
import util.Util;
import wrappers.GPSFormat;
import wrappers.SimpleFormat;
import wrappers.Trajectory;

/*Trujillo- May 15, 2013
 * As a rule, an anonymization method can be especified by the distance metric it uses and
 * the distortion method. There are plenty of distance metric (e.g. Euclidean, Frechet, or EDR) 
 * and distotion methods (e.g. permutation, average or centroide, or random distortion). However, 
 * it is usually the case that the distortion method is strongly related to the distance metric. 
 * Therefore, The way I see this class design is that every method based on a particular distance
 * should extend this abstract class. Then, each particular type of distortion must be placed
 * at the second level.*/
public abstract class DistanceBasedAnonymizationMethod {

	/*Trujillo- Jan 15, 2014
	 * Usually, there exist a trajectory dataset that is the input for this method. Alternatively, we could
	 * consider a constructor where a set of trajectories is given as a parameter.*/
	private String pathToTrajectoryDataset;
	
	protected Random random;//for randomness
	
	protected Distance distance;//the distance that going to be used for anonymization
	
	
	/*Trujillo- Jan 15, 2014
	 * Constructor where the dataset is in a persistent form.*/
	protected DistanceBasedAnonymizationMethod(String pathToTrajectoryDataset, Distance distance){
		this.pathToTrajectoryDataset = pathToTrajectoryDataset;
		this.distance = distance;
		random = new Random();		
	}
	

	/*Trujillo- May 15, 2013
	 * This method should create the clusters according to the specific algorithm*/
	public final List<List<Trajectory>> createClusters(int k) throws FileNotFoundException, IOException, ClassNotFoundException{
		TrajectoryDatasetLoader dataset = new TrajectoryDatasetLoader(pathToTrajectoryDataset);
		List<List<Trajectory>> result = new LinkedList<List<Trajectory>>();
		while (dataset.hasNext()){
			for (List<Trajectory> partialResult : createClusters(k, dataset.next())){
				result.add(partialResult);
			}
		}
		return result;
	}


	/*Trujillo- Jan 15, 2014
	 * Method that should be obviously be implemented by the subclasses*/
	public abstract List<List<Trajectory>> createClusters(int k, List<Trajectory> database);


	/*Trujillo- May 15, 2013
	 * This a method that can be only implemented once the distance metric and the distortion method are known*/
	public abstract List<Trajectory> anonymizeCluster(List<Trajectory> cluster);

	/*Trujillo- Jan 15, 2014
	 * Provides a whole list of anonymize trajectories. Note that, even if the dataset was split 
	 * in different list, this method returns a single data set.*/
	public List<Trajectory> anonymizeAll(int k) throws FileNotFoundException, ClassNotFoundException, IOException{
		//loadTrajectories();
		List<Trajectory> result = new LinkedList<Trajectory>();
		List<Trajectory> tmp;
		for (List<Trajectory> cluster : createClusters(k)){
			tmp = anonymizeCluster(cluster);
			for (Trajectory t : tmp){
				result.add(t);
			}
		}
		return result;
	}

	/*Trujillo- Jan 15, 2014
	 * This method is exactly equal to the previous one, however, here the clusters 
	 * are already provided by input.*/
	public List<Trajectory> anonymizeAll(List<List<Trajectory>> clusters){
		List<Trajectory> result = new LinkedList<Trajectory>();
		List<Trajectory> tmp;
		for (List<Trajectory> cluster : clusters){
			tmp = anonymizeCluster(cluster);
			for (Trajectory t : tmp){
				result.add(t);
			}
		}
		return result;
	}

	
	/*Trujillo- Jan 15, 2014
	 * A name for the method that works as an identifier*/
	public abstract String getName();
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public String toString() {
		return getName()+distance.toString();
	}

}
