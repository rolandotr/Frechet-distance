package clustering;

import java.util.List;

import util.UtilityCriterion;
import wrappers.GPSFormat;

import clustering.Cluster.PointInsideTrajectory;
import distances.Distance;

/** 17/08/2010 Trujillo Comment
 * Esta clase por simplicidad va a ser una clase con memoria. O sea, se le pone un factor, y de acuerdo
 * con este factor y el promedio de distancia interna entre cluster, entonces si decide si este
 * nuevo cluster entra como bueno o como malo. Es de destacar que todos los cluster entran, tanto
 * los buenos como los malos, todos cuentan.*/
public class FixedUtilityCriterion implements UtilityCriterion{
	
	private double distance;
	private int total;
	private Distance dist;
	
	public FixedUtilityCriterion(double distance, Distance dist){
		this.distance = distance;
		this.dist = dist;
	}

	public boolean approves(List<PointInsideTrajectory> cluster) {
		double newDistance = dist.intraClusterAverageDistance(cluster);
		return newDistance < distance;
	}

	@Override
	public double getSpaceThreshold() {
		return distance;
	}

}
