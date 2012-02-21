package wrappers;


public class SimpleFormat extends GPSFormat{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5943285422001742593L;

	public SimpleFormat(long time, double x, double y) {
		super.time = time;
		super.latitude = x;
		super.longitude = y;
	}
	
	@Override
	public double getStandarHeight() {
		return -1;
	}

	@Override
	public boolean isValidateData() {
		return true;
	}

}
