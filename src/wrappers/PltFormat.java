package wrappers;


import exceptions.IncorrectLineFormatException;

/** 15/06/2010 Trujillo Comment
 * En este formato las latitudes y longitudes estan dadas de una forma rarao. Los tres primeros
 * digitos de la longitud son los grados, pero lugeo viene un numero que significa los segundos
 * estos segundos deben ser divididos entre 60 y annadidos a los grados. De esta forma es que se obtiene
 * los grados de forma estandar*/

public class PltFormat extends GPSFormat{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6377839403406736638L;
	private boolean discontinuity;

	public PltFormat() {
		// TODO Auto-generated constructor stub
	}


	public void setLongitude(String lon) {
		setLongitude(Double.parseDouble(lon));
		if (getLongitude() < 0 || getLongitude() > 180) throw new IncorrectLineFormatException("La " +
				"longitude resultante es "+getLongitude()+" lo cual esta fuera de rango");
	}


	public void setLatitude(String lat) {
		setLatitude(Double.parseDouble(lat));
		if (getLatitude() < 0 || getLatitude() > 90) throw new IncorrectLineFormatException("La " +
				"latitud resultante es "+getLatitude()+" lo cual esta fuera de rango");
	}


	public boolean isValidateData() {
		return true;
	}


	public void setDiscontinuity(String c) {
		if (c.equals("1")) discontinuity = true;
	}


	public boolean isDiscontinuity() {
		return discontinuity;
	}


	public void setDiscontinuity(boolean discontinuity) {
		this.discontinuity = discontinuity;
	}


	/** 17/06/2010 Trujillo Comment
	 * Aqui se dan en pies, por tanto debemos convertirlo a metros que es lo estandar
	 * un metro es 3.2808399 pies*/
	@Override
	public double getStandarHeight() {
		return getHeight()/3.2808399;
	}

	
	
}
