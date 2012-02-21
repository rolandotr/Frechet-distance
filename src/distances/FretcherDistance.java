package distances;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;


import trajectory.SimpleTrajectory;
import trajectory.Trajectory;
import wrappers.GPSFormat;
import wrappers.SimpleFormat;

public class FretcherDistance extends Distance{

	private double epsilon;
	
	public FretcherDistance(double epsilon){
		this.epsilon = epsilon;
	}

	@Override
	public double distance(Trajectory t1, Trajectory t2){
		Cell[][] freeSpace = computeCellOfFreeSpace(t1, t2);
		//to do, now I should compute the distance according to this free space.
		return 0;
	}
	
	public double[][] computeMonotoneCurves(Cell[][] reachableFreeSpace){
		int i = reachableFreeSpace.length-1;
		int j = reachableFreeSpace[reachableFreeSpace.length-1].length-1;
		double actualX = reachableFreeSpace[i][j].time1;
		double actualY = reachableFreeSpace[i][j].time2;
		List<Double> trajectory1 = new ArrayList<Double>();
		List<Double> trajectory2 = new ArrayList<Double>();
		trajectory1.add(actualX);
		trajectory2.add(actualY);
		Cell tmp;
		while (actualX != reachableFreeSpace[0][0].lastTime1 || 
				actualY != reachableFreeSpace[0][0].lastTime2){
			tmp = reachableFreeSpace[i][j];
			if (tmp.t4 == null && tmp.t1 == null) return null;
			if (tmp.t4 != null){
				actualX = tmp.t4[0];
				actualY = tmp.lastTime2;
				if (tmp.t4[0] == tmp.lastTime1) i--;
				j--;
			}
			else {
				actualX = tmp.lastTime1;
				actualY = tmp.t1[0];
				if (tmp.t1[0] == tmp.lastTime2) j--;
				i--;
			}
			if (i < 0) i = 0;
			if (j < 0) j = 0;
			trajectory1.add(actualX);
			trajectory2.add(actualY);
		}
		//now we invert the values
		double[] t1 = new double[trajectory1.size()];
		for (int k = 0; k < t1.length; k++){
			t1[k] = trajectory1.get(t1.length-k-1);
		}
		double[] t2 = new double[trajectory2.size()];
		for (int k = 0; k < t2.length; k++){
			t2[k] = trajectory2.get(t2.length-k-1);
		}
		return new double[][]{t1, t2};
	}

	@Override
	public double distance(Point p1, Point p2) {
		throw new RuntimeException();
	}

	@Override
	public double distance(double x1, double y1, double x2, double y2) {
		throw new RuntimeException();
	}

	@Override
	public double distance(GPSFormat p1, GPSFormat p2) {
		throw new RuntimeException();
	}
	
	public Cell[][] computeCellOfFreeSpace(Trajectory t1, Trajectory t2){
		Cell[][] freeSpace = new Cell[t1.size()-1][t2.size()-1];
		long lastTime1 = -1;
		long lastTime2 = -1;
		GPSFormat p1;
		GPSFormat p2;
		GPSFormat lastP1 = null;
		GPSFormat lastP2 = null;
		int i = 0;
		int j = 0;
		for (long time1 : t1.times()) {
			if (lastP1 == null) {
				lastTime1 = time1;
				lastP1 = t1.getPoint(time1);
				continue;
			}
			p1 = t1.getPoint(time1);
			for (long time2 : t2.times()) {
				if (lastP2 == null) {
					lastTime2 = time2;
					lastP2 = t2.getPoint(time2);
					continue;
				}
				p2 = t2.getPoint(time2);
				freeSpace[i][j] = computesFreeSpaceForSegment(lastTime1, time1, lastP1, p1, 
						lastTime2, time2, lastP2, p2, epsilon);
				//System.out.println(freeSpace[i][j]);
				j++;
				lastTime2 = time2;
				lastP2 = p2;
			}
			i++;
			j = 0;
			lastP2 = null;
			lastTime1 = time1;
			lastP1 = p1;
		}
		return freeSpace;
	}
	

	private Cell computesFreeSpaceForSegment(long lastTime1, long time1,
			GPSFormat lastP1, GPSFormat p1, long lastTime2, long time2,
			GPSFormat lastP2, GPSFormat p2, double epsilon) {
		double[] t4 = computesTimesOfIntersectionWithElipse(lastTime1, time1, 
				lastP1, p1, lastP2, epsilon);
		double[] t2 = computesTimesOfIntersectionWithElipse(lastTime1, time1, 
				lastP1, p1, p2, epsilon);
		double[] t1 = computesTimesOfIntersectionWithElipse(lastTime2, time2, 
				lastP2, p2, lastP1, epsilon);
		double[] t3 = computesTimesOfIntersectionWithElipse(lastTime2, time2, 
				lastP2, p2, p1, epsilon);
		return new Cell(lastTime1, time1, lastTime2, time2, t1, t2, t3, t4);
	}
	
	private double[] computesTimesOfIntersectionWithElipse(long t1, long t2, GPSFormat p1, 
			GPSFormat p2, GPSFormat p, double epsilon){
		double ax = p1.getLatitude();
		double ay = p1.getLongitude();
		double bx = p2.getLatitude()-ax;
		double by = p2.getLongitude()-ay;
		double b = 2*(bx*(ax-p.getLatitude())+by*(ay-p.getLongitude()));
		double a = bx*bx + by*by;
		double c = Math.pow(ax-p.getLatitude(), 2)+Math.pow(ay-p.getLongitude(), 2)-Math.pow(epsilon, 2);
		double D = b*b-4*a*c;
		if (D < 0){
			//en este caso es que no hay solucion
			return null;
		}
		double z1 = (-b - Math.sqrt(D))/(2*a);
		double z2 = (-b + Math.sqrt(D))/(2*a);
		double result1 = z1*(t2-t1)+t1;
		double result2 = z2*(t2-t1)+t1;
		if (result1 > Math.max(t1, t2) || result2 < Math.min(t1, t2)){
			return null;
		}
		if (result1 < Math.min(t1, t2)) result1 = Math.min(t1, t2);
		if (result2 > Math.max(t1, t2)) result2 = Math.max(t1, t2);
		return new double[]{result1, result2};
	}

	public static void main(String[] args) {
		Trajectory t1 = new SimpleTrajectory("t1");
		Trajectory t2 = new SimpleTrajectory("t2");
		t1.addPoint(new SimpleFormat(0, 0, 0));
		t1.addPoint(new SimpleFormat(1, 1, 1));
		t2.addPoint(new SimpleFormat(0, 0, 1));
		t2.addPoint(new SimpleFormat(1, 1, 0));
		FretcherDistance distance = new FretcherDistance(0.8);
		distance.distance(t1, t2);
	}
	
	public class Cell{

		public long lastTime1, time1, lastTime2, time2;
		public double[] t1, t2, t3, t4;

		public Cell(long lastTime1, long time1, long lastTime2, long time2,
				double[] t1, double[] t2, double[] t3, double[] t4) {
			super();
			this.lastTime1 = lastTime1;
			this.time1 = time1;
			this.lastTime2 = lastTime2;
			this.time2 = time2;
			this.t1 = t1;
			/*if (t1 != null){
				t1[0] = Math.max(t1[0], lastTime2);
				t1[1] = Math.min(t1[1], time2);
			}*/
			this.t2 = t2;
			/*if (t2 != null){
				t2[0] = Math.max(t2[0], lastTime1);
				t2[1] = Math.min(t2[1], time1);
			}*/
			this.t3 = t3;
			/*if (t3 != null){
				t3[0] = Math.min(t3[0], time2);
				t3[1] = Math.max(t3[1], lastTime2);
			}*/
			this.t4 = t4;
			/*if (t4 != null){
				t4[0] = Math.min(t4[0], time1);
				t4[1] = Math.max(t4[1], lastTime1);
			}*/
		}
		
		public Cell clone() {
			return new Cell(lastTime1, time1, lastTime2, time2, t1, t2, t3, t4);
		}
		
		@Override
		public String toString() {
			String result1 = "Rectangle is ["+lastTime1+", "+lastTime2+"], ["+lastTime1+", "+time2+
			"], ["+time1+", "+time2+"], ["+time1+", "+lastTime2+"]";
			String t1S;
			String t2S;
			String t3S;
			String t4S;
			if (t1 == null) t1S = "[null]";
			else t1S = "["+this.t1[0]+", "+this.t1[1]+"]";
			if (t2 == null) t2S = "[null]";
			else t2S = "["+this.t2[0]+", "+this.t2[1]+"]";
			if (t3 == null) t3S = "[null]";
			else t3S = "["+this.t3[0]+", "+this.t3[1]+"]";
			if (t4 == null) t4S = "[null]";
			else t4S = "["+this.t4[0]+", "+this.t4[1]+"]";
			return result1+"\n"+t1S+"\n"+t2S+"\n"+t3S+"\n"+t4S;
		}

		public void setAllToNull() {
			t1 = null;
			t2 = null;
			t3 = null;
			t4 = null;
		}
	}
	
	/** 16/02/2012 Trujillo Comment
	 * Given the cells of a free space, it computes the reachable free space*/
	public Cell[][] computeCellOfReachableFreeSpace(Cell[][] freeSpace) {
		System.out.println("Computing reachability");
		Cell[][] result = new Cell[freeSpace.length][];
		result[0] = new Cell[freeSpace[0].length];
		result[0][0] = freeSpace[0][0].clone();
		if (freeSpace[0][0].t1 != null && freeSpace[0][0].t3 != null ){
			result[0][0].t3[0] = Math.max(freeSpace[0][0].t1[0], freeSpace[0][0].t3[0]);
			if (result[0][0].t3[0] > result[0][0].t3[1]) result[0][0].t3 = null;
		}
		else{
			result[0][0].t3 = null;
		}
		if (freeSpace[0][0].t4 != null && freeSpace[0][0].t2 != null){
			result[0][0].t2[0] = Math.max(freeSpace[0][0].t4[0], freeSpace[0][0].t2[0]);
			if (result[0][0].t2[0] > result[0][0].t2[1]) result[0][0].t2 = null;
		}
		else{
			result[0][0].t2 = null;
		}
		//System.out.println(result[0][0]);
		for (int i = 1; i < freeSpace.length; i++){
			result[i] = new Cell[freeSpace[i].length];			
			result[i][0] = freeSpace[i][0].clone();
			if (result[i-1][0].t3 == null){
				result[i][0].setAllToNull();
			}
			else if (result[i-1][0].t3 != null && freeSpace[i][0].t3 != null ){
				result[i][0].t3[0] = Math.max(result[i-1][0].t3[0], freeSpace[i][0].t3[0]);
				if (result[i][0].t3[0] > result[i][0].t3[1]) result[i][0].t3 = null;
			}
			else{
				result[i][0].t3 = null;
			}
			result[i][0].t1 = result[i-1][0].t3; 
			//System.out.println(result[i][0]);
		}
		for (int j = 1; j < freeSpace[0].length; j++){
			result[0][j] = freeSpace[0][j].clone();
			if (result[0][j-1].t2 == null){
				result[0][j].setAllToNull();
			}
			else if (result[0][j-1].t2 != null && freeSpace[0][j].t2 != null){
				result[0][j].t2[0] = Math.max(result[0][j-1].t2[0], freeSpace[0][j].t2[0]);
				if (result[0][j].t2[0] > result[0][j].t2[1]) result[0][j].t2 = null;
			}
			else{
				result[0][j].t2 = null;
			}
			result[0][j].t4 = result[0][j-1].t2; 
			//System.out.println(result[0][j]);
		}
		for (int i = 1; i < freeSpace.length; i++){
			for (int j = 1; j < freeSpace[i].length; j++){
				result[i][j] = freeSpace[i][j].clone();
				result[i][j].t1 = result[i-1][j].t3; 
				result[i][j].t4 = result[i][j-1].t2; 
				if (result[i-1][j].t3 != null && freeSpace[i][j].t3 != null ){
					result[i][j].t3[0] = Math.max(result[i-1][j].t3[0], freeSpace[i][j].t3[0]);
					if (result[i][j].t3[0] > result[i][j].t3[1]) result[i][j].t3 = null;
				}
				if (result[i][j-1].t2 != null && freeSpace[i][j].t2 != null){
					result[i][j].t2[0] = Math.max(result[i][j-1].t2[0], freeSpace[i][j].t2[0]);
					if (result[i][j].t2[0] > result[i][j].t2[1]) result[i][j].t2 = null;
				}
				if (result[i-1][j].t3 == null && result[i][j-1].t2 == null){
					result[i][j].setAllToNull();
				}
				//System.out.println(result[i][j]);
			}
		}		
		return result;
	}
	
}
