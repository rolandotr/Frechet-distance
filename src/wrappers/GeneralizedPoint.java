package wrappers;

public class GeneralizedPoint{
	public double t1;
	public double t2;
	public double x1;
	public double x2;
	public double y1;
	public double y2;
	
	public GeneralizedPoint(double t1, double t2, double x1, double x2,
			double y1, double y2) {
		this.t1 = t1;
		this.t2 = t2;
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
	}
	
	public GeneralizedPoint(GPSFormat p) {
		this(p.time, p.time, p.getLatitude(), p.getLatitude(), 
				p.getLongitude(), p.getLongitude());
	}

	@Override
	public String toString() {
		String result = "["+t1+", "+t2+"]"+","+"["+x1+", "+x2+"]"+"["+y1+", "+y2+"]";
		return result;
	}
}