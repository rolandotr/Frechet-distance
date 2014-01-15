package distances;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import algorithms.generalization.GeneralizedTrajectory;


import wrappers.GPSFormat;
import wrappers.GeneralizedPoint;
import wrappers.Trajectory;

public class LogCostDistance extends Distance{

	private static double REMOVED_WEIGHT = 20;

	@Override
	public double distance(Point p1, Point p2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double distance(double x1, double y1, double x2, double y2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double distance(GPSFormat p1, GPSFormat p2) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public double distance(Trajectory t1, Trajectory t2){
		return logCostdistance(generalizeAtomicTrajectory(t1), generalizeAtomicTrajectory(t2), REMOVED_WEIGHT).cost;
	}
	
	public Transformation logCostDistance(Trajectory t1, Trajectory t2){
		return logCostdistance(generalizeAtomicTrajectory(t1), generalizeAtomicTrajectory(t2), REMOVED_WEIGHT);
	}

	public Transformation logCostDistance(GeneralizedTrajectory t1, GeneralizedTrajectory t2){
		return logCostdistance(t1, t2, REMOVED_WEIGHT);
	}

	public static GeneralizedTrajectory generalizeAtomicTrajectory(
			Trajectory t) {
		GeneralizedTrajectory result = new GeneralizedTrajectory(t.getIdentifier());
		for (GPSFormat p : t.points()) {
			result.addPoint(new GeneralizedPoint(p));
		}
		return result;
	}

	private Transformation logCostdistance(GeneralizedTrajectory t1, GeneralizedTrajectory t2, double u){
		Transformation result = new Transformation(t1, t2);
		Wrapper[][] matrix = new Wrapper[t1.size()+1][t2.size()+1];
		for (int i = 0; i < matrix.length; i++) {
			matrix[i][0] = new Wrapper(i*Math.log(u), DIRECTION.BACK_X);
		}
		for (int i = 0; i < matrix[0].length; i++) {
			matrix[0][i] = new Wrapper(i*Math.log(u), DIRECTION.BACK_Y);
		}
		GeneralizedPoint[] locations1 = t1.toArray();
		GeneralizedPoint[] locations2 = t2.toArray();
		for (int i = 1; i < matrix.length; i++) {
			for (int j = 1; j < matrix[i].length; j++) {
				Wrapper w = min(matrix[i-1][j-1].cost+
						logCost(locations1[i-1], locations2[j-1]),
						matrix[i][j-1].cost+Math.log(u),
						matrix[i-1][j].cost+Math.log(u)
						);
				matrix[i][j] = w;
			}
		}
		int i = matrix.length-1;
		int j = matrix[0].length-1;
		result.setCost(matrix[i][j].cost);
		List<GeneralizedPoint> tmp1 = new ArrayList<GeneralizedPoint>();
		List<GeneralizedPoint> tmp2 = new ArrayList<GeneralizedPoint>();
		int matchesCounter = 0;
		while (i != 0 && j != 0){
			if (matrix[i][j].direction.equals(DIRECTION.DIAGONAL)){
				//result.addMatch(locations1[i-1], locations2[j-1], --matchesCounter);
				tmp1.add(locations1[i-1]);
				tmp2.add(locations2[j-1]);
				matchesCounter++;
				i--;
				j--;
			}
			else if (matrix[i][j].direction.equals(DIRECTION.BACK_X)){
				i--;
			}
			else{
				j--;
			}
		}
		result.setSizeOfMatches(matchesCounter);
		for (int k = 0; k < matchesCounter; k++) {
			result.addMatch(tmp1.get(matchesCounter-k-1), tmp2.get(matchesCounter-k-1), k);
		}
		return result;
	}

	private Wrapper min(double d1, double d2, double d3) {
		double min = d1;
		DIRECTION direction = DIRECTION.DIAGONAL;
		if (d2 < min){
			min = d2;
			direction = DIRECTION.BACK_Y;
		}
		if (d3 < min){
			min = d3;
			direction = DIRECTION.BACK_X;
		}
		return new Wrapper(min, direction);
	}
	
	private double logCost(GeneralizedPoint p1, GeneralizedPoint p2) {
		double lat = findMaxInterval(p1.x1, p1.x2, p2.x1, p2.x2);
		double lon = findMaxInterval(p1.y1, p1.y2, p2.y1, p2.y2);
		double time = findMaxInterval(p1.t1, p1.t2, p2.t1, p2.t2);
		return Math.log(1+lat)+Math.log(1+lon)+Math.log(1+time);
			
	}
	
	private double findMaxInterval(double x1, double x2, double y1,
			double y2) {
		double min = (x1 < y1)?x1:y1;
		double max = (x2 > y2)?x2:y2;
		return max-min;
	}


	enum DIRECTION {
		DIAGONAL, BACK_X, BACK_Y;
	}

	class Wrapper{
		
		DIRECTION direction;
		
		double cost;
		
		public Wrapper(double cost, DIRECTION direction){
			this.direction = direction;
			this.cost = cost;
		}
	}

	public class Transformation {
		public String t1; 
		public String t2; 
		public GeneralizedPoint[] matchesForT1;
		public GeneralizedPoint[] matchesForT2;
		public double cost;
		
		public Transformation(GeneralizedTrajectory t1, GeneralizedTrajectory t2){
			this.t1 = t1.getIdentifier();
			this.t2 = t2.getIdentifier();
		}

		public void setCost(double cost) {
			this.cost = cost;
		}

		public void addMatch(GeneralizedPoint p1, GeneralizedPoint p2, int pos) {
			matchesForT1[pos] = p1;
			matchesForT2[pos] = p2;
		}
		public void setSizeOfMatches(int size){
			matchesForT1 = new GeneralizedPoint[size];
			matchesForT2 = new GeneralizedPoint[size];
		}
	}
	
	@Override
	public String getName() {
		return "log_cost_distance";
	}


}
