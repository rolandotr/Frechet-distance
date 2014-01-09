package probability;

import java.math.BigDecimal;


public class KAnonymity implements Prob{
	
	int k;
	
	public KAnonymity(int k){
		this.k = k;
	}

	@Override
	public BigDecimal getSubsetProb(int i, int x, int value) {
		return new BigDecimal("1");
	}

	@Override
	public int getPivoteClusterSize() {
		return k;
	}
	
}
