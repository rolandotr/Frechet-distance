package clustering;

import java.awt.Point;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;


import distances.Distance;
import distances.FrechetDistance;
import distances.FrechetDistanceEuclideanBased;
import distances.Transformation;


import trajectory.SimpleTrajectory;
import trajectory.Trajectory;
import util.Timer;
import util.Vertex;
import wrappers.GPSFormat;
import wrappers.PairObject;
import wrappers.SimpleFormat;

public class Cluster {
	
	
	/** 14/07/2010 Trujillo Comment
	 * Ojo que este metodo es de transformacion y consulta, o sea, la lista que se pasa como 
	 * argumento se va a modificar. El que invoca este metodo es el encargado de hacerle una
	 * copia en caso de que sea necesario*/
	public static <X extends Measurable<X>> List<List<X>> greedyClustering(List<X> records, int k,
			Distance dist){
		List<List<X>> result = new ArrayList<List<X>>();
		List<X> c;
		if (records.size() <= k) {
			result.add(records);
			return result;
		}
		X r = records.get((new Random()).nextInt(records.size()));
		while(records.size() >= k){
			r = findFurthestRecordFrom(r, records, dist);
			records.remove(r);
			c = new ArrayList<X>();
			c.add(r);
			while(c.size() < k){				
				r = findBestRecord(records,c, dist);
				records.remove(r);
				c.add(r);
			}
			result.add(c);
		}
		while (records.size() > 0){
			r = records.get((new Random()).nextInt(records.size()));
			records.remove(r);
			c = findBestCluster(result, r, dist);
			c.add(r);
		}
		return result;
	}
	
	public static <X extends Measurable<X>> X findFurthestRecordFrom(X r, List<X> records, Distance dist){
		double max = -1;
		double tmp;
		X result = null;
		for (X record : records){
			tmp = r.distance(record, dist);
			if (tmp > max){
				max = tmp;
				result = record;
			}
		}
		return result;
	}
	
	public static <X extends Measurable<X>> X findBestRecord(List<X> s, List<X> c, Distance dist){
		double min = Double.MAX_VALUE;
		X best = null;
		double diff;
		for (X r : s){
			diff = informationLostWithElement(r, c, dist);
			if (diff < min){
				min = diff;
				best = r;
			}
		}
		return best;
	}

	public static <X extends Measurable<X>> double informationLost(List<X> c, Distance dist){
		double sum = 0;
		for (X r1 : c){
			for (X r2 : c){
				sum += r1.distance(r2, dist);
			}
		}
		return sum/(2*c.size());
	}
	
	public static <X extends Measurable<X>> double informationLostWithElement(X r, List<X> c, 
			Distance dist){
		double i1 = informationLost(c, dist); 
		c.add(r);
		double i2 = informationLost(c, dist);
		c.remove(r);
		return i2-i1;
	}
	
	public static <X extends Measurable<X>> List<X> findBestCluster(List<List<X>> clusters, X r, 
			Distance dist){
		List<X> best = null;
		double min = Double.MAX_VALUE;
		double diff;
		for (List<X> c : clusters){
			diff = informationLostWithElement(r, c, dist);
			if (diff < min){
				min = diff;
				best = c;
			}
		}
		return best;
	}
	
	//he comentado esto para hacer algo mas eficiente
	public static List<Trajectory> findBestCluster6(List<Trajectory> trajectories,
			 int k, Distance dist) {
		if (k < 2) return null;
		if (trajectories.size() < k) return null;
		List<Trajectory> c = new ArrayList<Trajectory>();
		double min = Double.MAX_VALUE;
		double distance;
		Trajectory centroide = null;
		Random r = new Random();
		centroide = trajectories.get(r.nextInt(trajectories.size()));
		TreeMap<Double, Trajectory> distances = new TreeMap<Double, Trajectory>();
		distances.put(0d, centroide);
		double max = 0;
		Entry<Double, Trajectory> entry;
		Trajectory maxTrajectory = null;
		for (Trajectory t1 : trajectories){
			if (t1.equals(centroide)) continue;
			distance = dist.distance(t1, centroide);
			//if no esta lleno pngo lo que sea
			entry = distances.lastEntry();
			if (c.size() < k) {
				distances.put(distance, t1);
				maxTrajectory = entry.getValue();
				max = entry.getKey();
				c.add(t1);
			}
			else if (entry.getKey() < max){
				//en este caso sacamos el max y ponemos este nuevo
				distances.remove(max);
				distances.put(distance, t1);
				c.remove(maxTrajectory);
				entry = distances.lastEntry();
				maxTrajectory = entry.getValue();
				max = entry.getKey();
				c.add(t1);
			}
		}
		if (c.size() < k) return null;
		else return c;
	}

	//he comentado esto para hacer algo mas eficiente
	public static List<Trajectory> findBestCluster7(List<Trajectory> trajectories,
			 int k, Distance dist) {
		if (k < 2) return null;
		if (trajectories.size() < k) return null;
		List<Trajectory> c = new ArrayList<Trajectory>();
		double distance;
		Trajectory centroide = null;
		//Random r = new Random();
		//centroide = trajectories.get(r.nextInt(trajectories.size()));
		centroide = trajectories.get(0);
		TreeMap<Double, Trajectory> distances = new TreeMap<Double, Trajectory>();
		distances.put(0d, centroide);
		double max = 0;
		Entry<Double, Trajectory> entry;
		Trajectory maxTrajectory = null;
		for (Trajectory t1 : trajectories){
			if (t1.equals(centroide)) continue;
			distance = dist.distance(t1, centroide);
			//if no esta lleno pngo lo que sea
			entry = distances.lastEntry();
			maxTrajectory = entry.getValue();
			max = entry.getKey();
			if (c.size() < k) {
				distances.put(distance, t1);
				c.add(t1);
			}
			else if (distance < max){
				//en este caso sacamos el max y ponemos este nuevo
				distances.remove(max);
				distances.put(distance, t1);
				c.remove(maxTrajectory);
				c.add(t1);
			}
		}
		if (c.size() < k) return null;
		else return c;
	}
	/** 11/08/2010 Trujillo Comment
	 * Dada las trajectorieas como primer argumento, se tiene que buscar el conjunto de trajectorias
	 * de tamanno k que tenga la menor distancia intra-cluster. Hay que tener en cuenta que el segundo
	 * argumento dice las trajectorias que no se deben considerar en este algoritmo*/
	public static List<Trajectory> findBestCluster2(List<Trajectory> trajectories,
			 int k, Distance dist) {
		if (k < 2) return null;
		if (trajectories.size() < k) return null;
		List<Trajectory> c = new ArrayList<Trajectory>();
		double min = Double.MAX_VALUE;
		double distance;
		Trajectory ti = null;
		Trajectory tj = null;
		for (Trajectory t1 : trajectories){
			for (Trajectory t2 : trajectories){
				if (t1.equals(t2)) continue;
				distance = dist.distance(t1, t2);
				if (distance < min){
					min = distance;
					ti = t1;
					tj = t2;
				}
			}
		}
		/*if(ti == null) {
			System.out.println("Why the cluster fails");
			System.out.println("The size of the data set is "+trajectories.size()+" where k = "+k);
			System.out.println("Its trajectories are:");
			for (Trajectory trajectory : trajectories) {
				System.out.println(trajectory);
			}
		}*/
		c.add(ti);
		c.add(tj);
		Trajectory tmp = null;
		while (c.size() != k){
			min = Double.MAX_VALUE;
			tmp = null;
			for (Trajectory t1 : trajectories){
				boolean goOn = false;
				distance = 0;
				for (Trajectory t2 : c){
					if (t2.equals(t1)) goOn = true;
					distance += dist.distance(t1, t2);
				}
				if (goOn) continue;
				if (distance < min){
					min = distance;
					tmp = t1;
				}
			}
			if (tmp == null) throw new RuntimeException();
			c.add(tmp);
		}
		return c;
	}

	/** 11/08/2010 Trujillo Comment
	 * Dada las trajectorieas como primer argumento, se tiene que buscar el conjunto de trajectorias
	 * de tamanno k que tenga la menor distancia intra-cluster. Hay que tener en cuenta que el segundo
	 * argumento dice las trajectorias que no se deben considerar en este algoritmo*/
	public static List<Trajectory> findBestCluster5(List<Trajectory> trajectories,
			 int k, Distance dist) {
		if (k < 2) return null;
		if (trajectories.size() < k) return null;
		List<Trajectory> c = new ArrayList<Trajectory>();
		double min = Double.MAX_VALUE;
		double distance;
		Trajectory ti = null;
		Trajectory tj = null;
		for (Trajectory t1 : trajectories){
			for (Trajectory t2 : trajectories){
				if (t1.equals(t2)) continue;
				distance = dist.distance(t1, t2);
				if (distance < min){
					min = distance;
					ti = t1;
					tj = t2;
				}
			}
		}
		c.add(ti);
		c.add(tj);
		Trajectory tmp = null;
		while (c.size() != k){
			min = Double.MAX_VALUE;
			for (Trajectory t1 : trajectories){
				boolean goOn = false;
				distance = 0;
				for (Trajectory t2 : c){
					if (t2.equals(t1)) goOn = true;
					distance += t1.distance(t2, dist);
				}
				if (goOn) continue;
				if (distance < min){
					min = distance;
					tmp = t1;
				}
			}
			c.add(tmp);
		}
		return c;
	}


	/** 22/11/2011 Trujillo Comment
	 * Esto es mas o menos lo mismo que el metodo de arriba. Sin embargo, aqui le damos un peso
	 * al hecho de que dos trajectorias se parezcan en terminos de longitud.*/
	public static List<Trajectory> findBestCluster4(List<Trajectory> trajectories,
			 int k, Distance dist) {
		if (k < 2) return null;
		if (trajectories.size() < k) return null;
		List<Trajectory> c = new ArrayList<Trajectory>();
		double min = Double.MAX_VALUE;
		double distance;
		Trajectory ti = null;
		Trajectory tj = null;
		for (Trajectory t1 : trajectories){
			for (Trajectory t2 : trajectories){
				if (t1.equals(t2)) continue;
				distance = dist.distance(t1, t2);
				//distance = 1;
				//distance = distance*(Math.abs(t1.size()-t2.size()+1)/Math.max(t1.size()+1, t2.size()+1));
				//distance = (Math.abs(t1.size()-t2.size()+1)/Math.max(t1.size()+1, t2.size()+1));
				if (distance < min){
					min = distance;
					ti = t1;
					tj = t2;
				}
			}
		}
		c.add(ti);
		c.add(tj);
		Trajectory tmp = null;
		while (c.size() != k){
			min = Double.MAX_VALUE;
			for (Trajectory t1 : trajectories){
				boolean goOn = false;
				distance = 0;
				for (Trajectory t2 : c){
					if (t2.equals(t1)) goOn = true;
					double d = dist.distance(t1, t2);
					//double d = 1;
					d = d*(Math.abs(t1.size()-t2.size())/Math.max(t1.size(), t2.size()));
					distance += d;
				}
				if (goOn) continue;
				if (distance < min){
					min = distance;
					tmp = t1;
				}
			}
			c.add(tmp);
		}
		return c;
	}

	/** 28/09/2010 Trujillo Comment
	 * Mas o menos lo mismo que el findBestCluster3, sin embargo ahora ya se tiene un pivote por el cual
	 * empezar*/
	public static List<Trajectory> findBestCluster3(List<Trajectory> trajectories,
			 int k, List<Trajectory> reference, Distance dist) {
		if (k < 2) return null;
		if (reference.size() + trajectories.size() < k)  return null;
		List<Trajectory> c = new ArrayList<Trajectory>();
		for (Trajectory t : reference){
			c.add(t);
		}
		double min;
		double distance;
		Trajectory tmp = null;
		while (c.size() != k){
			min = Double.MAX_VALUE;
			for (Trajectory t1 : trajectories){
				boolean goOn = false;
				distance = 0;
				for (Trajectory t2 : c){
					if (t2.equals(t1)) goOn = true;
					distance += t1.distance(t2, dist);
				}
				if (goOn) continue;
				if (distance < min){
					min = distance;
					tmp = t1;
				}
			}
			if (tmp == null) return null;
			c.add(tmp);
		}
		return c;
	}			

	/** 28/09/2010 Trujillo Comment
	 * Mas o menos lo mismo que el findBestCluster3, sin embargo ahora ya se tiene un pivote por el cual
	 * empezar*/
	public static List<Trajectory> findBestCluster3(List<Trajectory> trajectories,
			 int k, Trajectory pivote, Distance dist) {
		if (k < 2) return null;
		List<Trajectory> c = new ArrayList<Trajectory>();
		c.add(pivote);
		double min;
		double distance;
		Trajectory tmp = null;
		while (c.size() != k){
			min = Double.MAX_VALUE;
			for (Trajectory t1 : trajectories){
				boolean goOn = false;
				distance = 0;
				for (Trajectory t2 : c){
					if (t2.equals(t1)) goOn = true;
					distance += t1.distance(t2, dist);
				}
				if (goOn) continue;
				if (distance < min){
					min = distance;
					tmp = t1;
				}
			}
			c.add(tmp);
		}
		return c;
	}

	/** 10/08/2010 Trujillo Comment
	 * El punto de una trajectoria se puede definir por el timespan que lo contiene. Por tanto, 
	 * nosotros lo que vamos a clusterizar es para cada trajectoria, el timespan que define a ese
	 * punto que esta en el cluster*/
	public static List<List<PointInsideTrajectory>> clusteringPoints(List<Trajectory> t, int k, 
			Hashtable<String, boolean[]> swapped, Distance dist){
		List<PairObject<PointInsideTrajectory>> l = createSortedList(t, swapped, dist);
		List<List<PointInsideTrajectory>> result = new LinkedList<List<PointInsideTrajectory>>();
		Hashtable<String, Trajectory> usedTrajectories;
		for (PairObject<PointInsideTrajectory> pair : l) {
			int indeX = pair.x.t.getIndex(pair.x.time);
			int indeY = pair.y.t.getIndex(pair.y.time);
			if (swapped.get(pair.x.t.getIdentifier())[indeX] || 
					swapped.get(pair.y.t.getIdentifier())[indeY]) continue;
			List<PointInsideTrajectory> cp = new LinkedList<PointInsideTrajectory>();
			swapped.get(pair.x.t.getIdentifier())[indeX] = true;
			swapped.get(pair.y.t.getIdentifier())[indeY] = true;
			cp.add(pair.x);
			cp.add(pair.y);
			usedTrajectories = new Hashtable<String, Trajectory>();
			usedTrajectories.put(pair.x.t.getIdentifier(), pair.x.t);
			usedTrajectories.put(pair.y.t.getIdentifier(), pair.y.t);
			for (int i = 0; i < t.size(); i++){
				Trajectory  trajectory = t.get(i);
				if (usedTrajectories.containsKey(trajectory.getIdentifier())) continue;
				PointInsideTrajectory p = findBestPoint(trajectory, swapped, cp, dist);
				if (p == null){
					/** 10/08/2010 Trujillo Comment
					 * Es porque no hay mas puntos que swapear y por tanto no se 
					 * pudieron completar los k puntos necesarion*/
					System.out.println("La trajectoria de Id = "+trajectory.getIdentifier()+
							" se quedo sin puntos para swapear");
					for (PointInsideTrajectory point : cp){
						swapped.get(point.t.getIdentifier())[point.t.getIndex(point.time)] = false;
					}
					return result;
				}
				cp.add(p);
				int indeP = p.t.getIndex(p.time);
				swapped.get(p.t.getIdentifier())[indeP] = true;
				usedTrajectories.put(trajectory.getIdentifier(), trajectory);
			}
			result.add(cp);
		}
		return result;
	}
	
	public static Trajectory findSmallerTrajectory(List<Trajectory> trajectories){
		Trajectory smaller = null;
		int min = Integer.MAX_VALUE;
		for (Trajectory t : trajectories){
			//System.out.println(t);
			//if (t == null) System.out.println(t);
			if (t.size() < min){
				smaller = t;
				min = t.size();
			}
		}
		return smaller;
	}

	public static Trajectory findSmallerTrajectory(Hashtable<String, Trajectory> trajectories){
		Trajectory smaller = null;
		int min = Integer.MAX_VALUE;
		for (Trajectory t : trajectories.values()){
			//System.out.println(t);
			//if (t == null) System.out.println(t);
			if (t.size() < min){
				smaller = t;
				min = t.size();
			}
		}
		return smaller;
	}

	public static Trajectory findBiggerTrajectory(List<Trajectory> trajectories){
		Trajectory bigger = null;
		int max = 0;
		for (Trajectory t : trajectories){
			if (t.size() > max){
				bigger = t;
				max = t.size();
			}
		}
		return bigger;
	}

	private static PointInsideTrajectory findBestPoint(Trajectory t, 
			Hashtable<String, boolean[]> swapped, List<PointInsideTrajectory> cp, Distance dist) {
		GPSFormat p;
		double d;
		PointInsideTrajectory result = null;
		double min = Double.MAX_VALUE;
		long time;
		boolean[] swap = swapped.get(t.getIdentifier());
		for (int i = 0; i < t.size(); i++){
			if (swap[i]) continue;
			time = t.getTime(i);
			p = t.getPoint(time);
			d = 0;
			for (PointInsideTrajectory pointInsideTrajectory : cp) {
				d += dist.distance(p, pointInsideTrajectory.p);
			}
			if (d < min){
				min = d;
				result = new PointInsideTrajectory(t, p, time);
			}
		}
		return result;
	}

	private static List<PairObject<PointInsideTrajectory>> createSortedList(
			List<Trajectory> t, Hashtable<String, boolean[]> swapped, Distance dist) {
		Trajectory t1;
		Trajectory t2;
		GPSFormat p1;
		GPSFormat p2;
		long time1;
		long time2;
		List<PairObject<PointInsideTrajectory>> result = new ArrayList<PairObject<PointInsideTrajectory>>();
		for (int i = 0; i < t.size()-1; i++) {
			t1 = t.get(i);
			for (int j = i+1; j < t.size(); j++) {
				t2 = t.get(j);
				for (int k = 0; k < t1.size(); k++){
					if (swapped.get(t1.getIdentifier())[k]) continue;
					time1 = t1.getTime(k);
					p1 = t1.getPoint(time1);					
					for (int l = 0; l < t2.size(); l++){
						if (swapped.get(t2.getIdentifier())[l]) continue;
						time2 = t2.getTime(l);						
						p2 = t2.getPoint(time2);
						double distance = dist.distance(p1, p2); 
						int index = -1;
					    for (int m = 0; m < result.size(); m++){
					    	PairObject<PointInsideTrajectory> pair = result.get(m);
					    	if (distance < dist.distance(pair.x.p, pair.y.p)){
					    		index = m;
					    		break;
					    	}
					    }
					    if (index == -1) result.add(new PairObject<PointInsideTrajectory>(
					    		new PointInsideTrajectory(t1, p1, time1), new PointInsideTrajectory(t2, p2, time2)));
					    else result.add(index, new PairObject<PointInsideTrajectory>(
					    		new PointInsideTrajectory(t1, p1, time1), new PointInsideTrajectory(t2, p2, time2)));
					}
				}
			}
		}
		return result;
	}

	public static class PointInsideTrajectory{
		public Trajectory t;
		public GPSFormat p;
		public long time;
		public boolean swapped = false;
		
		public PointInsideTrajectory(Trajectory t, GPSFormat p, long time){
			this.t = t;
			this.p = p;
			this.time = time;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof PointInsideTrajectory){
				PointInsideTrajectory p = (PointInsideTrajectory)obj;
				return t.equals(p.t) && time == p.time;
			}
			return false;
		}
		
		@Override
		public String toString() {
			String result = t.getIdentifier()+" -> "+ p.toString();
			return result;
		}
	}
	
	public static List<List<String>> findBestClustersForSwapLocations(
			Hashtable<String, Trajectory> dst, int k, Distance dist) {
		List<List<String>> result = new LinkedList<List<String>>();
		int clusterSize = dst.size()/k;
		for (int i = 0; i < clusterSize; i++){
			Trajectory centroide = findCentroide(dst);
			Trajectory pivote = findCloserTrajectoryFrom(centroide, dst, dist);
			List<String> cluster = new LinkedList<String>();
			cluster.add(pivote.getIdentifier());
			for (int j = 1; j < k; j++){
				double min = Double.MAX_VALUE;
				Trajectory tmp = null;
				for (Trajectory t1 : dst.values()){
					boolean goOn = false;
					double distance = 0;
					for (String t2 : cluster){
						if (t2.equals(t1.getIdentifier())) {
							goOn = true;
							break;
						}
						distance += dist.distance(t1, dst.get(t2));
					}
					if (goOn) continue;
					if (distance < min){
						min = distance;
						tmp = t1;
					}
				}
				if (tmp == null) {
					break;
				}
				cluster.add(tmp.getIdentifier());
			}
			if (cluster.size() != k) {
				if (dst.remove(pivote) == null) throw new RuntimeException();
				break;
			}
			for (String t : cluster) {
				if (dst.remove(t) == null) throw new RuntimeException();
			}
			result.add(cluster);
		}
		return result;
	}

	public static List<List<String>> findBestClustersForPermutation(
			Hashtable<String, Trajectory> dst, int k, Distance dist) {
		if (k == 1) return null;
		List<List<String>> result = new LinkedList<List<String>>();
		int numberOfCluster = dst.size();
		Timer timer = new Timer();
		while (dst.size() >= k){
			timer.reset();
			Trajectory centroide = findCentroide(dst);
			Trajectory pivote = findCloserTrajectoryFrom(centroide, dst, dist);
			//System.out.println("Founded pivote = "+pivote);
			//List<String> cluster = new LinkedList<String>();
			//cluster.add(pivote.getIdentifier());
			Hashtable<String, Double> partialResult = new Hashtable<String, Double>();
			double worstDistance = -1;
			String toRemove = null;			
			for (String idTrajectory : dst.keySet()) {
				if (pivote.getIdentifier().equals(idTrajectory)) continue;
				//System.out.println("Computing distance to "+idTrajectory);
				Trajectory trajectory = dst.get(idTrajectory);
				Double transformation = dist.distance(pivote, trajectory);
				if (Double.isNaN(transformation) || Double.isInfinite(transformation)){
					throw new RuntimeException("Distance between pivote "+pivote.getIdentifier()+
							" and trajectory "+idTrajectory+" is "+transformation);
					//continue;
				}
				//System.out.println("distance = "+transformation);
				//System.out.println("the cluster size is = "+partialResult.size());
				if (partialResult.size() < k-1){
					//System.out.println("Since the size is less than "+(k-1)+" we add the trajectory");
					partialResult.put(idTrajectory, transformation);
					if (transformation > worstDistance) {
						worstDistance = transformation;
						toRemove = idTrajectory;
					}
					//System.out.println("Now, worstDistance = "+worstDistance);
					//System.out.println("toRemove = "+toRemove);
					//System.out.println("and the cluster size is = "+partialResult.size());
				}
				else{
					if (transformation > worstDistance) {
						//System.out.println("the distance is too long, go for next one");
						continue;
					}
					//ahora estamos con una trayectoria que es mejor que alguna del grupo, 
					//por tanto hay que eliminar esa que es peor.
					partialResult.remove(toRemove);
					//System.out.println("remove trajectory "+toRemove);
					//System.out.println("and the cluster size is = "+partialResult.size());
					//System.out.println("adding trajectory "+idTrajectory);
					partialResult.put(idTrajectory, transformation);
					//System.out.println("and the cluster size is = "+partialResult.size());
					worstDistance = 0;
					for (String id : partialResult.keySet()) {
						Double value = partialResult.get(id);
						if (value > worstDistance) {
							worstDistance = value;
							toRemove = id;
						}
					}
					//System.out.println("Now worstDistance is = "+worstDistance);
					//System.out.println("toRemove = "+toRemove);
				}
			}
			//System.out.println("size = "+result.size());
			//eliminamos el pivote
			List<String> finalCluster = new LinkedList<String>();
			if (partialResult.size() == k-1){
				//es porque el cluster es bueno, por tanto borramos las trajectorias.
				//System.out.println("Cluster found for pivote = "+pivote.getIdentifier());
				finalCluster.add(pivote.getIdentifier());
				for (String id : partialResult.keySet()) {
					if (dst.remove(id) == null) 
						throw new RuntimeException("Could not remove from dst the trajectory "+id);
					finalCluster.add(id);
				}
				result.add(finalCluster);
				numberOfCluster--;
			}
			else{
				//System.out.println("Cluster NOT found for pivote = "+pivote.getIdentifier());
			}
			if (dst.remove(pivote.getIdentifier()) == null) {
				throw new RuntimeException("The pivote ="+pivote.getIdentifier()+" does no appear");
			}
			//System.out.println("Remaining time = "+timer.getTimeInHours()+" hours");
		}
		return result;
	}

	private static Trajectory findCentroide(Hashtable<String, Trajectory> dst) {
		TreeMap<Long, GPSFormat> centroide = new TreeMap<Long, GPSFormat>();
		TreeMap<Long, Integer> counter = new TreeMap<Long, Integer>();
		GPSFormat tmp;
		GPSFormat tmpC;
		for (Trajectory t : dst.values()){
			for (long time : t.times()){
				tmp = t.getPoint(time);
				if (centroide.containsKey(time)){
					tmpC = centroide.get(time);
					centroide.put(time, new SimpleFormat(time, tmp.getLatitude()+tmpC.getLatitude(), 
							tmp.getLongitude()+tmp.getLongitude()));
					counter.put(time, counter.get(time)+1);
				}
				else{
					centroide.put(time, tmp);
					counter.put(time, 1);
				}
			}
		}
		TreeMap<Long, GPSFormat> result = new TreeMap<Long, GPSFormat>();
		for (long time : centroide.keySet()){
			result.put(time, new SimpleFormat(time, 
					centroide.get(time).getLatitude()/counter.get(time), 
					centroide.get(time).getLongitude()/counter.get(time)));
		}
		return new SimpleTrajectory("centroide", result);
	}

	private static GPSFormat findLocationCentroide(Hashtable<String, Trajectory> dst) {
		GPSFormat centroide = null;
		int counter = 0;
		GPSFormat tmp;
		GPSFormat tmpC;
		for (Trajectory t : dst.values()){
			for (long time : t.times()){
				tmp = t.getPoint(time);
				if (centroide == null){
					centroide = new SimpleFormat(time, tmp.getLatitude(), tmp.getLongitude());
				}
				else{
					centroide = new SimpleFormat(time+centroide.getTime(), 
							tmp.getLatitude()+centroide.getLatitude(), 
							tmp.getLongitude()+centroide.getLongitude());
				}
				counter++;
			}
		}
		return new SimpleFormat(centroide.getTime()/counter, centroide.getLatitude()/counter, 
				centroide.getLongitude()/counter);
	}

	public static Trajectory findCloserTrajectoryFrom(Trajectory r, Hashtable<String, Trajectory> records, Distance dist){
		double min = Double.MAX_VALUE;
		double tmp;
		Trajectory result = null;
		for (Trajectory record : records.values()){
			tmp = dist.distance(r,record);
			if (tmp < min){
				min = tmp;
				result = record;
			}
		}
		return result;
	}
	
	public static PointInsideTrajectory findCloserLocationFrom(GPSFormat r, 
			Hashtable<String, Trajectory> records, Distance dist){
		double min = Double.MAX_VALUE;
		double tmp;
		PointInsideTrajectory result = null;
		for (Trajectory record : records.values()){
			for (long time : record.times()) {
				GPSFormat location = record.getPoint(time);
				tmp = dist.distance(r,location);
				tmp = (tmp+1)*(Math.abs(r.getTime()-time)+1);
				if (tmp < min){
					min = tmp;
					result = new PointInsideTrajectory(record, location, time);
				}
			}
		}
		return result;
	}
}
