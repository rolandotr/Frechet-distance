package distances;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import algorithms.generalization.GeneralizedTrajectory;


import trajectory.SimpleTrajectory;
import trajectory.Trajectory;
import wrappers.GPSFormat;
import wrappers.GeneralizedPoint;
import wrappers.SimpleFormat;


/*Trujillo- May 30, 2013
 * This is a distance that we expect to fulfil the following requirements:
 * - Continuity: This means that the distance between two trajectory should not be significantly distorted bbecause
 *               they are represented as a polyline. The basic idea to avoid this is to interpolate new points if needed.
 * - Absortion: A trajectory could have more points than other or be recorded in different granularities. This should not
 *              affect the final result. In this sense, we would like to be roughly as the Frechet Distance.
 * - Noise: Should resist noise somehow.
 * - Un-parametizable: Meaning that the distance should use as less parameters as possible.*/
public abstract class MyRecursiveDistance extends Distance{

	private int l;

	public MyRecursiveDistance(int l){
		this.l = l;
	}
	
	public Transformation getTransformation(Trajectory t1, Trajectory t2){
		double delta = 0;
		Transformation tmp;
		GPSFormat[] trajectory1 = t1.toArray();
		GPSFormat[] trajectory2 = t2.toArray();
		do{
			tmp = recursiveTransformation(delta, l, trajectory1, trajectory2, 
					trajectory1.length-1, trajectory2.length-1, l, l, Double.MAX_VALUE);
			if (tmp.feasable) return tmp;
			else{
				delta = tmp.minimum;
			}
		}while(true);
	}
	
	private Transformation recursiveTransformation(double delta, int l,
			GPSFormat[] t1, GPSFormat[] t2, int i, int j, int c1, int c2, double min) {
		if (i < 0 || j < 0) return new Transformation(false, min);

		double distance = distance(t1[i], t2[j]);
		
		//first we need some stop conditions
		if (i == 0 && j == 0) {
			if (distance > delta) {
				//we need to check whether we can erase one of them.
				if (c1 == 0 || c2 == 0) return new Transformation(false, Math.min(min, distance));
				else {
					Transformation result = new Transformation(true, Double.MAX_VALUE);
					result.setCost(0);
					return result;
				}
			}
			else{
				Transformation result = new Transformation(true, min);
				result.setCost(distance);
				result.addMatch(t1[0], t2[0]);
				return result;
			}
		}
		
		Transformation best = null;;
		if (distance > delta){
			double minimum = Math.min(min, distance);
			//if (c2 == l && c1 == l) minimum = distance;
			if (c1 == 0 && c2 == 0){
				//there is no way to return succesfully
				return new Transformation(false, minimum);
			}
			else if (c1 == 0 && c2 > 0){
				//only t2 could have a noisy point.
				Transformation x = recursiveTransformation(delta, l, t1, t2, i, j-1, c1, c2-1, minimum);
				if (x.feasable) return x;
				else return new Transformation(false, x.minimum);
			}
			else if (c1 > 0 && c2 == 0){
				//only t2 could have a noisy point.
				Transformation x = recursiveTransformation(delta, l, t1, t2, i-1, j, c1-1, c2, minimum);
				if (x.feasable) return x;
				else return new Transformation(false, x.minimum);
			}
			else {
				//only t2 could have a noisy point.
				Transformation x = recursiveTransformation(delta, l, t1, t2, i, j-1, c1, c2-1, minimum);
				Transformation y = recursiveTransformation(delta, l, t1, t2, i-1, j, c1-1, c2, minimum);
				if (x.feasable & !y.feasable) return x;
				else if (!x.feasable & y.feasable) return y;
				else if (!x.feasable & !y.feasable) return new Transformation(false, Math.min(x.minimum, y.minimum));
				else if (x.cost < y.cost) return x;
				else return y;
			}
		}
		else {
			//in this case things seem to be ok
			Transformation x = recursiveTransformation(delta, l, t1, t2, i-1, j, l, l, Double.MAX_VALUE);
			Transformation y = recursiveTransformation(delta, l, t1, t2, i, j-1, l, l, Double.MAX_VALUE);
			Transformation z = recursiveTransformation(delta, l, t1, t2, i-1, j-1, l, l, Double.MAX_VALUE);
			//in all the cases the edge <i, j> is added.
			if (x.feasable){
				double minCost = x.cost;
				best = x;
				if (y.feasable && y.cost < minCost){
					minCost = y.cost; 
					best = y;
				}
				if (z.feasable && z.cost < minCost){
					minCost = z.cost; 
					best = z;
				}
			}
			else if (y.feasable){
				double minCost = y.cost;
				best = y;
				if (z.feasable && z.cost < minCost){
					minCost = z.cost; 
					best = z;
				}
			}
			else if (z.feasable){
				best = z;
			}
			else{
				//noone is feasable
				return new Transformation(false, Math.min(Math.min(x.minimum, y.minimum), z.minimum));
			}
			best.addMatch(t1[i], t2[j]);
			best.setCost(best.cost+distance);
			return best;
		}
	}

	@Override
	public double distance(Trajectory t1, Trajectory t2){
		return getTransformation(t1, t2).cost;
	}
	

	public class Transformation {
		public Hashtable<GPSFormat, List<GPSFormat>> matches;
		public double cost;
		public boolean feasable;
		public double minimum; 
		
		public Transformation(boolean feasable, double minimum){
			this.feasable = feasable;
			this.minimum = minimum;
			matches = new Hashtable<GPSFormat, List<GPSFormat>>();
		}

		public void setCost(double cost) {
			this.cost = cost;
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
		
		@Override
		public String toString() {
			String result = "cost = "+cost+"\n";
			result += "feasable = "+feasable+"\n";
			result += "minmum = "+minimum+"\n";
			for (GPSFormat key : matches.keySet()) {
				for (GPSFormat values : matches.get(key)){
					result += key + "<---->"+values+"\n";
				}
			}
			return result;
		}
	}


}
