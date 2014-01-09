package util;

import java.io.Serializable;

public class Vertex implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2171265153109810593L;
	
	private String name;
	private long id;
	private double x;
	private double y;
	
	public Vertex(String name, long id, double x, double y) {
		super();
		this.name = name;
		this.id = id;
		this.x = x;
		this.y = y;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Vertex){
			Vertex tmp = (Vertex)obj;
			return tmp.getX() == this.getX() && tmp.getY() == this.getY();
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "name : "+name +", id : "+id+", x : "+x+", y : "+y;
	}
	
}
