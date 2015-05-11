package range_queries;

import util.*;
import wrappers.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

import distances.Distance;

public abstract class Query {

	private static final double CASE_ERROR = -1;

	public double averageQueries(Collection<Trajectory> datasetInitial, Collection<Trajectory> datasetForRegions, 
			List<Trajectory> datasetAnonymous, double radius, long seconds, int numRuns, 
			Distance dist) {
		double average = 0.0;
		double result = 0.0;
		int denominator = 0;
		for (int i = 0; i < numRuns; i++) {
			result = runQuery(datasetInitial, datasetForRegions, datasetAnonymous, radius, seconds, dist);
			//If result = caseError --> Doesn't take into account.
			if (result != CASE_ERROR) {
				average += result;
				denominator++;
			}
		}		
		//System.out.println("Num. of good cases = "+denominator+ " over " + numRuns+" runs");
		if (denominator == 0) {
			//System.out.println("Num. of good cases = "+denominator+ " over " + numRuns+" runs");
			return 1;
		}
		return (average/denominator);
	}

	
	private double runQuery(Collection<Trajectory> datasetInitial, Collection<Trajectory> datasetForRegions, 
			List<Trajectory> datasetAnonymous, double radius, long seconds,
			Distance dist) {
		Region r = computeRegion(radius, seconds, datasetForRegions);
		int quantIni = executeQuery(datasetInitial, r, dist);
		int quantFin = executeQuery(datasetAnonymous, r, dist);
		if (quantIni == 0 && quantFin == 0) {
			return CASE_ERROR;
		} else {
			return Math.abs(((double)quantIni) - ((double)quantFin)) / ((double)Math.max(quantIni,quantFin));			
		}	
	}

	protected abstract int executeQuery(Collection<Trajectory> datasetInitial, Region r,
			Distance dist);

	private Trajectory getRandomTrajectory(Collection<Trajectory> dataset){
		Random randomizer = new Random(System.nanoTime());
		int index = randomizer.nextInt(dataset.size());
		int cont = 0;
		for (Trajectory t : dataset){
			if (cont == index) return t;
			cont++;
		}
		throw new RuntimeException();
	}
	
	//Computes a random Region giving a time and the dataset.
	//It chooses randomly a point of the dataset and extends a region of Radius "radius".
	
	private Region computeRegion(double radius, long seconds, Collection<Trajectory> dataset) {
		//Trajectory randomSimpleTrajectory = dataset.get(randomizer.nextInt(dataset.size()));
		Trajectory randomSimpleTrajectory = getRandomTrajectory(dataset);
		//Trajectory randomSimpleTrajectory = dataset.iterator().next();
		List<GPSFormat> pointsOfRegion = new LinkedList<GPSFormat>();		
		if (randomSimpleTrajectory.length() <= seconds) {
			for (GPSFormat gpsFormat : randomSimpleTrajectory.points()) {
				pointsOfRegion.add(gpsFormat);
			}
			return new Region(pointsOfRegion, radius);
		}
		Random r = new Random();
		long beggining = randomSimpleTrajectory.firstTime()+(long)(r.nextDouble()*(randomSimpleTrajectory.length()-seconds));
		GPSFormat firstPoint = randomSimpleTrajectory.interpolateTime(beggining);
		pointsOfRegion.add(firstPoint);
		for (long time : randomSimpleTrajectory.times()){
			if (time > (beggining+seconds)) break;
			if (time > beggining){
				pointsOfRegion.add(randomSimpleTrajectory.getPoint(time));
			}
		}
		return new Region(pointsOfRegion, radius);
	}

	//Determines the maximum time of a SimpleTrajectory in the dataset.
	private long maxTime(List<Trajectory> dataset) {
		long maxTime = Long.MIN_VALUE;
		
		for (Trajectory str : dataset) {
			if (str.getTime(str.times().size()-1) > maxTime) {
				maxTime = str.getTime(str.times().size()-1); 
			}
		}
		
		return maxTime;
	}

	//Determines the minimum time of a SimpleTrajectory in the dataset.
	private long minTime(List<Trajectory> dataset) {
		long minTime = Long.MAX_VALUE;
		
		for (Trajectory str : dataset) {
			if (str.getTime(0) < minTime) {
				minTime = str.getTime(0); 
			}
		}		
		return minTime;
	}

	
	
	//WhereAt (SimpleTrajectory T, time t): returns the location visited by at time t, if any.
	private GPSFormat whereAt (Trajectory str, long t) {
		return str.getPoint(t);
	}
}
