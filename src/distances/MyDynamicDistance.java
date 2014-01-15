package distances;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;


import trajectory.SimpleTrajectory;
import trajectory.Trajectory;
import wrappers.GPSFormat;
import wrappers.GeneralizedPoint;
import wrappers.SimpleFormat;

public abstract class MyDynamicDistance extends Distance{

	
	public static void main(String[] args) {
		SimpleTrajectory t1 = new SimpleTrajectory("t1");
		t1.addPoint(new SimpleFormat(0, -1, 1));
		t1.addPoint(new SimpleFormat(1, 0, 1));
		t1.addPoint(new SimpleFormat(2, 0.5, 3));
		t1.addPoint(new SimpleFormat(3, 1, 1));
		t1.addPoint(new SimpleFormat(4, 2, 1));
		t1.addPoint(new SimpleFormat(5, 3, 1));

		SimpleTrajectory t2 = new SimpleTrajectory("t2");
		t2.addPoint(new SimpleFormat(1, 0, 0));
		t2.addPoint(new SimpleFormat(2, 1, 0));
		
		MyDynamicDistance distance = new MyDynamicDistance(6) {
			
			@Override
			public double distance(GPSFormat p1, GPSFormat p2) {
				return new EuclideanDistance().distance(p1, p2);
			}
			
			@Override
			public double distance(double x1, double y1, double x2, double y2) {
				throw new RuntimeException();
			}
			
			@Override
			public double distance(Point p1, Point p2) {
				throw new RuntimeException();
			}

			@Override
			public String getName() {
				return "temporal_dynamic_distance";
			}
		};
		
		Transformation t = distance.getTransformation(t1, t2);
		
		System.out.println(t.toString());
	}
	
	protected int noiseSize;
	
	public MyDynamicDistance(int noiseSize){
		this.noiseSize = noiseSize;
	}
	
	@Override
	public double distance(Trajectory t1, Trajectory t2){
		int size = 0;		
		Transformation transf = getTransformation(t1, t2);
		for (GPSFormat key : transf.matches.keySet()) {
			size += transf.matches.get(key).size();
		}
		return transf.cost/size;
	}
	
	public Hashtable<GPSFormat, List<GPSFormat>> getMatches(Trajectory t1, Trajectory t2){
		return getTransformation(t1, t2).matches;
	}
	
	protected Transformation getTransformation(Trajectory t1, Trajectory t2){
		GPSFormat[] trajectory1 = t1.toArray();
		GPSFormat[] trajectory2 = t2.toArray();
		//double delta = Double.MAX_VALUE;
		TreeMap<Double, Double> values = new TreeMap<Double, Double>();
		for (int i = 0; i < trajectory1.length; i++) {
			for (int j = 0; j < trajectory2.length; j++) {
				values.put(distance(trajectory1[i], trajectory2[j]), distance(trajectory1[i], trajectory2[j]));
			}			
		}
		double[] criticalValues = new double[values.size()];
		int cont = 0;
		for (Double value : values.values()){
			criticalValues[cont++] = value;
		}
		int min = 0;
		int max = criticalValues.length;
		double epsilon = -1;
		Transformation transformation = null;
		Transformation bestSoFar = null;
		double bestEpsilon = Double.MAX_VALUE;
		while (max > min){
			//System.out.println("min = "+min+" and max = "+max);
			int pos = min+(max-min)/2;
			epsilon = criticalValues[pos];
			transformation = getTransformation(epsilon, trajectory1, trajectory2);
			if (!transformation.feasable){
				//epsilon is too low
				if (min == pos){
					//es porque max = min+1 o max = min, en ambos casos, damos por terminado el tema
					break;
				}
				min = pos;
			}
			else{
				//epsilon is too high
				max = pos;
				if (epsilon < bestEpsilon){
					bestSoFar = transformation;
					bestEpsilon = epsilon;
				}
				//epsilon is too high
				/*System.out.println("alpha size = "+monotoneFunctions[0].length
						+" and beta size = "+monotoneFunctions[1].length
						+" and epsilon = "+epsilon
						+", best epsilon = "+bestEpsilon);*/
			}
		}
		return bestSoFar;
	}
	
	
	private class MyCell{
		
		public MyCell(MyCell previousCell, int currentX, int currentY, double cost, boolean matched, boolean available) {
			this.currentX = currentX;
			this.currentY = currentY;
			this.cost = cost;
			this.previousCell = previousCell;
			this.matched = matched;
			this.available = available;
		}
		
		MyCell previousCell;
		boolean available;
		boolean matched;
		int currentX;
		int currentY;
		double cost;
	}
	

	private void fillCell(int i, int j, GPSFormat[] t1, GPSFormat[] t2, double delta, 
			MyCell[][] values){
		//System.out.println("filling i = "+i+" and j = "+j);
		double distance = distance(t1[i-1], t2[j-1]);
		if (distance > delta){
			double min = Double.MAX_VALUE;
			double minimumDistance = distance; 
			int previousX = -1;
			int previousY = -1;
			for (int x = 1; x <= noiseSize; x++){
				for (int y = 0; y <= 2*x; y++){
					int x1;
					int y1;
					if (y > x){
						x1 = i - (y-x-1);
						y1 = j - x;
					}
					else{
						x1 = i - x;
						y1 = j - y;
					}
					if (x1 == i && y1 == j) throw new RuntimeException();
					if (x1 < 1 || y1 < 1) continue;
					if (!values[x1][y1].available) continue;
					double distanceTmp = distance(t1[x1-1], t2[y1-1]);
					if (distanceTmp <= delta){
						//this means that x, y, is a candidate, we keep the better.
						if (values[x1][y1].cost < min){
							min = values[x1][y1].cost;
							previousX = x1;
							previousY = y1;
						}
					}
					else{
						if (distanceTmp < minimumDistance){
							minimumDistance = distanceTmp;
						}	
					}
				}
				if (previousX != -1){
					//this is because some nice value was found;
					MyCell cell = new MyCell(values[previousX][previousY], i, j, values[previousX][previousY].cost, false, true);
					values[i][j] = cell;
					return;
				}
			}
			//we cannot compute a distance for this i, j. So, we should return false
			//together with the minimum value require.
			//First, we check either the row or column 0 could finish this.
			if ((i <= noiseSize) && (j <= noiseSize)) 
				values[i][j] = new MyCell(null, i, j, 0, false, true);
			else values[i][j] = new MyCell(null, i, j, Double.POSITIVE_INFINITY, false, false);
		}
		else{
			//in this case the edge should be putted. So, we need to first the best.
			boolean downOk = (j > 1  && values[i][j-1].available); 
			boolean leftOk = (i > 1  && values[i-1][j].available); 
			boolean diagonalOk = (j > 1  && i > 1 && values[i-1][j-1].available); 
			double minimum = Double.POSITIVE_INFINITY;
			MyCell bestCell = null;
			if (diagonalOk && values[i-1][j-1].cost < minimum){
				minimum = values[i-1][j-1].cost;
				bestCell = values[i-1][j-1];
			}
			if (leftOk && values[i-1][j].cost < minimum){
				minimum = values[i-1][j].cost;
				bestCell = values[i-1][j];
			}
			if (downOk && values[i][j-1].cost < minimum){
				minimum = values[i][j-1].cost;
				bestCell = values[i][j-1];
			}
			if (downOk || leftOk || diagonalOk || (i == 1 && j == 1)) 
				values[i][j] = new MyCell(bestCell, i, j, ((bestCell != null)?bestCell.cost:0)+distance, true, true);
			else values[i][j] = new MyCell(null, i, j, Double.POSITIVE_INFINITY, true, false);
		}
	}
	
	private Transformation getTransformation(double delta, 
			GPSFormat[] t1, GPSFormat[] t2) {
		MyCell[][] values = new MyCell[t1.length+1][t2.length+1];
		int max = (int)Math.max(t1.length, t2.length)+1;
		for (int i = 1; i < max; i++) {
			boolean reachable = false;
			for (int j = 1; j < i; j++) {
				if (i <= t1.length && j <= t2.length) {
					fillCell(i, j, t1, t2, delta, values);
					if (values[i][j].available) reachable = true;
				}
				if (j <= t1.length && i <= t2.length) {
					fillCell(j, i, t1, t2, delta, values);
					if (values[j][i].available) reachable = true;
				}
			}
			if (i <= t1.length && i <= t2.length) {
				fillCell(i, i, t1, t2, delta, values);
				if (values[i][i].available) reachable = true;
			}
			if (!reachable) return new Transformation(false, 0);
		}
		MyCell currentCell = values[t1.length][t2.length];
		if (!currentCell.available) return new Transformation(false, 0);
		Transformation result = new Transformation(currentCell.cost, delta);		
		do {
			if (currentCell.matched)
				result.addMatch(t1[currentCell.currentX-1], t2[currentCell.currentY-1]);
			currentCell = currentCell.previousCell;
		}while (currentCell != null);
		return result;
	}

	public class Transformation {

		public Hashtable<GPSFormat, List<GPSFormat>> matches;
		public double cost;
		public double delta;
		public double minimum;
		public boolean feasable;
		
		@Override
		public String toString() {
			String result = "feasable = "+feasable+"\n";
			result += "cost = "+cost+"\n";
			result += "delta = "+delta+"\n";
			result += "minmum = "+minimum+"\n";
			for (GPSFormat key : matches.keySet()) {
				for (GPSFormat value : matches.get(key)) {
					result += key+"<--->"+value+"\n";
				}
			}
			return result;
		}
		
		public Transformation(boolean feasable, double minimum){
			cost = Double.POSITIVE_INFINITY;
			this.minimum = minimum;
			this.feasable = feasable;
			matches = new Hashtable<GPSFormat, List<GPSFormat>>();
		}

		public Transformation(double cost, double delta){
			this(true, delta);
			this.cost = cost;
			this.delta = delta;
		}

		public void addMatch(GPSFormat p1, GPSFormat p2) {
			if (matches.containsKey(p1)){
				matches.get(p1).add(p2);
			}
			else{
				List<GPSFormat> tmp = new LinkedList<GPSFormat>();
				tmp.add(p2);
				matches.put(p1, tmp);
			}
		}
	}
	

}
