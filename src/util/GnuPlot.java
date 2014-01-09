package util;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

public abstract class GnuPlot {

	public static String newline = System.getProperty("line.separator");
	
	public static void printChart(String folder, String outputFileName, String xLabel, String yLabel, Hashtable<String, double[]> plots, String[] xtics) throws IOException{
		File file = new File(folder+outputFileName+".plt");
		//System.out.println(outputFileName+".plt");
		FileWriter writer = new FileWriter(file);
		String heading = "reset "+newline+" set terminal postscript eps enhanced color "+newline+" set out \""+outputFileName+".eps\" "+newline;
		heading += "set xlabel \""+xLabel + "\""+newline;
		heading += "set ylabel \""+yLabel + "\""+newline;
		heading += "set key box" + newline;
		heading += "set yrange [0:1]" + newline;
		heading += "set grid" + newline;
		String plotPart = "plot ";
		int cont = 1;
		for (String name : plots.keySet()) {
			String dataFileName = folder+outputFileName+"-"+name+".DAT"; 
			writeDataFile(dataFileName, plots.get(name), xtics);
			plotPart += "\""+outputFileName+"-"+name+".DAT"+"\" using 2:xticlabel(1) with lp lw 2 lt "+(cont++)+" t \""+ name+"\", ";
		}
		plotPart = plotPart.substring(0, plotPart.length()-2);
		writer.write(heading+plotPart);
		writer.close();
		//String cmdLine = "\'C:\\Program Files (x86)\\gnuplot\\bin\\gnuplot\' "+file.getAbsolutePath();
		/*String cmdLine = "load \'"+file.getAbsolutePath()+"\'";
		System.out.println(cmdLine);
		Runtime run = Runtime.getRuntime();
		run.exec("gnuplot");
		run.exec(cmdLine);*/
	}

	public static void writeDataFile(String dataFileName, double[] values,
			String[] xtics) throws IOException {
		File file = new File(dataFileName);
		FileWriter writer = new FileWriter(file);
		if (xtics.length != values.length) throw new RuntimeException();
		for (int i = 0; i < xtics.length; i++) {
			writer.append(xtics[i]+" "+values[i]+newline);
		}
		writer.close();
	}

	public static void writeDataFileForTradeOff(String dataFileName, List<Point2D> values) throws IOException {
		File file = new File(dataFileName);
		FileWriter writer = new FileWriter(file);
		for (Point2D point : values) {
			writer.append(point.getX()+" "+point.getY()+newline);
		}
		writer.close();
	}


	public static void printTradeOffChart(String folder, String outputFileName, String xLabel, String yLabel, 
			Hashtable<String, List<Point2D>> plots) throws IOException{
		File file = new File(folder+outputFileName+".plt");
		//System.out.println(outputFileName+".plt");
		FileWriter writer = new FileWriter(file);
		String heading = "reset "+newline+" set terminal postscript eps enhanced color "+newline+" set out \""+outputFileName+".eps\" "+newline;
		heading += "set xlabel \""+xLabel + "\""+newline;
		heading += "set ylabel \""+yLabel + "\""+newline;
		heading += "set key box" + newline;
		heading += "set key outside" + newline;
		String plotPart = "plot ";
		int cont = 1;
		for (String name : plots.keySet()) {
			String dataFileName = folder+outputFileName+"-"+name+".DAT";
			writeDataFileForTradeOff(dataFileName, plots.get(name));
			plotPart += "\""+outputFileName+"-"+name+".DAT"+"\" pt  "+(cont++)+" t \""+ name+"\", ";
		}
		plotPart = plotPart.substring(0, plotPart.length()-2);
		writer.write(heading+plotPart);
		writer.close();
	}

}
