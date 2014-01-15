package range_queries;

import java.util.Collection;

import trajectory.Trajectory;
import wrappers.GPSFormat;
import distances.Distance;

public class DAIQuery extends Query{
	
	
	@Override
	public int executeQuery (Collection<Trajectory> dataset, 
			Region r, Distance dist) {
		int count = 0;
		//Query execution for all Trajectories in the dataset.
		for (Trajectory str : dataset) {
			if (alwaysInside(str, r, dist))
				count++;
		}
		return count;
	}
	
	//D_A_I Query: True <--> for every time t in interval. exists a pair (lambda, t) that t 
	//is in the interval and lambda inside the Region.
	private boolean alwaysInside(Trajectory trajectory, Region r, Distance dist) {
		
		for (GPSFormat pointInsideRegion : r.getPointsOfRegion()){
			GPSFormat tmp;
			if (trajectory.timeOutOfInterval(pointInsideRegion.getTime())){
				return false;
			}
			if (trajectory.containsTime(pointInsideRegion.getTime())){
				tmp = trajectory.getPoint(pointInsideRegion.getTime());
			}
			else{
				//el punto la trayectoria no lo tiene, pues habra que interpolarlo.
				tmp = trajectory.interpolateTime(pointInsideRegion.getTime());
			}
			if (dist.distance(tmp, pointInsideRegion) > r.getRadius()) {
				//System.out.println("Always inside distance between "+tmp.toString()+" and "+pointInsideRegion.toString()+" is "+dist.distance(tmp, pointInsideRegion));
				return false;
			}
		}
		return true;
	
	}



}
