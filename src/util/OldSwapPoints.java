package util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


import clustering.Cluster;
import clustering.Cluster.PointInsideTrajectory;
import distances.Distance;

import wrappers.GPSFormat;
import wrappers.SimpleFormat;
import wrappers.SimpleTrajectory;
import wrappers.Trajectory;

public class OldSwapPoints extends DistortionMethod{

	
	public OldSwapPoints(){
	}
	
	@Override
	public List<Trajectory> distorte(List<Trajectory> trajectories, double timeThreshold, 
			double spaceThreshold, Distance dist, boolean lDiversity) {
		if (trajectories.size() < 2) return trajectories;
		Hashtable<String, Trajectory> result = new Hashtable<String, Trajectory>();
		for (Trajectory t : trajectories){
			result.put(t.getIdentifier(), new SimpleTrajectory(t.getIdentifier()));
		}
		Random r = new Random();
		List<PointInsideTrajectory> pointCluster;
		List<PointInsideTrajectory> tmp;
		double dTmp;
		
		while (trajectories.size() > 0){
			Trajectory bigger = Cluster.findBiggerTrajectory(trajectories);
			/** 03/11/2010 Trujillo Comment
			 * Ahora vamos a buscar para cada punto el conjunto de puntos que cumple con las condiciones
			 * dadas por DefaultUtilityCriterion. Los puntos dentro de este conjunto seran permutados al igual que
			 * se hace en Anonymization.journalAnonymizationMethod2. */
			for (GPSFormat p : bigger.points()){
				pointCluster = findBestPointCluster(trajectories, p, bigger, timeThreshold, 
						spaceThreshold, dist);
				//Anonymization.pointsSwapped += 1;
				//Anonymization.pointsAnalyzed+= pointCluster.size();
				for (PointInsideTrajectory point : pointCluster){
					if (point.t.equals(bigger)) continue;
					point.t.removePoint(point.p.getTime());
					if (point.t.size() == 0) trajectories.remove(point.t);
				}
				/** 03/11/2010 Trujillo Comment
				 * primero quitamos los puntos que sabemos no se van a swapear*/
				tmp = new ArrayList<Cluster.PointInsideTrajectory>();
				for (PointInsideTrajectory point : pointCluster){
					if (r.nextInt(trajectories.size()) == 0){
						//entonces en este caso este punto no va a ser swapeado
						//esta es la trajectoria que debe contener el punto pivote
						Trajectory toTransform = result.get(point.t.getIdentifier());
						//aqui annadimos un punto que es la mezacla del pivote y el toSwap
						toTransform.addPoint(new SimpleFormat(point.p.getTime(), point.p.getLatitude(), 
								point.p.getLongitude()));
					}
					else {
						tmp.add(point);
					}
				}
				pointCluster = tmp;
				//bueno ya aqui tenemos el cluster y sabemos que todos deben ser cambiados aleateoreamente de lugar
				double[] x = new double[pointCluster.size()];
				double[] y = new double[pointCluster.size()];
				int cont = 0;
				for (PointInsideTrajectory point : pointCluster){
					x[cont] = point.p.getLatitude();
					y[cont++] = point.p.getLongitude();
				}
				cont = pointCluster.size();
				//este contador nos va ayudara a eliminar elementos
				for (PointInsideTrajectory point : pointCluster){
					int toSwap = r.nextInt(cont);
					dTmp = x[toSwap];
					x[toSwap] = x[cont-1];
					x[cont-1] = dTmp;
					dTmp = y[toSwap];
					y[toSwap] = y[cont-1];
					y[cont-1] = dTmp;
					//hasta aqui lo que hicimos es pasar para ultima posicion el elemento que vamos a usar
					Trajectory toTransform = result.get(point.t.getIdentifier());
					toTransform.addPoint(new SimpleFormat(point.p.getTime(), x[cont-1], 
							y[cont-1]));
					cont--;
				}
			}
			trajectories.remove(bigger);
		}
		List<Trajectory> result2 = new ArrayList<Trajectory>();
		for (Trajectory t : result.values()){
			result2.add(t);
		}
		return result2;
	}

	@Override
	public List<Trajectory> distorte(List<Trajectory> trajectories, Distance dist) {
		throw new RuntimeException();
	}
	
}
