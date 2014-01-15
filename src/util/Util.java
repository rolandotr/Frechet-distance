package util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import clustering.MyTrajectoryFormat;

import compression.BasicCompression;

import distances.GPSDistance;

import trajectory.SimpleTrajectory;
import trajectory.Trajectory;
import wrappers.GPSFormat;
import wrappers.SimpleFormat;

public class Util {
	
	public static void printTrajectory(Trajectory t) throws IOException {
		printTrajectory(t, t.getIdentifier());
	}

	public static void printTrajectory(Trajectory t, String name) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(name+".txt"));
		for (GPSFormat point : t.values()){
			 writer.write(point.getLatitude()+" "+point.getLongitude()+" 0 "+point.getTime());
			 writer.newLine();
		}
		writer.close();
	}

	
	public static void printValuesOfHashtable(Hashtable<Object, Object> values){
		for (Object obj : values.values()) {
			System.out.println(obj.toString());
		}
	}
	
	public static void printValuesOfTree(TreeMap<Double, Double> values){
		for (Double obj : values.values()) {
			System.out.println(obj);
		}
	}
	
	public static void printMaximumDistance(Trajectory t1, Trajectory t2){
		double max = 0;
		GPSDistance distance = new GPSDistance();
		for (GPSFormat p1 : t1.values()) {
			for (GPSFormat p2 : t2.values()) {
				if (distance.distance(p1, p2) > max) max = distance.distance(p1, p2); 
			}			
		}
		System.out.println("maximum distance = "+max);
	}

	public static void printValuesOfArray(double[] values) {
		for (Double obj : values) {
			System.out.println(obj);
		}
	}
	
	public static void printTrajectory(String preffix, String name, double threshold) throws IOException{
		List<Trajectory> dst = MyTrajectoryFormat.loadCompressedTrajecotriesInList(preffix);
		//List<Trajectory> dst = MyTrajectoryFormat.loadTrajecotriesInList(preffix);
		BufferedWriter writer1 = new BufferedWriter(new FileWriter(name+".txt"));
		BufferedWriter writer2 = new BufferedWriter(new FileWriter(name+"_compressed_gps.txt"));
		for (Trajectory trajectory : dst) {
			//System.out.println(trajectory.getIdentifier());
			if (trajectory.getIdentifier().equals(name)){				
				Trajectory compressed = BasicCompression.compressGPS(trajectory, threshold, new GPSDistance());
				for (GPSFormat point : trajectory.values()){
					 writer1.write(point.getLatitude()+" "+point.getLongitude()+" 0 "+point.getTime());
					 writer1.newLine();
				}
				for (GPSFormat point : compressed.values()){
					 writer2.write(point.getLatitude()+" "+point.getLongitude()+" 0 "+point.getTime());
					 writer2.newLine();
				}
			}
		}		
		writer1.close();
		writer2.close();
	}
	
	public static Trajectory getCompressedTrajectory(String preffix, String name){
		List<Trajectory> dst = MyTrajectoryFormat.loadCompressedTrajecotriesInList(preffix);
		//List<Trajectory> dst = MyTrajectoryFormat.loadTrajecotriesInList(preffix);
		for (Trajectory trajectory : dst) {
			if (trajectory.getIdentifier().equals(name)) return trajectory;				
		}
		return null;
	}
	
	
	public static Trajectory averageTrajectory(
			List<Trajectory> datasetIni, String id) {
		return averageTrajectory(datasetIni, id);
	}
	public static Trajectory averageTrajectory(
			Collection<Trajectory> datasetIni, String id) {
		
		TreeMap<Long, GPSFormat> trajectory = new TreeMap<Long, GPSFormat>();

		Trajectory randomTrajectory = getRandomTrajectory(datasetIni);
		
		for (long time : randomTrajectory.times()) {
			double x = 0;
			double y = 0;
			GPSFormat point = null;
			
			for (Trajectory dt : datasetIni) {
				point = dt.getPoint(time);
				x += point.getX();
				y += point.getY();
			}
			trajectory.put(time, new SimpleFormat(time, x/datasetIni.size(), y/datasetIni.size()));
		}
		
		return new SimpleTrajectory (id, trajectory);
	}

	public static Trajectory averageTrajectory(
			Hashtable<String, Trajectory> datasetIni, String id) {
		return averageTrajectory(datasetIni.values(), id);
	}
	
	public static Trajectory getRandomTrajectory(Hashtable<String, Trajectory> trajectories){
		return getRandomTrajectory(trajectories.values());
	}
	
	public static Trajectory getRandomTrajectory(Collection<Trajectory> trajectories){
		Random r = new Random();
		int index = r.nextInt(trajectories.size());
		int cont = 0;
		for (Trajectory t : trajectories){
			if (cont == index) return t;
			cont++;
		}
		throw new RuntimeException();
	}
	

}
