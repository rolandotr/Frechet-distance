package util;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.management.RuntimeErrorException;

import compression.BasicCompression;

import parsers.GPXParser;

import clustering.Cluster;
import clustering.MyTrajectoryFormat;
import distances.Distance;
import distances.EuclideanDistance;
import distances.FrechetDistanceEuclideanBased;
import distances.FrechetDistanceGPSBased;
import distances.GPSDistance;
import distances.JosepEuclideanDistanceOnTheFly;
import distances.JosepEuclideanDistanceOnTheFlyVs2;
import distances.JosepGPSDistanceOnTheFlyVs2;
import distances.LogCostDistance;
import distances.MyDistanceEuclideanBased;
import distances.MyDistanceGPSBased;

import trajectory.SimpleTrajectory;
import trajectory.Trajectory;
import wrappers.GPSFormat;
import wrappers.SimpleFormat;

public class Syntetic {

	private static final int MAXIMUM_CLUSTER_SIZE = 1024;

	/**
	 * 19/07/2010 Trujillo Comment Este metodo coge los valores de las
	 * trajectorias en el archivo trujilloTest.txt el cual esta en el formato de
	 * la aplicacion que genera datos sinteticos. entonces lo leemos y guardamos
	 * en un fichero syntetic_allTrajectories.obj un estructura de datos que es
	 * Hashtable<String, TreeMap<Long, GPSFormat>>, donde el key es el id de la
	 * trajectoria
	 */
	public static void buildingAllTrajectoriesToList(String file, String preffix)
			throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String[] split;
		Hashtable<String, Trajectory> result = new Hashtable<String, Trajectory>();
		Hashtable<String, TreeMap<Long, GPSFormat>> allTrajectories = new Hashtable<String, TreeMap<Long, GPSFormat>>();
		TreeMap<Long, GPSFormat> tmp = null;
		SimpleFormat gps;
		for (String line = reader.readLine(); line != null; line = reader
				.readLine()) {
			split = line.split("\t");
			if (allTrajectories.containsKey(split[1])) {
				tmp = allTrajectories.get(split[1]);
			} else {
				tmp = new TreeMap<Long, GPSFormat>();
			}
			gps = new SimpleFormat(Long.parseLong(split[4]),
					Double.parseDouble(split[5]), Double.parseDouble(split[6]));
			tmp.put(Long.parseLong(split[4]), gps);
			allTrajectories.put(split[1], tmp);
		}
		for (String id : allTrajectories.keySet()) {
			Trajectory tmp2 = new SimpleTrajectory(id, allTrajectories.get(id));
			// System.out.println(tmp2);
			result.put(tmp2.getIdentifier(), tmp2);
			/*
			 * if (Integer.parseInt(tmp2.getIdentifier()) %
			 * (allTrajectories.size()+1) == 0) throw new
			 * RuntimeException(Integer.parseInt(tmp2.getIdentifier()) +"%"+
			 * allTrajectories.size());
			 */
		}
		// hasta aqui ya tengo la lista de trajectorias
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
				"./" + preffix + "/" + preffix + "_allTrajectoriesList.obj"));
		List<Trajectory> r = new LinkedList<Trajectory>();
		for (Trajectory trajectory : result.values()) {
			r.add(trajectory);
		}
		out.writeObject(r);
		out.close();
		System.out.println(allTrajectories.size());
	}

	/**
	 * 19/07/2010 Trujillo Comment Este metodo coge los valores de las
	 * trajectorias en el archivo trujilloTest.txt el cual esta en el formato de
	 * la aplicacion que genera datos sinteticos. entonces lo leemos y guardamos
	 * en un fichero syntetic_allTrajectories.obj un estructura de datos que es
	 * Hashtable<String, TreeMap<Long, GPSFormat>>, donde el key es el id de la
	 * trajectoria
	 */
	public static void buildingAllTrajectories(String file, String preffix)
			throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String[] split;
		Hashtable<String, Trajectory> result = new Hashtable<String, Trajectory>();
		Hashtable<String, TreeMap<Long, GPSFormat>> allTrajectories = new Hashtable<String, TreeMap<Long, GPSFormat>>();
		TreeMap<Long, GPSFormat> tmp = null;
		SimpleFormat gps;
		for (String line = reader.readLine(); line != null; line = reader
				.readLine()) {
			split = line.split("\t");
			if (allTrajectories.containsKey(split[1])) {
				tmp = allTrajectories.get(split[1]);
			} else {
				tmp = new TreeMap<Long, GPSFormat>();
			}
			gps = new SimpleFormat(Long.parseLong(split[4]),
					Double.parseDouble(split[5]), Double.parseDouble(split[6]));
			tmp.put(Long.parseLong(split[4]), gps);
			allTrajectories.put(split[1], tmp);
		}
		List<Trajectory> result2 = new LinkedList<Trajectory>();
		for (String id : allTrajectories.keySet()) {
			Trajectory tmp2 = new SimpleTrajectory(id, allTrajectories.get(id));
			// System.out.println(tmp2);
			result.put(tmp2.getIdentifier(), tmp2);
			/*
			 * if (Integer.parseInt(tmp2.getIdentifier()) %
			 * (allTrajectories.size()+1) == 0) throw new
			 * RuntimeException(Integer.parseInt(tmp2.getIdentifier()) +"%"+
			 * allTrajectories.size());
			 */
			result2.add(tmp2);
		}
		// hasta aqui ya tengo la lista de trajectorias
		File f = new File("./" + preffix + "/" + preffix
				+ "_allTrajectories.obj");
		if (!f.exists())
			f.createNewFile();
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
		out.writeObject(result);
		out.close();

		f = new File("./" + preffix + "/" + preffix
				+ "_allTrajectoriesList.obj");
		if (!f.exists())
			f.createNewFile();
		out = new ObjectOutputStream(new FileOutputStream(f));
		out.writeObject(result2);
		out.close();
		System.out.println(allTrajectories.size());
	}

	/**
	 * 19/07/2010 Trujillo Comment Este metodo coge los valores de las
	 * trajectorias en la carpeta "file". Alli estan muchos archivos .txt que
	 * comienzan con _new. El formato que tienen estos archivos es [latitude,
	 * longitude, occupancy, time]. El tiempo esta dado en una unidad rara que
	 * no es convetible al Date de java asi que cuidado. Entonces, cada fichero
	 * pertenece a un usuario. A la hora de construir deberias pasar los test de
	 * consistencia que hiciste para los datos reales que anteriormente
	 * guardaste.
	 * 
	 * Otra cosa que hay que destacar es que vamos a hacer un proceso de
	 * analisis de discontinuidad para entonces cortar las trajectorias.
	 */
	public static void buildingAllRealTrajectories(String file, String preffix,
			long discontinuityMeasure) throws IOException {
		File folder = new File(file);
		if (!folder.isDirectory())
			throw new IllegalArgumentException(file + " debe ser una carpeta");
		File[] files = folder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().contains("new_");
			}
		});
		Hashtable<String, Trajectory> allTrajectories = new Hashtable<String, Trajectory>();
		Hashtable<String, Trajectory> trajectoriesToCompare = new Hashtable<String, Trajectory>();
		boolean toCompare = false;
		int cont = 0;
		for (File f : files) {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String[] split;
			Trajectory tmp = new SimpleTrajectory(f.getName() + cont);
			GPSFormat gps;
			boolean first = true;
			if (allTrajectories.size() >= 30000)
				toCompare = true;
			if (toCompare)
				trajectoriesToCompare.put(tmp.getIdentifier(), tmp);
			else
				allTrajectories.put(tmp.getIdentifier(), tmp);
			long lastTime = Long.MAX_VALUE;
			for (String line = reader.readLine(); line != null; line = reader
					.readLine()) {
				split = line.split(" ");
				gps = new SimpleFormat(Long.parseLong(split[3]),
						Double.parseDouble(split[0]),
						Double.parseDouble(split[1]));
				if (!first
						&& Math.abs(gps.getTime() - lastTime) > discontinuityMeasure) {
					// en este caso ha habido una discontinuidad.
					// si la trajectoria anterior esta vacia la borramos
					if (tmp.isEmpty()) {
						if (toCompare)
							trajectoriesToCompare.remove(tmp.getIdentifier());
						else
							allTrajectories.remove(tmp.getIdentifier());
					}
					cont++;
					tmp = new SimpleTrajectory(f.getName() + cont);
					if (toCompare)
						trajectoriesToCompare.put(tmp.getIdentifier(), tmp);
					else
						allTrajectories.put(tmp.getIdentifier(), tmp);
					// System.out.println("Discontinuity found for trajectory named "+f.getName());
				}
				first = false;
				if (toCompare)
					trajectoriesToCompare.get(tmp.getIdentifier())
							.addPoint(gps);
				else
					allTrajectories.get(tmp.getIdentifier()).addPoint(gps);
				lastTime = gps.getTime();
			}
			// System.out.println("Trajectories = "+allTrajectories.size());
			// System.out.println("File named "+f.getName()+" done!!!");
		}
		System.out.println("Number of original files " + files.length);
		System.out.println("Number of discontinuity " + cont);
		System.out.println("Number of discontinuity per file "
				+ ((double) cont) / files.length);
		System.out
				.println("number of trajectories = " + allTrajectories.size());
		System.out.println("number of trajectories to Compare = "
				+ trajectoriesToCompare.size());

		// hasta aqui ya tengo la lista de trajectorias
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
				file + "/" + preffix + "_allTrajectories.obj"));
		out.writeObject(allTrajectories);
		out.close();

		List<Trajectory> toCompareList = new LinkedList<Trajectory>();
		for (Trajectory t : trajectoriesToCompare.values()) {
			toCompareList.add(t);
		}
		out = new ObjectOutputStream(new FileOutputStream(file + "/" + preffix
				+ "_toCompare.obj"));
		out.writeObject(toCompareList);
		out.close();
		List<Trajectory> trajectoriesList = new LinkedList<Trajectory>();
		for (Trajectory t : allTrajectories.values()) {
			trajectoriesList.add(t);
		}
		out = new ObjectOutputStream(new FileOutputStream(file + "/" + preffix
				+ "_allTrajectoriesList.obj"));
		out.writeObject(trajectoriesList);
		out.close();
	}

	public static void buildingAllRealTrajectories(String file, String preffix,
			long discontinuityMeasure, double intervalParam, int clones) throws IOException {
		File folder = new File(file);
		if (!folder.isDirectory())
			throw new IllegalArgumentException(file + " debe ser una carpeta");
		File[] files = folder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().contains("new_");
			}
		});
		Hashtable<String, Trajectory> allTrajectories = new Hashtable<String, Trajectory>();
		Hashtable<String, Trajectory> trajectoriesToCompare = new Hashtable<String, Trajectory>();
		boolean toCompare = false;
		int cont = 0;
		for (File f : files) {
			if (allTrajectories.size() > 10000) break;
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String[] split;
			List<Trajectory> tmp = new LinkedList<Trajectory>();
			for (int i = 0; i < clones; i++) {
				tmp.add(new SimpleTrajectory(f.getName() + cont+"_clones_"+i));
			}
			GPSFormat gps;
			boolean first = true;
			if (allTrajectories.size() > trajectoriesToCompare.size())
				toCompare = true;
			else
				toCompare = false;
			if (toCompare){
				for (Trajectory trajectory : tmp) {
					trajectoriesToCompare.put(trajectory.getIdentifier(), trajectory);
				}
			}
			else{
				for (Trajectory trajectory : tmp) {
					allTrajectories.put(trajectory.getIdentifier(), trajectory);
				}
			}
			long lastTime = Long.MAX_VALUE;
			for (String line = reader.readLine(); line != null; line = reader
					.readLine()) {
				split = line.split(" ");
				gps = new SimpleFormat(Long.parseLong(split[3]),
						Double.parseDouble(split[0]),
						Double.parseDouble(split[1]));
				if (!first
						&& Math.abs(gps.getTime() - lastTime) > discontinuityMeasure) {
					// en este caso ha habido una discontinuidad.
					// si la trajectoria anterior esta vacia la borramos
					if (tmp.isEmpty()) {
						if (toCompare){
							for (Trajectory trajectory : tmp) {
								trajectoriesToCompare.remove(trajectory.getIdentifier());
							}
						}
						else{
							for (Trajectory trajectory : tmp) {
								allTrajectories.remove(trajectory.getIdentifier());
							}
						}
					}
					cont++;
					tmp = new LinkedList<Trajectory>();
					for (int i = 0; i < clones; i++) {
						tmp.add(new SimpleTrajectory(f.getName() + cont+"_clones_"+i));
					}
					if (toCompare){
						for (Trajectory trajectory : tmp) {
							trajectoriesToCompare.put(trajectory.getIdentifier(), trajectory);
						}
					}
					else{
						for (Trajectory trajectory : tmp) {
							allTrajectories.put(trajectory.getIdentifier(), trajectory);
						}
					}
					// System.out.println("Discontinuity found for trajectory named "+f.getName());
				}
				first = false;
				if (toCompare){
					for (Trajectory trajectory : tmp) {
						trajectoriesToCompare.get(trajectory.getIdentifier()).addPoint(gps);
					}
				}
				else{
					for (Trajectory trajectory : tmp) {
						allTrajectories.get(trajectory.getIdentifier()).addPoint(gps);
					}
				}
				lastTime = gps.getTime();
			}
			// System.out.println("Trajectories = "+allTrajectories.size());
			// System.out.println("File named "+f.getName()+" done!!!");
			reader.close();
		}

		double mean = Statistics.averageLocations(allTrajectories);
		double minimum = mean * intervalParam;
		double maximum = mean / intervalParam;

		System.out.println("Total number of trajectories to keep = "
				+ allTrajectories.size());
		int removedTrajectories = 0;
		List<String> toRemove = new LinkedList<String>();

		for (String id : allTrajectories.keySet()) {
			Trajectory tmp = allTrajectories.get(id);
			if (tmp.size() < minimum || tmp.size() > maximum) {
				toRemove.add(id);
			}
		}

		for (String id : toRemove) {
			allTrajectories.remove(id);
			removedTrajectories++;
		}
		System.out.println("number of trajectories after removing = "
				+ allTrajectories.size());

		System.out
				.println("Total number of trajectories to compare before removing = "
						+ trajectoriesToCompare.size());
		removedTrajectories = 0;
		toRemove = new LinkedList<String>();

		mean = Statistics.averageLocations(trajectoriesToCompare);
		minimum = mean * intervalParam;
		maximum = mean / intervalParam;
		for (String id : trajectoriesToCompare.keySet()) {
			Trajectory tmp = trajectoriesToCompare.get(id);
			if (tmp.size() < minimum || tmp.size() > maximum) {
				toRemove.add(id);
			}
		}

		for (String id : toRemove) {
			trajectoriesToCompare.remove(id);
			removedTrajectories++;
		}
		System.out
				.println("number of trajectories to Compare after removing = "
						+ trajectoriesToCompare.size());

		System.out.println("Number of original files " + files.length);
		System.out.println("Number of discontinuity " + cont);
		System.out.println("Number of discontinuity per file "
				+ ((double) cont) / files.length);

		// hasta aqui ya tengo la lista de trajectorias
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
				file + "/" + preffix + "_allTrajectories.obj"));
		out.writeObject(allTrajectories);
		out.close();

		List<Trajectory> toCompareList = new LinkedList<Trajectory>();
		for (Trajectory t : trajectoriesToCompare.values()) {
			toCompareList.add(t);
		}
		out = new ObjectOutputStream(new FileOutputStream(file + "/" + preffix
				+ "_toCompare.obj"));
		out.writeObject(toCompareList);
		out.close();
		List<Trajectory> trajectoriesList = new LinkedList<Trajectory>();
		for (Trajectory t : allTrajectories.values()) {
			trajectoriesList.add(t);
		}
		out = new ObjectOutputStream(new FileOutputStream(file + "/" + preffix
				+ "_allTrajectoriesList.obj"));
		out.writeObject(trajectoriesList);
		out.close();
	}

	public static void buildingAllSynteticTrajectories() throws IOException {
		Hashtable<String, Trajectory> allTrajectories = new Hashtable<String, Trajectory>();
		Hashtable<String, Trajectory> trajectoriesToCompare = new Hashtable<String, Trajectory>();
		allTrajectories = MyTrajectoryFormat.loadTrajecotries("syntetic");
		for (Trajectory t : allTrajectories.values()) {
			trajectoriesToCompare.put(t.getIdentifier(), (Trajectory)t.clone());
		}
		
		System.out.println("Total number of trajectories to keep = "
				+ allTrajectories.size());
		System.out.println("number of trajectories after removing = "
				+ allTrajectories.size());

		System.out
				.println("Total number of trajectories to compare before removing = "
						+ trajectoriesToCompare.size());
		System.out
				.println("number of trajectories to Compare after removing = "
						+ trajectoriesToCompare.size());
		
		// hasta aqui ya tengo la lista de trajectorias
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
				"syntetic/syntetic_allTrajectories.obj"));
		out.writeObject(allTrajectories);
		out.close();

		List<Trajectory> toCompareList = new LinkedList<Trajectory>();
		for (Trajectory t : trajectoriesToCompare.values()) {
			toCompareList.add(t);
		}
		out = new ObjectOutputStream(new FileOutputStream("syntetic/syntetic_toCompare.obj"));
		out.writeObject(toCompareList);
		out.close();
		List<Trajectory> trajectoriesList = new LinkedList<Trajectory>();
		for (Trajectory t : allTrajectories.values()) {
			trajectoriesList.add(t);
		}
		out = new ObjectOutputStream(new FileOutputStream("syntetic/syntetic_allTrajectoriesList.obj"));
		out.writeObject(trajectoriesList);
		out.close();
	}

	public static void saveCompressedTrajectories3D(String preffix,
			double threshold) throws IOException {
		List<Trajectory> allTrajectoriesInList = MyTrajectoryFormat
				.loadTrajecotriesInList(preffix);
		allTrajectoriesInList = BasicCompression.compressGPS(
				allTrajectoriesInList, threshold, new GPSDistance());
		Hashtable<String, Trajectory> allTrajectories = new Hashtable<String, Trajectory>();
		for (Trajectory trajectory : allTrajectoriesInList) {
			allTrajectories.put(trajectory.getIdentifier(), trajectory);
		}
		// hasta aqui ya tengo la lista de trajectorias
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
				preffix + "/" + preffix + "_allTrajectories_compressed.obj"));
		out.writeObject(allTrajectories);
		out.close();

		out = new ObjectOutputStream(new FileOutputStream(preffix + "/"
				+ preffix + "_allTrajectoriesList_compressed.obj"));
		out.writeObject(allTrajectoriesInList);
		out.close();
	}

	public static void saveCompressedTrajectories2D(String preffix,
			double threshold) throws IOException {
		List<Trajectory> allTrajectoriesInList = MyTrajectoryFormat
				.loadTrajecotriesInList(preffix);
		allTrajectoriesInList = BasicCompression.compress(
				allTrajectoriesInList, threshold, new EuclideanDistance());
		Hashtable<String, Trajectory> allTrajectories = new Hashtable<String, Trajectory>();
		for (Trajectory trajectory : allTrajectoriesInList) {
			allTrajectories.put(trajectory.getIdentifier(), trajectory);
		}
		// hasta aqui ya tengo la lista de trajectorias
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
				preffix + "/" + preffix + "_allTrajectories_compressed.obj"));
		out.writeObject(allTrajectories);
		out.close();

		out = new ObjectOutputStream(new FileOutputStream(preffix + "/"
				+ preffix + "_allTrajectoriesList_compressed.obj"));
		out.writeObject(allTrajectoriesInList);
		out.close();
	}

	public static Hashtable<String, Trajectory> buildingAllRealTrajectories(
			String file, String preffix, long discontinuityMeasure, long ini,
			long end) throws IOException {
		File folder = new File(file);
		if (!folder.isDirectory())
			throw new IllegalArgumentException(file + " debe ser una carpeta");
		File[] files = folder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().contains("new_");
			}
		});
		Hashtable<String, Trajectory> allTrajectories = new Hashtable<String, Trajectory>();
		int cont = 0;
		for (File f : files) {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String[] split;
			Trajectory tmp = new SimpleTrajectory(f.getName() + cont);
			SimpleFormat gps;
			boolean first = true;
			allTrajectories.put(tmp.getIdentifier(), tmp);
			long lastTime = Long.MAX_VALUE;
			for (String line = reader.readLine(); line != null; line = reader
					.readLine()) {
				split = line.split(" ");
				gps = new SimpleFormat(Long.parseLong(split[3]),
						Double.parseDouble(split[0]),
						Double.parseDouble(split[1]));
				if (gps.getTime() < ini || gps.getTime() > end) {
					// aqui hay que seguir hasta la proxima linea
					continue;
				}
				if (!first
						&& Math.abs(gps.getTime() - lastTime) > discontinuityMeasure) {
					// en este caso ha habido una discontinuidad.
					// si la trajectoria anterior esta vacia la borramos
					if (tmp.isEmpty())
						allTrajectories.remove(f.getName() + cont);
					cont++;
					tmp = new SimpleTrajectory(f.getName() + cont);
					allTrajectories.put(tmp.getIdentifier(), tmp);
					// System.out.println("Discontinuity found for trajectory named "+f.getName());
				}
				first = false;
				allTrajectories.get(tmp.getIdentifier()).addPoint(gps);
				lastTime = gps.getTime();
			}
			reader.close();
			if (tmp.isEmpty())
				allTrajectories.remove(f.getName() + cont);
			// System.out.println("File named "+f.getName()+" done!!!");
		}
		System.out.println("Number of original files/trajectories "
				+ files.length);
		System.out.println("Number of discontinuity " + cont);
		System.out.println("Number of discontinuity per file "
				+ ((double) cont) / files.length);
		System.out
				.println("number of trajectories = " + allTrajectories.size());

		// hasta aqui ya tengo la lista de trajectorias
		/*
		 * ObjectOutputStream out = new ObjectOutputStream(new
		 * FileOutputStream(file+"/"+preffix+"_allTrajectories.obj"));
		 * out.writeObject(allTrajectories); out.close();
		 */
		return allTrajectories;
	}

	public static void buildingReal() throws IOException {
		
		//buildingAllRealTrajectories("real", "real", 300, 0.95);
		//saveCompressedTrajectories3D("real", 1.5d);
		
		buildingAllRealTrajectories("real", "real", 300, 0.9d, 3);
		saveCompressedTrajectories3D("real", 0.0d);

		List<Trajectory> original = MyTrajectoryFormat
				.loadTrajecotriesInList("real");
		GPSDistance distance = new GPSDistance();
		double averageLenght = Statistics.averageLenght(original, distance);
		double averageLocations = Statistics.averageLocations(original);
		System.out.println("average locations of original = "
				+ averageLocations);

		List<Trajectory> compressed = MyTrajectoryFormat
				.loadCompressedTrajecotriesInList("real");
		double averageLenghtCompressed = Statistics.averageLenght(compressed,
				distance);
		double averageLocationsCompressed = Statistics
				.averageLocations(compressed);
		System.out.println("average locations of compressed = "
				+ averageLocationsCompressed);
		double minimumLocationsCompressed = Statistics
				.minimumNumberOfLocations(compressed);
		System.out.println("minimum locations of compressed = "
				+ minimumLocationsCompressed);
		double maximumLocationsCompressed = Statistics
				.maximumNumberOfLocations(compressed);
		System.out.println("maximum locations of compressed = "
				+ maximumLocationsCompressed);

		double compressionRatio = averageLocationsCompressed / averageLocations;
		double compressionLost = averageLenghtCompressed / averageLenght;

		System.out.println("Compression ratio = " + compressionRatio);
		System.out.println("Compression lost = " + compressionLost);
		
		System.out.println("josep");
		saveTrajectoriesInClusters("real", new JosepGPSDistanceOnTheFlyVs2(), "josep");
		System.out.println("generalization");
		saveTrajectoriesInClusters("real", new LogCostDistance(), "generalization");
		System.out.println("myone");
		saveTrajectoriesInClusters("real", new MyDistanceGPSBased(3), "myone");
		System.out.println("frechet");
		saveTrajectoriesInClusters("real", new FrechetDistanceGPSBased(), "frechet");
		// System.out.println("average = "+averageLocationsPerTrajectory("real",
		// "real_allTrajectories.obj"));
		// findAndRemoveIncosistencies("real/real_allTrajectories.obj", new
		// GPSDistance());
	}

	private static void saveTrajectoriesInClusters(String preffix,
			Distance distance, String folder) throws FileNotFoundException, IOException {
		Hashtable<String, Trajectory> trajectories = MyTrajectoryFormat.loadCompressedTrajecotries(preffix);
		//Trajectory average = Util.averageTrajectory(trajectories, "average");
		Trajectory random = Util.getRandomTrajectory(trajectories);
		TreeMap<Double, List<Trajectory>> distances = new TreeMap<Double, List<Trajectory>>();
		List<Trajectory> tmp;
		File f = new File(preffix + "/" + folder);
		f.mkdir();
		File[] filesIn = f.listFiles();
		for (int i = 0; i < filesIn.length; i++) {
			filesIn[i].delete();
		}
		for (Trajectory t : trajectories.values()) {
			double d = distance.distance(random, t);
			tmp = distances.get(d);
			if (tmp == null){
				tmp = new LinkedList<Trajectory>();
			}
			tmp.add(t);
			distances.put(d, tmp);
		}
		int cont = 0;
		int k = 1;
		List<Trajectory> toBeStoraged = new LinkedList<Trajectory>();
		for (List<Trajectory> clusters : distances.values()){
			for (Trajectory trajectory : clusters) {
				toBeStoraged.add(trajectory);
				cont++;
			}
			if (cont > MAXIMUM_CLUSTER_SIZE){
				// hasta aqui ya tengo la lista de trajectorias
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
						preffix + "/" + folder+ "/"+preffix + "_allTrajectories_"+k+".obj"));
				out.writeObject(toBeStoraged);
				out.close();
				toBeStoraged = new LinkedList<Trajectory>();
				cont = 0;
				k++;
			}
		}
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
				preffix + "/" + folder+ "/"+preffix + "_allTrajectories_"+k+".obj"));
		out.writeObject(toBeStoraged);
		out.close();
	}

	public static void buildingToy() throws IOException {
		
		buildingAllRealTrajectories("toy", "toy", Long.MAX_VALUE, 0d, 0);
		saveCompressedTrajectories3D("toy", 0d);

		List<Trajectory> original = MyTrajectoryFormat
				.loadTrajecotriesInList("toy");
		GPSDistance distance = new GPSDistance();
		double averageLenght = Statistics.averageLenght(original, distance);
		double averageLocations = Statistics.averageLocations(original);
		System.out.println("average locations of original = "
				+ averageLocations);

		List<Trajectory> compressed = MyTrajectoryFormat
				.loadCompressedTrajecotriesInList("toy");
		double averageLenghtCompressed = Statistics.averageLenght(compressed,
				distance);
		double averageLocationsCompressed = Statistics
				.averageLocations(compressed);
		System.out.println("average locations of compressed = "
				+ averageLocationsCompressed);

		double compressionRatio = averageLocationsCompressed / averageLocations;
		double compressionLost = averageLenghtCompressed / averageLenght;

		System.out.println("Compression ratio = " + compressionRatio);
		System.out.println("Compression lost = " + compressionLost);
		// System.out.println("average = "+averageLocationsPerTrajectory("real",
		// "real_allTrajectories.obj"));
		// findAndRemoveIncosistencies("real/real_allTrajectories.obj", new
		// GPSDistance());
	}

	public static void buildingSyntetic() throws IOException {

		buildingAllTrajectories("syntetic.txt", "syntetic");
		buildingAllSynteticTrajectories();
		saveCompressedTrajectories2D("syntetic", 0.0d);

		List<Trajectory> original = MyTrajectoryFormat
				.loadTrajecotriesInList("syntetic");
		EuclideanDistance distance = new EuclideanDistance();
		double averageLenght = Statistics.averageLenght(original, distance);
		double averageLocations = Statistics.averageLocations(original);
		System.out.println("average locations of original = "
				+ averageLocations);

		List<Trajectory> compressed = MyTrajectoryFormat
				.loadCompressedTrajecotriesInList("syntetic");
		double averageLenghtCompressed = Statistics.averageLenght(compressed,
				distance);
		double averageLocationsCompressed = Statistics
				.averageLocations(compressed);
		System.out.println("average locations of compressed = "
				+ averageLocationsCompressed);

		double compressionRatio = averageLocationsCompressed / averageLocations;
		double compressionLost = averageLenghtCompressed / averageLenght;

		System.out.println("Compression ratio = " + compressionRatio);
		System.out.println("Compression lost = " + compressionLost);
		// System.out.println("average = "+averageLocationsPerTrajectory("real",
		// "real_allTrajectories.obj"));
		// findAndRemoveIncosistencies("real/real_allTrajectories.obj", new
		// GPSDistance());
		System.out.println("josep");
		saveTrajectoriesInClusters("syntetic", new JosepEuclideanDistanceOnTheFlyVs2(), "josep");
		System.out.println("generalization");
		saveTrajectoriesInClusters("syntetic", new LogCostDistance(), "generalization");
		System.out.println("myone");
		saveTrajectoriesInClusters("syntetic", new MyDistanceEuclideanBased(3), "myone");
		System.out.println("frechet");
		saveTrajectoriesInClusters("syntetic", new FrechetDistanceEuclideanBased(), "frechet");
	}

	public static void main(String[] args) throws IOException,
			ClassNotFoundException {
		buildingSyntetic();
		//buildingReal();
		//buildingToy();
	}

	/**
	 * 29/12/2010 Trujillo Comment Aqui buscamos inconsistencia en as
	 * trajectorias tales como recorrer mucho kilometroa en poco tiempo,
	 * asumiendo una velocidad maxima de 240 km por hora
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void findAndRemoveIncosistencies(String fileInput,
			Distance dist) throws IOException, ClassNotFoundException {
		File file = new File(fileInput);
		ObjectInputStream input = new ObjectInputStream(new FileInputStream(
				file));
		Hashtable<String, Trajectory> trajectories = (Hashtable<String, Trajectory>) input
				.readObject();
		input.close();
		Trajectory tmp;
		int cont = 1;
		long lastTime = -1;
		GPSFormat last;
		GPSFormat current;
		List<String> toRemove = new LinkedList<String>();
		for (String key : trajectories.keySet()) {
			tmp = trajectories.get(key);
			if (tmp.size() == 1) {
				toRemove.add(key);
				continue;
			}
			last = null;
			for (long time : tmp.times()) {
				current = tmp.getPoint(time);
				if (last == null) {
					last = current;
					lastTime = time;
					continue;
				}
				double distance = dist.distance(last.getLatitude(),
						last.getLongitude(), current.getLatitude(),
						current.getLongitude());
				double maxDistance = ((double) (time - lastTime) * 480) / 3600;// distance
																				// equation
				lastTime = time;
				last = current;
				if (12 < distance) {
					System.out.println("There was an incosistency in " + key
							+ " we found a distance of " + distance
							+ "where the maximun should be of " + maxDistance);
					toRemove.add(key);
					break;
				}
			}
		}
		System.out.println("Removing " + toRemove.size()
				+ " trajectories from " + trajectories.size());
		for (String t : toRemove) {
			trajectories.remove(t);
		}
		ObjectOutputStream output = new ObjectOutputStream(
				new FileOutputStream(file));
		output.writeObject(trajectories);
		output.close();
	}

	public static void generateGPXFiles(String preffix, String fileName,
			String outputFile) throws FileNotFoundException, IOException,
			ClassNotFoundException {
		File file = new File(preffix + "/" + fileName);
		ObjectInputStream input = new ObjectInputStream(new FileInputStream(
				file));
		Hashtable<String, TreeMap<Long, GPSFormat>> trajectories = (Hashtable<String, TreeMap<Long, GPSFormat>>) input
				.readObject();
		input.close();
		TreeMap<Long, GPSFormat> tmp;
		int cont = 1;
		for (String key : trajectories.keySet()) {
			tmp = trajectories.get(key);
			GPXParser.buildGpxFileWithUnixEpochTime(tmp, "./" + outputFile
					+ "/" + key + ".gpx", cont++);
		}
	}

	/**
	 * 22/12/2010 Trujillo Comment Aqui lo que vamos a buscar es un intervalo de
	 * tiempo donde se cumplan ciertas condiciones que se pidan
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private static void findindBestSubInterval(int numberOfTrajectories,
			int avgLocations, String preffix) throws IOException,
			ClassNotFoundException {
		long gap = 80;
		Hashtable<String, Trajectory> trajectories;
		int number = 0;
		int locations;
		double min = Double.MAX_VALUE;
		long ini = 0;
		long end = 0;
		double tmpD;
		int step = 86400;// un dia completo
		long lowerTime = -1;
		long higherTime = -1;
		double gloabalMin = Double.MAX_VALUE;
		do {
			buildingAllRealTrajectories("real", "real", gap);
			trajectories = getRealTrajectories("real");
			if (lowerTime == -1) {
				lowerTime = Statistics.findLowerTime(trajectories);
				higherTime = Statistics.findHigherTime(trajectories);
			}
			System.out.println("builded");
			locations = Statistics.averageLocations(trajectories);
			if (locations < avgLocations) {
				gap += 10;
				number = trajectories.size();
				continue;
			}
			min = Double.MAX_VALUE;
			for (int i = 1; lowerTime + i * step <= higherTime; i++) {
				trajectories = buildingAllRealTrajectories("real", "real", gap,
						lowerTime + (i - 1) * step, lowerTime + i * step);
				locations = Statistics.averageLocations(trajectories);
				number = trajectories.size();
				tmpD = Math.abs(number - numberOfTrajectories)
						/ numberOfTrajectories
						+ Math.abs(locations - avgLocations) / avgLocations;
				if (tmpD < min) {
					min = tmpD;
					ini = lowerTime + (i - 1) * step;
					end = lowerTime + i * step;
				}
			}
			// ya en este paso tengo en ini y end lo mejorsillo, asi que lo
			// busco y lo guardo
			// en fichero junto con la informacion en pantalla
			trajectories = buildingAllRealTrajectories("real", "real", gap,
					ini, end);
			ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream(preffix + "/" + preffix
							+ "_bestTrajectories_" + gap + ".obj"));
			out.writeObject(trajectories);
			out.close();
			locations = Statistics.averageLocations(trajectories);
			number = trajectories.size();
			System.out.println("The best for gap = " + gap + " has " + number
					+ " trajectories and " + locations
					+ " locations per trajectory");
			gap += 10;
			number = trajectories.size();
		} while (number >= numberOfTrajectories || gloabalMin > min);
	}

	private static Hashtable<String, Trajectory> getRealTrajectories(
			String preffix) throws FileNotFoundException, IOException,
			ClassNotFoundException {
		String fileName = preffix + "/" + preffix + "_allTrajectories.obj";
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(
				new File(fileName)));
		Hashtable<String, Trajectory> result = (Hashtable<String, Trajectory>) in
				.readObject();
		in.close();
		return result;
	}

	private static Hashtable<String, TreeMap<Long, GPSFormat>> getTrajectories(
			Hashtable<String, TreeMap<Long, GPSFormat>> trajectories, long ini,
			long end) {
		Hashtable<String, TreeMap<Long, GPSFormat>> result = new Hashtable<String, TreeMap<Long, GPSFormat>>();
		TreeMap<Long, GPSFormat> t;
		TreeMap<Long, GPSFormat> tmp;
		GPSFormat gps;
		for (String id : trajectories.keySet()) {
			t = trajectories.get(id);
			tmp = new TreeMap<Long, GPSFormat>();
			for (long time : t.keySet()) {
				if (time < ini || time > end)
					continue;
				gps = t.get(time);
				tmp.put(time, gps);
			}
			if (tmp.size() > 0)
				result.put(id, tmp);
		}
		return result;
	}

	public static double averageLocationsPerTrajectory(String preffix,
			String fileName) throws FileNotFoundException, IOException,
			ClassNotFoundException {
		File file = new File(preffix + "/" + fileName);
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		Hashtable<String, Trajectory> trajectories = (Hashtable<String, Trajectory>) in
				.readObject();
		double average = 0;
		int cont = 0;
		for (Trajectory trajectory : trajectories.values()) {
			average += trajectory.size();
			cont++;
		}
		System.out.println("trajectories = " + cont);
		return average / cont;
	}

	/**
	 * 20/12/2010 Trujillo Comment Aqui vamos leyendo las trajectorias,
	 * guardamos todos los puntos de tiempo en los que esta definidos estas
	 * trajectorias. Para cada punto de tiempo y para cada trajectoria
	 * verificamos si este punto de tiempo esta dentro de la trajectoria pero no
	 * contenido en ella, en cuyo caso entonces lo fijamos dentro de la
	 * trajectoria.
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ClassNotFoundException
	 */
	public static void synchronizeTrajectories(String preffix, String fileName)
			throws FileNotFoundException, IOException, ClassNotFoundException {
		File file = new File(preffix + "/" + fileName);
		ObjectInputStream input = new ObjectInputStream(new FileInputStream(
				file));
		Hashtable<String, TreeMap<Long, GPSFormat>> trajectories = (Hashtable<String, TreeMap<Long, GPSFormat>>) input
				.readObject();
		input.close();
		Long end;
		Long ini;
		long offset;
		long distance;
		TreeSet<Long> dates = new TreeSet<Long>();
		for (TreeMap<Long, GPSFormat> t : trajectories.values()) {
			for (Long time : t.keySet())
				dates.add(time);
		}
		System.out.println("There are " + dates.size() + " time stamps");
		TreeMap<Long, GPSFormat> tmp;
		int cont = 0;
		int c;
		for (String tId : trajectories.keySet()) {
			tmp = trajectories.get(tId);
			System.out.println("Analyzing trajectory with " + tmp.size()
					+ " time stamps");
			ini = tmp.firstKey();
			end = tmp.lastKey();
			c = 0;
			for (Long time : dates) {
				if (time < ini || time > end)
					continue;
				if (tmp.containsKey(time))
					continue;
				SortedMap<Long, GPSFormat> tail = tmp.tailMap(time);
				SortedMap<Long, GPSFormat> head = tmp.headMap(time);
				GPSFormat p2 = tail.get(tail.firstKey());
				GPSFormat p1 = head.get(head.lastKey());
				// entonces ahora debemos interpolar
				GPSFormat newPoint = interpolate(p1, p2, time);
				tmp.put(time, newPoint);
				c++;
			}
			System.out.println("Analysis finished with " + (c)
					+ " new time stamps");
		}
		ObjectOutputStream output = new ObjectOutputStream(
				new FileOutputStream(preffix + "/" + preffix
						+ "_allTrajectories_sync.obj"));
		output.writeObject(trajectories);
	}

	/**
	 * 30/12/2010 Trujillo Comment La diferencia con synchronizeTrajectories es
	 * que ahora en vez de recolectar los time-stamps desde el primer momento,
	 * se van recolectando a medida que se analizan las trajectorias.
	 */
	public static void synchronizeTrajectories3(String preffix, String fileName)
			throws FileNotFoundException, IOException, ClassNotFoundException {
		File file = new File(preffix + "/" + fileName);
		ObjectInputStream input = new ObjectInputStream(new FileInputStream(
				file));
		Hashtable<String, TreeMap<Long, GPSFormat>> trajectories = (Hashtable<String, TreeMap<Long, GPSFormat>>) input
				.readObject();
		input.close();
		Long end;
		Long ini;
		TreeMap<Long, GPSFormat> tmp;
		TreeMap<Long, GPSFormat> tmp1;
		int c;
		/**
		 * 30/12/2010 Trujillo Comment Primero ordenamos de menor a mayor las
		 * trajectorias segun su longitud
		 */
		String[] sort = new String[trajectories.size()];
		int pos = 0;
		for (String id : trajectories.keySet()) {
			sort[pos++] = id;
		}
		String minS;
		String tmpS;
		for (int i = 0; i < sort.length; i++) {
			minS = sort[i];
			pos = i;
			for (int j = i + 1; j < sort.length; j++) {
				tmpS = sort[j];
				if (trajectories.get(tmpS).size() < trajectories.get(minS)
						.size()) {
					minS = tmpS;
					pos = j;
				}
			}
			sort[pos] = sort[i];
			sort[i] = minS;
		}
		for (int i = 0; i < sort.length; i++) {
			System.out.println(trajectories.get(sort[i]).size());
		}
		List<Long> toRemove = new LinkedList<Long>();
		List<Long> dates = new LinkedList<Long>();
		for (int i = 0; i < sort.length; i++) {
			tmp = trajectories.get(sort[i]);
			// System.out.println("Analyzing trajectory with "+tmp.size()+" time stamps");
			for (Long time : tmp.keySet()) {
				boolean remove = false;
				for (int j = 0; j < i; j++) {
					tmp1 = trajectories.get(sort[j]);
					if (tmp1 == null)
						continue;
					ini = tmp1.firstKey();
					end = tmp1.lastKey();
					if (time <= end && time >= ini)
						remove = true;
					if (tmp1.containsKey(time)) {
						remove = false;
						break;
					}
				}
				if (remove)
					toRemove.add(time);
			}
			for (Long time : toRemove) {
				tmp.remove(time);
			}
			if (tmp.size() == 0) {
				System.out.println("removing trajectory with name " + sort[i]);
				trajectories.remove(sort[i]);
				continue;
			}
			ini = tmp.firstKey();
			end = tmp.lastKey();
			c = 0;
			for (Long time : dates) {
				if (time < ini || time > end)
					continue;
				if (tmp.containsKey(time))
					continue;
				SortedMap<Long, GPSFormat> tail = tmp.tailMap(time);
				SortedMap<Long, GPSFormat> head = tmp.headMap(time);
				GPSFormat p2 = tail.get(tail.firstKey());
				GPSFormat p1 = head.get(head.lastKey());
				// entonces ahora debemos interpolar
				GPSFormat newPoint = interpolate(p1, p2, time);
				tmp.put(time, newPoint);
				c++;
			}
			// System.out.println("Analysis finished with "+(c)+" new time stamps");
			for (Long time : tmp.keySet())
				dates.add(time);
		}
		for (int i = 0; i < sort.length; i++) {
			System.out.println(trajectories.get(sort[i]).size());
		}
		ObjectOutputStream output = new ObjectOutputStream(
				new FileOutputStream(preffix + "/" + preffix
						+ "_allTrajectories_sync.obj"));
		output.writeObject(trajectories);
	}

	/**
	 * 29/12/2010 Trujillo Comment Lo mismo que el de arriba, pero esta vez lo
	 * que vamos a hacer es buscar el valor minimo de los time stamps, entonces
	 * buscar un paso, que puede ser el average, y luego cada punto en la
	 * trajectoria estara definido por ini+k*paso, o sea un multiplo de ese paso
	 * plus el offset que es el inicio
	 */
	public static void synchronizeTrajectories2(String preffix, String fileName)
			throws FileNotFoundException, IOException, ClassNotFoundException {
		int average = 10;
		System.out.println("Average = " + average);
		File file = new File(preffix + "/" + fileName);
		ObjectInputStream input = new ObjectInputStream(new FileInputStream(
				file));
		Hashtable<String, TreeMap<Long, GPSFormat>> trajectories = (Hashtable<String, TreeMap<Long, GPSFormat>>) input
				.readObject();
		input.close();
		long offset = Long.MAX_VALUE;
		for (TreeMap<Long, GPSFormat> t : trajectories.values()) {
			if (t.firstKey() < offset)
				offset = t.firstKey();
		}
		List<Long> dates = new LinkedList<Long>();
		for (TreeMap<Long, GPSFormat> t : trajectories.values()) {
			long min = t.firstKey();
			long max = t.lastKey();
			// System.out.println("max = "+Math.floor((max-offset)/average));
			// System.out.println("min = "+Math.floor((min-offset)/average));
			// System.out.println("Size = "+t.size());
			if (Math.floor((max - offset) / average)
					- Math.floor((min - offset) / average) < t.size()) {
				for (long time : t.keySet()) {
					dates.add(time);
				}
			} else {
				// en este caso añadimos los offset+k*average que se encuentren
				// dentro de los
				// limites permitidos
				for (int k = (int) Math.floor((min - offset) / average); k <= (int) Math
						.floor((max - offset) / average); k++) {
					dates.add(offset + k * average);
				}
			}
		}
		Long end;
		Long ini;
		TreeMap<Long, GPSFormat> tmp;
		for (String tId : trajectories.keySet()) {
			tmp = trajectories.get(tId);
			System.out.println("Analyzing trajectory with " + tmp.size()
					+ " time stamps");
			ini = tmp.firstKey();
			end = tmp.lastKey();
			int c = 0;
			List<Long> toRemove = new LinkedList<Long>();
			for (long time : tmp.keySet()) {
				boolean remove = true;
				for (Long time1 : dates) {
					if (time1 == time)
						remove = false;
				}
				if (remove)
					toRemove.add(time);
			}
			for (Long time : dates) {
				if (time < ini || time > end)
					continue;
				if (tmp.containsKey(time))
					continue;
				SortedMap<Long, GPSFormat> tail = tmp.tailMap(time);
				SortedMap<Long, GPSFormat> head = tmp.headMap(time);
				GPSFormat p2 = tail.get(tail.firstKey());
				GPSFormat p1 = head.get(head.lastKey());
				// entonces ahora debemos interpolar
				GPSFormat newPoint = interpolate(p1, p2, time);
				tmp.put(time, newPoint);
				c++;
			}
			for (Long time : toRemove) {
				tmp.remove(time);
				c--;
			}
			System.out.println("Analysis finished with " + (c)
					+ " new time stamps");
		}
		ObjectOutputStream output = new ObjectOutputStream(
				new FileOutputStream(preffix + "/" + preffix
						+ "_allTrajectories_sync.obj"));
		output.writeObject(trajectories);
	}

	/**
	 * 08/07/2010 Trujillo Comment
	 * */
	public static GPSFormat interpolate(GPSFormat p1, GPSFormat p2,
			long newPoint) {
		double frac = ((double) (newPoint - p1.getTime()))
				/ ((double) (p2.getTime() - p1.getTime()));
		final double newLatitude = p1.getLatitude() + frac
				* (p2.getLatitude() - p1.getLatitude());
		final double newLongitude = p1.getLongitude() + frac
				* (p2.getLongitude() - p1.getLongitude());
		final Long newDate = newPoint;
		return new SimpleFormat(newDate, newLatitude, newLongitude);
	}

}
