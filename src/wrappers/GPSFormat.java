package wrappers;

import java.io.Serializable;

import exceptions.IncorrectLineFormatException;

public abstract class GPSFormat implements Serializable{


	private static final long serialVersionUID = -347714158680340168L;

	protected long time;
	
	protected double longitude;
	
	protected double latitude;
	
	protected double height;

	public abstract boolean isValidateData();

	@Override
	public String toString() {
		String result = "";
		//result += "Date="+getDate()+" longitude="+getLongitude()+" latitude="+getLatitude()+" height ="+getHeight();
		result = "["+getTime()+","+getLatitude()+","+getLongitude()+"]";
		return result;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public void setHeight(String s) {
		try{
			double h = Double.parseDouble(s);
			setHeight(h);
		}catch (NumberFormatException e) {
			throw new IncorrectLineFormatException("La " +
					"altura resultante es "+s+" y esto no es un numero");
		}
	}
	
	/** 17/06/2010 Trujillo Comment
	 * La altura estandar se da en metros*/
	public abstract double getStandarHeight();
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GPSFormat){
			GPSFormat f = (GPSFormat)obj;
			return f.getTime() == this.getTime() && f.getLatitude() == this.getLatitude()
			&& f.getLongitude() == this.getLongitude();
		}
		else return false;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}


	public double getX(){
		return latitude;
	}
	
	public double getY(){
		return longitude;
	}

	public boolean equalsInSpace(GPSFormat p) {
		return p.getLatitude() == this.getLatitude()
		&& p.getLongitude() == this.getLongitude();
	}

	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new SimpleFormat(getTime(), getLatitude(), getLongitude());
	}
}
