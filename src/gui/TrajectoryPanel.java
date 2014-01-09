package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import util.Converter;
import util.Interpolation;
import wrappers.Rectangle;
import wrappers.SimpleTrajectory;
import wrappers.Trajectory;

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
		super.paint(g);
		if (trajectory1 == null && trajectory2 == null) return;
		java.awt.Rectangle bound = g.getClipBounds();
		bound.x += 20;
		bound.y -= 20;
		bound.width -= 40;
		bound.height-= 40;
		Rectangle trajectoryRectangle = getRectangle(trajectory1, trajectory2);
		/*animationPanel.setClipBound(bound);
		animationPanel.setRectangleBound(trajectoryRectangle);*/
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
			System.out.println(trajectory1.size());
			/*for (GPSFormat p : trajectory1.points()) {
				double[] l = Converter.degreesToXYZ(p);
				System.out.println("x = "+l[0]+", y = "+l[1]+", z = "+l[2]);
			}*/
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
			System.out.println(trajectory2.size());
		}catch(IOException exc){
			JOptionPane.showConfirmDialog(this, "File "+file.getName()+" could not be properly read");
		}
		repaint();
	}

	public void setDistance(Distance distance) {
		this.distance = distance;
	}
	
	double[][] trajectories;
	GPSFormat[] t1, t2;
	int pos = -1;
	Timer timer;
	private final int POINTS = 1000;
	
	
	public void setAnimation(double[][] trajectories) {
		this.trajectories = trajectories;
		//this.t1 = new GPSFormat[(trajectories[0].length-1)*STEP];
		//this.t2 = new GPSFormat[(trajectories[1].length-1)*STEP];
		this.t1 = new GPSFormat[POINTS+1];
		this.t2 = new GPSFormat[POINTS+1];
		double iniT1 = trajectories[0][0];
		double iniT2 = trajectories[1][0];
		double endT1 = trajectories[0][trajectories[0].length-1];
		double endT2 = trajectories[1][trajectories[1].length-1];
		for (int i = 0; i <= POINTS; i++){
			/*
			 double time1 = trajectories[0][i / STEP]+
			 	(trajectories[0][i / STEP + 1]-trajectories[0][i / STEP])*(i % STEP)/STEP;
			double time2 = trajectories[1][i / STEP]+
				(trajectories[1][i / STEP + 1]-trajectories[1][i / STEP])*(i % STEP)/STEP;
			*/
			double time1 = iniT1+(endT1-iniT1)*i/POINTS;
			double time2 = iniT2+(endT2-iniT2)*i/POINTS;
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

	/***Trujillo- Oct 26, 2012
	 * Esto tienes que arreglarlo bien, esta es mas o menos la version correcta
	 * pero necesita mejoras, por el momento la tengo comentada y dejo la vieja
	 * que no es correcta pero se va mas bonita.
	 */
	/*
	public void setAnimation(double[][] trajectories) {
		this.trajectories = trajectories;
		//this.t1 = new GPSFormat[(trajectories[0].length-1)*STEP];
		//this.t2 = new GPSFormat[(trajectories[1].length-1)*STEP];
		this.t1 = new GPSFormat[POINTS+1];
		this.t2 = new GPSFormat[POINTS+1];
		double iniT1 = trajectories[0][0];
		double iniT2 = trajectories[1][0];
		double endT1 = trajectories[0][trajectories[0].length-1];
		double endT2 = trajectories[1][trajectories[1].length-1];
		int index = 1;
		for (int i = 0; i <= POINTS; i++){
			double time1 = iniT1+(endT1-iniT1)*i/POINTS;
			//ahora voy a buscar el punto que se acerca a este time
			for (int j = index; j < trajectories[0].length; j++){
				if (time1 == trajectories[0][j-1]){
					index = j;
					break;
				}
				else if (time1 <= trajectories[0][j] && time1 > trajectories[0][j-1]){
					index = j;
					break;
				}
				if (time1 < trajectories[0][j])
					throw new RuntimeException();
			}
			double time2 = trajectories[1][index];
			//System.out.println("time1 = "+time1+", time2 = "+time2);
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
	}*/


	boolean animating = false;
	
	public void setAnimation(boolean animating) {
		this.animating = animating;
	}

	public boolean isAnimating() {
		return animating;
	}

	public Distance getDistance() {
		return distance;
	}

	public double[][] getMonoticCurves() {
		return trajectories;
	}

	public void setFirstTrajectory(Trajectory trajectory) {
		trajectory1 = trajectory;
		repaint();
	}

	public void setSecondTrajectory(Trajectory trajectory) {
		trajectory2 = trajectory;
		repaint();
	}

	public Trajectory getFirstTrajectory() {
		return trajectory1;
	}
	public Trajectory getSecondTrajectory() {
		return trajectory2;
	}



}
