package algorithms.frechet;

import java.util.ArrayList;
import java.util.List;

import util.Interpolation;
import wrappers.GPSFormat;
import wrappers.SimpleTrajectory;
import wrappers.Trajectory;
import distances.FrechetDistance;
import distances.Transformation;

/*Trujillo- May 15, 2013
 * The purpose of this class is to define the distortion to the centroide (average)*/
public class FrechetCentroideMethod extends FrechetBasedAnonymization{

	public FrechetCentroideMethod(String preffix, FrechetDistance distance) {
		super(preffix, distance);
	}

	/*Trujillo- May 15, 2013
	 * Here, we should create the transformations and then to anonymizae as always*/
	@Override
	public List<Trajectory> anonymizeCluster(List<Trajectory> cluster) {
		List<Transformation> transformations = createTransformations(cluster);
		return anonymizeClusterEfficiently(transformations);
	}

	//@Override
	protected List<Trajectory> anonymizeClusterEfficiently(
			List<Transformation> cluster) {
		Trajectory pivot = cluster.get(0).t1;
		double[] alpha = cluster.get(0).alpha;
		int size = alpha.length;
		//primero buscamos el alpha mas pequenno
		for (Transformation tmp : cluster) {
			if (tmp.alpha.length < size){
				alpha = tmp.alpha;
				size = alpha.length;
			}
		}
		List<Trajectory> result = new ArrayList<Trajectory>(cluster.size()+1);
		result.add(0, new SimpleTrajectory(pivot.getIdentifier()));
		int cont = 1;
		//System.out.println("Vamos a ver que tal los alpha y beta");
		for (Transformation t : cluster){
			//System.out.println("alpha = "+t.alpha.length+", beta = "+t.beta.length);
			result.add(cont++, new SimpleTrajectory(t.t2.getIdentifier()));
		}		
		for (int pos = 0; pos < alpha.length; pos++) {
			GPSFormat[] points = new GPSFormat[cluster.size()+1];
			int[] positions = getRelativePositions(cluster, alpha, pos);
			
			points[0] = Interpolation.interpolate(pivot, (long)alpha[pos]);
			cont = 1;
			for (Transformation t : cluster){
				points[cont] = Interpolation.interpolate(t.t2, 
						(long)t.beta[positions[cont-1]]);
				cont++;
			}
			//aqui ya tenemos los puntos, ahora nos falta el centroide.
			GPSFormat centerPoint = Interpolation.centroide(points);
			for (int i = 0; i < points.length; i++){
				result.get(i).addPoint(centerPoint);
			}
		}
		return result;
	}
	
	@Override
	public String toString() {
		return getName();
	}

	@Override
	public String getName() {
		return "frechet-centroide";
	}


}
