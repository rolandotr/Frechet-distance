package util;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import clustering.*;
import clustering.Cluster.PointInsideTrajectory;
import distances.Distance;

import trajectory.SimpleTrajectory;
import trajectory.Trajectory;
import wrappers.GPSFormat;
import wrappers.SimpleFormat;

/** 21/09/2010 Trujillo Comment
 * Esta clase es para implementar estaticamente los metodos de anonimizacion que vayas proponiendo*/
public class Anonymization {



	public static int probability = 0;
	
	public static double DISTORTION;

	public static float POINTS_REMOVED;

	public static float POINTS_SWAPPED;
	
	public static int TRAJECTORIES_REMOVED;
	
	/** 25/09/2010 Trujillo Comment
	 * Este metodo va a transformar las trajectorias que se estan pasando como parametro
	 * asi que ojo porque el que quiera mentenerlas debe hacerle una copia. Vease que este metodo puede
	 * ser usado para el paper del congreso porque se basa en clusters fijos*/
	public static List<List<Trajectory>>  conferenceAnonymizationMethod(List<Trajectory> trajectories, 
			DistortionMethod distortionMethod, int k, double timethreshold, 
			double spaceThreshold, Distance dist, boolean lDiversity){
		List<List<Trajectory>> clusters = Cluster.greedyClustering(trajectories, k, dist);
		List<List<Trajectory>> result = new ArrayList<List<Trajectory>>();
		int cont = 1;
		for (List<Trajectory> cluster : clusters){
			List<Trajectory> anonymizedSet = distortionMethod.distorte(cluster, timethreshold, 
					spaceThreshold, dist, lDiversity);
			result.add(anonymizedSet);
		}
		return result;
	}
	

	/** 28/09/2010 Trujillo Comment
	 * La diferencia de este metodo con respecto al primero es que en este vamos a tratar de no eliminar
	 * tantos puntos. Esto lo lograremos no haciendo clusters fijos, y simplemente cada vez que se le acaben 
	 * los puntos a una trajectoria entonces buscar otra trajectoria para incorporarla al proceso de seleccion
	 * de puntos
	 * @throws IOException */
	public static List<Trajectory>  journalAnonymizationMethod2(List<Trajectory> trajectories, 
			int k, UtilityCriterion criterion, Distance dist, boolean lDiversity, 
			ObjectOutputStream probOut) throws IOException{
		Anonymization.probability = 0;
		//lo primero es buscar el mejor cluster a partir de la trajectoria mas larga
		//Trajectory biggerTrajectory = Cluster.findBiggerTrajectory(trajectories);
		//List<Trajectory> cluster = Cluster.findBestCluster3(trajectories, k, biggerTrajectory, dist);
		List<Trajectory> cluster = Cluster.findBestCluster2(trajectories, k, dist);
		Hashtable<String, Trajectory> result = new Hashtable<String, Trajectory>();
		//Hashtable<Trajectory, Hashtable<Trajectory, Integer>> probabilities = new Hashtable<Trajectory, Hashtable<Trajectory,Integer>>();
		for (Trajectory t : trajectories){
			Trajectory tmp = new SimpleTrajectory(t.getIdentifier());
			result.put(t.getIdentifier(), tmp);
			//probabilities.put(t, new Hashtable<Trajectory, Integer>());
		}
		Random r = new Random();
		List<PointInsideTrajectory> pointCluster;
		int removedTrajectories = 0;
		while (cluster != null){
			Trajectory smaller = Cluster.findSmallerTrajectory(cluster);
			for (GPSFormat p : smaller.points()){
				pointCluster = DistortionMethod.findBestPointCluster(cluster, 
						p, smaller, dist, criterion.getSpaceThreshold());
				/** 17/06/2011 Trujillo Comment
				 * Cada uno de los puntos dentro de este cluster, tienen la posibilidad
				 * de pertenecer a cualquier trayectoria de las que estan aqui*/
				if (pointCluster == null) continue;
				/*for (PointInsideTrajectory p1 : pointCluster){
					Hashtable<Trajectory, Integer> clusterM = probabilities.get(p1.t);
					//System.out.println(clusterM);
					for (PointInsideTrajectory p2 : pointCluster){
						if (!clusterM.containsKey(p2.t))
							clusterM.put(p2.t, 0);
						clusterM.put(p2.t, clusterM.get(p2.t)+1);
					}
					//probabilities.put(p1.t, clusterM);
				}*/
				/*pointCluster = DistortionMethod.findBestPointCluster2(cluster, 
						p, smaller, criterion.getSpaceThreshold(), dist);*/
				applyDistortion(pointCluster, smaller, result, k, r, dist);
			}
			//ahora removemos la trajectoria smaller porque ya se deben haber swapeado todos
			//los puntos
			trajectories.remove(smaller);
			cluster.remove(smaller);
			List<Trajectory> toRemove = new ArrayList<Trajectory>();
			for (int i = 0; i < cluster.size(); i++){
				Trajectory t = cluster.get(i);
				if (t.size() == 0) {
					trajectories.remove(t);
					toRemove.add(t);
				}
			}
			for (Trajectory t : toRemove){
				cluster.remove(t);
			}
			//System.out.println("Data set size decreasing: "+trajectories.size());
			cluster = Cluster.findBestCluster3(trajectories, k, cluster, dist);
		}
		List<Trajectory> result2 = new ArrayList<Trajectory>();
		removedTrajectories = 0;
		for (Trajectory t : result.values()){
			if (t.size() == 0) continue;
			result2.add(t);
			removedTrajectories++;
		}
		/*Hashtable<Trajectory, Hashtable<Trajectory, Integer>> probabilities2 = new Hashtable<Trajectory, Hashtable<Trajectory,Integer>>();
		for (Trajectory original : probabilities.keySet()){
			for (Trajectory anonymized : result2){
				if (!original.getIdentifier().equals(anonymized.getIdentifier()))
					continue;
				Hashtable<Trajectory, Integer> origCluster = probabilities.get(original);
				Hashtable<Trajectory, Integer> anonyCluster = new Hashtable<Trajectory, Integer>();
				for (Trajectory origT : origCluster.keySet()){
					for (Trajectory anonyT : result2){
						if (!origT.equals(anonyT)) continue;
						anonyCluster.put(anonyT, origCluster.get(origT));
					}
				}
				probabilities2.put(anonymized, anonyCluster);
			}
		}
		System.out.println("There are "+probabilities2.size()+" trajectories from "+result2.size());
		probOut.writeObject(probabilities2);*/
		return result2;
	}

	private static List<Trajectory> findCluster(List<Trajectory> trajectories, 
			int k, Distance dist){
		boolean old = true;
		List<Trajectory> clusterNew = null;
		List<Trajectory> clusterOld = null;
		if (!old)
			clusterNew = Cluster.findBestCluster4(trajectories, k, dist);
		else
			clusterOld = Cluster.findBestCluster2(trajectories, k, dist);
		if (clusterOld == null && old) return null;
		if (clusterNew == null && !old) return null;
		if (!old)
			return clusterNew;
		else return clusterOld;
	}
	
	public static List<Trajectory>  journalAnonymizationMethod4(Hashtable<String, Trajectory> trajectories, 
			int k, UtilityCriterion criterion, Distance dist, List<List<String>> clusters){
		Anonymization.POINTS_REMOVED = 0;
		Anonymization.POINTS_SWAPPED = 0;
		Anonymization.DISTORTION = 0;
		Anonymization.TRAJECTORIES_REMOVED = 0;
		/*List<List<Trajectory>> clustersTmp = new LinkedList<List<Trajectory>>();
		for (List<Trajectory> c : clusters){
			List<Trajectory> tmp = new LinkedList<Trajectory>();
			for (Trajectory t : c){
				tmp.add((Trajectory)t.clone());
			}
			clustersTmp.add(tmp);
		}*/
		Hashtable<String, Trajectory> result = new Hashtable<String, Trajectory>();
		int totalPoints = 0;
		for (Trajectory t : trajectories.values()){
			result.put(t.getIdentifier(), new SimpleTrajectory(t.getIdentifier()));
			totalPoints += t.size();
		}
		Random r = new Random();
		List<PointInsideTrajectory> pointCluster;
		long ini;
		long end;
		double time;
		for (List<String> c : clusters){
			List<Trajectory> cluster = new LinkedList<Trajectory>();
			for (String t : c){
				cluster.add(trajectories.get(t));
			}
			ini = System.currentTimeMillis();
			Trajectory smaller = Cluster.findSmallerTrajectory(cluster);
			for (GPSFormat p : smaller.points()){
				long iniLocal = System.currentTimeMillis();
				pointCluster = DistortionMethod.findBestPointCluster(cluster, 
						p, smaller, dist, criterion.getSpaceThreshold());
				if (pointCluster == null) {
					continue;
				}
				long endLocal = System.currentTimeMillis();
				time = ((double)endLocal-iniLocal)/60000;
				if (time > 1){
					System.out.println();
					System.out.println("Finding cluster of points tooks "+time+" minutes");
				}
				iniLocal = System.currentTimeMillis();
				applyDistortion2(pointCluster, smaller, result, k, r, dist);
				endLocal = System.currentTimeMillis();
				time = ((double)endLocal-iniLocal)/60000;
				if (time > 1){
					System.out.println();
					System.out.println("Anonymizing a cluster of points took "+time+" minutes");
				}
				//despues de esto, los puntos de estecluster han sido swappeados
				POINTS_SWAPPED += pointCluster.size();
			}
			//ahora removemos la trajectoria smaller porque ya se deben haber swapeado todos
			//los puntos
			//antes de elimar estas trajectorias, hacemos el conteo de lo que se ha
			//eliminado y lo que no
			POINTS_REMOVED += smaller.size()-result.get(smaller.getIdentifier()).size();
			//trajectories.remove(smaller);
			//cluster.remove(smaller);
			for (Trajectory t : cluster){
				if (!t.equals(smaller))
					POINTS_REMOVED += t.size();
				trajectories.remove(t);
			}
			end = System.currentTimeMillis();
			time = ((double)end-ini)/60000;
			if (time > 1){
				System.out.println();
				System.out.println("Anonymizing a cluster of trajectories took "+time+" minutes");
			}
		}
		List<Trajectory> result2 = new ArrayList<Trajectory>();
		for (Trajectory t : result.values()){
			if (t.size() == 0) {
				TRAJECTORIES_REMOVED ++;
				continue;
			}
			result2.add(t);
		}
		/*if (totalPoints != POINTS_REMOVED+POINTS_SWAPPED){
			throw new RuntimeException("Total points = "+totalPoints+", but "+POINTS_REMOVED+
					" where removed and "+POINTS_SWAPPED+" swapped");
		}*/
		POINTS_REMOVED = POINTS_REMOVED*100/totalPoints;
		POINTS_SWAPPED = POINTS_SWAPPED*100/totalPoints;
		return result2;
	}

	/** 09/12/2010 Trujillo Comment
	 * Este metodo es completmente distinto a los demas. Este ya no hace cluster de trajectorias,
	 * sino que hace cluster de puntos de tamaño k que cumplan con las restricciones dadas.*/
	public static List<Trajectory>  journalAnonymizationMethod3(List<Trajectory> trajectories, 
			int k, UtilityCriterion criterion, Distance dist){
		Random r = new Random();
		Hashtable<String, Trajectory> result = new Hashtable<String, Trajectory>();
		for (Trajectory t : trajectories){
			result.put(t.getIdentifier(), new SimpleTrajectory(t.getIdentifier()));
		}
		List<PointInsideTrajectory> pointCluster;
		while (!trajectories.isEmpty()){
			Trajectory selected = trajectories.get(r.nextInt(trajectories.size()));
			for (GPSFormat p : selected.points()){
				pointCluster = DistortionMethod.findBestPointCluster2(trajectories, p, selected, k, 
						criterion.getSpaceThreshold(), dist);
				if (pointCluster == null) continue;
//System.out.println(pointCluster.size());
				//pointCluster = DistortionMethod.findBestPointCluster2(trajectories, p, selected);
				applyDistortion(pointCluster, selected, result, k, r, dist);
				//ahora removemos la trajectoria smaller porque ya se deben haber swapeado todos
				//los puntos
			}
			trajectories.remove(selected);
			List<Trajectory> toRemove = new ArrayList<Trajectory>();
			for (int i = 0; i < trajectories.size(); i++){
				Trajectory t = trajectories.get(i);
				if (t.size() == 0) {
					toRemove.add(t);
				}
			}
			for (Trajectory t : toRemove){
				trajectories.remove(t);
			}
		}
		List<Trajectory> result2 = new ArrayList<Trajectory>();
		for (Trajectory t : result.values()){
			if (t.size() == 0) continue;
			result2.add(t);
		}
		return result2;
	}
	
	
	
	private static void applyDistortion(List<PointInsideTrajectory> pointCluster, 
			Trajectory smaller, Hashtable<String, Trajectory> result,
			int k, Random r, Distance dist){
		if (pointCluster == null || pointCluster.size() < k){
			/** 03/11/2010 Trujillo Comment
			 * En este caso, no el cluster no se va a usar y por tanto los puntos dentro de el
			 * no deben ser marcados como usados*/
			throw new RuntimeException();
		}
		if (pointCluster.size() != k) throw new RuntimeException();		
		List<Trajectory> candidateTrajectories = new ArrayList<Trajectory>();
		candidateTrajectories.add(result.get(smaller.getIdentifier()));
		for (PointInsideTrajectory point : pointCluster){
			if (point.t.equals(smaller)) continue;
			//aqui estamos eliminando los puntos que pertenecen al cluster
			//pero solo con probabilidad 1/k
			point.t.removePoint(point.p.getTime());
			//por tanto ponemos el punto como candidato a swapearse
			//y ahora ponemos la trajectoria
			candidateTrajectories.add(result.get(point.t.getIdentifier()));
		}
		long ini;
		long end;
		double time;
		for (PointInsideTrajectory point : pointCluster){
			ini = System.currentTimeMillis();
			int index = r.nextInt(candidateTrajectories.size());
			boolean ok = false;
			for (int cont = 0; cont < k; cont++){
				Trajectory trajectory = candidateTrajectories.get((index+cont) % candidateTrajectories.size());
				if (!trajectory.containsTime(point.p.getTime())){
					//pues lo ponemos
					trajectory.addPoint(point.p);
					//y agregamos la distorcion
					ok = false;
					for (PointInsideTrajectory p : pointCluster){
						if (p.t.equals(trajectory)){
							//Entonces este es el punto que se quito
							Anonymization.DISTORTION += 
								dist.distance(p.p, point.p);
							ok = true;
							break;
						}
					}			
					if (!ok) throw new RuntimeException();
					//y eliminamos esta trajectoria
					candidateTrajectories.remove(trajectory);
					break;
				}
			}			
			end = System.currentTimeMillis();
			time = ((double)end-ini)/60000;
			if (time > 1){
				System.out.println();
				System.out.println("Adding point normally took "+((double)end-ini)/60000+" minutes");
				System.out.println();
			}
			if (!ok) {
				//es porque el punto nunca se pudo insertar, asi que nada, le decimos
				//que se añada en la trayectoria mas cercana posible
				//System.out.println("Conflict adding point");
				ini = System.currentTimeMillis();
				long min = Long.MAX_VALUE;
				Trajectory candidate = null;
				for (Trajectory t : candidateTrajectories){
					long timeT = t.closestTimeNotUsed(point.p.getTime());
					if (Math.abs(timeT) < Math.abs(min)){
						min = timeT;
						candidate = t;
					}
				}
				//System.out.println(min);
				candidate.addPoint(point.p, point.p.getTime()+min);
				//y agregamos la distorcion
				for (PointInsideTrajectory p : pointCluster){
					if (p.t.equals(candidate)){
						//Entonces este es el punto que se quito
						Anonymization.DISTORTION += 
							dist.distance(p.p, point.p);
						ok = true;
						break;
					}
				}					
				if (!ok) throw new RuntimeException();
				//y eliminamos esta trajectoria
				candidateTrajectories.remove(candidate);
				end = System.currentTimeMillis();
				time = ((double)end-ini)/60000;
				if (time > 1){
					System.out.println();
					System.out.println("Conflict adding point took "+((double)end-ini)/60000+" minutes");
					System.out.println();
				}
			}
		}
	}
	
	private static void applyDistortion2(List<PointInsideTrajectory> pointCluster, 
			Trajectory smaller, Hashtable<String, Trajectory> result,
			int k, Random r, Distance dist){
		if (pointCluster == null || pointCluster.size() < k){
			/** 03/11/2010 Trujillo Comment
			 * En este caso, no el cluster no se va a usar y por tanto los puntos dentro de el
			 * no deben ser marcados como usados*/
			throw new RuntimeException();
		}
		if (pointCluster.size() != k) throw new RuntimeException();		
		GPSFormat points[] = new GPSFormat[k];
		int cont = 0;
		for (PointInsideTrajectory point : pointCluster){
			points[cont++] = point.p;
			if (point.t.equals(smaller)) continue;
			//aqui estamos eliminando los puntos que pertenecen al cluster
			//pero solo con probabilidad 1/k
			point.t.removePoint(point.p.getTime());
			//por tanto ponemos el punto como candidato a swapearse
			//y ahora ponemos la trajectoria
		}
		GPSFormat centroide = Interpolation.centroide(points);
		for (PointInsideTrajectory point : pointCluster){
			//if (!point.t.equals(smaller)) point.t.removePoint(point.time);
			Trajectory toChange = result.get(point.t.getIdentifier());
			if (toChange.containsTime(centroide.getTime())) continue;
			toChange.addPoint(centroide);
			Anonymization.DISTORTION += dist.distance(point.p, centroide);
		}
	}
	public static List<Trajectory> getSet(List<List<Trajectory>> clusters){
		List<Trajectory> result = new ArrayList<Trajectory>();
		for (List<Trajectory> cluster : clusters){
			for (Trajectory t : cluster)
				result.add(t);
		}
		return result;
	}
	
}
