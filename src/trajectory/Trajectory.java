package trajectory;

import java.awt.Rectangle;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import wrappers.GPSFormat;


import distances.Distance;

public abstract class Trajectory implements Serializable, Cloneable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5788288245433252276L;
	
	/** 14/07/2010 Trujillo Comment
	 * Por cuestion de performance de este modo es que se va a definir la igualdad entre
	 * trajectorias*/
	private String identifier;
	
	public Trajectory(String identifier){
		this.identifier = identifier;
	}

	public String getIdentifier() {
		return identifier;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Trajectory){
			Trajectory t = (Trajectory)obj;
			return t.getIdentifier().equals(this.getIdentifier());
		}
		return false;
	}

	public abstract int size();

	public abstract Set<Long> times();
	
	public abstract Collection<GPSFormat> values();

	public abstract GPSFormat getPoint(long time);

	/** 14/07/2010 Trujillo Comment
	 * Devuleve el tiempo i-th de esta trajectoria*/
	public abstract long getTime(int j);


	public abstract boolean containsTime(long time);

	public double distance(Trajectory t2, Distance dist) {
		double distance = 0;
		for (long time : this.times()){
			GPSFormat p1 = this.getPoint(time);
			GPSFormat p2 = t2.getPoint(time);
			distance += dist.distance(p1, p2);
		}
		for (long time : t2.times()){
			if (!this.containsTime(time)) throw new RuntimeException("What !!!");
		}
		return distance;
	}

	public double getEuclideanLenth(Distance dist) {
		GPSFormat first = null;
		double size = 0;
		for (GPSFormat p : this.points()){
			if (first == null) first = p;
			else{
				size += dist.distance(p, first); 
			}
			first = p;
		}
		return size;
	}

	public abstract Collection<GPSFormat> points();

	public abstract void setPoint(long time, double x, double y);
	
	public abstract Object clone();

	public int getIndex(long time) {
		int index = 0;
		for (long t : times()){
			if (t == time) return index;
			index++;
		}
		throw new RuntimeException("No se encontro el tiempo "+time+" dentro de esta trajectoria");
	}
	
	@Override
	public String toString() {
		String result = "";
		GPSFormat tmp;
		result += getIdentifier()+" --> ";
		for (long time : times()){
			tmp = getPoint(time);
			result += "["+time+","+tmp.getX()+","+tmp.getY()+"], ";
		}
		return result;
	}

	public abstract void removePoint(long time);

	public abstract void addPoint(GPSFormat p);

	public abstract long lastTime();
	public abstract long firstTime();

	public long closestTime(long time){
		long lower = closestTimeLower(time);
		long greater = closestTimeGreater(time);
		if (lower < greater) return time-lower;
		else return time+greater;
	}
	
	public abstract long closestTimeNotUsed(long time);

	/** 11/01/2012 Trujillo Comment
	 * Siempre deben retornar numero positivos indicando cuanto hay que desplazarse*/
	public abstract long closestTimeGreater(long time);
	
	public abstract long closestTimeLower(long time);

	public abstract void addPoint(GPSFormat p, long min);
	
	@Override
	public int hashCode() {
		return getIdentifier().hashCode();
	}

	public abstract boolean timeOutOfInterval(long time);

	public abstract GPSFormat interpolateTime(long time);

	/** 09/02/2012 Trujillo Comment
	 * Retorna el menor bounding box, o sea, el rectangulo que la contiene.*/
	public Rectangle getRectangle() {
		double minx = Double.MAX_VALUE;
		double maxx = -Double.MAX_VALUE;
		double miny = Double.MAX_VALUE;
		double maxy = -Double.MAX_VALUE;
		for (GPSFormat p : values()){
			if (p.getLatitude() < minx) minx = p.getLatitude();
			if (p.getLatitude() > maxx) maxx = p.getLatitude();
			if (p.getLongitude() < miny) miny = p.getLongitude();
			if (p.getLongitude() > maxy) maxy = p.getLongitude();
		}
		return new Rectangle((int)minx, (int)miny, (int)(maxx-minx), (int)(maxy-miny));
	}
	
}
