package wrappers;


import exceptions.IncorrectLineFormatException;

/** 15/06/2010 Trujillo Comment
 * En este formato las latitudes y longitudes estan dadas de una forma rarao. Los tres primeros
 * digitos de la longitud son los grados, pero lugeo viene un numero que significa los segundos
 * estos segundos deben ser divididos entre 60 y annadidos a los grados. De esta forma es que se obtiene
 * los grados de forma estandar*/

public class LogFormat extends GPSFormat{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4341819522811133229L;

	private CardinalPoint longitudeCardinalPoint;
	
	private CardinalPoint latitudeCardinalPoint;
	
	private double speed;
	
	private boolean validateData;
	

	public LogFormat() {
		// TODO Auto-generated constructor stub
	}

	/** 15/06/2010 Trujillo Comment
	 * El setter es con el string donde los tres primeros digitos son el grado, luego vienen los
	 * segundos que deben ser divididos por 60 para entonces ser annadidos a los grados*/
	public void setLongitude(String l) {
		try{
		int degree = Integer.parseInt(l.substring(0, 3));
		double seconds = Double.parseDouble(l.substring(3));
		setLongitude(degree+seconds/60);
		}catch (NumberFormatException e) {
			throw new IncorrectLineFormatException("La " +
					"longitude resultante es "+getLongitude()+" y no es un numero");
		}
		if (getLongitude() < 0 || getLongitude() > 180) throw new IncorrectLineFormatException("La " +
				"longitude resultante es "+getLongitude()+" lo cual esta fuera de rango");
	}
	
	public CardinalPoint getLongitudeCardinalPoint() {
		return longitudeCardinalPoint;
	}
	public void setLongitudeCardinalPoint(CardinalPoint longitudeCardinalPoint) {
		this.longitudeCardinalPoint = longitudeCardinalPoint;
	}

	/** 16/06/2010 Trujillo Comment
	 * Para el caso de las longitudes debe ser este u oeste representados por E u O*/
	public void setLongitudeCardinalPoint(String s) {
		if (s.equals("E")) setLongitudeCardinalPoint(CardinalPoint.East);
		else if (s.equals("W")) setLongitudeCardinalPoint(CardinalPoint.West);
		else throw new IncorrectLineFormatException("El " +
				"punto cardinal resultante es "+s+" y deberia ser E u W");
	}

	/** 16/06/2010 Trujillo Comment
	 * Para el caso de las latitudes debe ser norte o sur representados por N o S*/
	public void setLatitudeCardinalPoint(String s) {
		if (s.equals("N")) setLongitudeCardinalPoint(CardinalPoint.North);
		else if (s.equals("S")) setLongitudeCardinalPoint(CardinalPoint.South);
		else throw new IncorrectLineFormatException("El " +
				"punto cardinal resultante es "+s+" y deberia ser N u S");
	}

	/** 15/06/2010 Trujillo Comment
	 * Ver lo de longitud que esto es lo mismo pero con un digito menos*/
	public void setLatitude(String l) {
		try{
		int degree = Integer.parseInt(l.substring(0, 2));
		double seconds = Double.parseDouble(l.substring(2));
		setLatitude(degree+seconds/60);
		}catch (NumberFormatException e) {
			throw new IncorrectLineFormatException("La " +
					"latitud resultante es "+getLatitude()+" y no es un numero");
		}
		if (getLatitude() < 0 || getLatitude() > 90) throw new IncorrectLineFormatException("La " +
				"latitud resultante es "+getLatitude()+" lo cual esta fuera de rango");
	}
	public CardinalPoint getLatitudeCardinalPoint() {
		return latitudeCardinalPoint;
	}
	public void setLatitudeCardinalPoint(CardinalPoint latitudeCardinalPoint) {
		this.latitudeCardinalPoint = latitudeCardinalPoint;
	}
	public double getSpeed() {
		return speed;
	}
	public void setSpeed(double speed) {
		this.speed = speed;
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

	public void setSpeed(String s) {
		try{
			double h = Double.parseDouble(s);
			setSpeed(h);
		}catch (NumberFormatException e) {
			/*throw new IncorrectLineFormatException("La " +
					"velocidad resultante es "+s+" y esto no es un numero");*/
		}
	}

	public boolean isValidateData() {
		return validateData;
	}

	public void setValidateData(boolean validateData) {
		this.validateData = validateData;
	}
	
	@Override
	public double getStandarHeight() {
		return getHeight();
	}
	
}
