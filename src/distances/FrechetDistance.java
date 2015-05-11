package distances;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;


import util.Geometry;
import util.Util;
import distances.Distance;
import distances.EuclideanDistance;
import wrappers.GPSFormat;
import wrappers.SimpleFormat;
import wrappers.SimpleTrajectory;
import wrappers.Trajectory;

public abstract class FrechetDistance extends Distance{

	
	protected Distance distance;
	protected double timeConstraint = 0; 
	protected int percentage;
	protected double compressThreshold = 0;

	public FrechetDistance(){
		percentage = -1; 
	}

	public FrechetDistance(int timeConstraintPercentage){
		this.percentage = timeConstraintPercentage;
	}

	protected void setTimeConstraint(Trajectory t1, Trajectory t2){
		long t1Length = t1.length();
		long t2Length = t2.length();
		long min = (t1Length < t2Length)?t1Length:t2Length;
		timeConstraint = min*(percentage/100);
	}

	public Transformation distanceWithTransformationOptimized(Trajectory t1, Trajectory t2){
		//System.out.println("Computing distance between "+t1);
		//System.out.println("and "+t2);		
		if (t1.size() == 1 || t2.size() == 1){
			return null;
		}
		setTimeConstraint(t1, t2);
		TreeMap<Double, Double> values = new TreeMap<Double, Double>();
		//List<Double> values = new LinkedList<Double>();
	//System.out.println("Extreme values");
		getExtremeValues(t1, t2, values);
	//Util.printValuesOfTree(values);
	//System.out.println("Proyection values");
		getProjectionValues(t1, t2, values);
	//Util.printValuesOfTree(values);
	//System.out.println("Meditriz values");
		getMediatrizValues(t1, t2, values); ///IMPORTANT!!!!, THIS SHOULD BE ACTIVED IN PRACTICE
	//Util.printValuesOfTree(values);
		//getMediatrizAndProjectionValues(t1, t2, values);
	//System.out.println("Worst cases values");
		getAWorstCaseValue(t1, t2, values);
	//Util.printValuesOfTree(values);
		
		double[] criticalValues = new double[values.size()];
		int cont = 0;
		for (Double value : values.values()){
			criticalValues[cont++] = value;
		}
		
		criticalValues = compressCriticalValues(criticalValues);
		//lo ponemos uno por encima para hacer un parche por errores numericos.
		//criticalValues[criticalValues.length-1] = criticalValues[criticalValues.length-2]+1;
		int min = 0;
		int max = criticalValues.length;
		double epsilon = -1;
		//Cell[][] freeSpace;
		double[][] monotoneFunctions = null;
		double[][] bestSoFar = null;
		double bestEpsilon = Double.MAX_VALUE;
		//System.out.println("Distance between "+t1.getIdentifier()+" and "+t2.getIdentifier());
		//System.out.println("Sizes equal to "+t1.size()+" and "+t2.size()+" respectively");
		//System.out.println("Critical values size = "+criticalValues.length);
		//x(t1, t2, epsilon);
		Cell[][] reachableFreeSpace = new Cell[t1.size()-1][t2.size()-1];
		while (max > min){
			//System.out.println("min = "+min+" and max = "+max);
			int pos = min+(max-min)/2;
			epsilon = criticalValues[pos];
			//System.out.println("epsilon = "+epsilon);
			//freeSpace = computeCellOfFreeSpace(t1, t2, epsilon);
			//freeSpace = computeReachableFreeSpaceOptimized(t1, t2, epsilon);
			//System.out.println("Computing reachable space for epsilon = "+ epsilon);
			computeReachableFreeSpaceOptimized(t1, t2, epsilon, reachableFreeSpace);
			//reachableFreeSpace = computeCellOfReachableFreeSpace(freeSpace);
			//System.out.println("Computing monotone curves for epsilon = "+ epsilon);
			monotoneFunctions = computeMonotoneCurves(reachableFreeSpace);
			if (monotoneFunctions == null){
				//epsilon is too low
				if (min == pos){
					//es porque max = min+1 o max = min, en ambos casos, damos por terminado el tema
					break;
				}
				min = pos;
			}
			else{
				//epsilon is too high
				max = pos;
				if (epsilon < bestEpsilon){
					bestSoFar = monotoneFunctions;
					bestEpsilon = epsilon;
				}
				//epsilon is too high
				/*System.out.println("alpha size = "+monotoneFunctions[0].length
						+" and beta size = "+monotoneFunctions[1].length
						+" and epsilon = "+epsilon
						+", best epsilon = "+bestEpsilon);*/
			}
//min = max;
		}
		//monotonoFunctions should contain the monotone values.
		if (bestSoFar == null) {
			System.out.println("These are hte values used");
			Util.printValuesOfArray(criticalValues);
			throw new RuntimeException("Could not compute frechet distance between \n"+t1+"\n"+t2);
		}
		//if (bestSoFar == null) return null;//esto es solo por ahora		
		return new Transformation(bestEpsilon, t1, t2, bestSoFar[0], bestSoFar[1]);
	}
	
	
	private double[] compressCriticalValues(double[] criticalValues) {
		List<Double> result = new ArrayList<Double>();
		double last = criticalValues[0];
		result.add(last);
		double threshold = compressThreshold;
		for (int i = 0; i < criticalValues.length; i++) {
			if ((criticalValues[i] - last) > threshold){
				last = criticalValues[i];
				result.add(last);
			}
		}
		double[] tmp = new double[result.size()];
		int cont = 0;
		for (Double d : result) {
			tmp[cont++] = d;
		}
		//System.out.println("Before = "+criticalValues.length+" and after = "+tmp.length);
		return tmp;
	}

	private void getAWorstCaseValue(Trajectory t1, Trajectory t2,
			TreeMap<Double, Double> values) {
		values.put(values.lastKey()+1, values.lastKey()+1);
		values.put((double)Short.MAX_VALUE, (double)Short.MAX_VALUE);
	}

	protected abstract void getMediatrizValues(Trajectory t1, Trajectory t2,
			TreeMap<Double, Double> values);

	protected abstract void getProjectionValues(Trajectory t1, Trajectory t2,
			TreeMap<Double, Double> values);

	@Override
	public double distance(Trajectory t1, Trajectory t2){
		Transformation t = distanceWithTransformationOptimized(t1, t2);
		if (t == null) return Double.MAX_VALUE;
		else return t.distance;
	}
	
	private void getExtremeValues(Trajectory t1, Trajectory t2, TreeMap<Double, Double> result) {
		GPSFormat p1 = t1.getPoint(t1.firstTime());
		GPSFormat p2 = t2.getPoint(t2.firstTime());
		double d = distance.distance(p1, p2);
		result.put(d, d);
		p1 = t1.getPoint(t1.lastTime());
		p2 = t2.getPoint(t2.lastTime());
		d = distance.distance(p1, p2);
		result.put(d, d);
	}

	public double[][] computeMonotoneCurves(Cell[][] reachableFreeSpace){
		if (reachableFreeSpace == null) return null;
		int i = reachableFreeSpace.length-1;
		int j = reachableFreeSpace[reachableFreeSpace.length-1].length-1;
		if (reachableFreeSpace[i][j].t2 == null || reachableFreeSpace[i][j].t3 == null) return null; 
		double actualX = reachableFreeSpace[i][j].time1;
		double actualY = reachableFreeSpace[i][j].time2;
		List<Double> trajectory1 = new ArrayList<Double>();
		List<Double> trajectory2 = new ArrayList<Double>();
		trajectory1.add(actualX);
		trajectory2.add(actualY);
		Cell tmp;
	//	while (actualX != reachableFreeSpace[0][0].lastTime1 || 
		//		actualY != reachableFreeSpace[0][0].lastTime2){
		boolean stop = false;
		do{
			//System.out.println("ActualX = "+actualX+" and ActualY = "+ actualY);
			//System.out.println("LastTime1 = "+reachableFreeSpace[0][0].lastTime1+" and LastTime2 = "+ reachableFreeSpace[0][0].lastTime1);
			if (i == 0 && j == 0) stop = true;
			tmp = reachableFreeSpace[i][j];
			if (tmp.t4 == null && tmp.t1 == null) {
				throw new RuntimeException();
			}
			if (tmp.t4 != null){
				actualX = tmp.t4[0];
				actualY = tmp.lastTime2;
				if (tmp.t4[0] == tmp.lastTime1 && i > 0 && j > 0 && 
						(reachableFreeSpace[i-1][j-1].t1 != null || reachableFreeSpace[i-1][j-1].t4 != null)) i--;
				j--;
			}
			else {				
				actualX = tmp.lastTime1;
				actualY = tmp.t1[0];
				if (tmp.t1[0] == tmp.lastTime2 && j > 0 && i > 0 &&
						(reachableFreeSpace[i-1][j-1].t1 != null || reachableFreeSpace[i-1][j-1].t4 != null)) j--;
				i--;
			}
			if (actualX < 0 || actualX == Double.NaN || actualY < 0 || actualY == Double.NaN){
				System.out.println("actualX = "+actualX+" and actualY = "+actualY);
			}
			//System.out.println("actualX = "+actualX+" and actualY = "+actualY);
			trajectory1.add(actualX);
			trajectory2.add(actualY);
		}while (!(actualX == reachableFreeSpace[0][0].lastTime1 && 
 				actualY == reachableFreeSpace[0][0].lastTime2) && !stop);
		//now we invert the values
		double[] t1 = new double[trajectory1.size()];
		double[] t2 = new double[trajectory2.size()];
		int cont = t1.length-1;
		for (Double reverse : trajectory1)
			t1[cont--] = reverse;
		cont = t2.length-1;
		for (Double reverse : trajectory2)
			t2[cont--] = reverse;
		return new double[][]{t1, t2};
	}

	@Override
	public double distance(Point p1, Point p2) {
		return distance.distance(p1, p2);
	}

	@Override
	public double distance(double x1, double y1, double x2, double y2) {
		return distance.distance(x1, y1, x2, y2);
	}

	@Override
	public double distance(GPSFormat p1, GPSFormat p2) {
		return distance.distance(p1, p2);
	}
	
	public Cell[][] computeCellOfFreeSpace(Trajectory t1, Trajectory t2, double epsilon){
		Cell[][] freeSpace = new Cell[t1.size()-1][t2.size()-1];
		long lastTime1 = -1;
		long lastTime2 = -1;
		GPSFormat p1;
		GPSFormat p2;
		GPSFormat lastP1 = null;
		GPSFormat lastP2 = null;
		int i = 0;
		int j = 0;
		for (long time1 : t1.times()) {
			if (lastP1 == null) {
				lastTime1 = time1;
				lastP1 = t1.getPoint(time1);
				continue;
			}
			p1 = t1.getPoint(time1);
			for (long time2 : t2.times()) {
				if (lastP2 == null) {
					lastTime2 = time2;
					lastP2 = t2.getPoint(time2);
					continue;
				}
				p2 = t2.getPoint(time2);
				freeSpace[i][j] = computesFreeSpaceForSegment(lastTime1, time1, lastP1, p1, 
						lastTime2, time2, lastP2, p2, epsilon);
				//System.out.println(freeSpace[i][j]);
				j++;
				lastTime2 = time2;
				lastP2 = p2;
			}
			i++;
			j = 0;
			lastP2 = null;
			lastTime1 = time1;
			lastP1 = p1;
		}
		return freeSpace;
	}
	
	
	
	private void fillFirstCell(Cell[][] cells, Trajectory t1, Trajectory t2, double epsilon){
		long lastTime1 = -1;
		long lastTime2 = -1;
		GPSFormat p1 = null;
		GPSFormat p2 = null;
		GPSFormat lastP1 = null;
		GPSFormat lastP2 = null;
		long time1 = -1;
		long time2 = -1;
		for (long t : t1.times()) {
			if (lastP1 == null) {
				lastTime1 = t;
				lastP1 = t1.getPoint(lastTime1);
				continue;
			}
			else{
				time1 = t;
				p1 = t1.getPoint(time1);
				break;
			}
		}
		//con esto tenemos los dos primeros puntos de t1.
		for (long t : t2.times()) {
			if (lastP2 == null) {
				lastTime2 = t;
				lastP2 = t2.getPoint(lastTime2);
				continue;
			}
			else{
				time2 = t;
				p2 = t2.getPoint(time2);
				break;
			}
		}
		//con esto tenemos los dos primeros puntos de t2.
		cells[0][0] = computesFreeSpaceForSegment(lastTime1, time1, lastP1, p1, 
				lastTime2, time2, lastP2, p2, epsilon);
//checkCell(cells[0][0]);
		convertTheFirstCellToReachable(cells[0][0]);
		//if (cells[0][0].isUnreachable()) cells[0][0] = null;
//checkCell(cells[0][0]);
	}
	
	public static void checkCell(Cell cell){
		if (cell.t1 != null) checkBounds(cell.t1);
		if (cell.t2 != null) checkBounds(cell.t2);
		if (cell.t3 != null) checkBounds(cell.t3);
		if (cell.t4 != null) checkBounds(cell.t4);
	}
	
	public static void checkBounds(double[] bounds){
		if (!(bounds[0] >= 0 && bounds[1] >= 0))
			throw new RuntimeException("0 = "+bounds[0]+", 1 = "+bounds[1]);
	}
	
	private void fillFirstColumn(Cell[][] cells, Trajectory t1, Trajectory t2, double epsilon){
		long lastTime1 = -1;
		long lastTime2 = -1;
		GPSFormat p1 = null;
		GPSFormat p2 = null;
		GPSFormat lastP1 = null;
		GPSFormat lastP2 = null;
		long time1 = -1;
		int j = 1;
		//a partir de aqui comenzamos a llenar la primera filea [0][0]....[0][t2.size-2].
		for (long t : t1.times()) {
			if (lastP1 == null) {
				lastTime1 = t;
				lastP1 = t1.getPoint(lastTime1);
				continue;
			}
			else{
				time1 = t;
				p1 = t1.getPoint(time1);
				break;
			}
		}
		//con esto tenemos los dos primeros puntos de t1.
		for (long time2 : t2.times()) {
			if (time2 == t2.firstTime()){
				//con esto, nos evitamos examinar el primero
				continue;
			}
			if (lastP2 == null) {
				lastTime2 = time2;
				lastP2 = t2.getPoint(time2);
				continue;
			}
			p2 = t2.getPoint(time2);
			cells[0][j] = computesFreeSpaceForSegment(lastTime1, time1, lastP1, p1, 
					lastTime2, time2, lastP2, p2, epsilon);
			cells[0][j].t1 = null;
//checkCell(cells[0][j]);
			convertAFirstColumnCellToReachable(cells[0][j], cells[0][j-1]);
//checkCell(cells[0][j]);
			j++;
			lastTime2 = time2;
			lastP2 = p2;
		}
	}
	
	private void fillFirstRow(Cell[][] cells, Trajectory t1, Trajectory t2, double epsilon){
		long lastTime1 = -1;
		long lastTime2 = -1;
		GPSFormat p1 = null;
		GPSFormat p2 = null;
		GPSFormat lastP1 = null;
		GPSFormat lastP2 = null;
		long time2 = -1;
		int i = 1;
		//a partir de aqui comenzamos a llenar la primera filea [0][0]....[0][t2.size-2].
		for (long t : t2.times()) {
			if (lastP2 == null) {
				lastTime2 = t;
				lastP2 = t2.getPoint(lastTime2);
				continue;
			}
			else{
				time2 = t;
				p2 = t2.getPoint(time2);
				break;
			}
		}
		//con esto tenemos los dos primeros puntos de t2.
		for (long time1 : t1.times()) {
			if (time1 == t1.firstTime()){
				//con esto, nos evitamos examinar el primero
				continue;
			}
			if (lastP1 == null) {
				lastTime1 = time1;
				lastP1 = t1.getPoint(time1);
				continue;
			}
			p1 = t1.getPoint(time1);
			cells[i][0] = computesFreeSpaceForSegment(lastTime1, time1, lastP1, p1, 
					lastTime2, time2, lastP2, p2, epsilon);
//checkCell(cells[i][0]);
			cells[i][0].t4 = null;
//checkCell(cells[i][0]);
			convertAFirstRowCellToReachable(cells[i][0], cells[i-1][0]);
//checkCell(cells[i][0]);
			//System.out.println(result[i][0]);
			i++;
			lastTime1 = time1;
			lastP1 = p1;
		}
	}
	
	private void fillRemainingCells(Cell[][] cells, Trajectory t1, Trajectory t2, double epsilon){
		long lastTime1 = -1;
		long lastTime2 = -1;
		GPSFormat p1 = null;
		GPSFormat p2 = null;
		GPSFormat lastP1 = null;
		GPSFormat lastP2 = null;
		int i = 1;
		int j = 1;
		for (long time1 : t1.times()) {
			if (time1 == t1.firstTime()) continue;
			if (lastP1 == null) {
				lastTime1 = time1;
				lastP1 = t1.getPoint(time1);
				continue;
			}
			p1 = t1.getPoint(time1);
			for (long time2 : t2.times()) {
				if (time2 == t2.firstTime()) continue;
				if (lastP2 == null) {
					lastTime2 = time2;
					lastP2 = t2.getPoint(time2);
					continue;
				}
				p2 = t2.getPoint(time2);
				cells[i][j] = computesFreeSpaceForSegment(lastTime1, time1, lastP1, p1, 
						lastTime2, time2, lastP2, p2, epsilon);
				//System.out.println(freeSpace[i][j]);
//checkCell(cells[i][j]);
				if (cells[i][j-1] == null || cells[i-1][j] == null){
					System.out.println("Here woth i="+i+" and j = "+j);
				}
				if (cells[i][j].t1 == null && cells[i][j].t4 == null){
					//es porque esta celda esta completamente tapada, asi que ni vale la pena convertirla
					cells[i][j].setAllToNull();
				}
				else convertAnInternalCellToReachable(cells[i][j], cells[i-1][j], cells[i][j-1]);
//checkCell(cells[i][j]);
				j++;
				lastTime2 = time2;
				lastP2 = p2;
			}
			i++;
			j = 1;
			lastP2 = null;
			lastTime1 = time1;
			lastP1 = p1;
		}
	}
	
	public void computeReachableFreeSpaceOptimized(Trajectory t1, Trajectory t2, double epsilon, Cell[][] result){
		//primero computamos el [0][0]
		fillFirstCell(result, t1, t2, epsilon);		
		//y ahora comenzamos a llenar la primera fila [0][1]....[0][t2.size-2].
		fillFirstRow(result, t1, t2, epsilon);
		//y ahora comenzamos a llenar la primera columna [1][0]....[t1.size-2][0].
		fillFirstColumn(result, t1, t2, epsilon);
		//y ahora el resto 
		fillRemainingCells(result, t1, t2, epsilon);
		
		//return null;
	}
	

	protected Cell computesFreeSpaceForSegment(long lastTime1, long time1,
			GPSFormat lastP1, GPSFormat p1, long lastTime2, long time2,
			GPSFormat lastP2, GPSFormat p2, double epsilon) {
		if (outOfTimeConstraint(lastTime1, lastTime2) && outOfTimeConstraint(lastTime1, time2)
				&& outOfTimeConstraint(time1, time2) && outOfTimeConstraint(time1, lastTime2)){
			return new Cell(lastTime1, time1, lastTime2, time2, null, null, null, null);
		}
		double[] t4 = computesTimesOfIntersectionWithElipse(lastTime1, time1, 
				lastP1, p1, lastP2, epsilon);
		double[] t2 = computesTimesOfIntersectionWithElipse(lastTime1, time1, 
				lastP1, p1, p2, epsilon);
		double[] t1 = computesTimesOfIntersectionWithElipse(lastTime2, time2, 
				lastP2, p2, lastP1, epsilon);
		double[] t3 = computesTimesOfIntersectionWithElipse(lastTime2, time2, 
				lastP2, p2, p1, epsilon);
		Cell result = new Cell(lastTime1, time1, lastTime2, time2, t1, t2, t3, t4);
//checkCell(result);
		if (t1 != null)
			t1 = computesTimesOfIntersectionWithLineX(lastTime1, t1, timeConstraint);
		if (t2 != null)
			t2 = computesTimesOfIntersectionWithLineY(time2, t2, timeConstraint);
		if (t3 != null)
			t3 = computesTimesOfIntersectionWithLineX(time1, t3, timeConstraint);
		if (t4 != null)
			t4 = computesTimesOfIntersectionWithLineY(lastTime2, t4, timeConstraint);
		result = new Cell(lastTime1, time1, lastTime2, time2, t1, t2, t3, t4);
//checkCell(result);
		return result;
	}
	
	/***Trujillo- Aug 24, 2012
	 * El  timeConstraint define dos rectas, donde se es menor que su negativo y donde se es mayor. 
	 * Por tanto, se calculan dos puntos (up, down) segun la interseccion con la regta "segment". "up" representa
	 * la intereseccion con > t, y "down" lo contrario. Se debe complir entonces que el segmento (up, down), 
	 * tenga alguna intereseccion con segment. Dicha intereseccio es la respuesta que buscamos.  
	 */
	private double[] computesTimesOfIntersectionWithLineX(long x, double[] segment,
			double timeConstraint) {
		if (percentage == -1) return segment;
		double y1 = -timeConstraint + x; 
		double y2 = timeConstraint + x;
		return segmentInteresection(y1, y2, segment[0], segment[1]);
	}
	
	private double[] segmentInteresection(double a, double b, double x,
			double y) {
		double resultFirst = a;
		double resultSecond = b;
		if (y <= b) {
			resultSecond = y;
		}
		if (x >= a) {
			resultFirst = x;
		}
		if (resultFirst > resultSecond) return null;
		return new double[]{resultFirst, resultSecond};
	}

	private double[] computesTimesOfIntersectionWithLineY(long y, double[] segment,
			double timeConstraint) {
		if (percentage == -1) return segment;
		double x1 = -timeConstraint + y; 
		double x2 = timeConstraint + y;
		return segmentInteresection(x1, x2, segment[0], segment[1]);
	}

	private boolean outOfTimeConstraint(long x, long y) {
		if (percentage == -1) return false;
		return (x-y > timeConstraint) || (x-y < - timeConstraint);
	}
	

	protected abstract double[] computesTimesOfIntersectionWithElipse(long t1, long t2, GPSFormat p1, 
			GPSFormat p2, GPSFormat p, double epsilon);
	
	private void convertTheFirstCellToReachable(Cell cell){
		if (cell.t1 != null){
			if (cell.t1[0] != cell.lastTime2){
				cell.setAllToNull();
			}
			else{
				if (cell.t4 == null){
					//esto no puede pasar en la primera casilla
					//throw new RuntimeException();
					//System.out.println("Warning!, first cell where t1 != null but t4 == null");
				}
			}
		}
		else{
			cell.setAllToNull();
			if (cell.t4 != null){
				//esto no puede pasar en la primera casilla
				//throw new RuntimeException();
				//System.out.println("Warning!, first cell where t1 == null but t4 != null");
			}
		}
		//notese que en la primera casilla, lo unico importante es que se pueda comenzar 
		//desde el principio.
	}
	
	private void convertAFirstColumnCellToReachable(Cell current, Cell previous){
		if (previous.t2 == null){
			current.setAllToNull();
		}
		else {
			if (current.t2 != null){
				current.t2[0] = Math.max(previous.t2[0], current.t2[0]);
				if (current.t2[0] > current.t2[1]) current.t2 = null;
			}
		}
		current.t4 = previous.t2;
		//System.out.println(result[0][j]);
	}
	
	private void convertAFirstRowCellToReachable(Cell current, Cell previous){
		if (previous.t3 == null){
			current.setAllToNull();
		}
		else {
			if (current.t3 != null){
				current.t3[0] = Math.max(previous.t3[0], current.t3[0]);
				if (current.t3[0] > current.t3[1]) current.t3 = null;
			}
		}
		current.t1 = previous.t3; 
		//System.out.println(result[0][j]);
	}
	
	private void convertAnInternalCellToReachable(Cell current, Cell previousLeft, Cell previousDown){
		current.t1 = previousLeft.t3; 
		current.t4 = previousDown.t2;
		if (current.t4 == null && current.t1 == null){
			current.setAllToNull();
		}
		else if (current.t4 != null && current.t1 != null){
			return;
		}
		else if (current.t4 != null && current.t1 == null){
			convertAFirstColumnCellToReachable(current, previousDown);
		}
		else{
			convertAFirstRowCellToReachable(current, previousLeft);
		}
	}
	
	/** 16/02/2012 Trujillo Comment
	 * Given the cells of a free space, it computes the reachable free space*/
	public Cell[][] computeCellOfReachableFreeSpace(Cell[][] freeSpace) {
		if (freeSpace == null) return null;
		//System.out.println("Computing reachability");
		Cell[][] result = new Cell[freeSpace.length][freeSpace[0].length];
		//result[0] = new Cell[freeSpace[0].length];
		result[0][0] = freeSpace[0][0].clone();
		convertTheFirstCellToReachable(result[0][0]);
		
		for (int j = 1; j < freeSpace[0].length; j++){
			result[0][j] = freeSpace[0][j].clone();
			result[0][j].t1 = null;
			convertAFirstColumnCellToReachable(result[0][j], result[0][j-1]);
		}
		//System.out.println(result[0][0]);
		for (int i = 1; i < freeSpace.length; i++){
			//result[i] = new Cell[freeSpace[i].length];			
			result[i][0] = freeSpace[i][0].clone();
			result[i][0].t4 = null;
			convertAFirstRowCellToReachable(result[i][0], result[i-1][0]);
		}
		for (int i = 1; i < freeSpace.length; i++){
			for (int j = 1; j < freeSpace[i].length; j++){
				result[i][j] = freeSpace[i][j].clone();
				convertAnInternalCellToReachable(result[i][j], result[i-1][j], result[i][j-1]);
			}
		}		
		return result;
	}

}
