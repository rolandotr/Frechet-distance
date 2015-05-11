package algorithms.generalization;

import java.util.LinkedList;
import java.util.List;



import wrappers.GPSFormat;
import wrappers.GeneralizedPoint;
import wrappers.SimpleFormat;
import wrappers.SimpleTrajectory;
import wrappers.Trajectory;
import distances.LogCostDistance;
import distances.LogCostDistance.Transformation;

/*Trujillo- May 15, 2013
 * The purpose of this class is to define the distortion to the centroide (average)*/
public class GeneralizationCentroideMethod extends GeneralizationAnonymization{

	public GeneralizationCentroideMethod(String preffix, LogCostDistance distance) {
		super(preffix, distance);
		super.name = "GC";
	}

	@Override
	public List<Trajectory> anonymizeCluster(List<Trajectory> cluster) {
		GeneralizedTrajectory trajectory = anonymizeClusterToGeneralized(cluster);
		Trajectory tmp;
		List<Trajectory> result = new LinkedList<Trajectory>();
		for (Trajectory t : cluster) {
			tmp = getCentroide(trajectory);
			tmp.setIdentifier(t.getIdentifier());
			result.add(tmp);
		}
		return result;
	}
	
	private Trajectory getCentroide(GeneralizedTrajectory t) {
		Trajectory result = new SimpleTrajectory(t.getIdentifier());
		for (GeneralizedPoint point : t.points()) {
			result.addPoint(getCentroide(point));
		}
		return result;
	}
	
	private GPSFormat getCentroide(GeneralizedPoint point) {
		double lat = (point.x1+point.x2)/2;
		double lon = (point.y1+point.y2)/2;
		long time = (long)(point.t1+point.t2)/2;
		return new SimpleFormat(time, lat, lon);
	}

	private GeneralizedTrajectory anonymizeClusterToGeneralized(List<Trajectory> cluster) {
		Trajectory minimum = findBestTrajectory(cluster);
		GeneralizedTrajectory result = LogCostDistance.generalizeAtomicTrajectory(minimum);
		cluster.remove(minimum);
		for (Trajectory trajectory : cluster) {
			result = anonymize(result, LogCostDistance.generalizeAtomicTrajectory(trajectory));
		}
		cluster.add(minimum);
		return result;
	}
	
	/***Trujillo- Feb 18, 2013
	 * Dos trajectories se anonymizan cogiendo los matching points 
	 * y cogiendo el bounding box.  
	 */
	private GeneralizedTrajectory anonymize(GeneralizedTrajectory t1, 
			GeneralizedTrajectory t2) {
		Transformation transf = distance.logCostDistance(t1, t2);
		GeneralizedTrajectory result = new GeneralizedTrajectory(t1.getIdentifier()+"-"+
				t2.getIdentifier());
		GeneralizedPoint p1;
		GeneralizedPoint p2;
		GeneralizedPoint tmp;
		for (int i = 0; i < transf.matchesForT1.length; i++) {
			p1 = transf.matchesForT1[i];
			p2 = transf.matchesForT2[i];
			tmp = mergePoints(p1, p2);
			result.addPoint(tmp);
		}
		return result;
	}

	private GeneralizedPoint mergePoints(GeneralizedPoint p1,
			GeneralizedPoint p2) {
		double minTime = (p1.t1 < p2.t1)?p1.t1:p2.t1;
		double maxTime = (p1.t2 > p2.t2)?p1.t2:p2.t2;
		double minX = (p1.x1 < p2.x1)?p1.x1:p2.x1;
		double maxX = (p1.x2 > p2.x2)?p1.x2:p2.x2;
		double minY = (p1.y1 < p2.y1)?p1.y1:p2.y1;
		double maxY = (p1.y2 > p2.y2)?p1.y2:p2.y2;
		return new GeneralizedPoint(minTime, maxTime, minX, maxX, minY, maxY);
	}

	private Trajectory findBestTrajectory(List<Trajectory> cluster) {
		double min = Double.MAX_VALUE;
		Transformation trans = null;
		Trajectory best = null;
		for (Trajectory t1 : cluster) {
			double totalCost = 0;
			for (Trajectory t2 : cluster) {
				trans = distance.logCostDistance(t1,t2);
				totalCost += trans.cost;
			}
			if (totalCost < min){
				min = totalCost;
				best = t1;
			}
		}
		return best;
	}
	
	@Override
	public String toString() {
		return "generalization-centroide";
	}

}
