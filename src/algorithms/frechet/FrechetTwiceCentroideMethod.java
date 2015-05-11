package algorithms.frechet;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import parsers.GPXParser;

import util.Converter;
import util.Interpolation;
import util.Util;
import wrappers.GPSFormat;
import wrappers.SimpleFormat;
import wrappers.SimpleTrajectory;
import wrappers.Trajectory;
import distances.FrechetDistance;
import distances.FrechetDistanceGPSBased;
import distances.Transformation;

/*Trujillo- May 15, 2013
 * The purpose of this class is to define the distortion to the centroide (average)*/
public class FrechetTwiceCentroideMethod extends FrechetCentroideMethod{

	public static void main(String[] args) throws IOException {
		//System.setOut(new PrintStream("testing.txt"));
		FrechetDistance distance = new FrechetDistanceGPSBased();
		Util.printTrajectory("real", "new_ispawwye.txt61862", 1);
		Util.printTrajectory("real", "new_arlwras.txt14327", 1);
		Trajectory t1 = Util.getCompressedTrajectory("real", "new_ispawwye.txt61862");
		Trajectory t2 = Util.getCompressedTrajectory("real", "new_arlwras.txt14327");
		Trajectory t1_inverted = t1.invert();
		Trajectory t2_inverted = t2.invert();
		Converter.checkDegreesConverter(t1);
		Converter.checkDegreesConverter(t2_inverted);
		GPXParser.buildGpxFileWithUnixEpochTime(t1.getTree(), "new_ispawwye.txt61862.gpx", 0);
		GPXParser.buildGpxFileWithUnixEpochTime(t2.getTree(), "new_arlwras.txt14327.gpx", 0);
		Transformation transformation = distance.distanceWithTransformationOptimized(t1, t2);
		System.out.println(transformation+" and whose distance is = "+transformation.distance);
		Util.printMaximumDistance(t1, t2_inverted);
		transformation = distance.distanceWithTransformationOptimized(t1, t2_inverted);
		System.out.println(transformation+" and whose distance is = "+transformation.distance);
		transformation = distance.distanceWithTransformationOptimized(t1_inverted, t2_inverted);
		System.out.println(transformation+" and whose distance is = "+transformation.distance);
		transformation = distance.distanceWithTransformationOptimized(t1_inverted, t2);
		System.out.println(transformation+" and whose distance is = "+transformation.distance);
		/*List<Trajectory> cluster = new LinkedList<Trajectory>();
		cluster.add(t1);
		cluster.add(t2);
		FrechetTwiceCentroideMethod method = new FrechetTwiceCentroideMethod("real", distance);
		method.anonymizeCluster(cluster);*/

	}
	

	public FrechetTwiceCentroideMethod(String preffix, FrechetDistance distance) {
		super(preffix, distance);
	}

	@Override
	public String toString() {
		return "frechet-twice-centroide";
	}
	
	@Override
	protected List<Transformation> getTransformation(
			List<Trajectory> trajectories, int k, FrechetDistance distance) {
		if (trajectories.size() < k) return null;
		Trajectory pivot = getRandomPivotTrajectory(trajectories);		
		List<Transformation> result = new ArrayList<Transformation>();
		double worstDistance = 0;
		Transformation transformation1;
		Transformation transformation2;
		Transformation transformation;
		Transformation toRemove = null;
		for (Trajectory trajectory : trajectories) {
			if (trajectory.equals(pivot)) continue;
			transformation1 = distance.distanceWithTransformationOptimized(pivot, trajectory);
			Trajectory inverted = trajectory.invert();
			transformation2 = distance.distanceWithTransformationOptimized(pivot, inverted);
			/*if (transformation == null && transformation2 == null){
				continue;
			}*/
			transformation = (transformation1.distance > transformation2.distance)?transformation2:transformation1;
			if (result.size() < k-1){
				result.add(transformation);
				if (transformation.distance > worstDistance) {
					worstDistance = transformation.distance;
					toRemove = transformation;
				}
				continue;
			}
			if (transformation.distance > worstDistance) continue;
			result.remove(toRemove);
			result.add(transformation);
			worstDistance = 0;
			for (Transformation t : result) {
				if (t.distance > worstDistance) {
					worstDistance = t.distance;
					toRemove = t;
				}
			}
		}
		if (!trajectories.remove(pivot)) throw new RuntimeException();
		if (result.size() == k-1){
			for (Transformation t : result) {
				if (!trajectories.remove(t.t2)) throw new RuntimeException();
			}
		}
		else{
			return null;
		}
		return result;
	}
	
	@Override
	public String getName() {
		return "FTC";
		//return "frechet-twice-centroide";
	}


}
