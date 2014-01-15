package gui;

import java.awt.Color;
import java.awt.Graphics;

import trajectory.Trajectory;
import wrappers.Rectangle;

import javax.swing.JOptionPane;

import distances.FrechetDistance;
import distances.FrechetDistanceEuclideanBased;
import distances.Cell;




public class ReachableFreeSpacePanel extends FreeSpacePanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7777051799815451318L;
	double[][] trajectories;
	
	public ReachableFreeSpacePanel() {
	}

	public void computeFreeSpace(Cell[][] freeSpace, FrechetDistance distance){
		super.freeSpace = distance.computeCellOfReachableFreeSpace(freeSpace);
		trajectories = null;
		repaint();
	}

	@Override
	public void computeFreeSpace(Trajectory t1, Trajectory t2, double epsilon) {
		FrechetDistanceEuclideanBased distance = new FrechetDistanceEuclideanBased();
		Cell[][] freeSpace = distance.computeCellOfFreeSpace(t1, t2, epsilon);
		super.freeSpace = distance.computeCellOfReachableFreeSpace(freeSpace);
		trajectories = null;
		repaint();
	}

	public double[][] computeMonotoneCurve(FrechetDistance distance) {
		if (freeSpace == null){
			JOptionPane.showConfirmDialog(this, "The free space must be computed first");
			return null;
		}
		trajectories = distance.computeMonotoneCurves(freeSpace);		
		repaint();
		return trajectories;
	}
	
	public void paint(Graphics g){
		super.paint(g);
		//now we draw the curve
		if (trajectories == null) return;
		Color original = g.getColor();
		g.setColor(Color.blue);
		java.awt.Rectangle bound = getBound(g);
		Rectangle gridBound = getRectangle(freeSpace);
		int[] p1, p2;
		for (int i = 0; i < trajectories[0].length-1; i++){
			p1 = getRelativeCoordinate(trajectories[0][i], trajectories[1][i], bound, gridBound);
			p2 = getRelativeCoordinate(trajectories[0][i+1], trajectories[1][i+1], bound, gridBound);
			g.drawLine(p1[0], p1[1], p2[0], p2[1]);
		}
		g.setColor(original);
	}

}
