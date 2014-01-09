package util;

public class MinutesCoordinates {

	private int degrees;
	private int minutes; 
	private float sec;

	public MinutesCoordinates(int intValue, int minutes, float sec) {
		this.degrees = intValue;
		this.minutes = minutes;
		this.sec = sec;
	}

	public int getDegrees() {
		return degrees;
	}

	public void setDegrees(int intValue) {
		this.degrees = intValue;
	}

	public int getMinutes() {
		return minutes;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}

	public float getSec() {
		return sec;
	}

	public void setSec(float sec) {
		this.sec = sec;
	}

}
