package gui;

import distances.FretcherDistance;

import trajectory.Trajectory;



public class ConstrainedFreeSpacePanel extends FreeSpacePanel{

	private static final long serialVersionUID = -915078959716157447L;

	public ConstrainedFreeSpacePanel() {
	}

	public void computeFreeSpace(Trajectory t1, Trajectory t2, double epsilon){
		FretcherDistance distance = new FretcherDistance(epsilon);
		freeSpace = distance.computeCellOfFreeSpace(t1, t2);
		repaint();
	}

}
