package util;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import clustering.Cluster;
import distances.Distance;
import distances.EuclideanDistance;

import trajectory.Trajectory;


public class Tables {

	public static void distanceBetweenTrajectories(String preffix, Distance dist) throws IOException, ClassNotFoundException{
		List<Integer> l = new ArrayList<Integer>();
		l.add(2);
		l.add(4);
		l.add(6);
		l.add(8);
		l.add(10);
		l.add(15);
		for (int i : l){
			distanceBetweenTrajectories(preffix, i, dist);
		}
	}
	
	/** 19/07/2010 Trujillo Comment
	 * Aqui vamos a analizar cada trajectoria con su transformada, y entonces vamos a ver dos cosas:
	 * 1- La distancia punto a punto para ver cuanto como promedio a cambiado cada trajectoria
	 * 2- La diferencia de longitud de una trajectoria respecto a su homologa cambiada.
	 * */
	public static void distanceBetweenTrajectories(String preffix, int i, 
			Distance dist) throws IOException, ClassNotFoundException{
		List<List<Trajectory>> clustersOrig;
		List<List<Trajectory>> clustersNoise;
		List<Trajectory> cluster1;
		List<Trajectory> cluster2;
		Hashtable<String, Double> distance = new Hashtable<String, Double>();
		Hashtable<String, Pair> size = new Hashtable<String, Pair>();
		BufferedWriter w1;
		w1 = new BufferedWriter(new FileWriter("./"+preffix+"/"+"distanceDifference"+i+".DAT"));
		distance = new Hashtable<String, Double>();
		size = new Hashtable<String, Pair>();
		ObjectInputStream in = new ObjectInputStream(new FileInputStream("./"+preffix+"/"+preffix+"_clusters_k_"+i+".obj"));
		clustersOrig = (List<List<Trajectory>>)in.readObject();
		in.close();
		in = new ObjectInputStream(new FileInputStream("./"+preffix+"/"+preffix+"_clusters_with_noise_k_"+i+".obj"));
		clustersNoise = (List<List<Trajectory>>)in.readObject();
		in.close();
		if (clustersNoise.size() != clustersOrig.size())
			throw new RuntimeException("WEhat!!!");
		for (int j = 0; j < clustersNoise.size(); j++){
			cluster1 = clustersNoise.get(j);
			cluster2 = clustersOrig.get(j);
			for (Trajectory t1 : cluster1){
				for (Trajectory t2 : cluster2){
					if (!t1.equals(t2)) continue;
					distance.put(t2.getIdentifier(), t1.distance(t2, dist));
					size.put(t2.getIdentifier(), new Pair(dist.length(t2), dist.length(t1)));
				}
			}
		}
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("./"+preffix+"/"+"distanceDifference"+i+".obj"));
		out.writeObject(distance);
		out.close();
		out = new ObjectOutputStream(new FileOutputStream("./"+preffix+"/"+"lengthDifference"+i+".obj"));
		out.writeObject(size);
		out.close();
	}

	private static Double getLengthDifference(Trajectory t1, Trajectory t2, Distance dist) {
		double size1 = dist.length(t1);
		double size2 = dist.length(t2);
		return size1 - size2;
	}
	
	public static void writeLengthData(String preffix, int k) throws FileNotFoundException, IOException, ClassNotFoundException{
		ObjectInputStream in = new ObjectInputStream(new FileInputStream("./"+preffix+"/"+"lengthDifference"+k+".obj"));
		Hashtable<String, Pair> length = (Hashtable<String, Pair>)in.readObject();
		in.close();
		BufferedWriter writer = new BufferedWriter(new FileWriter("./"+preffix+"/"+"length"+k+".dat"));
		writer.write("- \t - \t Original \t Distorted");
		writer.newLine();
		int cont = 1;
		in = new ObjectInputStream(new FileInputStream("./"+preffix+"/"+preffix+"_clusters_k_"+k+".obj"));
		List<List<Trajectory>> clustersOrig = (List<List<Trajectory>>)in.readObject();
		in.close();
		in = new ObjectInputStream(new FileInputStream("./"+preffix+"/"+preffix+"_clusters_with_noise_k_"+k+".obj"));
		List<List<Trajectory>> clustersNoise = (List<List<Trajectory>>)in.readObject();
		in.close();
		double distance = 0;
		double difference = 0;
		for (int i = 0; i < clustersOrig.size(); i++){
			List<Trajectory> clusterNoise = clustersNoise.get(i); 
			List<Trajectory> clusterOrig = clustersOrig.get(i);
			distance = 0;
			difference = 0;
			for (Trajectory t : clusterOrig){
				Pair p = length.get(t.getIdentifier());
				distance += p.x;
				difference += p.y;
			}
			writer.write((cont++)+" \t "+(distance/clusterOrig.size())+
					" \t "+(difference/clusterOrig.size()));
			writer.newLine();
		}
		writer.close();
	}
	
	public static class Pair implements Serializable{
		double x;
		double y;
		public Pair(double x, double y){
			this.x = x;
			this.y = y;
		}
	}

	/** 21/07/2010 Trujillo Comment
	 * Aqui computamos lo que en el paper le llaman discernibility. 
	 * @throws IOException */
	public static void computeDiscernibility(String preffix, int i,
			List<List<Trajectory>> clusters, int originalSize, int discardedTrajectories) throws IOException {
		double result = 0;
		for (List<Trajectory> cluster : clusters){
			result += Math.pow(cluster.size(), 2);
		}
		result += originalSize*discardedTrajectories;
		BufferedWriter writer = new BufferedWriter(new FileWriter("./"+preffix+"/"+preffix+"_discernibility_"+i+".dat"));
		writer.write(""+result);
		writer.close();
	}

	public static double distortion(String preffix, int i, 
			Distance dist) throws IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream("./"+preffix+"/"+preffix+"_clusters_k_"+i+".obj"));
		List<List<Trajectory>> clustersOrig = (List<List<Trajectory>>)in.readObject();
		in.close();
		in = new ObjectInputStream(new FileInputStream("./"+preffix+"/"+preffix+"_clusters_with_noise_k_"+i+".obj"));
		List<List<Trajectory>> clustersNoise = (List<List<Trajectory>>)in.readObject();
		in.close();
		if (clustersNoise.size() != clustersOrig.size())
			throw new RuntimeException("WEhat!!!");
		double result = 0;
		for (int j = 0; j < clustersNoise.size(); j++){
			List<Trajectory> cluster1 = clustersNoise.get(j);
			List<Trajectory> cluster2 = clustersOrig.get(j);
			for (Trajectory t1 : cluster1){
				for (Trajectory t2 : cluster2){
					if (!t1.equals(t2)) continue;
					for (Long time : t2.times()){
						if (!t1.containsTime(time))
							throw new RuntimeException("What!!");
						result += dist.distance(t2.getPoint(time), t1.getPoint(time));
					}
				}
			}
		}
		BufferedWriter w1 = new BufferedWriter(new FileWriter("./"+preffix+"/"+preffix+"_distortion_"+i+".DAT"));
		w1.write(result+"");
		w1.close();
		return result;
	}

	public static void writePrivacyProbability(String preffix, int pointsTmp, int swappedOptions, int swappedPoints, int i) throws IOException {
		BufferedWriter w1 = new BufferedWriter(new FileWriter("./"+preffix+"/"+preffix+"_privacy_probability_"+i+".dat"));
		w1.write(((double)pointsTmp/(double)swappedOptions)+"");
		w1.newLine();
		w1.write("Percent \t "+((double)100*swappedPoints/(double)pointsTmp));
		w1.newLine();
		w1.write("Indistiguibility per swapped point \t "+((double)swappedPoints/(double)swappedOptions));
		w1.newLine();
		w1.write("Indistiguibility in general\t "+((double)pointsTmp/(double)swappedOptions));
		w1.close();
	}
	
}
