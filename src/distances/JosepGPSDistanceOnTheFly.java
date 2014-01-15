package distances;

import java.awt.Point;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import trajectory.Trajectory;
import util.Vertex;
import wrappers.GPSFormat;

public class JosepGPSDistanceOnTheFly extends GPSDistance{

	
	private double[][] distances;
	private int size; 
	
	public JosepGPSDistanceOnTheFly(int size){
		this.size = size;
		distances = new double[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				distances[i][j] = -1;
			}
		}
	}

	@Override
	public double distance(Trajectory t1, Trajectory t2){
		//System.out.println("Distance from "+t1+" to "+t2);
		boolean indexed = true;
		int index1 = -1;
		int index2 = -1;
		try{
			index1 = Integer.parseInt(t1.getIdentifier()) % size;
			index2 = Integer.parseInt(t2.getIdentifier()) % size;
		}catch(NumberFormatException en){
			//es porque no estamos tratando con trayectorias con identificadore normales.
			indexed = false;
		}		
		if (indexed && distances[index1][index2] != -1){
			//System.out.println("pre-computed");
			return distances[index1][index2];
		}
		//System.out.println("computing");
		double I = Math.max(0, Math.min(t1.lastTime(), t2.lastTime())-
				Math.max(t1.firstTime(), t2.firstTime()));
		double p = (100*Math.min(I/(t1.lastTime()-t1.firstTime()), 
				I/(t2.lastTime()-t2.firstTime())));
		double distance = 0;
		int points = 0;
		if (p <= 0) distance = Double.NaN;
		else{
			boolean first = false;
			boolean last = false;
			GPSFormat p1;
			GPSFormat p2;
			for (Long d : t1.times()){
				if (t2.containsTime(d)){
					if (last) throw new RuntimeException("Algo paso con el intervalo");
					first = true;
					p1 = t1.getPoint(d); 
					p2 = t2.getPoint(d); 
					distance += super.distance(p1.getLatitude(), p1.getLongitude(), p2.getLatitude(), 
							p2.getLongitude());				
					/*distance += Math.pow(p1.getLatitude()-p2.getLatitude(), 2)
						+ Math.pow(p1.getLongitude()-p2.getLongitude(), 2);*/
					points++;
				}
				else{
					if (first) last = true;
				}
			}
		}
		//double distance = dist.distance(tree1, tree2);
		if (Double.isNaN(distance) || distance < 0){
			distance = super.distance(t1, t2);
			//System.out.println("No overlapping = "+distance);
			if (indexed){
				distances[index1][index2] = distance*(Math.abs(t1.size()-t2.size())+1); 
				distances[index2][index1] = distances[index1][index2]; 
				return distances[index1][index2];
			}else return distance*(Math.abs(t1.size()-t2.size())+1);
		}
		else {
			//System.out.println("With overlapping = "+distance);
			if (indexed){
				distances[index1][index2] = distance/(p*points); 
				distances[index2][index1] = distances[index1][index2]; 
				return distances[index1][index2];
			}else return distance/(p*points);
		}
	}

}
