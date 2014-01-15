package trajectory;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import wrappers.GPSFormat;
import wrappers.SimpleFormat;
import distances.Distance;



/*Trujillo- Jan 15, 2014
 * We do not consider a trajectory dataset as a single list of trajectories. Instead, 
 * we consider a dataset as a set of list of trajectories. This gives us more flexibility 
 * since, obviously, it not is able to represent a single list of trajectories, but it 
 * also improves performance when the dataset is too large and it is smartly split. The 
 * idea here is first creating large clusters, which them can be processed again to find smaller
 * clusters.*/

/*Trujillo- Jan 15, 2014
 * This class is simply a List of Lists of trajectories. However, it has been designed to add other
 * functionalities not provided by the class List.*/
public class TrajectoryDataset {

	List<List<Trajectory>> trajectoryDataset;
	
	/*Trujillo- Jan 15, 2014
	 * Builds and empty dataset*/
	public TrajectoryDataset(){
		trajectoryDataset = new LinkedList<List<Trajectory>>();
	}
	
	/*Trujillo- Jan 15, 2014
	 * dataset provided*/
	public TrajectoryDataset(List<List<Trajectory>> trajectoryDataset){
		this.trajectoryDataset = trajectoryDataset;
	}

	/*Trujillo- Jan 15, 2014
	 * Path to dataset provided*/
	public TrajectoryDataset(String trajectoryDatasetPath){
		this();
		TrajectoryDatasetLoader loader = new TrajectoryDatasetLoader(trajectoryDatasetPath);
		while(loader.hasNext()){
			trajectoryDataset.add(loader.next());
		}
	}
	
	public static Trajectory getRandomPivotTrajectory(List<Trajectory> trajectories) {
		Random random = new Random();
		return trajectories.get(random.nextInt(trajectories.size()));
	}
	public static Trajectory findCloserTrajectoryFrom(Trajectory trajectory, List<Trajectory> trajectories, Distance distance){
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
	
	public static Trajectory findCentroide(List<Trajectory> trajectories){
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
	


}
