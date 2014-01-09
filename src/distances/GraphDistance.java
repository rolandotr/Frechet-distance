package distances;

import java.util.Hashtable;

import util.Vertex;

public class GraphDistance {

	private Hashtable<Vertex, Hashtable<Vertex, Double>> graphDistances;
	
	public GraphDistance(Hashtable<Vertex, Hashtable<Vertex, Double>> graphDistances){
		this.graphDistances = graphDistances;
	}
	
	public double graphDistance(double x1, double y1, double x2, double y2){
		Vertex v1 = new Vertex(null, 0, x1, y1);
		Vertex v2 = new Vertex(null, 0, x2, y2);
		return graphDistances.get(v1).get(v2);
	}

}
