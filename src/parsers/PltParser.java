package parsers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;



import wrappers.GPSFormat;
import wrappers.PltFormat;

public class PltParser{

	/** 15/06/2010 Trujillo Comment
	 * este metodo parsea una linea de un fichero plt, y entonces construye un punto de la trayectoria
	 * el algoritmo es el siguiente:
	 * 1- latitud
	 * 2- longitud
	 * 3- linea de codigo, 0- si es normal y 1-si es una discontinuidad
	 * 4- altitud en pies
	 * 5- fecha y hora modificada
	 * 6- fecha
	 * 7- hora
	 * @throws ParseException */
	public static PltFormat parsePltLine(String line) throws ParseException{

		PltFormat result = new PltFormat(); 
		String[] split = line.split(",");
		result.setLongitude(split[1]);
		result.setLatitude(split[0]);
		result.setHeight(split[3]);
		result.setDiscontinuity(split[2]);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss");
		Date date = null;
		try{
		date = dateFormat.parse(split[5]+" "+split[6]);
		}catch (ParseException e) {
			dateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
			date = dateFormat.parse(split[5]+" "+split[6]);
		}
		result.setTime(date.getTime());
		
		return result;		
	}
	
	public static void main(String[] args) throws ParseException, IOException {
		//parsePltLine("39.975574,116.330861,0,152,39580.610845,2008/05/12,14:39:37");
		//parsePltFile("D:\\Trabajo\\trayectories\\data\\GeoLife GPS Trajectories\\0001\\trajectory\\OZI__20080627084625.plt");
		//parseLogFiles("D:\\Trabajo\\trayectories\\data\\GeoLife GPS Trajectories\\0001\\trajectory");
		/*String m = "2009-07-28T15:03:21Z";
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy kk:mm:ss");
		m = m.replace("T", " ");
		m = m.replace("Z", " ");
		Date date = dateFormat.parse(m);*/
	}

	private static void parsePltFile(String fileName, TreeMap<Long, GPSFormat> tree) throws IOException{
		BufferedReader read = new BufferedReader(new FileReader(fileName));
		PltFormat tmp = null;
		File fileError = new File(fileName+".error");
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileError, false));
		for (String line = read.readLine(); line != null; line = read.readLine()){
			try {
				tmp = parsePltLine(line);
			} catch (Exception e) {
				writer.write("Error in --> "+line);
				writer.newLine();
				writer.write(e.getMessage());
				writer.newLine();
				continue;
			}
			if (tree.containsKey(tmp.getTime())){
				/*System.out.println("Parseando la linea ");
				System.out.println(line);
				System.out.println("Nos encontramos con otra de la misma fecha: ");
				System.out.println(tree.get(tmp.getDate()));
				System.out.println("Por otro lado el objeto que hemos construido es:");
				System.out.println(tmp);
				System.out.println("Y el fichero es:");
				System.out.println(fileName);
				throw new InconsistencyTimeException(tmp.getDate());*/
				//System.out.println("date conflict");
			}
			tree.put(tmp.getTime(), tmp);
		}
		read.close();
		writer.close();
	}

	public static TreeMap<Long, GPSFormat> parsePltFile(String fileName) throws IOException{
		TreeMap<Long, GPSFormat> result = new TreeMap<Long, GPSFormat>();
		parsePltFile(fileName, result);
		return result;
	}
	
	/** 17/06/2010 Trujillo Comment
	 * A partir de una carpeta comienza a buscar en esta carpeta todos los archivos con
	 * extension .log y los intenta parsear uno a uno y va construyendo el arbol.
	 * @throws IOException */
	public static void parsePltFiles(String folder, TreeMap<Long, GPSFormat> tree) throws IOException{
		File f =  new File(folder);
		if (!f.isDirectory()) throw new IllegalArgumentException(folder+" deberia ser un directorio");
		File[] files = f.listFiles(new FileFilter(){
			@Override
			public boolean accept(File pathname) {
				if (pathname.getName().substring(pathname.getName().length()-4).equals(".plt")) return true;
				return false;
			}
		});
		for (int i = 0; i < files.length; i++) {
			parsePltFile(files[i].getAbsolutePath(), tree);
		}
	}
}
