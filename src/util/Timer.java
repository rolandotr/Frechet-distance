package util;

public class Timer {

	private long ini; 
	
	public Timer(){
		
	}
	
	public void reset(){
		ini = System.currentTimeMillis();
	}
	
	public double getTimeInHours(){
		return ((double)(System.currentTimeMillis()-ini))/(1000*60*60);
	}
}
