package parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.TreeMap;

import wrappers.GPSFormat;
import wrappers.SimpleFormat;

public class TxtParser {

	public static TreeMap<Long, GPSFormat> parseTxtFile(File f) throws NumberFormatException, IOException{
		TreeMap<Long, GPSFormat> trajectory = new TreeMap<Long, GPSFormat>();
		parseTxtFile(f, trajectory);
		return trajectory;
	}
	public static void parseTxtFile(File f, TreeMap<Long, GPSFormat> trajectory) throws NumberFormatException, IOException{
		int cont = 0;
		BufferedReader reader = new BufferedReader(new FileReader(f));
		String[] split;
		SimpleFormat gps;
		boolean first = true;
		long lastTime = Long.MAX_VALUE;
		for (String line = reader.readLine(); line != null; line = reader.readLine()){
			split = line.split(" ");
			gps = new SimpleFormat(Long.parseLong(split[3]), Double.parseDouble(split[0]), Double.parseDouble(split[1]));
			trajectory.put(gps.getTime(), gps);
		}
	}
}
