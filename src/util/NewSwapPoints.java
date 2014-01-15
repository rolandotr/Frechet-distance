package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;



import clustering.Cluster;
import clustering.Cluster.PointInsideTrajectory;
import distances.Distance;

import trajectory.SimpleTrajectory;
import trajectory.Trajectory;
import wrappers.GPSFormat;
import wrappers.SimpleFormat;

public class NewSwapPoints extends DistortionMethod{

	
	public NewSwapPoints(){
	}
	
	@Override
	public List<Trajectory> distorte(List<Trajectory> trajectories, double timeThreshold, 
			double spaceThreshold, Distance dist, boolean lDiversity) {
		if (trajectories.size() < 2) return trajectories;
		Trajectory smaller = Cluster.findSmallerTrajectory(trajectories);
		List<Trajectory> result = new ArrayList<Trajectory>();
		for (Trajectory t : trajectories){
			result.add(new SimpleTrajectory(t.getIdentifier()));
		}
		Random r = new Random();
		List<PointInsideTrajectory> pointCluster;
		
		for (GPSFormat p : smaller.points()){
			pointCluster = findBestPointCluster(trajectories, p, smaller, dist, spaceThreshold);
			if (pointCluster == null){
				/** 24/09/2010 Trujillo Comment
				 * no se pudo encontrar un cluster de puntos, por tanto hasta aqui llego la historia con este punto*/
				continue;
			}
			for (PointInsideTrajectory point : pointCluster){
				if (point.t.equals(smaller)) continue;
				point.t.removePoint(point.p.getTime());
			}
			//bueno ya aqui tenemos el cluster y sabemos ademas que los puntos se ordenan de la misma forma
			//en que se ordenan en el for each
			long[] times = new long[pointCluster.size()];
			int cont = 0;
			for (PointInsideTrajectory point : pointCluster){
				times[cont++] = point.p.getTime();
			}
			for (int i = 0; i < result.size(); i++){
				int index = r.nextInt(pointCluster.size());
				//este es el punto qye vamos a usar para swapear
				PointInsideTrajectory toSwap = pointCluster.get(index);
				//y este es el punto que tiene el tiempo donde se pondra el punto
				long pivote = times[i];
				//esta es la trajectoria que debe contener el punto pivote
				Trajectory toTransform = result.get(i);
				//aqui annadimos un punto que es la mezacla del pivote y el toSwap
				toTransform.addPoint(new SimpleFormat(pivote, toSwap.p.getLatitude(), 
						toSwap.p.getLongitude()));
				//entonces el que se swapeo debe ser eliminado
				pointCluster.remove(index);
			}
		}
		return result;
	}

	@Override
	public List<Trajectory> distorte(List<Trajectory> trajectories,
			Distance dist) {
		throw new RuntimeException();
	}

}
