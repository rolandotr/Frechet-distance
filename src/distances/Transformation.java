package distances;

import wrappers.Trajectory;

public class Transformation{
	
	public double distance;
	public Trajectory t1;
	public Trajectory t2;
	public double[] alpha;
	public double[] beta;
	
	public Transformation(double distance, Trajectory t1,
			Trajectory t2, double[] alpha, double[] beta) {
		super();
		this.distance = distance;
		this.t1 = t1;
		this.t2 = t2;
		this.alpha = alpha;
		this.beta = beta;
	}
	
	@Override
	public String toString() {
		return t1.getIdentifier()+" with "+t2.getIdentifier();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Transformation){
			return ((Transformation)obj).t2.equals(t2); 
		}
		return false;
	}
}
