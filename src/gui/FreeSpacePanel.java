package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Area;

import trajectory.Trajectory;
import wrappers.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import distances.Distance;
import distances.Cell;




public abstract class FreeSpacePanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5873159664258016347L;
	protected Cell[][] freeSpace;
	protected Distance distance;
	
	protected FreeSpacePanel() {
		this.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		super.setPreferredSize(new Dimension(1800, 1600));
	}

	public void paint(Graphics g){
		super.paint(g);
		if (freeSpace != null){
			java.awt.Rectangle bound = getBound(g);
			Rectangle gridBound = getRectangle(freeSpace);
			for (int i = 0; i < freeSpace.length; i++){
				for (int j = 0; j < freeSpace[i].length; j++){
					paint(g, freeSpace[i][j], bound, gridBound);
				}
			}
		}
	}
	
	protected java.awt.Rectangle getBound(Graphics g){
		java.awt.Rectangle bound = g.getClipBounds();
		bound.x += 20;
		bound.y -= 20;
		bound.width -= 40;
		bound.height-= 40;
		return bound;
	}

	protected int[] getRelativeCoordinate(double x, double y, java.awt.Rectangle bound, Rectangle gridBound){
		int relativeX;
		int relativeY;
		double z;
		if (gridBound.width == 0) z = 0;
		else z = bound.getWidth()/gridBound.width;
		relativeX = (int)(bound.x+(x-gridBound.x)*z);
		if (gridBound.height == 0) z = 0;
		else z = bound.getHeight()/gridBound.height;
		relativeY = (int)(bound.y+(y-gridBound.y)*z);
		relativeY = bound.height-relativeY;
		return new int[]{relativeX, relativeY};
	}
	
	private void paint(Graphics g, Cell cell, java.awt.Rectangle bound, Rectangle gridBound) {
		int[] min = getRelativeCoordinate(cell.lastTime1, cell.time2, bound, gridBound);
		int[] max = getRelativeCoordinate(cell.time1, cell.lastTime2, bound, gridBound);
		Area rectangle = new Area(new java.awt.Rectangle(min[0], min[1], max[0]-min[0], max[1]-min[1]));
		java.awt.Rectangle rectangle2 = new java.awt.Rectangle(min[0], min[1], max[0]-min[0], max[1]-min[1]);
		int[][] points = new int[][]{
				(cell.t1 == null)?null:getRelativeCoordinate(cell.lastTime1, cell.t1[0], bound, gridBound),
				(cell.t1 == null)?null:getRelativeCoordinate(cell.lastTime1, cell.t1[1], bound, gridBound),
			    (cell.t2 == null)?null:getRelativeCoordinate(cell.t2[0], cell.time2, bound, gridBound),
			    (cell.t2 == null)?null:getRelativeCoordinate(cell.t2[1], cell.time2, bound, gridBound),
			    (cell.t3 == null)?null:getRelativeCoordinate(cell.time1, cell.t3[1], bound, gridBound),
			    (cell.t3 == null)?null:getRelativeCoordinate(cell.time1, cell.t3[0], bound, gridBound),
			    (cell.t4 == null)?null:getRelativeCoordinate(cell.t4[1], cell.lastTime2, bound, gridBound),
			    (cell.t4 == null)?null:getRelativeCoordinate(cell.t4[0], cell.lastTime2, bound, gridBound),
			};
		
		Polygon p = new Polygon();
		for (int i = 0; i < points.length; i++){
			if (points[i] == null) continue;
			p.addPoint(points[i][0], points[i][1]);
		}
		
		//Path2D path = new Path2D.Double(p);
		Graphics2D g2 = (Graphics2D) g;
		Color c = g2.getColor(); 
		g2.setColor(Color.red);
		g2.fill(rectangle);
		rectangle.intersect(new Area(p));
		g2.setColor(g2.getBackground());
		g2.fill(rectangle);
		g2.setColor(Color.black);
		g2.draw(rectangle2);
		g2.setColor(c);
	}

	protected Rectangle getRectangle(Cell[][] freeSpace) {
		double minx = Double.MAX_VALUE;
		double maxx = -Double.MAX_VALUE;
		double miny = Double.MAX_VALUE;
		double maxy = -Double.MAX_VALUE;
		for (int i = 0; i < freeSpace.length; i++){
			for (int j = 0; j < freeSpace[i].length; j++){
				if (freeSpace[i][j].lastTime1 < minx) minx = freeSpace[i][j].lastTime1;
				if (freeSpace[i][j].time1 > maxx) maxx = freeSpace[i][j].time1;
				if (freeSpace[i][j].lastTime2 < miny) miny = freeSpace[i][j].lastTime2;
				if (freeSpace[i][j].time2 > maxy) maxy = freeSpace[i][j].time2;
			}
		}
		return new Rectangle(minx, miny, (maxx-minx), (maxy-miny));
	}

	public abstract void computeFreeSpace(Trajectory t1, Trajectory t2, double epsilon);

	public Cell[][] getFreeSpace() {
		return freeSpace;
	}

}
