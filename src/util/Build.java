package util;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeMap;


import clustering.MyTrajectoryFormat;
import distances.Distance;
import distances.GPSDistance;

import trajectory.Trajectory;
import wrappers.GPSFormat;


public class Build {

	/** 19/07/2010 Trujillo Comment
	 * Dado el fichero donde esta guardadas todas las trajectorias, este coge y comienza
	 * a formar el grafo donde dos trajectorias son adyacentes si se solapan en tiempo. Para 
	 * este tipo de trajectorias pues esta definida la distancia segun se indica en el paper.*/
	/*public static void buildingGraph(String preffix, String suffix, Distance dist) throws IOException, ClassNotFoundException {
		EdgeFactory<String, WEdge> edge = new ClassBasedEdgeFactory<String, WEdge>(WEdge.class); 
		SimpleWeightedGraph<String, WEdge> graph = new SimpleWeightedGraph<String, WEdge>(edge);
		Hashtable<String, Trajectory> allTrajectories;
		Trajectory t1 = null;
		Trajectory t2 = null;
		ObjectInputStream input;
		input = new ObjectInputStream(new FileInputStream("./"+preffix+"/"+preffix+suffix));
		allTrajectories = (Hashtable<String, Trajectory>)input.readObject();
		input.close();
		int cont = allTrajectories.size();
		for (String key1 : allTrajectories.keySet()){
			long ini = System.currentTimeMillis();
			graph.addVertex(key1);
			t1 = allTrajectories.get(key1);
			for (String key2 : allTrajectories.keySet()){
				if (key1.equals(key2)) continue;
				t2 = allTrajectories.get(key2);				
				//double distance1 = dist.distance(t1, t2);
				double distance2 = getRealDistance(t1, t2, dist);
				//double distance3 = getSpaceVsLengthTradeOffDistance(t1, t2, dist);
				double distance = distance2;
				if (Double.isNaN(distance)) {
					//throw new RuntimeException();
					continue;
				}
				if (distance < 0) throw new RuntimeException();
				graph.addVertex(key2);
				graph.addEdge(key1, key2, new WEdge(distance));
			}
			long end = System.currentTimeMillis();
			System.out.println("building graph "+key1+" of "+allTrajectories.size()+" left "+cont+" more");
			System.out.println("Time to finish = "+((end-ini)/3600000d)*cont+" hours");
			cont--;
		}
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("./"+preffix+"/"+preffix+"_graph.obj"));
		out.writeObject(graph);
		out.close();
		System.out.println(graph.vertexSet().size());
	}*/

	private static double getSpaceVsLengthTradeOffDistance(
			TreeMap<Long, GPSFormat> t1, TreeMap<Long, GPSFormat> t2,
			Distance dist) {
		double distance = 0;
		int cont = 0;
		List<GPSFormat> l1 = new ArrayList<GPSFormat>();
		List<GPSFormat> l2 = new ArrayList<GPSFormat>();
		for (long time : t1.keySet()){
			l1.add(t1.get(time));
		}
		for (long time : t2.keySet()){
			l2.add(t2.get(time));
		}
		for (int i = 0; i < Math.min(l1.size(), l2.size()); i++){
			distance += dist.distance(l1.get(i), l2.get(i));
			cont++;
		}
		if (cont == 0) return Double.MAX_VALUE;
		return distance*(Math.abs(t1.size()-t2.size()+1))/(Math.max(t1.size(), t2.size())+1);
	}

	
	/** 08/07/2010 Trujillo Comment
	 * Distance entre dos trajectorias. Para calcularla primero hay que buscar el intervalo
	 * en que se solapan, y en fin, en el articulo esta la forma en que se calcula la distance*/
	public static double getRealDistance(TreeMap<Long, GPSFormat> tree1,
			TreeMap<Long, GPSFormat> tree2, Distance dist) {
		double I = Math.max(0, Math.min(tree1.lastKey(), tree2.lastKey())-
				Math.max(tree1.firstKey(), tree2.firstKey()));
		double p = (100*Math.min(I/(tree1.lastKey()-tree1.firstKey()), 
				I/(tree2.lastKey()-tree2.firstKey())));
		if (p <= 0) return Double.NaN;
		boolean first = false;
		boolean last = false;
		double distance = 0;
		GPSFormat p1;
		GPSFormat p2;
		int points = 0;
		for (Long d : tree1.keySet()){
			if (tree2.containsKey(d)){
				if (last) throw new RuntimeException("Algo paso con el intervalo");
				first = true;
				p1 = tree1.get(d); 
				p2 = tree2.get(d); 
				distance += dist.distance(p1.getLatitude(), p1.getLongitude(), p2.getLatitude(), 
						p2.getLongitude());				
				/*distance += Math.pow(p1.getLatitude()-p2.getLatitude(), 2)
					+ Math.pow(p1.getLongitude()-p2.getLongitude(), 2);*/
				points++;
			}
			else{
				if (first) last = true;
			}
		}
		//double distance = dist.distance(tree1, tree2);
		return distance/(p*points);
	}

	/** 08/07/2010 Trujillo Comment
	 * Distance entre dos trajectorias. Para calcularla primero hay que buscar el intervalo
	 * en que se solapan, y en fin, en el articulo esta la forma en que se calcula la distance*/
	public static double getDistance(TreeMap<Long, GPSFormat> tree1,
			TreeMap<Long, GPSFormat> tree2, Distance dist) {
		double I = Math.max(0, Math.min(tree1.lastKey(), tree2.lastKey())-
				Math.max(tree1.firstKey(), tree2.firstKey()));
		double p = (100*Math.min(I/(tree1.lastKey()-tree1.firstKey()), 
				I/(tree2.lastKey()-tree2.firstKey())));
		if (p <= 0) return Double.NaN;
		boolean first = false;
		boolean last = false;
		double distance = 0;
		GPSFormat p1;
		GPSFormat p2;
		int points = 0;
		for (Long d : tree1.keySet()){
			if (tree2.containsKey(d)){
				if (last) throw new RuntimeException("Algo paso con el intervalo");
				first = true;
				p1 = tree1.get(d); 
				p2 = tree2.get(d); 
				distance += dist.distance(p1, p2);
				points++;
			}
			else{
				if (first) last = true;
			}
		}
		return Math.sqrt(distance)/(p*points);
	}

	/** 19/07/2010 Trujillo Comment
	 * Manda a construit los caminos minimos de este grafo para asi calcular la distancia entre
	 * ellos*/
	public static void shortestPath(String preffix) throws FileNotFoundException, IOException, ClassNotFoundException{
		/*ObjectInputStream in= new ObjectInputStream(new FileInputStream("./"+preffix+"/"+preffix+"_graph.obj"));
		WeightedGraph<String, WEdge> g = (WeightedGraph<String, WEdge>)in.readObject();
		in.close();
		System.out.println("Graph loaded");
	
		MyTrajectoryFormat.setDistanceGraph(g);
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("./"+preffix+"/"+preffix+"_shortestPath.obj"));
		out.writeObject(MyTrajectoryFormat.shortestPath);
		out.close();*/
	}
	

}
