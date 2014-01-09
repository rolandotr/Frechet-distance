package clustering;

import distances.Distance;

public class DoubleMeasurable implements Measurable<DoubleMeasurable>{
	
	private double x;
	
	public DoubleMeasurable(double x){
		this.x = x;
	}
	@Override
	public double distance(DoubleMeasurable x, Distance dist) {
		return Math.abs(this.x - x.x);
	}
	
	@Override
	public String toString() {
		return x+"";
	}

}
