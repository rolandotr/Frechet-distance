package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import trajectory.SimpleTrajectory;
import trajectory.Trajectory;
import util.Interpolation;
import wrappers.Rectangle;
import java.io.File;
import java.io.IOException;
import javax.swing.Timer;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import distances.Distance;
import distances.GPSDistance;

import parsers.TxtParser;

import wrappers.GPSFormat;



public class TrajectoryPanel extends JPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6471620649066137114L;
	Trajectory trajectory1;
	Trajectory trajectory2;
	Distance distance;
	//AnimationPanel animationPanel;
	
	public TrajectoryPanel() {
		this.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		super.setPreferredSize(new Dimension(1800, 1600));
		this.setLayout(new BorderLayout(0,0));
		/*animationPanel = new AnimationPanel();
		this.add(animationPanel, BorderLayout.CENTER);*/
	}

	public void paint(Graphics g){
		java.awt.Rectangle bound = g.getClipBounds();
		bound.x += 20;
		bound.y -= 20;
		bound.width -= 40;
		bound.height-= 40;
		Rectangle trajectoryRectangle = getRectangle(trajectory1, trajectory2);
		/*animationPanel.setClipBound(bound);
		animationPanel.setRectangleBound(trajectoryRectangle);*/
		super.paint(g);
		if (trajectory1 != null && trajectory2 != null){
			//System.out.println(bound.toString());
			paint(g, trajectory1, trajectory2, Color.black, Color.red, Color.yellow, Color.blue, 
					bound, trajectoryRectangle);
		}
		if (trajectories == null || !animating) {
			pos = 0;
			if (timer != null) timer.stop();
			return;
		}
		if (pos == t1.length){
			pos = 0;
		}
		if (pos < 0) return;
		//System.out.println("pos = "+pos+" points are "+t1[pos]+"->"+t2[pos]);
		//bound = g.getClipBounds(bound);
		paint(g, t1[pos], t2[pos], Color.red, Color.black, bound, trajectoryRectangle);
	}
	
	protected void paint(Graphics g, GPSFormat p1, GPSFormat p2, Color point, Color line, 
			java.awt.Rectangle bound, Rectangle trajectoryRectangle) {
		//System.out.println(bound.toString());
		double x;
		double y;
		double z;
		double lastX = 0;
		double lastY = 0;
		if (trajectoryRectangle.width == 0) z = 0;
		else z = bound.getWidth()/trajectoryRectangle.width;
		lastX = (int)(bound.x+(getX(p1)-trajectoryRectangle.x)*z);
		if (trajectoryRectangle.height == 0) z = 0;
		else z = bound.getHeight()/trajectoryRectangle.height;
		lastY = (int)(bound.y+(getY(p1)-trajectoryRectangle.y)*z);
		lastY = bound.getHeight()-lastY;
		if (trajectoryRectangle.width == 0) z = 0;
		else z = bound.getWidth()/trajectoryRectangle.width;
		x = (int)(bound.x+(getX(p2)-trajectoryRectangle.x)*z);
		if (trajectoryRectangle.height == 0) z = 0;
		else z = bound.getHeight()/trajectoryRectangle.height;
		y = (int)(bound.y+(getY(p2)-trajectoryRectangle.y)*z);
		y = bound.getHeight()-y;
		g.setColor(point);
		g.fillOval((int)x-3, (int)y-3, 6, 6);
		g.fillOval((int)lastX-3, (int)lastY-3, 6, 6);
		g.setColor(line);
		g.drawLine((int)lastX, (int)lastY, (int)x, (int)y);
	}


	private void paint(Graphics g, Trajectory trajectory1, Trajectory trajectory2, 
			Color point1, Color line1, Color point2, Color line2, java.awt.Rectangle bound, 
			Rectangle trajectoryRectangle) {		
		paint(g, trajectory1, point1, line1, bound, trajectoryRectangle);
		paint(g, trajectory2, point2, line2, bound, trajectoryRectangle);
	}

	private void paint(Graphics g, Trajectory trajectory, Color point, Color line, 
			java.awt.Rectangle bound, Rectangle trajectoryRectangle) {
		double x;
		double y;
		double z;
		double lastX = 0;
		double lastY = 0;
		boolean first = true;
		for (GPSFormat p : trajectory.values()){
			if (trajectoryRectangle.width == 0) z = 0;
			else z = bound.getWidth()/trajectoryRectangle.width;
			x = (int)(bound.x+(getX(p)-trajectoryRectangle.x)*z);
			if (trajectoryRectangle.height == 0) z = 0;
			else z = bound.getHeight()/trajectoryRectangle.height;
			y = (int)(bound.y+(getY(p)-trajectoryRectangle.y)*z);
			y = bound.getHeight()-y;
			g.setColor(point);
			g.fillOval((int)x-3, (int)y-3, 6, 6);
			if (!first){
				g.setColor(line);
				g.drawLine((int)lastX, (int)lastY, (int)x, (int)y);
			}
			first = false;
			lastX = x;
			lastY = y;
		}
	}


	private Rectangle getRectangle(Trajectory trajectory1, Trajectory trajectory2) {
		double minx = Double.MAX_VALUE;
		double maxx = -Double.MAX_VALUE;
		double miny = Double.MAX_VALUE;
		double maxy = -Double.MAX_VALUE;
		for (GPSFormat p : trajectory1.values()){
			if (getX(p) < minx) minx = getX(p);
			if (getX(p) > maxx) maxx = getX(p);
			if (getY(p) < miny) miny = getY(p);
			if (getY(p) > maxy) maxy = getY(p);
		}
		for (GPSFormat p : trajectory2.values()){
			if (getX(p) < minx) minx = getX(p);
			if (getX(p) > maxx) maxx = getX(p);
			if (getY(p) < miny) miny = getY(p);
			if (getY(p) > maxy) maxy = getY(p);
		}
		return new Rectangle(minx, miny, (maxx-minx), (maxy-miny));
	}

	private int getX(GPSFormat p){
		if (distance instanceof GPSDistance){
			return (int)(p.getLatitude()*10000);
		}
		else{
			return (int)p.getLatitude();
		}
	}
	private int getY(GPSFormat p){
		if (distance instanceof GPSDistance){
			return (int)(p.getLongitude()*10000);
		}
		else{
			return (int)p.getLongitude();
		}
	}
	
	/** 09/02/2012 Trujillo Comment
	 * Esto debe ser un fichero .gpx
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IOException */
	public void setFirstTrajectory(File file) throws IOException, ParserConfigurationException, SAXException {
		trajectory1 = new SimpleTrajectory("t1");
		//GPXParser.parseXMLFile(file.getAbsolutePath(), ((SimpleTrajectory)trajectory1).getTree());
		try{
			TxtParser.parseTxtFile(file, ((SimpleTrajectory)trajectory1).getTree());
		}catch(IOException exc){
			JOptionPane.showConfirmDialog(this, "File "+file.getName()+" could not be properly read");
		}
		repaint();
	}

	public void setSecondTrajectory(File file) throws IOException, ParserConfigurationException, SAXException {
		trajectory2 = new SimpleTrajectory("t2");
		/*GPXParser.parseXMLFile(file.getAbsolutePath(), ((SimpleTrajectory)trajectory2).getTree());*/
		try{
			TxtParser.parseTxtFile(file, ((SimpleTrajectory)trajectory2).getTree());
		}catch(IOException exc){
			JOptionPane.showConfirmDialog(this, "File "+file.getName()+" could not be properly read");
		}
		repaint();
	}

	public void setDistance(Distance distance) {
		this.distance = distance;
	}
	
	/*class AnimationPanel extends JPanel{

		double[][] trajectories;
		GPSFormat[] t1, t2;
		int x1, x2, y1, y2;
		private int pos;
		private Timer timer;
		java.awt.Rectangle bound;
		Rectangle trajectoryRectangle;
		
		public void setTrajectories(double[][] trajectories, Trajectory trajectory1, 
				Trajectory trajectory2){
			this.trajectories = trajectories;
			this.t1 = new GPSFormat[trajectories[0].length];
			this.t2 = new GPSFormat[trajectories[1].length];
			for (int i = 0; i < trajectories[0].length; i++){
				this.t1[i] = Sync.interpolate(trajectory1, (long)trajectories[0][i]);
				this.t2[i] = Sync.interpolate(trajectory2, (long)trajectories[1][i]);
			}
			pos = -1;
			timer = new Timer(1000, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					repaint();
					pos++;
				}
			});
			timer.start();
		}
		
		public void setRectangleBound(Rectangle trajectoryRectangle) {
			this.trajectoryRectangle = trajectoryRectangle;
		}

		public void setClipBound(java.awt.Rectangle bound) {
			this.bound = bound;
		}

		@Override
		public void paint(Graphics g) {
			//super.paint(g);
			//System.out.println("Painting "+trajectories);
			if (trajectories == null) return;
			if (pos == trajectories[0].length){
				pos = 0;
				trajectories = null;
				timer.stop();
			}
			if (pos < 0) return;
			System.out.println("pos = "+pos+" points are "+t1[pos]+"->"+t2[pos]);
			//bound = g.getClipBounds(bound);
			paint(g, t1[pos], t2[pos], Color.red, Color.black, bound, trajectoryRectangle);
		}
		
		protected void paint(Graphics g, GPSFormat p1, GPSFormat p2, Color point, Color line, 
				java.awt.Rectangle bound, Rectangle trajectoryRectangle) {
			System.out.println(bound.toString());
			double x;
			double y;
			double z;
			double lastX = 0;
			double lastY = 0;
			if (trajectoryRectangle.width == 0) z = 0;
			else z = bound.getWidth()/trajectoryRectangle.width;
			lastX = (int)(bound.x+(getX(p1)-trajectoryRectangle.x)*z);
			if (trajectoryRectangle.height == 0) z = 0;
			else z = bound.getHeight()/trajectoryRectangle.height;
			lastY = (int)(bound.y+(getY(p1)-trajectoryRectangle.y)*z);
			lastY = bound.getHeight()-lastY;
			if (trajectoryRectangle.width == 0) z = 0;
			else z = bound.getWidth()/trajectoryRectangle.width;
			x = (int)(bound.x+(getX(p2)-trajectoryRectangle.x)*z);
			if (trajectoryRectangle.height == 0) z = 0;
			else z = bound.getHeight()/trajectoryRectangle.height;
			y = (int)(bound.y+(getY(p2)-trajectoryRectangle.y)*z);
			y = bound.getHeight()-y;
			g.setColor(point);
			g.fillOval((int)x-3, (int)y-3, 6, 6);
			g.fillOval((int)lastX-3, (int)lastY-3, 6, 6);
			g.setColor(line);
			g.drawLine((int)lastX, (int)lastY, (int)x, (int)y);
		}

		private int getX(GPSFormat p){
			if (distance instanceof GPSDistance){
				return (int)(p.getLatitude()*10000);
			}
			else{
				return (int)p.getLatitude();
			}
		}
		private int getY(GPSFormat p){
			if (distance instanceof GPSDistance){
				return (int)(p.getLongitude()*10000);
			}
			else{
				return (int)p.getLongitude();
			}
		}
	}*/
	double[][] trajectories;
	GPSFormat[] t1, t2;
	int pos = -1;
	Timer timer;
	private final int STEP = 100;
	
	public void setAnimation(double[][] trajectories) {
		this.trajectories = trajectories;
		this.t1 = new GPSFormat[(trajectories[0].length-1)*STEP];
		this.t2 = new GPSFormat[(trajectories[1].length-1)*STEP];
		for (int i = 0; i < t1.length; i++){
			double time1 = trajectories[0][i / STEP]+
				(trajectories[0][i / STEP + 1]-trajectories[0][i / STEP])*(i % STEP)/STEP;
			double time2 = trajectories[1][i / STEP]+
			(trajectories[1][i / STEP + 1]-trajectories[1][i / STEP])*(i % STEP)/STEP;
			this.t1[i] = Interpolation.interpolate(trajectory1, (long)time1);
			this.t2[i] = Interpolation.interpolate(trajectory2, (long)time2);
		}
		pos = -1;
		timer = new Timer(10, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				repaint();
				pos++;
			}
		});
		timer.start();
	}


	boolean animating = false;
	
	public void setAnimation(boolean animating) {
		this.animating = animating;
	}

	public boolean isAnimating() {
		return animating;
	}


}
