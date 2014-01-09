package util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public abstract class Latex {

	public static void main(String[] args) throws IOException {
		double[][] values = new double[][]{new double[]{1,2},new double[]{3,4}};
		String[] labels = new String[]{"A", "B"};
		String source = generateTableWithDegraded(values, labels);
		printToLatexFile(source, "table.tex");
	}
	
	
	public static String generateTableWithDegraded(double[][] table, String[] labels){
		String columns = "c|";
		String columnNames = " & ";
		for (int i = 0; i < table.length; i++) {
			columns += "c|";
			columnNames += labels[i];
			if (i < (table.length-1)) columnNames+=" & ";
			else columnNames += "\\\\ \\hline \n";
		}
		String header = "\\documentclass{article} \n \\usepackage[table]{xcolor} \n \\begin{document} \n \\centering \n \\begin{tabular}{"+columns+"}\n  \\hline\n";
		String footer = "\\hline \n \\end{tabular} \n \\end{document} \n";
		double max = MathFunctions.maximum(table); 
		double min = MathFunctions.minimum(table);
		double percetange;
		String body = columnNames;
		for (int i = 0; i < table.length; i++) {
			String line = labels[i]+" & ";
			for (int j = 0; j < table.length; j++) {
				percetange = MathFunctions.getPercetange(min, max, table[i][j]);
				line += "\\cellcolor{black!"+(percetange*2/3)+"}"+table[i][j];
				if (j < (table[i].length-1)) line+=" & ";
				else line += "\\\\";
			}
			body += line+"\n \\hline \n";
		}
		return header+body+footer;
	}
	
	public static void printToLatexFile(String source, String file) throws IOException{
		File f = new File(file);
		FileWriter writer = new FileWriter(f);
		writer.write(source);
		writer.close();
	}
	
}
