package gui;

import distances.Distance;
import distances.EuclideanDistance;
import distances.FrechetDistance;
import distances.FrechetDistanceEuclideanBased;
import distances.Cell;

import trajectory.Trajectory;



public class ConstrainedFreeSpacePanel extends FreeSpacePanel{

	private static final long serialVersionUID = -915078959716157447L;
	protected FrechetDistance distance;

	public ConstrainedFreeSpacePanel(FrechetDistance distance) {
		this.distance = distance;
	}

	public void computeFreeSpace(Trajectory t1, Trajectory t2, double epsilon){
		freeSpace = distance.computeCellOfFreeSpace(t1, t2, epsilon);
		for (Cell[] row : freeSpace){
			for (Cell cell : row){
				
			}
		}
		//freeSpace = distance.computeReachableFreeSpaceOptimized(t1, t2, epsilon);
		repaint();
	}

}
