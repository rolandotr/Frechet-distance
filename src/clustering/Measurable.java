package clustering;

import distances.Distance;

public interface Measurable<X> {

	/** 14/07/2010 Trujillo Comment
	 * La operacion esta debe ser modular*/
	public double distance(X x, Distance dist);
	
}
