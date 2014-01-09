package util;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import distances.Distance;
import distances.EuclideanDistance;
import distances.GPSDistance;


import wrappers.GPSFormat;
import wrappers.SimpleTrajectory;
import wrappers.Trajectory;


public class Statistics {

	
	public static void main(String[] args) {
		Trajectory t1 = new SimpleTrajectory("t1");
		Trajectory t2 = new SimpleTrajectory("t1");
		t1.setPoint(0, 0, 0);
		t1.setPoint(4, 1, 1);
		t1.setPoint(6, 2, 1);
		t2.setPoint(0, 0, 1);
		t2.setPoint(1, 0.5, 0.5);
		t2.setPoint(5, 2.5, 0.5);
		List<Trajectory> original = new ArrayList<Trajectory>();
		List<Trajectory> anonymized = new ArrayList<Trajectory>();
		original.add(t1);
		anonymized.add(t2);
		Hashtable<String, double[]> result = lengthComputation(original, anonymized, new EuclideanDistance());
		System.out.println(result.get("t1")[0]);
		System.out.println(result.get("t1")[1]);
	}
	
	
	/** 13/08/2010 Trujillo Comment
	 * Aqui lo que vamos a ver es la distancia euclideana entre las trajectorias originales
	 * y las anonymizadas. Hay que tener en cuenta, que solo se van a acontar los puntos 
	 * que estan definidos en las trajectorias originales. Por tanto, el conjunto de trajectorias
	 * originales tiene que ser exactmente el que se da de inicio inicio, y no 
	 * ninguno syncronizado ni nada de eso. 
	 * 
	 * OJO, que si se trabaja con trajectorias reales es muy probable que haga falta
	 * synronizacion, y entonces este metodo puede dar resultados incorrectos cuando
	 * las trajectorias han sido syncronizadas.*/
	public static double spaceDistortion(Collection<Trajectory> original, List<Trajectory> anonymized, 
			Distance dist){
		GPSFormat p1;
		GPSFormat p2;
		double result = 0;
		int cont = 0;
		for (Trajectory t1 : original){
			for (Trajectory t2 : anonymized){
				if (!t1.equals(t2)) continue;
				//syncTrajectories(t1, t2);
				for (long time : t1.times()){
					p1 = t1.getPoint(time);
					p2 = t2.getPoint(time);
					if (p1 == null || p2 == null) continue;
					result += dist.distance(p1, p2);
				}
			}
		}
		return result;
	}
	
	public static double spaceDistortion(List<Trajectory> original, List<Trajectory> anonymized, 
			Distance dist, double delta){
		if (anonymized == null) return Double.MAX_VALUE;
		GPSFormat p1;
		GPSFormat p2;
		double result = 0;
		int cont = 0;
		for (Trajectory t1 : original){
			for (Trajectory t2 : anonymized){
				if (!t1.equals(t2)) continue;
				//syncTrajectories(t1, t2);
				for (long time : t1.times()){
					p1 = t1.getPoint(time);
					p2 = t2.getPoint(time);
					if (p1 == null || p2 == null) continue;
					double d = dist.distance(p1, p2);
					//if (d >= delta/2) d -= delta; 
					result += d;
				}
			}
		}
		return result;
	}

	public static double gpsSpaceDistortion(List<Trajectory> original, List<Trajectory> anonymized, 
			Distance dist){
		GPSFormat p1;
		GPSFormat p2;
		double result = 0;
		int cont = 0;
		for (Trajectory t1 : original){
			for (Trajectory t2 : anonymized){
				if (!t1.equals(t2)) continue;
				//syncTrajectories(t1, t2);
				for (long time : t1.times()){
					p1 = t1.getPoint(time);
					p2 = t2.getPoint(time);
					if (p1 == null || p2 == null) continue;
					result += dist.distance(p1, p2);
					//System.out.println("p1 = "+p1+" p2 = "+p2 +" result = "+result);
				}
			}
		}
		return result;
	}
	

	/** 04/11/2010 Trujillo Comment
	 * Aqui en primer lugar hay que contar las que estan en el original y no estan el conjunto
	 * anonynizado, pero tambien ha que ver si el conjunto hay trajectorias que tienen tamaño
	 * cero*/
	public static int removedTrajectories(Collection<Trajectory> original, List<Trajectory> anonymized){
		if (anonymized == null) return Integer.MAX_VALUE;
		int cont = 0;
		for (Trajectory t1 : original){
			for (Trajectory t2 : anonymized){
				if (t1.equals(t2)) cont++;
			}
		}
		for (Trajectory t : anonymized)
			if (t.size() == 0) cont--;
		return original.size()-cont;
	}
	
	
	/** 15/12/2010 Trujillo Comment
	 * Da el por ciento de puntos que fueron eliminados*/
	public static float removedPoints(Collection<Trajectory> original, List<Trajectory> anonymized){
		int totalOriginalPoints = 0;
		for (Trajectory t : original){
			totalOriginalPoints += t.size();
		}
		int totalAnonymizedPoints = 0;
		for (Trajectory t : anonymized){
			totalAnonymizedPoints += t.size();
		}
		return ((float)(totalOriginalPoints-totalAnonymizedPoints)*100)/totalOriginalPoints;
	}
	
	/** 15/12/2010 Trujillo Comment
	 * Da el por ciento de puntos que fueron eliminados de las trakectorias que realmente
	 * se han quedado en el conjunto anonymizado*/
	public static float removedPoints2(Collection<Trajectory> original, List<Trajectory> anonymized){
		int totalOriginalPoints = 0;
		int totalAnonymizedPoints = 0;
		for (Trajectory t1 : original){
			for (Trajectory t2: anonymized){
				if (t1.equals(t2)){
					totalOriginalPoints += t1.size();
					totalAnonymizedPoints += t2.size();
				}
			}
		}
		return ((float)(totalOriginalPoints-totalAnonymizedPoints)*100)/totalOriginalPoints;
	}

	public static Hashtable<String, double[]> lengthComputation(List<Trajectory> original, 
			List<Trajectory> anonymized, Distance dist){
		GPSFormat p1;
		GPSFormat p2;
		GPSFormat lastP1;
		GPSFormat lastP2;
		double length1;
		double length2;
		int cont;
		Hashtable<String, double[]> result = new Hashtable<String, double[]>();
		for (Trajectory t1 : original){
			for (Trajectory t2 : anonymized){
				if (!t1.equals(t2)) continue;
				length1 = 0;
				length2 = 0;
				lastP1 = null;
				lastP2 = null;
				cont = 0;
				for (long time : t1.times()){
					p1 = t1.getPoint(time);
					if (lastP1 == null){
						lastP1 = p1;
						continue;
					}
					length1 += dist.distance(p1, lastP1);
					lastP1 = p1;
					cont++;
				}
				length1 = length1/cont;
				cont = 0;
				for (long time : t2.times()){
					p2 = t2.getPoint(time);
					if (lastP2 == null){
						lastP2 = p2;
						continue;
					}
					length2 += dist.distance(p2, lastP2);
					lastP2 = p2;
					cont++;
				}
				length2 = length2/cont;
				result.put(t1.getIdentifier(), new double[]{length1, length2});
			}
		}
		return result;
	}

	/** 17/08/2010 Trujillo Comment
	 * Aqui nos vamos a asegurar que ambas estan definidas sobre los mismos puntos*/
	private static void syncTrajectories(Trajectory t1, Trajectory t2) {
		List<Long> times1 = new LinkedList<Long>();
		for (long time : t1.times()){
			if (!t2.containsTime(time)) times1.add(time);
		}
		List<Long> times2 = new LinkedList<Long>();
		for (long time : t2.times()){
			if (!t1.containsTime(time)) times2.add(time);
		}
		GPSFormat p1 = null;
		GPSFormat p2 = null;
		for (long time : times1){
			//estos son los que estan en t1 y no estan en t2
			p1 = null;
			p2 = null;
			for (long t : t2.times()){
				if (p1 == null) {
					p1 = t2.getPoint(t);
				}
				else{
					p2 = t2.getPoint(t);
					if (t > time){
						//entonces es porque estamos en el intervalo
						GPSFormat f = Interpolation.interpolate(p1, p2, time);
						t2.setPoint(f.getTime(), f.getX(), f.getY());
						break;
					}
					p1 = p2;
				}
			}
		}
		
		for (long time : times2){
			//estos son los que estan en t2 y no estan en t1
			p1 = null;
			p2 = null;
			for (long t : t1.times()){
				if (p1 == null) {
					p1 = t1.getPoint(t);
				}
				else{
					p2 = t1.getPoint(t);
					if (t > time){
						//entonces es porque estamos en el intervalo
						GPSFormat f = Interpolation.interpolate(p1, p2, time);
						t1.setPoint(f.getTime(), f.getX(), f.getY());
						break;
					}
					p1 = p2;
				}
			}
		}
	}

	public static void writeLengthData(Hashtable<String, double[]> length, int k, String file) throws FileNotFoundException, IOException, ClassNotFoundException{
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write("- \t - \t Original \t Distorted");
		writer.newLine();
		int cont = 1;
		double distance = 0;
		double difference = 0;
		for (String id : length.keySet()){
			if ((cont % k) == 0){
				writer.write((cont/k)+" \t "+(distance/k)+
						" \t "+(difference/k));
				writer.newLine();
				distance = 0;
				difference = 0;
			}
			cont++;
			double[] l = length.get(id);
			distance += l[0];
			difference += l[1]-l[0];
		}
		writer.close();		
	}


	public static int averageLocations(
			Hashtable<String, Trajectory> trajectories) {
		if (trajectories.size() == 0) return 0;
		int average = 0;
		for (Trajectory t : trajectories.values()) {
			average += t.size(); 
		}
		return average/trajectories.size();
	}


	public static long findLowerTime(
			Hashtable<String, Trajectory> trajectories) {
		long min = Long.MAX_VALUE;
		for (Trajectory t : trajectories.values()){
			if (t.firstTime() < min){
				min = t.firstTime();
			}
		}
		return min;
	}

	public static long findHigherTime(
			Hashtable<String, Trajectory> trajectories) {
		long max = 0;
		for (Trajectory t : trajectories.values()){
			if (t.lastTime() > max){
				max = t.lastTime();
			}
		}
		return max;
	}


	public static double averageLocations(List<Trajectory> list) {
		double result = 0;
		for (Trajectory trajectory : list) {
			result += trajectory.size();
		}
		return result/list.size();
	}

	public static double minimumNumberOfLocations(List<Trajectory> list) {
		double minmum = Double.MAX_VALUE;
		for (Trajectory trajectory : list) {
			if (trajectory.size() < minmum) minmum = trajectory.size();
		}
		return minmum;
	}

	public static double maximumNumberOfLocations(List<Trajectory> list) {
		double max = 0;
		for (Trajectory trajectory : list) {
			if (trajectory.size() > max) max = trajectory.size();
		}
		return max;
	}


	public static double averageLenght(Hashtable<String, Trajectory> list, Distance distance) {
		double result = 0;
		for (Trajectory trajectory : list.values()) {
			result += trajectory.spatialLength(distance);
		}
		return result/list.size();
	}

	public static double averageLenght(List<Trajectory> list, Distance distance) {
		double result = 0;
		for (Trajectory trajectory : list) {
			result += trajectory.spatialLength(distance);
		}
		return result/list.size();
	}


	public static long averageDuration(Hashtable<String, Trajectory> list) {
		long result = 0;
		for (Trajectory trajectory : list.values()) {
			result += trajectory.length();
		}
		return result/list.size();
	}

	public static long averageDuration(List<Trajectory> list) {
		long result = 0;
		for (Trajectory trajectory : list) {
			result += trajectory.length();
		}
		return result/list.size();
	}


	public static double averageLocationsHashtable(Hashtable<String, Trajectory> dst) {
		int average = 0;
		for (Trajectory t : dst.values()) {
			average += t.size(); 
		}
		return average/dst.size();
	}
	
}
