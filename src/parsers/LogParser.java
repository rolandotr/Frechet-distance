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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import exceptions.IncorrectLineFormatException;


import wrappers.GPSFormat;
import wrappers.LogFormat;

public class LogParser {

	public static final Pattern pDate = Pattern.compile("\\d\\d-\\d\\d-\\d\\d");
	public static final Pattern pTime = Pattern.compile("\\d\\d:\\d\\d:\\d\\d");
	
	/** 15/06/2010 Trujillo Comment
	 * este metodo parsea una linea de un fichero log, y entonces construye un punto de la trayectoria
	 * el algoritmo es el siguiente:
	 * - Se busca un patron de fecha del tipo dd-mm-aa. Entonces esta fecha se transforma en dd/mm/aa. 
	 * Luego se busca un patron del tipo hh:mm:ss el cual se annade a la fecha que teniamos anteriormente
	 * y creamos el Date.
	 * - A partir de este momento dividimos por espacio y nos debe crear un arreglo de 8 elementos
	 * los cuales tienen el siguiente significado en su orden:
	 * 1- longitud
	 * 2- punto cardinal de la longitud
	 * 3- latitud
	 * 4- punto cardinal de la latitud
	 * 5- altura
	 * 6- velocidad
	 * 7 y 8 no te interesan
	 * @throws ParseException */
	public static LogFormat parseLogLine(String line) throws ParseException{
		Matcher mDate = pDate.matcher(line);
		Matcher mTime = pTime.matcher(line);
		if (!mDate.find()) throw new IncorrectLineFormatException("no se encontro el patron de fecha", line);
		if (!mTime.find()) throw new IncorrectLineFormatException("no se encontro el patron de tiempo", line);
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy kk:mm:ss");
		Date date = dateFormat.parse(mDate.group()+" "+mTime.group());
		LogFormat result = new LogFormat();
		if (line.startsWith("Y", 0)) result.setValidateData(true);
		else if (line.startsWith("N", 0)) result.setValidateData(false);
		else throw new IncorrectLineFormatException("Deberia haber una N o una Y al principio de la linea:", line);

		result.setTime(date.getTime());
		line = line.substring(mTime.end()+1);
		String[] split = line.split(" ");
		result.setLongitude(split[0]);
		result.setLongitudeCardinalPoint(split[1]);
		result.setLatitude(split[2]);
		result.setLatitudeCardinalPoint(split[3]);
		result.setHeight(split[4]);
		result.setSpeed(split[5]);
		return result;		
	}
	


	private static void parseLogFile(String fileName, TreeMap<Long, GPSFormat> tree) throws IOException{
		BufferedReader read = new BufferedReader(new FileReader(fileName));
		LogFormat tmp = null;
		File fileError = new File(fileName+".error");
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileError, false));
		for (String line = read.readLine(); line != null; line = read.readLine()){
			try {
				tmp = parseLogLine(line);
			} catch (Exception e) {
				writer.write("Error in --> "+line);
				writer.newLine();
				writer.write(e.getMessage());
				writer.newLine();
				continue;
			}
			if (tree.containsKey(tmp.getTime())){
				if (!tmp.isValidateData()) continue;
				/** 17/06/2010 Trujillo Comment
				 * He comentado estas lineas de abajo para no validar este problema
				 * de que hayan varios puntos en el mismo instante de tiempo*/
				/*if (tree.get(tmp.getDate()).isValidateData()){
					System.out.println("Parseando la linea ");
					System.out.println(line);
					System.out.println("Nos encontramos con otra de la misma fecha: ");
					System.out.println(tree.get(tmp.getDate()));
					System.out.println("Por otro lado el objeto que hemos construido es:");
					System.out.println(tmp);
					System.out.println("Y el fichero es:");
					System.out.println(fileName);
					throw new InconsistencyTimeException(tmp.getDate());
				}*/
			}
			tree.put(tmp.getTime(), tmp);
		}
		read.close();
		writer.close();
	}

	public static TreeMap<Long, GPSFormat> parseLogFile(String fileName) throws IOException{
		TreeMap<Long, GPSFormat> result = new TreeMap<Long, GPSFormat>();
		parseLogFile(fileName, result);
		return result;
	}
	
	/** 17/06/2010 Trujillo Comment
	 * A partir de una carpeta comienza a buscar en esta carpeta todos los archivos con
	 * extension .log y los intenta parsear uno a uno y va construyendo el arbol.
	 * @throws IOException */
	public static void parseLogFiles(String folder, TreeMap<Long, GPSFormat> tree) throws IOException{
		File f =  new File(folder);
		if (!f.isDirectory()) throw new IllegalArgumentException(folder+" deberia ser un directorio");
		File[] files = f.listFiles(new FileFilter(){
			@Override
			public boolean accept(File pathname) {
				if (pathname.getName().substring(pathname.getName().length()-4).equals(".log")) return true;
				return false;
			}
		});
		for (int i = 0; i < files.length; i++) {
			parseLogFile(files[i].getAbsolutePath(), tree);
		}
	}
}
