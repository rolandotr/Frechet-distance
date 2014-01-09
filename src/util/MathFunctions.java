package util;

import java.math.BigInteger;

public class MathFunctions {

	/** 25/11/2009 Trujillo Comment
	 * Factorial de n*/
	public static BigInteger fact(int n){
		return fact(n,1);
	}

	/** 25/11/2009 Trujillo Comment
	 * n!/u!*/
	public static BigInteger fact(int n, int u){
		BigInteger result =  new BigInteger(""+u);
		for (int i = u; i < n; i++) {
			result = result.multiply(new BigInteger(""+(i+1)));
		}
		return result;
	}

	public static BigInteger comb(int n, int k){
		if (n == k || k == 0) return new BigInteger("1");
		if (n < k) return new BigInteger("1");
		BigInteger num;
		BigInteger den;
		if (k < n - k) {
			num = fact(n, n-k+1);
			den = fact(k);
		}
		else{
			num = fact(n, k+1);
			den = fact(n-k);
		}
		return num.divide(den); 
	}


	public static double maximum(double[][] values){
		double max = values[0][0];
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < values.length; j++) {
				if (max < values[i][j]) max =values[i][j]; 
			}
		}
		return max;
	}
	public static double minimum(double[][] values){
		double min = values[0][0];
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < values.length; j++) {
				if (min > values[i][j]) min =values[i][j]; 
			}
		}
		return min;
	}

	public static int getPercetange(double min, double max, double value) {
		if (max < value || min > value) throw new RuntimeException();
		return (int)(100*(value-min)/(max-min));
	}
	

}
