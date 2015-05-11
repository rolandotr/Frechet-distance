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

import util.Util;
import wrappers.GPSFormat;
import wrappers.SimpleFormat;
import wrappers.SimpleTrajectory;
import wrappers.Trajectory;

/*Trujillo- May 15, 2013
 * As a rule, an anonymization method can be especified by the distance metric it uses and
 * the distortion method. There are plenty of distance metric (e.g. Euclidean, Frechet, or EDR) 
 * and distotion methods (e.g. permutation, average or centroide, or random distortion). However, 
 * it is usually the case that the distortion method is strongly related to the distance metric. 
 * Therefore, The way I see this class design is that every method based on a particular distance
 * should extend this abstract class. Then, each particular type of distortion must be placed
 * at the second level.*/
public abstract class AnonymizationMethod {

	private String preffix;
	protected Random random;
	protected String folder;
	protected String name;
	
	protected AnonymizationMethod(String preffix, String folder){
		this.preffix = preffix;
		random = new Random();
		this.folder = folder;
	}
	

	/*Trujillo- May 15, 2013
	 * This method should create the clusters according to the specific algorithm, that is why
	 * is an abstract method*/
	public final List<List<Trajectory>> createClusters(int k) throws FileNotFoundException, IOException, ClassNotFoundException{
		File f = new File(preffix+"/"+folder+"/");
		File[] files = f.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".obj");
			}
		});
		List<Trajectory> database;
		List<List<Trajectory>> result = new LinkedList<List<Trajectory>>();
		ObjectInputStream in;
		for (int i = 0; i < files.length; i++) {
			in = new ObjectInputStream(new FileInputStream(files[i]));
			database = (List<Trajectory>)in.readObject();
			for (List<Trajectory> partialResult : createClusters(k, database)){
				result.add(partialResult);
			}
		}
		return result;
	}

	public abstract List<List<Trajectory>> createClusters(int k, List<Trajectory> database);


	/*Trujillo- May 15, 2013
	 * This a method that can be only implemented once the distance metric and the distortion method are known*/
	public abstract List<Trajectory> anonymizeCluster(List<Trajectory> cluster);

	/*Trujillo- May 15, 2013
	 * Used for optimization only.*/
	//protected abstract List<Trajectory> anonymizeAllEfficiently(List<Trajectory> trajectories, int k);
	
	/*public List<Trajectory> anonymizeAllEfficiently(int k){
		loadTrajectories();
		return anonymizeAllEfficiently(trajectories, k);
	}*/
	
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

	protected Trajectory getRandomPivotTrajectory(List<Trajectory> trajectories) {
		return trajectories.get(random.nextInt(trajectories.size()));
	}

	/*protected void loadTrajectories() {
		trajectories = MyTrajectoryFormat.loadCompressedTrajecotriesInList(preffix);
	}*/
	
	public Trajectory findCloserTrajectoryFrom(Trajectory trajectory, List<Trajectory> trajectories, Distance distance){
		double min = Double.MAX_VALUE;
		double tmp;
		Trajectory result = null;
		for (Trajectory record : trajectories){
			tmp = distance.distance(trajectory,record);
			if (tmp < min){
				min = tmp;
				result = record;
			}
		}
		return result;
	}
	
	public Trajectory findCentroide(List<Trajectory> trajectories){
		Trajectory centroide = new SimpleTrajectory("temp");
		TreeMap<Long, Integer> counter = new TreeMap<Long, Integer>();
		GPSFormat tmp;
		GPSFormat tmpC;
		for (Trajectory t : trajectories){
			for (long time : t.times()){
				tmp = t.getPoint(time);
				if (centroide.containsTime(time)){
					tmpC = centroide.getPoint(time);
					centroide.addPoint(new SimpleFormat(time, tmp.getLatitude()+tmpC.getLatitude(), 
							tmp.getLongitude()+tmp.getLongitude()));
					counter.put(time, counter.get(time)+1);
				}
				else{
					centroide.addPoint(tmp);
					counter.put(time, 1);
				}
			}
		}
		Trajectory result = new SimpleTrajectory("centroide");
		for (long time : centroide.times()){
			result.addPoint(new SimpleFormat(time, 
					centroide.getPoint(time).getLatitude()/counter.get(time), 
					centroide.getPoint(time).getLongitude()/counter.get(time)));
		}
		return result;
	}
	
	public String getName(){
		return name;
	}
	
	@Override
	public int hashCode() {
		return getName().hashCode();
	}


	public void setName(String name) {
		this.name = name;
	}
}
