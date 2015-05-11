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

import util.Interpolation;
import util.Vertex;
import wrappers.GPSFormat;
import wrappers.Trajectory;

public class JosepGPSDistanceOnTheFlyVs2 extends GPSDistance{

	
	public JosepGPSDistanceOnTheFlyVs2(){
	}

	@Override
	public double distance(Trajectory t1, Trajectory t2){
		//System.out.println("Distance from "+t1+" to "+t2);
		//System.out.println("computing");
		double I = Math.max(0, Math.min(t1.lastTime(), t2.lastTime())-
				Math.max(t1.firstTime(), t2.firstTime()));
		double p = (100*Math.min(I/(t1.lastTime()-t1.firstTime()), 
				I/(t2.lastTime()-t2.firstTime())));
		double distance = 0;
		int points = 0;
		if (p <= 0 || Double.isInfinite(p) || Double.isNaN(p)) distance = Double.NaN;
		else{
			boolean first = false;
			boolean last = false;
			GPSFormat p1;
			GPSFormat p2;
			for (Long d : t1.times()){
				if (!t2.timeOutOfInterval(d)){
					if (last) throw new RuntimeException("Algo paso con el intervalo");
					first = true;
					if (t1.containsTime(d)) p1 = t1.getPoint(d);
					else p1 = Interpolation.interpolate(t1, d);
					if (t2.containsTime(d)) p2 = t2.getPoint(d);
					else p2 = Interpolation.interpolate(t2, d);
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
		if (Double.isNaN(distance) || distance < 0 || p*points == 0){
			distance = super.distance(t1, t2);
			//System.out.println("No overlapping = "+distance);
			return distance*(Math.abs(t1.size()-t2.size())+1);
		}
		else {
			return distance/(p*points);
		}
	}

}
