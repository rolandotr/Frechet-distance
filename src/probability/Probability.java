package probability;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import clustering.MyTrajectoryFormat;

import util.MathFunctions;
import wrappers.SimpleFormat;
import wrappers.SimpleTrajectory;
import wrappers.Trajectory;

public class Probability {

	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
		//LinkedList<LinkedList<Integer>> result = getSums(5, 1, 4, 13);
		/*List<Trajectory> trajectories = new ArrayList<Trajectory>();
		Trajectory t1 = new SimpleTrajectory("1");
		Trajectory t2 = new SimpleTrajectory("2");
		Trajectory t3 = new SimpleTrajectory("3");
		t1.addPoint(new SimpleFormat(0, 1, 1));
		t1.addPoint(new SimpleFormat(1, 1, 1));
		t1.addPoint(new SimpleFormat(2, 1, 1));
		t2.addPoint(new SimpleFormat(0, 1, 1));
		t2.addPoint(new SimpleFormat(1, 1, 1));
		t2.addPoint(new SimpleFormat(2, 1, 1));
		t3.addPoint(new SimpleFormat(0, 1, 1));
		t3.addPoint(new SimpleFormat(1, 1, 1));
		trajectories.add(t1);
		trajectories.add(t2);
		trajectories.add(t3);
		Hashtable<Trajectory, Hashtable<Trajectory, Integer>> probabilities = new Hashtable<Trajectory, Hashtable<Trajectory,Integer>>();
		Hashtable<Trajectory, Integer> common = new Hashtable<Trajectory, Integer>();
		common.put(t1, 3);
		common.put(t2, 2);
		common.put(t3, 1);
		probabilities.put(t1, common);
		common = new Hashtable<Trajectory, Integer>();
		common.put(t1, 2);
		common.put(t2, 3);
		common.put(t3, 1);
		probabilities.put(t2, common);
		common = new Hashtable<Trajectory, Integer>();
		common.put(t1, 1);
		common.put(t2, 1);
		common.put(t3, 2);
		probabilities.put(t3, common);
		System.out.println(computeAfversarySuccessProbabilty(probabilities, 2));*/
		//SimpleTrajectory t1 = new SimpleTrajectory("yo");
		//SimpleTrajectory t2 = new SimpleTrajectory("yo");
		//MyTrajectoryFormat t2 = new MyTrajectoryFormat("yo");
		ObjectInputStream in = new ObjectInputStream(new FileInputStream("dynamic_k_2not_l_j_5.obj"));
		Hashtable<Trajectory, Hashtable<Trajectory, Integer>> probabilities = 
			(Hashtable<Trajectory, Hashtable<Trajectory, Integer>>)in.readObject();
		in.close();
		double p = Probability.computeAfversarySuccessProbabilty(probabilities, 2);
		System.out.println(p);
	}
	
	
	public static double computeAfversarySuccessProbabilty(Hashtable<Trajectory, Hashtable<Trajectory, Integer>> probabilities,
			 int k){
		double result = 0;
		for (Trajectory pivote : probabilities.keySet()){
			//if (!pivote.getIdentifier().equals("1494")) continue;
			System.out.println("Analyzing trajetory "+pivote);
			Prob p = new ExperimentalProbability(probabilities, k, pivote);
			double rigthSide = computeRightSide(pivote.size(), k, p);
			System.out.println("Probability is "+rigthSide);
			result += rigthSide;
		}
		return result/probabilities.size();
	}

	private static double computeRightSide(int sizeOfAdversaryKnowledge, int k,
			Prob p) {
		int m = p.getPivoteClusterSize();
		double result = 0;
		for (int l = 1; l <= sizeOfAdversaryKnowledge; l++){
			//double probT = p.probT(1, l, m, sizeOfAdversaryKnowledge-0);
			//if (probT == 0) continue;
			double rigthSide = computeRightSide2(sizeOfAdversaryKnowledge, k, p, l, m);
			//System.out.println("With l = "+l+" p = "+rigthSide);
			result += rigthSide;
		}
		return result;
	}

	private static double computeRightSide2(int sizeOfAdversaryKnowledge, int k,
			Prob p, int l, int m) {
		double result = 0;
		for (int c = 1; c <= m; c++){
			double probC = (double)1/((double)(c));
			double smallResult = 0;
			for (LinkedList<Integer> x : getSumsComplete(m, c, l, sizeOfAdversaryKnowledge)){
				double product = 1;
				int cont = 1;
				int value = 0;
				for (Integer triples : x){
					BigDecimal p1 = new BigDecimal(""+(double)1/k);
					p1 = p1.pow(triples);
					BigDecimal p2 = new BigDecimal(MathFunctions.comb(sizeOfAdversaryKnowledge-value, triples));
					product *= p2.multiply(p1).multiply(p.getSubsetProb(cont, triples, value)).doubleValue();
					//product *= p2.multiply(p1).doubleValue();
					//product *= p.probT(cont, triples, m, sizeOfAdversaryKnowledge-value);
					value += triples;
					cont++;
				}
				smallResult += product;
			}
			result += probC*smallResult;
		}
		return result;
	}

	
	private static LinkedList<LinkedList<Integer>> getSumsComplete(int m, int c, int l,
			int sizeOfAdversaryKnowledge) {
		LinkedList<LinkedList<Integer>> result = getSums(m-1, c-1, l, sizeOfAdversaryKnowledge-l);
		for (LinkedList<Integer> list : result){
			list.addFirst(l);
		}
		return result;
	}

	/** 15/06/2011 Trujillo Comment
	 * Inicialmente pos = 0, o sea, este metodo debe ser llamado con pos = 0. Esto significa
	 * que se buscaran valores desde x_2, ..., x_m. A medida que se hacen llamadas recursivas
	 * entonces se buscaran los valores de x_{pos+1}, ...,x_m*/
	private static LinkedList<LinkedList<Integer>> getSums(int m, int c, int l,
			int value) {
		LinkedList<LinkedList<Integer>> result = new LinkedList<LinkedList<Integer>>();
		/** 15/06/2011 Trujillo Comment
		 * Cuando es que esto no tiene solucion*/
		//cuando el valor es menor que cero seguro
		if (value < 0) return result;
		//si hay mas iguales que valores posibles
		if (m < c) return result;
		//cuando no hay forma de alcanzar el valor deseado
		if (value < l*c) return result;
		//se sabe que el primerp tendra valor l
		if (m == 1){
			//en este caso solo hay uno que queda
			if (value > l) return result;
			if (c == 1 && value < l) return result;
			if (c == 0 && value == l) return result;
			LinkedList<Integer> tmp = new LinkedList<Integer>();
			tmp.add(value);
			result.add(tmp);
			if (c > 1) throw new RuntimeException();
			return result;
		}
		for (int i = 0; i < l; i++){
			LinkedList<LinkedList<Integer>> tmp = getSums(m-1, c, l, value-i);
			for (LinkedList<Integer> list : tmp){
				list.addFirst(i);
			}
			for (LinkedList<Integer> list : tmp){
				result.add(list);
			}
		}
		if (c > 0){
			//entonces puedo poner a l
			LinkedList<LinkedList<Integer>> tmp = getSums(m-1, c-1, l, value-l);
			for (LinkedList<Integer> list : tmp){
				list.addFirst(l);
			}
			for (LinkedList<Integer> list : tmp){
				result.add(list);
			}
		}
		return result;
	}
}
