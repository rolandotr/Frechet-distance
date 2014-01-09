package util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import clustering.Cluster.PointInsideTrajectory;
import distances.Distance;
import distances.GraphDistance;

import wrappers.GPSFormat;
import wrappers.SimpleFormat;
import wrappers.SimpleTrajectory;
import wrappers.Trajectory;


/** 21/09/2010 Trujillo Comment
 * Esta es una clase abstracta o incluso pudiera ser una interfaz para que se implementen 
 * todos los metodos de distorcion*/
public abstract class DistortionMethod {

	public abstract List<Trajectory> distorte(List<Trajectory> trajectories, double timeThreshold, 
			double spaceThreshold, Distance dist, boolean lDiversity);

	public abstract List<Trajectory> distorte(List<Trajectory> trajectories, Distance dist);
	
	/** 28/09/2010 Trujillo Comment
	 * Al rededor de un punto, busca los mejores puntos de las otras trajectorias. Aqui, 
	 * cada trajectoria tiene que aportar un punto, de lo contrario se va a considerar
	 * que no existe tal cluster de puntos*/
	public static List<PointInsideTrajectory> findBestPointCluster(List<Trajectory> trajectories, 
			GPSFormat p, Trajectory p_Trajectory, Distance dist, double spaceThreshold){
		List<PointInsideTrajectory> pointsCluster = new ArrayList<PointInsideTrajectory>(trajectories.size());
		for (Trajectory t : trajectories){
			if (t.equals(p_Trajectory)){
				pointsCluster.add(new PointInsideTrajectory(p_Trajectory, p, p.getTime()));
				continue;
			}
			double minDistance = Double.MAX_VALUE;
			GPSFormat selectedPoint = null;
			Trajectory selectedTrajectory = null;
			for (GPSFormat candidatePoints : t.points()){
				//if (lDiversity && candidatePoints.equalsInSpace(p)) continue;
				double distance = dist.distance(p, candidatePoints);
				if (distance > spaceThreshold) continue;
				if (distance < minDistance){
					minDistance = distance;
					selectedPoint = candidatePoints;
					selectedTrajectory = t;
				}
			}
			if (selectedPoint == null){
				//entonces es que hay una trajectoria que ya se quedo sin puntos
				//de modo que con esto terminamos el proceso. 
				return null;
			}
			pointsCluster.add(new PointInsideTrajectory(selectedTrajectory, selectedPoint, selectedPoint.getTime()));
			//t.removePoint(selectedPoint.getDate().getTime());
		}
		return pointsCluster;
	}
	
	/** 09/12/2010 Trujillo Comment
	 * La diferencia con el de arriba, es que en este no hace falta encontrar un punto 
	 * en cada trajectoria
	 * con que exista al menos un punto alrededor de p es suficiente.*/
	public static List<PointInsideTrajectory> findBestPointCluster2(List<Trajectory> trajectories, 
			GPSFormat p, Trajectory p_Trajectory, double spaceThreshold, Distance dist){
		List<PointInsideTrajectory> pointsCluster = new ArrayList<PointInsideTrajectory>();
		pointsCluster.add(new PointInsideTrajectory(p_Trajectory, p, p.getTime()));
		for (Trajectory t : trajectories){
			if (t.equals(p_Trajectory)){
				continue;
			}
			double minDistance = Double.MAX_VALUE;
			GPSFormat selectedPoint = null;
			Trajectory selectedTrajectory = null;
			for (GPSFormat candidatePoints : t.points()){
				if (candidatePoints.equalsInSpace(p)) continue;
				double distance = dist.distance(p, candidatePoints);
				for (PointInsideTrajectory usedPoint : pointsCluster){
					if (candidatePoints.getX() == usedPoint.p.getX() && candidatePoints.getY() == usedPoint.p.getY()) {						
						distance = Double.MAX_VALUE;  
						break;
					}
				}
				if (distance < minDistance){
					minDistance = distance;
					selectedPoint = candidatePoints;
					selectedTrajectory = t;
				}
			}
			if (selectedPoint == null){
				//entonces esta trajectoria no ha aportado nada
				continue;
			}
			//es cierto que tenemos un punto seleccionado, pero este punto solo sera añadido
			//si su distancia es menor que el umbral
			if (minDistance > spaceThreshold) continue;
			pointsCluster.add(new PointInsideTrajectory(selectedTrajectory, selectedPoint, selectedPoint.getTime()));
			//t.removePoint(selectedPoint.getDate().getTime());
		}
		return pointsCluster;
	}

	/** 15/12/2010 Trujillo Comment
	 * Esta forma de hacer cluster de puntos es rigida, o sea, o se encuentran k puntos
	 * o se retorna null*/
	public static List<PointInsideTrajectory> findBestPointCluster2(List<Trajectory> trajectories, 
			GPSFormat p, Trajectory p_Trajectory, int k, double spaceThreshold, Distance dist){
		List<PointInsideTrajectory> pointsCluster = new ArrayList<PointInsideTrajectory>(trajectories.size());
		pointsCluster.add(new PointInsideTrajectory(p_Trajectory, p, p.getTime()));
		for (Trajectory t : trajectories){
			if (t.equals(p_Trajectory)){
				continue;
			}
			double minDistance = Double.MAX_VALUE;
			GPSFormat selectedPoint = null;
			Trajectory selectedTrajectory = null;
			for (GPSFormat candidatePoints : t.points()){
				if (candidatePoints.equalsInSpace(p)) continue;
				double distance = dist.distance(p, candidatePoints);
				for (PointInsideTrajectory usedPoint : pointsCluster){
					if (candidatePoints.getX() == usedPoint.p.getX() && candidatePoints.getY() == usedPoint.p.getY()) {						
						distance = Double.MAX_VALUE;  
						break;
					}
				}
				if (distance < minDistance){
					minDistance = distance;
					selectedPoint = candidatePoints;
					selectedTrajectory = t;
				}
			}
			if (selectedPoint == null){
				//entonces esta trajectoria no ha aportado nada
				continue;
			}
			//es cierto que tenemos un punto seleccionado, pero este punto solo sera añadido
			//si su distancia es menor que el umbral
			if (minDistance > spaceThreshold) continue;
			pointsCluster.add(new PointInsideTrajectory(selectedTrajectory, selectedPoint, selectedPoint.getTime()));
			if (pointsCluster.size() == k) return pointsCluster;
			//t.removePoint(selectedPoint.getDate().getTime());
		}
		if (pointsCluster.size() != k) return null;
		else return pointsCluster;
	}

	/** 03/11/2010 Trujillo Comment
	 * Esta es la forma de buscar el cluster de puntos del paper de conferencia. La diferencia esta
	 * en que ahora cualquier punto que este dentro de la bola definida por el cluster se considera 
	 * un punto valido*/
	public static List<PointInsideTrajectory> findBestPointCluster(List<Trajectory> trajectories, 
			GPSFormat p, Trajectory p_Trajectory, double timeThreshold, double spaceThreshold,
			Distance dist){
		List<PointInsideTrajectory> pointsCluster = new ArrayList<PointInsideTrajectory>(trajectories.size());
		for (Trajectory t : trajectories){
			if (t.equals(p_Trajectory)){
				pointsCluster.add(new PointInsideTrajectory(p_Trajectory, p, p.getTime()));
				continue;
			}
			GPSFormat selectedPoint = null;
			Trajectory selectedTrajectory = null;
			for (GPSFormat candidatePoints : t.points()){
				double distance = dist.distance(p, candidatePoints);
				if (distance < spaceThreshold){
					selectedPoint = candidatePoints;
					selectedTrajectory = t;
				}
			}
			if (selectedPoint == null) continue;
			pointsCluster.add(new PointInsideTrajectory(selectedTrajectory, selectedPoint, selectedPoint.getTime()));
			//t.removePoint(selectedPoint.getDate().getTime());
		}
		return pointsCluster;
	}

	/** 03/11/2010 Trujillo Comment
	 * Esta es la forma de buscar el cluster de puntos del paper de conferencia. La diferencia esta
	 * en que ahora cualquier punto que este dentro de la bola definida por el cluster se considera 
	 * un punto valido. Y bueno, en este la bola se define a traves del grafo*/
	public static List<PointInsideTrajectory> findBestPointClusterUsingGraph(List<Trajectory> trajectories, 
			GPSFormat p, Trajectory p_Trajectory, double timeThreshold, 
			double spaceThreshold, GraphDistance dist){
		List<PointInsideTrajectory> pointsCluster = new ArrayList<PointInsideTrajectory>(trajectories.size());
		for (Trajectory t : trajectories){
			if (t.equals(p_Trajectory)){
				pointsCluster.add(new PointInsideTrajectory(p_Trajectory, p, p.getTime()));
				continue;
			}
			GPSFormat selectedPoint = null;
			Trajectory selectedTrajectory = null;
			for (GPSFormat candidatePoints : t.points()){
				double distance = dist.graphDistance(p.getLatitude(), p.getLongitude(), 
						candidatePoints.getLatitude(), candidatePoints.getLongitude());
				if (distance < spaceThreshold){
					selectedPoint = candidatePoints;
					selectedTrajectory = t;
				}
			}
			if (selectedPoint == null) continue;
			pointsCluster.add(new PointInsideTrajectory(selectedTrajectory, selectedPoint, selectedPoint.getTime()));
			//t.removePoint(selectedPoint.getDate().getTime());
		}
		return pointsCluster;
	}
}
