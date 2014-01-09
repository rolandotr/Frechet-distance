package util;

import java.util.List;

import clustering.Cluster.PointInsideTrajectory;

public interface UtilityCriterion {

	public boolean approves(List<PointInsideTrajectory> cluster);

	public double getSpaceThreshold();
	
}
