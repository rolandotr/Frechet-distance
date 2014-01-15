package probability;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Hashtable;

import trajectory.Trajectory;
import util.MathFunctions;

public class ExperimentalProbability implements Prob{

	int k;
	int[] numberOfTriples;
	int m;
	Trajectory pivote;
	
	public ExperimentalProbability(Hashtable<Trajectory, Hashtable<Trajectory, Integer>> probabilities,
			int k, Trajectory pivote){
		this.k = k;
		this.pivote = pivote;
		m = probabilities.get(pivote).size();
		numberOfTriples = new int[m+1];
		int  cont = 1;
		Hashtable<Trajectory, Integer> cluster = probabilities.get(pivote);
		System.out.println("Cluster size = "+cluster.size());
		numberOfTriples[cont] = cluster.get(pivote);
		System.out.println("|S| = "+pivote.size()+" Ni = "+numberOfTriples[cont]);
		for (Trajectory t : probabilities.get(pivote).keySet()){
			if (pivote.equals(t)) continue;
			numberOfTriples[++cont] = probabilities.get(pivote).get(t);
			System.out.println("|S| = "+pivote.size()+" Ni = "+numberOfTriples[cont]);
		}
	}
	
	
	@Override
	public BigDecimal getSubsetProb(int i, int x, int value) {
		int ni = numberOfTriples[i];
		int s = pivote.size();
		BigDecimal result = new BigDecimal("0");
		for (int y = 0; y <= ni; y++){
			if (y > ni || x > (ni-y) || x > (s-value))
				continue;
			if (value < y) break;
			BigDecimal tmp;
			BigDecimal smallResult = new BigDecimal(MathFunctions.comb(value, y));			
			for (int j = 0; j < y; j++){
				double nume = ni-j;
				tmp = new BigDecimal(""+(nume)/(s-j));
				smallResult = smallResult.multiply(tmp);
			}
			for (int j = 0; j < value-y; j++){
				double nume = ni-y-j;
				nume = 1 - nume/(s-y-j);
				tmp = new BigDecimal(""+nume);
				smallResult = smallResult.multiply(tmp);
			}
			//System.out.println("smallresult = "+smallResult);
			BigInteger numerator = MathFunctions.comb(ni-y, x);
			BigInteger denominator = MathFunctions.comb(s-value, x);
			//BigInteger denominator = MathFunctions.comb(s-value, x);
			tmp = (new BigDecimal(numerator)).divide(
					new BigDecimal(denominator), 32, RoundingMode.DOWN);
			smallResult = smallResult.multiply(tmp);
			tmp = smallResult;
			result = result.add(tmp);
			/*System.out.println("Probability of y = "+y+" when s = "+s+" and value = "+value+
					" and x = "+x+" is ");			
			System.out.println("p = "+tmp.doubleValue()+" result = "+result.doubleValue());*/
			/*BigInteger numerator = MathFunctions.comb(ni, y).multiply(
					MathFunctions.comb(s-ni, value - y)).multiply(
					MathFunctions.comb(ni-y, x));
			BigInteger denominator = MathFunctions.comb(s, value).multiply(
					MathFunctions.comb(s-value, x));
			//BigInteger denominator = MathFunctions.comb(s-value, x);
			BigDecimal tmp = (new BigDecimal(numerator)).divide(
					new BigDecimal(denominator), 32, RoundingMode.UP);
			result = result.add(tmp);
			System.out.println("Probability of y = "+y+" when s = "+s+" and value = "+value+
					" and x = "+x+" is ");*/			
			//System.out.println("p = "+tmp.doubleValue()+" result = "+result.doubleValue());
			
		}
		return result;
	}


	@Override
	public int getPivoteClusterSize() {
		return m;
	}
	
	
}
