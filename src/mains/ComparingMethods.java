package mains;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import range_queries.DAIQuery;
import range_queries.PSIQuery;
import range_queries.Query;

import util.GnuPlot;
import util.Latex;
import util.Print;
import util.Statistics;
import util.Timer;
import util.Util;
import wrappers.GPSFormat;
import wrappers.SimpleTrajectory;
import wrappers.Trajectory;
import algorithms.AnonymizationMethod;
import algorithms.frechet.FrechetCentroideMethod;
import algorithms.frechet.FrechetTwiceCentroideMethod;
import algorithms.generalization.GeneralizationCentroideMethod;
import algorithms.myalgorithm.MyCentroideMethod;
import algorithms.permutation.JosepCentroideMethod;
import clustering.MyTrajectoryFormat;
import distances.Distance;
import distances.EuclideanDistance;
import distances.FrechetDistance;
import distances.FrechetDistanceGPSBased;
import distances.GPSDistance;
import distances.JosepEuclideanDistanceOnTheFlyVs2;
import distances.LogCostDistance;
import distances.JosepGPSDistanceOnTheFlyVs2;
import distances.MyContextDependantDynamicDistance;
import distances.MyDistanceEuclideanBased;
import distances.MyDistanceGPSBased;
import distances.MyDynamicDistance;
import distances.MyLightContextDependantDynamicDistance;
import distances.Transformation;

public class ComparingMethods {

	//private static final double[] SPATIAL_RANGE = new double[]{0, 0.2, 0.5, 1, 2, 5};//in Km 
	private static final double[] SPATIAL_RANGE = new double[]{0, 20, 50, 100, 200, 500};//in Km 
	private static final long[] TEMPORAL_RANGE = new long[]{0, 5*60, 10*60, 30*60, 60*60, 5*60*60};//in seconds 
	private static final int[] K = new int[]{4};//cluster sizes 
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		/*System.setOut(new PrintStream(new File("out.txt")));
		
		Hashtable<String, Trajectory> dst = MyTrajectoryFormat.loadCompressedTrajecotries("real");
		AnonymizationMethod[] methods = new AnonymizationMethod[3];
		methods[0] = new MyCentroideMethod("real", new MyDistanceGPSBased(3));
		methods[1] = new MyCentroideMethod("real", new MyLightContextDependantDynamicDistance(3) {
			@Override
			public double distance(GPSFormat p1, GPSFormat p2) {
				return new GPSDistance().distance(p1, p2);
			}
			@Override
			public double distance(double x1, double y1, double x2, double y2) {
				throw new RuntimeException();
			}
			@Override
			public double distance(Point p1, Point p2) {
				throw new RuntimeException();
			}
		});
		methods[1].setName("mylightone");
		//methods[2] = new MyCentroideMethod(folderOfDataSet, new MyDistanceGPSBased(3));
		methods[2] = new GeneralizationCentroideMethod("real", new LogCostDistance());

		System.out.println("MyDistanceGPSBased with 4");
		methods[0].createClusters(4);
		System.out.println("MyLightContext with 4");
		methods[1].createClusters(4);
		System.out.println("GeneralizationBased with 4");
		methods[2].createClusters(4);
		
		System.out.println("MyDistanceGPSBased with 3");
		methods[0].createClusters(3);
		System.out.println("MyLightContext with 3");
		methods[1].createClusters(3);
		System.out.println("GeneralizationBased with 3");
		methods[2].createClusters(3);
		*/
		
		//tabularComparisonRealDatabase();
		tabularComparisonSynteticDatabase();
		
		//checkClustersUniformity("comparison_real/4/");

		
	}
	
	private static void tabularComparisonRealDatabase() throws IOException, ClassNotFoundException{
		tabularComparison("real", "comparison_real");
		//tabularComparisonOneToOne("real", "comparison_real");
	}
	
	private static void tabularComparisonSynteticDatabase() throws IOException, ClassNotFoundException{
		tabularComparisonSyntetic("syntetic", "comparison_syntetic");
		//tabularComparisonOneToOne("real", "comparison_real");
	}
	

	private static void tabularComparison(String folderOfDataSet, String folderOfDestiny) throws IOException, ClassNotFoundException{
		//all the results of this method will be stored in a folder named folderOfDestiny.
		//Hashtable<String, Trajectory> dst = MyTrajectoryFormat.loadTrajecotries(folderOfDataSet);
		Hashtable<String, Trajectory> dst = MyTrajectoryFormat.loadCompressedTrajecotries(folderOfDataSet);
		AnonymizationMethod[] methods = new AnonymizationMethod[4];
		methods[0] = new MyCentroideMethod(folderOfDataSet, new MyDistanceGPSBased(3));
		methods[1] = new MyCentroideMethod(folderOfDataSet, new MyLightContextDependantDynamicDistance(3) {
			@Override
			public double distance(GPSFormat p1, GPSFormat p2) {
				return new GPSDistance().distance(p1, p2);
			}
			@Override
			public double distance(double x1, double y1, double x2, double y2) {
				throw new RuntimeException();
			}
			@Override
			public double distance(Point p1, Point p2) {
				throw new RuntimeException();
			}
		});
		methods[1].setName("mylightone");
		//methods[2] = new MyCentroideMethod(folderOfDataSet, new MyDistanceGPSBased(3));
		methods[2] = new GeneralizationCentroideMethod(folderOfDataSet, new LogCostDistance());
		methods[3] = new JosepCentroideMethod(folderOfDataSet, new JosepGPSDistanceOnTheFlyVs2());
		//methods[3] = new FrechetCentroideMethod(folderOfDataSet, new FrechetDistanceGPSBased());
		//the methods are created
		//this data set is only for comparison during the queries
		GPSDistance gpsDist = new GPSDistance();		
		List<Trajectory> toCompareDataset = MyTrajectoryFormat.loadTrajecotriesInList(folderOfDataSet, folderOfDataSet+"_toCompare.obj");
		//List<Trajectory> toCompareDataset = MyTrajectoryFormat.loadCompressedTrajecotriesInList(folderOfDataSet);
		//this is also for comparison only
		List<List<Trajectory>> clusters;
		List<List<Trajectory>> clustersTmp; 
		//to save the generated clusters by each method
		Query querpsi = new PSIQuery();
		Query querdai = new DAIQuery();
		for (int k : K){
			//each k will generate a different set of results, therefore, it will be located in a different folder
			//the folder is folderOfDestiny/k
			String folderForK = "./"+folderOfDestiny+"/"+k+"/";
			File f = new File(folderForK);
			if (!f.exists()) f.mkdirs();
			double[][][][] psiValues = new double[methods.length][methods.length][SPATIAL_RANGE.length][TEMPORAL_RANGE.length]; 
			double[][][][] daiValues = new double[methods.length][methods.length][SPATIAL_RANGE.length][TEMPORAL_RANGE.length]; 
			//both psiValus and daiValues have four dimnsions x,y,z,w. X and y are the method used for clustering and anonymization 
			//respectively. Note that, the indexs come from the array methods. Then, z and w represent the spatial and temporal values used 
			//according to the arrays SPATIAL_RANGE TEMPORAL_RANGE respectively. All this, i.e. the method for clustering, the method
			//for anonymization, the spatial radious used, the time interval used, output a query range value stored in psiValues for 
			//psi and daiValues for dai.
			for (int a = 0; a < methods.length; a++) {
				System.out.println("Clustering with "+methods[a]);
				clusters = methods[a].createClusters(k);
				saveClustersToDisk(clusters, folderForK, methods[a].getName()+"-clusters");
				//Print.printList(clusters);
				//here we have the clusters
				for (int b = 0; b < methods.length; b++) {
					System.out.println("Anonymizing with "+methods[b].toString());
					clustersTmp = copyClusters(clusters);
					//we copy the clusters because usually anonymization mehtods modify it and don't want to create it again. 
					List<Trajectory> dAnonym = methods[b].anonymizeAll(clustersTmp);
					//anonymization set created.
					saveToDisk(dAnonym, folderForK, methods[a].getName()+"-"+methods[b].getName());
					for (int i = 0; i < SPATIAL_RANGE.length; i++){						
						for (int j = 0; j < TEMPORAL_RANGE.length; j++){
							System.out.println("Executing queries for radious = "+SPATIAL_RANGE[i]+" and time interval = "+TEMPORAL_RANGE[j]);
							psiValues[a][b][i][j] = querpsi.averageQueries(dst.values(), toCompareDataset, dAnonym, SPATIAL_RANGE[i], TEMPORAL_RANGE[j], 10000, gpsDist);
							System.out.println("Psi = "+psiValues[a][b][i][j]);
							daiValues[a][b][i][j] = querdai.averageQueries(dst.values(), toCompareDataset, dAnonym, SPATIAL_RANGE[i], TEMPORAL_RANGE[j], 10000, gpsDist);
							System.out.println("Dai = "+daiValues[a][b][i][j]);
						}					
					}
				}
			}
			//we have this done for a particular k. Meanining that we can already print results.
			//Now we should prepare the values to be printed
			Hashtable<String, double[][]> psiCharts = createCrossedTable(psiValues, methods);
			Hashtable<String, double[][]> daiCharts = createCrossedTable(daiValues, methods);
			printCharts(psiCharts, folderForK, "psi");
			printCharts(daiCharts, folderForK, "dai");
			printTables(methods, psiValues, folderForK, "psi");
			printTables(methods, daiValues, folderForK, "dai");
		}
	}

	private static void tabularComparisonSyntetic(String folderOfDataSet, String folderOfDestiny) throws IOException, ClassNotFoundException{
		//all the results of this method will be stored in a folder named folderOfDestiny.
		//Hashtable<String, Trajectory> dst = MyTrajectoryFormat.loadTrajecotries(folderOfDataSet);
		Hashtable<String, Trajectory> dst = MyTrajectoryFormat.loadCompressedTrajecotries(folderOfDataSet);
		AnonymizationMethod[] methods = new AnonymizationMethod[4];
		methods[0] = new MyCentroideMethod(folderOfDataSet, new MyDistanceEuclideanBased(3));
		methods[1] = new MyCentroideMethod(folderOfDataSet, new MyLightContextDependantDynamicDistance(3) {
			@Override
			public double distance(GPSFormat p1, GPSFormat p2) {
				return new EuclideanDistance().distance(p1, p2);
			}
			@Override
			public double distance(double x1, double y1, double x2, double y2) {
				throw new RuntimeException();
			}
			@Override
			public double distance(Point p1, Point p2) {
				throw new RuntimeException();
			}
		});
		methods[1].setName("mylightone");
		//methods[2] = new MyCentroideMethod(folderOfDataSet, new MyDistanceGPSBased(3));
		methods[2] = new GeneralizationCentroideMethod(folderOfDataSet, new LogCostDistance());
		methods[3] = new JosepCentroideMethod(folderOfDataSet, new JosepEuclideanDistanceOnTheFlyVs2());
		//methods[3] = new FrechetCentroideMethod(folderOfDataSet, new FrechetDistanceGPSBased());
		//the methods are created
		//this data set is only for comparison during the queries
		EuclideanDistance gpsDist = new EuclideanDistance();		
		List<Trajectory> toCompareDataset = MyTrajectoryFormat.loadTrajecotriesInList(folderOfDataSet, folderOfDataSet+"_toCompare.obj");
		//List<Trajectory> toCompareDataset = MyTrajectoryFormat.loadCompressedTrajecotriesInList(folderOfDataSet);
		//this is also for comparison only
		List<List<Trajectory>> clusters;
		List<List<Trajectory>> clustersTmp; 
		//to save the generated clusters by each method
		Query querpsi = new PSIQuery();
		Query querdai = new DAIQuery();
		for (int k : K){
			//each k will generate a different set of results, therefore, it will be located in a different folder
			//the folder is folderOfDestiny/k
			String folderForK = "./"+folderOfDestiny+"/"+k+"/";
			File f = new File(folderForK);
			if (!f.exists()) f.mkdirs();
			double[][][][] psiValues = new double[methods.length][methods.length][SPATIAL_RANGE.length][TEMPORAL_RANGE.length]; 
			double[][][][] daiValues = new double[methods.length][methods.length][SPATIAL_RANGE.length][TEMPORAL_RANGE.length]; 
			//both psiValus and daiValues have four dimnsions x,y,z,w. X and y are the method used for clustering and anonymization 
			//respectively. Note that, the indexs come from the array methods. Then, z and w represent the spatial and temporal values used 
			//according to the arrays SPATIAL_RANGE TEMPORAL_RANGE respectively. All this, i.e. the method for clustering, the method
			//for anonymization, the spatial radious used, the time interval used, output a query range value stored in psiValues for 
			//psi and daiValues for dai.
			for (int a = 0; a < methods.length; a++) {
				System.out.println("Clustering with "+methods[a]);
				clusters = methods[a].createClusters(k);
				saveClustersToDisk(clusters, folderForK, methods[a].getName()+"-clusters");
				//Print.printList(clusters);
				//here we have the clusters
				for (int b = 0; b < methods.length; b++) {
					System.out.println("Anonymizing with "+methods[b].toString());
					clustersTmp = copyClusters(clusters);
					//we copy the clusters because usually anonymization mehtods modify it and don't want to create it again. 
					List<Trajectory> dAnonym = methods[b].anonymizeAll(clustersTmp);
					//anonymization set created.
					saveToDisk(dAnonym, folderForK, methods[a].getName()+"-"+methods[b].getName());
					for (int i = 0; i < SPATIAL_RANGE.length; i++){						
						for (int j = 0; j < TEMPORAL_RANGE.length; j++){
							System.out.println("Executing queries for radious = "+SPATIAL_RANGE[i]+" and time interval = "+TEMPORAL_RANGE[j]);
							psiValues[a][b][i][j] = querpsi.averageQueries(dst.values(), toCompareDataset, dAnonym, SPATIAL_RANGE[i], TEMPORAL_RANGE[j], 10000, gpsDist);
							System.out.println("Psi = "+psiValues[a][b][i][j]);
							daiValues[a][b][i][j] = querdai.averageQueries(dst.values(), toCompareDataset, dAnonym, SPATIAL_RANGE[i], TEMPORAL_RANGE[j], 10000, gpsDist);
							System.out.println("Dai = "+daiValues[a][b][i][j]);
						}					
					}
				}
			}
			//we have this done for a particular k. Meanining that we can already print results.
			//Now we should prepare the values to be printed
			Hashtable<String, double[][]> psiCharts = createCrossedTable(psiValues, methods);
			Hashtable<String, double[][]> daiCharts = createCrossedTable(daiValues, methods);
			printCharts(psiCharts, folderForK, "psi");
			printCharts(daiCharts, folderForK, "dai");
			printTables(methods, psiValues, folderForK, "psi");
			printTables(methods, daiValues, folderForK, "dai");
		}
	}

	private static void tabularComparisonOneToOne(String folderOfDataSet, String folderOfDestiny) throws IOException, ClassNotFoundException{
		//all the results of this method will be stored in a folder named folderOfDestiny.
		//Hashtable<String, Trajectory> dst = MyTrajectoryFormat.loadTrajecotries(folderOfDataSet);
		Hashtable<String, Trajectory> dst = MyTrajectoryFormat.loadCompressedTrajecotries(folderOfDataSet);
		AnonymizationMethod[] methods = new AnonymizationMethod[4];
		methods[0] = new MyCentroideMethod(folderOfDataSet, new MyDistanceGPSBased(3));
		methods[1] = new MyCentroideMethod(folderOfDataSet, new MyLightContextDependantDynamicDistance(3) {
			@Override
			public double distance(GPSFormat p1, GPSFormat p2) {
				return new GPSDistance().distance(p1, p2);
			}
			@Override
			public double distance(double x1, double y1, double x2, double y2) {
				throw new RuntimeException();
			}
			@Override
			public double distance(Point p1, Point p2) {
				throw new RuntimeException();
			}
		});
		methods[1].setName("mylightone");
		//methods[2] = new MyCentroideMethod(folderOfDataSet, new MyDistanceGPSBased(3));
		methods[2] = new GeneralizationCentroideMethod(folderOfDataSet, new LogCostDistance());
		methods[3] = new JosepCentroideMethod(folderOfDataSet, new JosepGPSDistanceOnTheFlyVs2());
		//methods[3] = new FrechetCentroideMethod(folderOfDataSet, new FrechetDistanceGPSBased());
		//the methods are created
		//this data set is only for comparison during the queries
		GPSDistance gpsDist = new GPSDistance();		
		List<Trajectory> toCompareDataset = MyTrajectoryFormat.loadTrajecotriesInList(folderOfDataSet, folderOfDataSet+"_toCompare.obj");
		//List<Trajectory> toCompareDataset = MyTrajectoryFormat.loadCompressedTrajecotriesInList(folderOfDataSet);
		//this is also for comparison only
		List<List<Trajectory>> clusters;
		List<List<Trajectory>> clustersTmp; 
		//to save the generated clusters by each method
		Query querpsi = new PSIQuery();
		Query querdai = new DAIQuery();
		for (int k : K){
			//each k will generate a different set of results, therefore, it will be located in a different folder
			//the folder is folderOfDestiny/k
			String folderForK = "./"+folderOfDestiny+"/"+k+"/";
			File f = new File(folderForK);
			if (!f.exists()) f.mkdirs();
			double[][][] psiValues = new double[methods.length][SPATIAL_RANGE.length][TEMPORAL_RANGE.length]; 
			double[][][] daiValues = new double[methods.length][SPATIAL_RANGE.length][TEMPORAL_RANGE.length]; 
			//both psiValus and daiValues have four dimnsions x,y,z,w. X and y are the method used for clustering and anonymization 
			//respectively. Note that, the indexs come from the array methods. Then, z and w represent the spatial and temporal values used 
			//according to the arrays SPATIAL_RANGE TEMPORAL_RANGE respectively. All this, i.e. the method for clustering, the method
			//for anonymization, the spatial radious used, the time interval used, output a query range value stored in psiValues for 
			//psi and daiValues for dai.
			for (int a = 0; a < methods.length; a++) {
				System.out.println("Clustering with "+methods[a]);
				clusters = methods[a].createClusters(k);
				saveClustersToDisk(clusters, folderForK, methods[a].getName()+"-clusters");
				//Print.printList(clusters);
				//here we have the clusters
				System.out.println("Anonymizing with "+methods[a].toString());
				clustersTmp = copyClusters(clusters);
				//we copy the clusters because usually anonymization mehtods modify it and don't want to create it again. 
				List<Trajectory> dAnonym = methods[a].anonymizeAll(clustersTmp);
				//anonymization set created.
				saveToDisk(dAnonym, folderForK, methods[a].getName()+"-"+methods[a].getName());
				for (int i = 0; i < SPATIAL_RANGE.length; i++){						
					for (int j = 0; j < TEMPORAL_RANGE.length; j++){
						System.out.println("Executing queries for radious = "+SPATIAL_RANGE[i]+" and time interval = "+TEMPORAL_RANGE[j]);
						psiValues[a][i][j] = querpsi.averageQueries(dst.values(), toCompareDataset, dAnonym, SPATIAL_RANGE[i], TEMPORAL_RANGE[j], 10000, gpsDist);
						System.out.println("Psi = "+psiValues[a][i][j]);
						daiValues[a][i][j] = querdai.averageQueries(dst.values(), toCompareDataset, dAnonym, SPATIAL_RANGE[i], TEMPORAL_RANGE[j], 10000, gpsDist);
						System.out.println("Dai = "+daiValues[a][i][j]);
					}					
				}
			}
			//we have this done for a particular k. Meanining that we can already print results.
			//Now we should prepare the values to be printed
			Hashtable<String, double[][]> psiCharts = createCrossedTable(psiValues, methods);
			Hashtable<String, double[][]> daiCharts = createCrossedTable(daiValues, methods);
			printCharts(psiCharts, folderForK, "psi");
			printCharts(daiCharts, folderForK, "dai");
			//printTables(methods, psiValues, folderForK, "psi");
			//printTables(methods, daiValues, folderForK, "dai");
		}
	}
	private static void checkClustersUniformity(String folder) throws FileNotFoundException, IOException, ClassNotFoundException {
		File f = new File(folder);
		String[] fileNames = f.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.contains("clusters") && name.contains(".obj"); 
			}
		});
		for (String fileName : fileNames) {
			System.out.println("");
			System.out.println("Analyzing "+fileName);
			System.out.println("");
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(folder+fileName)));
			List<List<Trajectory>> clusters = (List<List<Trajectory>>)in.readObject();
			in.close();
			for (List<Trajectory> list : clusters) {
				String preffix = null;
				for (Trajectory trajectory : list) {
					if (preffix == null){
						preffix = trajectory.getIdentifier().substring(0, trajectory.getIdentifier().length()-2);
					}
					else{
						String nextPreffix = trajectory.getIdentifier().substring(0, trajectory.getIdentifier().length()-2);
						if (!preffix.equals(nextPreffix)){
							System.out.println("The following cluster have been founded not to b uniform");
							Print.printList(list);
							break;
						}
					}
				}
			}
		}
	}

	private static void saveToDisk(List<Trajectory> dAnonym, String folderForK,
			String fileName) throws IOException {
		File f = new File(folderForK+"/"+fileName+".obj");
		//f.mkdir();
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
		out.writeObject(dAnonym);
		out.close();
	}

	private static void saveClustersToDisk(List<List<Trajectory>> dAnonym, String folderForK,
			String fileName) throws IOException {
		File f = new File(folderForK+"/"+fileName+".obj");
		//f.mkdir();
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
		out.writeObject(dAnonym);
		out.close();
	}

	private static void tabularComparisonFromDisk(String folderOfDataSet, String folderOfDestiny) throws IOException, ClassNotFoundException{
		//all the results of this method will be stored in a folder named folderOfDestiny.
		Hashtable<String, Trajectory> dst = MyTrajectoryFormat.loadTrajecotries(folderOfDataSet);
		AnonymizationMethod[] methods = new AnonymizationMethod[5];
		methods[0] = new MyCentroideMethod(folderOfDataSet, new MyDistanceGPSBased(3));
		methods[1] = new MyCentroideMethod(folderOfDataSet, new MyLightContextDependantDynamicDistance(3) {
			@Override
			public double distance(GPSFormat p1, GPSFormat p2) {
				return new GPSDistance().distance(p1, p2);
			}
			@Override
			public double distance(double x1, double y1, double x2, double y2) {
				throw new RuntimeException();
			}
			@Override
			public double distance(Point p1, Point p2) {
				throw new RuntimeException();
			}
		});
		methods[1].setName("mylightone");
		methods[2] = new MyCentroideMethod(folderOfDataSet, new MyDistanceGPSBased(3));
		methods[3] = new GeneralizationCentroideMethod(folderOfDataSet, new LogCostDistance());
		methods[4] = new JosepCentroideMethod(folderOfDataSet, new JosepGPSDistanceOnTheFlyVs2());
		//methods[3] = new FrechetCentroideMethod(folderOfDataSet, new FrechetDistanceGPSBased());
		//the methods are created
		//this data set is only for comparison during the queries
		GPSDistance gpsDist = new GPSDistance();		
		List<Trajectory> toCompareDataset = MyTrajectoryFormat.loadTrajecotriesInList(folderOfDataSet, folderOfDataSet+"_toCompare.obj");
		//this is also for comparison only
		List<List<Trajectory>> clusters;
		List<List<Trajectory>> clustersTmp; 
		//to save the generated clusters by each method
		Query querpsi = new PSIQuery();
		Query querdai = new DAIQuery();
		for (int k : K){
			//each k will generate a different set of results, therefore, it will be located in a different folder
			//the folder is folderOfDestiny/k
			String folderForK = "./"+folderOfDestiny+"/"+k+"/";
			double[][][][] psiValues = new double[methods.length][methods.length][SPATIAL_RANGE.length][TEMPORAL_RANGE.length]; 
			double[][][][] daiValues = new double[methods.length][methods.length][SPATIAL_RANGE.length][TEMPORAL_RANGE.length]; 
			//both psiValus and daiValues have four dimnsions x,y,z,w. X and y are the method used for clustering and anonymization 
			//respectively. Note that, the indexs come from the array methods. Then, z and w represent the spatial and temporal values used 
			//according to the arrays SPATIAL_RANGE TEMPORAL_RANGE respectively. All this, i.e. the method for clustering, the method
			//for anonymization, the spatial radious used, the time interval used, output a query range value stored in psiValues for 
			//psi and daiValues for dai.
			for (int a = 0; a < methods.length; a++) {
				System.out.println("Clustering with "+methods[a]);
				clusters = methods[a].createClusters(k);
				//here we have the clusters
				for (int b = 0; b < methods.length; b++) {
					System.out.println("Anonymizing with "+methods[b].toString());
					clustersTmp = copyClusters(clusters);
					//we copy the clusters because usually anonymization mehtods modify it and don't want to create it again. 
					List<Trajectory> dAnonym = methods[b].anonymizeAll(clustersTmp);
					//anonymization set created.
					for (int i = 0; i < SPATIAL_RANGE.length; i++){						
						for (int j = 0; j < TEMPORAL_RANGE.length; j++){
							System.out.println("Executing queries for radious = "+SPATIAL_RANGE[i]+" and time interval = "+TEMPORAL_RANGE[j]);
							psiValues[a][b][i][j] = querpsi.averageQueries(dst.values(), toCompareDataset, dAnonym, SPATIAL_RANGE[i], TEMPORAL_RANGE[j], 10000, gpsDist);
							System.out.println("Psi = "+psiValues[a][b][i][j]);
							daiValues[a][b][i][j] = querdai.averageQueries(dst.values(), toCompareDataset, dAnonym, SPATIAL_RANGE[i], TEMPORAL_RANGE[j], 10000, gpsDist);
							System.out.println("Dai = "+daiValues[a][b][i][j]);
						}					
					}
				}
			}
			//we have this done for a particular k. Meanining that we can already print results.
			//Now we should prepare the values to be printed
			Hashtable<String, double[][]> psiCharts = createCrossedTable(psiValues, methods);
			Hashtable<String, double[][]> daiCharts = createCrossedTable(daiValues, methods);
			printCharts(psiCharts, folderForK, "psi");
			printCharts(daiCharts, folderForK, "dai");
			printTables(methods, psiValues, folderForK, "psi");
			printTables(methods, daiValues, folderForK, "dai");
		}
	}
	
	private static double[][] getValuesInTabularForm(
			AnonymizationMethod[] methods, double[][][][] values, int radiousIndex, int timeIndex) {
		int cont = 0;
		double[][] result = new double[methods.length][methods.length];
		for (int a = 0; a < methods.length; a++) {
			for (int b = 0; b < methods.length; b++) {
				result[a][b] = values[a][b][radiousIndex][timeIndex];
			}
		}
		return result;
	}
	
	private static double[] getValuesInTabularFormOneToOne(
			AnonymizationMethod[] methods, double[][][] values, int radiousIndex, int timeIndex) {
		int cont = 0;
		double[] result = new double[methods.length];
		for (int a = 0; a < methods.length; a++) {
			result[a] = values[a][radiousIndex][timeIndex];
		}
		return result;
	}

	private static void printTables(AnonymizationMethod[] methods, double[][][][] values, String folderForK, String preffix) throws IOException {
		String[] labels  = new String[methods.length];
		for (int i = 0; i < methods.length; i++) {
			labels[i] = methods[i].getName();
		}
		for (int i = 0; i < SPATIAL_RANGE.length; i++) {
			for (int j = 0; j < TEMPORAL_RANGE.length; j++) {
				double[][] table = getValuesInTabularForm(methods, values, i, j);
				String source = Latex.generateTableWithDegraded(table, labels);
				Latex.printToLatexFile(source, folderForK+preffix+"/comparison_"+SPATIAL_RANGE[i]+"_"+TEMPORAL_RANGE[j]+".tex");
			}
		}

	}

	private static Hashtable<String, double[][]> createCrossedTable(
			double[][][][] values, AnonymizationMethod[] methods) {
		Hashtable<String, double[][]> result = new Hashtable<String, double[][]>();
		for (int i = 0; i < methods.length; i++) {
			for (int j = 0; j < methods.length; j++) {
				double[][] tmp = new double[SPATIAL_RANGE.length][TEMPORAL_RANGE.length];
				String key = methods[i].getName()+"-"+methods[j].getName();
				for (int k = 0; k < SPATIAL_RANGE.length; k++) {
					for (int w = 0; w < TEMPORAL_RANGE.length; w++) {
						tmp[k][w] = values[i][j][k][w];
					}
				}
				result.put(key, tmp);
			}
		}
		return result;
	}

	private static Hashtable<String, double[][]> createCrossedTable(
			double[][][] values, AnonymizationMethod[] methods) {
		Hashtable<String, double[][]> result = new Hashtable<String, double[][]>();
		for (int i = 0; i < methods.length; i++) {
			double[][] tmp = new double[SPATIAL_RANGE.length][TEMPORAL_RANGE.length];
			String key = methods[i].getName()+"-"+methods[i].getName();
			for (int k = 0; k < SPATIAL_RANGE.length; k++) {
				for (int w = 0; w < TEMPORAL_RANGE.length; w++) {
					tmp[k][w] = values[i][k][w];
				}
			}
			result.put(key, tmp);
		}
		return result;
	}

	private static void printCharts(Hashtable<String, double[][]> values,
			String folder, String preffix) throws IOException {
		printTablesTimeFixed(values, folder, preffix);
		printTablesSpatialFixed(values, folder, preffix);
		printTablesTradeOff(values, folder, preffix);
	}

	/*Trujillo- May 23, 2013
	 * Here what we do is to fix the time dimension. This means that each time will create a separated result.*/
	private static void printTablesTimeFixed(
			Hashtable<String, double[][]> values, String folder, String preffix) throws IOException {
		//values is a simplification of what was before double[][][][]. In that case, the first two dimension
		//were merged into a single name that reprresetn both methods. 
		String xLabel = "Radious";
		String yLabel = preffix;
		String[] xtics = new String[SPATIAL_RANGE.length];
		//the xtics are obviusly the spatial range (radious) used.
		for (int i = 0; i < SPATIAL_RANGE.length; i++) {
			xtics[i] = SPATIAL_RANGE[i]+"";
		}
		for (int i = 0; i < TEMPORAL_RANGE.length; i++) {
			//this fixes the time interval, which will cause to create a new folder
			//in order to put the results there.
			String newFolder = folder+preffix+"/"+TEMPORAL_RANGE[i]+"/";
			File f = new File(newFolder);
			if (!f.exists()) f.mkdirs();
			String fileName = "time-fixed_"+TEMPORAL_RANGE[i];
			Hashtable<String, double[]> plots = new Hashtable<String, double[]>();
			//here we will save for each combination the query range value.
			for (String name : values.keySet()) {
				double[][] table = values.get(name);//this are the values for the combination.
				double[] tmp = new double[SPATIAL_RANGE.length];//here we will save the values when the tima interval is fixed.
				for (int j = 0; j < tmp.length; j++) {
					tmp[j] = table[j][i];
				}
				plots.put(name, tmp);
			}
			//now we call this in order to print the chart.
			GnuPlot.printChart(newFolder, fileName, xLabel, yLabel, plots, xtics);
		}
	}

	private static void printTablesTradeOff(
			Hashtable<String, double[][]> values, String folder, String preffix) throws IOException {
		//values is a simplification of what was before double[][][][]. In that case, the first two dimension
		//were merged into a single name that reprresetn both methods. 
		String xLabel = "Radious";
		String yLabel = "Time interval";
		String[] xtics = new String[SPATIAL_RANGE.length];
		String[] ytics = new String[TEMPORAL_RANGE.length];
		//the xtics are obviusly the spatial range (radious) used.
		for (int i = 0; i < SPATIAL_RANGE.length; i++) {
			xtics[i] = SPATIAL_RANGE[i]+"";
		}
		//the ytics are obviusly the time intervals used.
		for (int i = 0; i < TEMPORAL_RANGE.length; i++) {
			xtics[i] = TEMPORAL_RANGE[i]+"";
		}
		String newFolder = folder+preffix+"/";
		String fileName = "trade-off";
		//Each method should have only the values in which they are the best. 
		Hashtable<String, List<Point2D>> plots = new Hashtable<String, List<Point2D>>();
		for (String method : values.keySet()) {
			plots.put(method, new LinkedList<Point2D>());
		}
		plots.put("Empty", new LinkedList<Point2D>());
		for (int i = 0; i < SPATIAL_RANGE.length; i++) {
			for (int j = 0; j < TEMPORAL_RANGE.length; j++) {
				double best = 1;
				String bestMethod = "Empty";//indicating that no method is better than 1.
				for (String method : values.keySet()) {
					double[][] methodValues = values.get(method);
					if (methodValues[i][j] < best){
						best = methodValues[i][j];
						bestMethod = method;
					}
				}
				plots.get(bestMethod).add(new Point2D.Double(SPATIAL_RANGE[i], TEMPORAL_RANGE[j]));
			}
		}		
		GnuPlot.printTradeOffChart(newFolder, fileName, xLabel, yLabel, plots);
	}

	/*Trujillo- May 23, 2013
	 * Here what we do is to fix the spatial dimension. This means that each time will create a separated result.*/
	private static void printTablesSpatialFixed(
			Hashtable<String, double[][]> values, String folder, String preffix) throws IOException {
		//values is a simplification of what was before double[][][][]. In that case, the first two dimension
		//were merged into a single name that reprresetn both methods. 
		String xLabel = "Minutes";
		String yLabel = preffix;
		String[] xtics = new String[TEMPORAL_RANGE.length];
		//the xtics are obviusly the time interval used.
		for (int i = 0; i < TEMPORAL_RANGE.length; i++) {
			xtics[i] = TEMPORAL_RANGE[i]+"";
		}
		for (int i = 0; i < SPATIAL_RANGE.length; i++) {
			//this fixes the radious, which will cause to create a new folder
			//in order to put the results there.
			String newFolder = folder+preffix+"/"+SPATIAL_RANGE[i]+"/";
			File f = new File(newFolder);
			if (!f.exists()) f.mkdirs();
			String fileName = "radious-fixed_"+SPATIAL_RANGE[i];
			Hashtable<String, double[]> plots = new Hashtable<String, double[]>();
			//here we will save for each combination the query range value.
			for (String name : values.keySet()) {
				double[][] table = values.get(name);//this are the values for the combination.
				/*double[] tmp = new double[TEMPORAL_RANGE.length];//here we will save the values when the radious is fixed.
				for (int j = 0; j < tmp.length; j++) {
					tmp[j] = table[i][j];
				}*/
				plots.put(name, table[i]);
			}
			//now we call this in order to print the chart.
			GnuPlot.printChart(newFolder, fileName, xLabel, yLabel, plots, xtics);
		}
	}

	private static void tabularComparisonToyExample() throws IOException, ClassNotFoundException{
		tabularComparison("toy", "comparison_toy");
	}
	
	private static void tabularComparisonBetweenFrechet() throws IOException, ClassNotFoundException{
		AnonymizationMethod[] methods = new AnonymizationMethod[3];
		String folder = "real";
		methods[0] = new FrechetCentroideMethod(folder, new FrechetDistanceGPSBased(0.0));
		methods[1] = new FrechetCentroideMethod(folder, new FrechetDistanceGPSBased(0.2));
		methods[2] = new FrechetCentroideMethod(folder, new FrechetDistanceGPSBased(0.5));
		String[] labels = new String[]{"Frechet-0", "Frechet-0.2", "Frechet-0.5"};
		double[][] table = getValuesInTabularForm(methods, folder, 8);
		String source = Latex.generateTableWithDegraded(table, labels);
		Latex.printToLatexFile(source, "comparison_just_frechet.tex");
	}
	
	private static void comparingWithCompressionFrechetToCentroide() throws IOException, ClassNotFoundException {
		int[] ks = new int[]{8};
		//int[] ks = new int[]{4, 8, 16};
		String preffix = "real";
		//Hashtable<String, Trajectory> dst = MyTrajectoryFormat.loadTrajecotries(preffix);
		Hashtable<String, Trajectory> dst = MyTrajectoryFormat.loadCompressedTrajecotries(preffix);
		GPSDistance gpsDist = new GPSDistance();
		
		List<Trajectory> toCompareDataset = MyTrajectoryFormat.loadTrajecotriesInList("compare", "real_toCompare.obj");
		//System.setOut(new PrintStream("frechetCentroide.txt"));		
		for (int k : ks){
			Timer timer = new Timer();
			timer.reset();
			double averageLenght = Statistics.averageLenght(dst, gpsDist);
			double averageDuration = Statistics.averageLocationsHashtable(dst);
			//System.out.println("Average length = "+averageLenght);
			//System.out.println("Average duration = "+averageDuration);
			double[] weights = new double[]{1};
			//double[] weights = new double[]{0.25, 0.5, 1, 2, 4};
			int total = ks.length;
			int cont = 0;
			//FrechetBasedMethodVs1 algorithmFrechet = new FrechetBasedMethodVs1(); 
			FrechetDistance frechetDistance = new FrechetDistanceGPSBased();
			FrechetCentroideMethod frechetMethod = new FrechetCentroideMethod("real", frechetDistance);
			List<Trajectory> dAnonym = frechetMethod.anonymizeAll(k);
			//List<Trajectory> dAnonym = algorithmFrechet.anonymizesToCentroide(dstForFrechet, k, frechetDistance);
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File(k+"-frechetCentroide.obj")));
			out.writeObject(dAnonym);
			out.close();
			//List<Trajectory> dAnonym = algorithmFrechet.anonymizes(dstForFrechet, k, frechetDistance);
			//dstForFrechet = MyTrajectoryFormat.loadCompressedTrajecotriesInList(preffix);
			for (int i = 0; i < weights.length; i++){
				for (int j = 0; j < weights.length; j++){
					int timeUpperBound = (int)(averageDuration*weights[j]);
					if (timeUpperBound == 0) timeUpperBound = 1;
					double spaceUpperBound = averageLenght*weights[i];
					//System.out.println("timeUpperBound = "+timeUpperBound);
					//System.out.println("spaceUpperBound = "+spaceUpperBound);
					
					Query querPsi = new PSIQuery();
					Query querDai = new PSIQuery();
											
					System.out.println("Performing queries on Frechet");
					//double psiFrechet = querPsi.averageQueries(dst.values(), toCompareDataset, dAnonym, spaceUpperBound, timeUpperBound, 10000, gpsDist);
					//double daiFrechet = querDai.averageQueries("DAI", dst.values(), toCompareDataset, dAnonym, spaceUpperBound, timeUpperBound, 10000, gpsDist);
					
					//System.out.println("Frechet unconstrained and uncompressed : K = "+k+", PSI = "+psiFrechet);
					//System.out.println("Frechet unconstrained and uncompressed : K = "+k+", DAI = "+daiFrechet);

				}
			}
			//System.out.println("Frechet unconstrained and uncompressed took "+(timer.getTimeInHours())+" seconds");
			//System.out.println("Remaining time = "+(timer.getTimeInHours()*(total - ++cont))+" hours");
		}
	}

	
	private static void comparingWithCompressionJosepToCentroide() throws IOException, ClassNotFoundException {
		int[] ks = new int[]{8};
		//int[] ks = new int[]{4, 8, 16};
		String preffix = "real";
		//Hashtable<String, Trajectory> dst = MyTrajectoryFormat.loadTrajecotries(preffix);
		Hashtable<String, Trajectory> dst = MyTrajectoryFormat.loadCompressedTrajecotries(preffix);
		GPSDistance gpsDist = new GPSDistance();
		
		List<Trajectory> toCompareDataset = MyTrajectoryFormat.loadTrajecotriesInList("compare", "real_toCompare.obj");
		//System.setOut(new PrintStream("josepCentroide.txt"));		
		for (int k : ks){
			Timer timer = new Timer();
			timer.reset();
			double averageLenght = Statistics.averageLenght(dst, gpsDist);
			double averageDuration = Statistics.averageLocationsHashtable(dst);
			//System.out.println("Average length = "+averageLenght);
			//System.out.println("Average duration = "+averageDuration);
			double[] weights = new double[]{1};
			//double[] weights = new double[]{0.25, 0.5, 1, 2, 4};
			int total = ks.length;
			int cont = 0;
			//JosepBasedMethodVs1 algorithmJosep = new JosepBasedMethodVs1(); 
			Distance josepDist = new JosepGPSDistanceOnTheFlyVs2();
			JosepCentroideMethod josepMethod = new JosepCentroideMethod("real", josepDist);
			List<Trajectory> dAnonym = josepMethod.anonymizeAll(k);
			//List<Trajectory> dAnonym = algorithmJosep.anonymizesToCentroide(dstForJosep, k, josepDistance);
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File(k+"-josepCentroide.obj")));
			out.writeObject(dAnonym);
			out.close();
			//List<Trajectory> dAnonym = algorithmJosep.anonymizes(dstForJosep, k, josepDistance);
			//dstForJosep = MyTrajectoryFormat.loadCompressedTrajecotriesInList(preffix);
			for (int i = 0; i < weights.length; i++){
				for (int j = 0; j < weights.length; j++){
					int timeUpperBound = (int)(averageDuration*weights[j]);
					if (timeUpperBound == 0) timeUpperBound = 1;
					double spaceUpperBound = averageLenght*weights[i];
					//System.out.println("timeUpperBound = "+timeUpperBound);
					//System.out.println("spaceUpperBound = "+spaceUpperBound);
					
					Query querpsi = new PSIQuery();
					Query querDai = new DAIQuery();
											
					System.out.println("Performing queries on Josep");
					/*double psiJosep = querpsi.averageQueries(dst.values(), toCompareDataset, dAnonym, spaceUpperBound, timeUpperBound, 10000, gpsDist);
					double daiJosep = querDai.averageQueries(dst.values(), toCompareDataset, dAnonym, spaceUpperBound, timeUpperBound, 10000, gpsDist);
					
					System.out.println("Josep unconstrained and uncompressed : K = "+k+", PSI = "+psiJosep);
					System.out.println("Josep unconstrained and uncompressed : K = "+k+", DAI = "+daiJosep);*/

				}
			}
			//System.out.println("Josep unconstrained and uncompressed took "+(timer.getTimeInHours())+" seconds");
			//System.out.println("Remaining time = "+(timer.getTimeInHours()*(total - ++cont))+" hours");
		}
	}

	private static void comparingWithCompressionGeneralizationToCentroide() throws IOException, ClassNotFoundException {
		int[] ks = new int[]{8};
		//int[] ks = new int[]{4, 8, 16};
		String preffix = "real";
		//Hashtable<String, Trajectory> dst = MyTrajectoryFormat.loadTrajecotries(preffix);
		Hashtable<String, Trajectory> dst = MyTrajectoryFormat.loadCompressedTrajecotries(preffix);
		GPSDistance gpsDist = new GPSDistance();
		
		List<Trajectory> toCompareDataset = MyTrajectoryFormat.loadTrajecotriesInList("compare", "real_toCompare.obj");
		//System.setOut(new PrintStream("generalizationCentroide.txt"));		
		for (int k : ks){
			Timer timer = new Timer();
			timer.reset();
			double averageLenght = Statistics.averageLenght(dst, gpsDist);
			double averageDuration = Statistics.averageLocationsHashtable(dst);
			//System.out.println("Average length = "+averageLenght);
			//System.out.println("Average duration = "+averageDuration);
			double[] weights = new double[]{1};
			//double[] weights = new double[]{0.25, 0.5, 1, 2, 4};
			int total = ks.length;
			int cont = 0;
			//generalizationBasedMethodVs1 algorithmgeneralization = new generalizationBasedMethodVs1(); 
			LogCostDistance generalizationDist = new LogCostDistance();
			GeneralizationCentroideMethod generalizationMethod = new GeneralizationCentroideMethod("real", generalizationDist);
			List<Trajectory> dAnonym = generalizationMethod.anonymizeAll(k);
			//List<Trajectory> dAnonym = algorithmgeneralization.anonymizesToCentroide(dstForgeneralization, k, generalizationDistance);
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File(k+"-generalizationCentroide.obj")));
			out.writeObject(dAnonym);
			out.close();
			//List<Trajectory> dAnonym = algorithmgeneralization.anonymizes(dstForgeneralization, k, generalizationDistance);
			//dstForgeneralization = MyTrajectoryFormat.loadCompressedTrajecotriesInList(preffix);
			for (int i = 0; i < weights.length; i++){
				for (int j = 0; j < weights.length; j++){
					int timeUpperBound = (int)(averageDuration*weights[j]);
					if (timeUpperBound == 0) timeUpperBound = 1;
					double spaceUpperBound = averageLenght*weights[i];
					//System.out.println("timeUpperBound = "+timeUpperBound);
					//System.out.println("spaceUpperBound = "+spaceUpperBound);
					
					Query querpsi = new PSIQuery();
					Query querDai = new DAIQuery();
											
					System.out.println("Performing queries on generalization");
					/*double psigeneralization = querpsi.averageQueries("PSI", dst.values(), toCompareDataset, dAnonym, spaceUpperBound, timeUpperBound, 10000, gpsDist);
					double daigeneralization = querDai.averageQueries("DAI", dst.values(), toCompareDataset, dAnonym, spaceUpperBound, timeUpperBound, 10000, gpsDist);
					
					System.out.println("generalization unconstrained and uncompressed : K = "+k+", PSI = "+psigeneralization);
					System.out.println("generalization unconstrained and uncompressed : K = "+k+", DAI = "+daigeneralization);*/

				}
			}
			//System.out.println("generalization unconstrained and uncompressed took "+(timer.getTimeInHours())+" seconds");
			//System.out.println("Remaining time = "+(timer.getTimeInHours()*(total - ++cont))+" hours");
		}
	}
	
	
	private static void comparingAll(AnonymizationMethod[] methods) throws IOException, ClassNotFoundException {
		int[] ks = new int[]{8};
		//int[] ks = new int[]{4, 8, 16};
		//Hashtable<String, Trajectory> dst = MyTrajectoryFormat.loadTrajecotries(preffix);
		Hashtable<String, Trajectory> dst = MyTrajectoryFormat.loadCompressedTrajecotries("real");
		GPSDistance gpsDist = new GPSDistance();		
		List<Trajectory> toCompareDataset = MyTrajectoryFormat.loadTrajecotriesInList("compare", "real_toCompare.obj");
		//System.setOut(new PrintStream("frechetCentroide.txt"));		
		for (int k : ks){
			Timer timer = new Timer();
			timer.reset();
			double averageLenght = Statistics.averageLenght(dst, gpsDist);
			double averageDuration = Statistics.averageLocationsHashtable(dst);
			//System.out.println("Average length = "+averageLenght);
			//System.out.println("Average duration = "+averageDuration);
			double[] weights = new double[]{0.1, 1};
			//double[] weights = new double[]{0.25, 0.5, 1, 2, 4};
			int total = ks.length;
			int cont = 0;
			//FrechetBasedMethodVs1 algorithmFrechet = new FrechetBasedMethodVs1();
			for (int a = 0; a < methods.length; a++) {
				for (int b = 0; b < methods.length; b++) {
					System.out.println("Anonymizing with "+methods[b].toString()+" and clustering with "+methods[a]);
					List<List<Trajectory>> clusters = methods[a].createClusters(k);
					List<Trajectory> dAnonym = methods[b].anonymizeAll(clusters);
					for (int i = 0; i < weights.length; i++){
						for (int j = 0; j < weights.length; j++){
							int timeUpperBound = (int)(averageDuration*weights[j]);
							if (timeUpperBound == 0) timeUpperBound = 1;
							double spaceUpperBound = averageLenght*weights[i];
							System.out.println("timeUpperBound = "+timeUpperBound);
							System.out.println("spaceUpperBound = "+spaceUpperBound);
							
							Query querpsi = new PSIQuery();
							Query querDai = new DAIQuery();
													
							System.out.println("Performing queries on Frechet");
							/*double psiFrechet = querpsi.averageQueries("PSI", dst.values(), toCompareDataset, dAnonym, spaceUpperBound, timeUpperBound, 10000, gpsDist);
							double daiFrechet = querDai.averageQueries("DAI", dst.values(), toCompareDataset, dAnonym, spaceUpperBound, timeUpperBound, 10000, gpsDist);
							
							System.out.println("Frechet unconstrained and uncompressed : K = "+k+", PSI = "+psiFrechet);
							System.out.println("Frechet unconstrained and uncompressed : K = "+k+", DAI = "+daiFrechet);*/

						}
					}
					//System.out.println("Frechet unconstrained and uncompressed took "+(timer.getTimeInHours())+" seconds");
					//System.out.println("Remaining time = "+(timer.getTimeInHours()*(total - ++cont))+" hours");
				}
			}
		}
	}

	private static double[][] getValuesInTabularForm(AnonymizationMethod[] methods, String preffix, int k) throws IOException, ClassNotFoundException {
		//Hashtable<String, Trajectory> dst = MyTrajectoryFormat.loadTrajecotries(preffix);
		Hashtable<String, Trajectory> dst = MyTrajectoryFormat.loadCompressedTrajecotries(preffix);
		GPSDistance gpsDist = new GPSDistance();		
		List<Trajectory> toCompareDataset = MyTrajectoryFormat.loadTrajecotriesInList(preffix, preffix+"_toCompare.obj");
		double averageLenght = Statistics.averageLenght(dst, gpsDist);
		double averageDuration = Statistics.averageLocationsHashtable(dst);
		int cont = 0;
		double[][] result = new double[methods.length][methods.length];
		//FrechetBasedMethodVs1 algorithmFrechet = new FrechetBasedMethodVs1();
		List<List<Trajectory>> clusters;
		List<List<Trajectory>> clustersTmp;
		for (int a = 0; a < methods.length; a++) {
			System.out.println("Clustering with "+methods[a]);
			clusters = methods[a].createClusters(k);
			for (int b = 0; b < methods.length; b++) {
				System.out.println("Anonymizing with "+methods[b].toString());
				clustersTmp = copyClusters(clusters);
				List<Trajectory> dAnonym = methods[b].anonymizeAll(clustersTmp);
				int timeUpperBound = (int)(averageDuration);
				if (timeUpperBound == 0) timeUpperBound = 1;
				double spaceUpperBound = averageLenght;
				System.out.println("timeUpperBound = "+timeUpperBound);
				System.out.println("spaceUpperBound = "+spaceUpperBound);
				
				Query querpsi = new PSIQuery();
				Query querDai = new DAIQuery();
										
				System.out.println("Performing queries on Frechet");
				double psiFrechet = querpsi.averageQueries(dst.values(), toCompareDataset, dAnonym, spaceUpperBound, timeUpperBound, 10000, gpsDist);
				//double daiFrechet = quer.averageQueries("DAI", dst.values(), toCompareDataset, dAnonym, spaceUpperBound, timeUpperBound, 10000, gpsDist);
				result[a][b] = psiFrechet;
			}
		}
		return result;
	}

	private static List<List<Trajectory>> copyClusters(
			List<List<Trajectory>> clusters) {
		List<List<Trajectory>> result = new LinkedList<List<Trajectory>>();
		for (List<Trajectory> trajectories : clusters) {
			List<Trajectory> resultTmp = new LinkedList<Trajectory>();
			for (Trajectory trajectory : trajectories) {
				resultTmp.add((Trajectory)trajectory.clone());
			}
			result.add(resultTmp);
		}
		return result;
	}
}
