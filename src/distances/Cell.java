package distances;


public class Cell{

	public long lastTime1, time1, lastTime2, time2;
	public double[] t1, t2, t3, t4;

	public Cell(long lastTime1, long time1, long lastTime2, long time2,
			double[] t1, double[] t2, double[] t3, double[] t4) {
		super();
		this.lastTime1 = lastTime1;
		this.time1 = time1;
		this.lastTime2 = lastTime2;
		this.time2 = time2;
		this.t1 = t1;
		/*if (t1 != null){
			t1[0] = Math.max(t1[0], lastTime2);
			t1[1] = Math.min(t1[1], time2);
		}*/
		this.t2 = t2;
		/*if (t2 != null){
			t2[0] = Math.max(t2[0], lastTime1);
			t2[1] = Math.min(t2[1], time1);
		}*/
		this.t3 = t3;
		/*if (t3 != null){
			t3[0] = Math.min(t3[0], time2);
			t3[1] = Math.max(t3[1], lastTime2);
		}*/
		this.t4 = t4;
		/*if (t4 != null){
			t4[0] = Math.min(t4[0], time1);
			t4[1] = Math.max(t4[1], lastTime1);
		}*/
	}
	
	public Cell clone() {
		double[] t1 = null;
		double[] t2 = null;
		double[] t3 = null;
		double[] t4 = null;
		if (this.t1 != null)
			t1 = new double[]{this.t1[0], this.t1[1]};
		if (this.t2 != null)
			t2 = new double[]{this.t2[0], this.t2[1]};
		if (this.t3 != null)
			t3 = new double[]{this.t3[0], this.t3[1]};
		if (this.t4 != null)
			t4 = new double[]{this.t4[0], this.t4[1]};
		return new Cell(lastTime1, time1, lastTime2, time2, t1, t2, t3, t4);
	}
	
	@Override
	public String toString() {
		String result1 = "Rectangle is ["+lastTime1+", "+lastTime2+"], ["+lastTime1+", "+time2+
		"], ["+time1+", "+time2+"], ["+time1+", "+lastTime2+"]";
		String t1S;
		String t2S;
		String t3S;
		String t4S;
		if (t1 == null) t1S = "[null]";
		else t1S = "["+this.t1[0]+", "+this.t1[1]+"]";
		if (t2 == null) t2S = "[null]";
		else t2S = "["+this.t2[0]+", "+this.t2[1]+"]";
		if (t3 == null) t3S = "[null]";
		else t3S = "["+this.t3[0]+", "+this.t3[1]+"]";
		if (t4 == null) t4S = "[null]";
		else t4S = "["+this.t4[0]+", "+this.t4[1]+"]";
		return result1+"\n"+t1S+"\n"+t2S+"\n"+t3S+"\n"+t4S;
	}

	public void setAllToNull() {
		t1 = null;
		t2 = null;
		t3 = null;
		t4 = null;
	}
}

