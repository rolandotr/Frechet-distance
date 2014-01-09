package wrappers;

import java.awt.Point;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import util.Interpolation;

import distances.Distance;


public class SimpleTrajectory extends Trajectory{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4640076566202050185L;
	
	private TreeMap<Long, GPSFormat> trajectory;
	
	public SimpleTrajectory(String identifier) {
		super(identifier);
		trajectory = new TreeMap<Long, GPSFormat>();
	}

	public SimpleTrajectory(String identifier, TreeMap<Long, GPSFormat> tree) {
		super(identifier);
		trajectory = tree;
	}
	
	@Override
	public void removePoint(long time) {
		if (trajectory.remove(time) == null){
			throw new RuntimeException("no se encontro el tiempo "+time+" en la trajectoria "+this.getIdentifier()+
					" por tanto no se pudo remover");
		}
	}
	
	@Override
	public Object clone() {
		SimpleTrajectory t = new SimpleTrajectory(this.getIdentifier());
		t.trajectory = (TreeMap<Long, GPSFormat>)trajectory.clone();
		return t;
	}

	@Override
	public boolean containsTime(long time) {
		return trajectory.containsKey(time);
	}

	@Override
	public GPSFormat getPoint(long time) {
		GPSFormat result = trajectory.get(time);
		//if (result == null) return null;
		//result.setDate(new Date(time));
		return result;
	}

	@Override
	public long getTime(int j) {
		int cont = 0;
		for (long time : trajectory.keySet()){
			if (cont == j) return time;
			cont++;
		}
		throw new IllegalArgumentException("The value of j is "+j+" and must be lower than "+trajectory.size());
	}

	@Override
	public Collection<GPSFormat> points() {
		return trajectory.values();
	}

	@Override
	public void setPoint(long time, double x, double y) {
		trajectory.put(time, new SimpleFormat(time, x, y));
	}

	@Override
	public int size() {
		return trajectory.size();
	}


	@Override
	public Set<Long> times() {
		return trajectory.keySet();
	}


	@Override
	public void addPoint(GPSFormat p) {
		trajectory.put(p.getTime(), p);
	}

	@Override
	public long lastTime() {
		return trajectory.lastKey();
	}

	@Override
	public long firstTime() {
		return trajectory.firstKey();
	}

	@Override
	public long closestTimeGreater(double time) {
		//if (trajectory.containsKey(time)) throw new RuntimeException();
		Entry<Long, GPSFormat> entry = trajectory.ceilingEntry((long)Math.ceil(time));
		return entry.getKey()-(long)Math.ceil(time);
	}

	@Override
	public long closestTimeLower(double time) {
		//if (trajectory.containsKey(time)) throw new RuntimeException();
		Entry<Long, GPSFormat> entry = trajectory.floorEntry((long)Math.floor(time));
		return (long)Math.floor(time)-entry.getKey();
	}
	
	@Override
	public long closestTimeNotUsed(long time){
		if (!trajectory.containsKey(time)) throw new RuntimeException();
		int cont = 1;
		while(true){
			if (!trajectory.containsKey(time+cont)) return cont;
			if (!trajectory.containsKey(time-cont)) return -cont;
			cont++;
		}
	}

	@Override
	public void addPoint(GPSFormat p, long time) {
		if (trajectory.containsKey(time)) throw new RuntimeException();
		p.setTime(time);
		addPoint(p);
	}

	public SimpleTrajectory reduceTime(long i, long j) {
		
		//Se clona la trayectoria para evitar que se altere la base de datos inicial.
		SimpleTrajectory newTraject = (SimpleTrajectory)this.clone();
		
		//Reducir por el tiempo inicial
		long tBeg = newTraject.getTime(0);
		while (tBeg < i) {
			newTraject.removePoint(tBeg);
			tBeg++;
		}
		
		//Reducir por el tiempo final
		long tEnd = newTraject.getTime(newTraject.points().size()-1);
		
		while (tEnd > j) {
			newTraject.removePoint(tEnd);
			tEnd--;
		}
		
		return newTraject;
	}

	@Override
	public Collection<GPSFormat> values() {
		return trajectory.values();
	}

	@Override
	public boolean timeOutOfInterval(double time) {
		if (size() < 2) return true;
		return (time < firstTime() || time > lastTime());
	}

	@Override
	public GPSFormat interpolateTime(double time) {
		if (timeOutOfInterval(time)) 
			throw new RuntimeException("requested time = "+time+", but interval should be" +
					"["+firstTime()+","+lastTime()+"]");
		//if (containsTime(time)) throw new RuntimeException();
		long timeLower = (long)Math.floor(time) - closestTimeLower(time);
		long timeGreater = (long)Math.ceil(time) + closestTimeGreater(time);
		return Interpolation.interpolate(getPoint(timeLower), getPoint(timeGreater), (long)time);
	}

	public TreeMap<Long, GPSFormat> getTree() {
		return trajectory;
	}

	@Override
	public Long ceilingTime(long time) {
		return trajectory.ceilingKey(time);
	}


}
