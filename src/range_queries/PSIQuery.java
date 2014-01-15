package range_queries;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import trajectory.Trajectory;
import wrappers.GPSFormat;
import distances.Distance;

public class PSIQuery extends Query{

	@Override
	public int executeQuery (Collection<Trajectory> dataset, 
			Region r, Distance dist) {
		int count = 0;
		//Query execution for all Trajectories in the dataset.
		for (Trajectory str : dataset) {
			if (sometimeInside(str, r, dist))
				count++;
		}
		return count;
	}
	
	//P_S_I Query: True <---> exists a pair (lambda, t) in SimpleTrajectory such that
	//t is in the interval and lambda inside the Region r.
	private boolean sometimeInside(Trajectory trajectory, Region r, 
			Distance dist) {
		for (GPSFormat pointInsideRegion : r.getPointsOfRegion()){
			if (trajectory.timeOutOfInterval(pointInsideRegion.getTime())) continue;
			GPSFormat p;
			if (trajectory.containsTime(pointInsideRegion.getTime())){
				p = trajectory.getPoint(pointInsideRegion.getTime());
			}
			else{
				//el punto la trayectoria no lo tiene, pues habra que interpolarlo.
				p = trajectory.interpolateTime(pointInsideRegion.getTime());
			}
			if (dist.distance(p, pointInsideRegion) <= r.getRadius()) {
				return true;
			}
		}
		return false;
	}	
	


}
