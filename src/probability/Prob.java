package probability;

import java.math.BigDecimal;
import java.math.BigInteger;

import util.MathFunctions;

public interface Prob{
	
	

	public BigDecimal getSubsetProb(int i, int x, int value);

	public int getPivoteClusterSize();

}

