package range_queries;

import java.io.Serializable;
import java.util.List;

import wrappers.*;

public class Region implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1297450342005037723L;
	
	private double radius;
	private List<GPSFormat> pointsOfRegion;	
	
	public Region (List<GPSFormat> pointsOfRegion, double radius) {
		this.setRadius(radius);
		this.setPointsOfRegion(pointsOfRegion);
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getRadius() {
		return radius;
	}

	public void setPointsOfRegion(List<GPSFormat> pointsOfRegion) {
		this.pointsOfRegion = pointsOfRegion;
	}

	public List<GPSFormat> getPointsOfRegion() {
		return pointsOfRegion;
	}
	

}
