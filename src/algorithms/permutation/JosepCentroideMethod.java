package algorithms.permutation;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import clustering.Cluster;
import clustering.Cluster.PointInsideTrajectory;

import util.Anonymization;
import util.DistortionMethod;
import util.Interpolation;
import wrappers.GPSFormat;
import wrappers.SimpleTrajectory;
import wrappers.Trajectory;
import distances.Distance;
import distances.FrechetDistance;
import distances.Transformation;

/*Trujillo- May 15, 2013
 * The purpose of this class is to define the distortion to the centroide (average)*/
public class JosepCentroideMethod extends JosepAnonymization{

	public JosepCentroideMethod(String preffix, Distance distance) {
		super(preffix, distance);
	}

	@Override
	public List<Trajectory> anonymizeCluster(List<Trajectory> cluster) {
		Trajectory smaller = Cluster.findSmallerTrajectory(cluster);
		List<PointInsideTrajectory> pointCluster;
		GPSFormat centerPoint;
		Trajectory centroide = new SimpleTrajectory("centroide");
		GPSFormat[] points;
		int cont;
		for (GPSFormat p : smaller.points()){
			pointCluster = DistortionMethod.findBestPointCluster(cluster, 
					p, smaller, distance, Double.MAX_VALUE);
			if (pointCluster == null) {
				continue;
			}
			cont = 0;
			points = new GPSFormat[cluster.size()];
			for (PointInsideTrajectory point : pointCluster) {
				points[cont++] = point.p;
			}
			centerPoint = Interpolation.centroide(points);
			centroide.addPoint(centerPoint);
			for (PointInsideTrajectory point : pointCluster) {
				if (smaller.equals(point.t)) continue;
				point.t.removePoint(point.p.getTime());
			}
		}
		List<Trajectory> tmp = new LinkedList<Trajectory>();
		Trajectory tmpTrajectory;
		for (Trajectory trajectory : cluster) {
			tmpTrajectory = (Trajectory)centroide.clone();
			tmpTrajectory.setIdentifier(trajectory.getIdentifier());
			tmp.add(tmpTrajectory);
		}
		return tmp;
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
	
	@Override
	public String toString() {
		return "josep-centroide";
	}

	@Override
	public String getName() {
		return "JC";
		//return "josep-centroide";
	}

}
