package parsers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import exceptions.InconsistencyTimeException;

import wrappers.GPSFormat;
import wrappers.GPXFormat;

public class GPXParser {

	private static TreeMap<Long, GPSFormat> parseXMLFile(String fileName) throws IOException, ParserConfigurationException, SAXException{
		TreeMap<Long, GPSFormat> tree = new TreeMap<Long, GPSFormat>();
		parseXMLFile(fileName, tree);
		return tree;
	}

	public static void parseXMLFile(String fileName, TreeMap<Long, GPSFormat> tree) throws IOException, ParserConfigurationException, SAXException{
	    File fXmlFile = new File(fileName);
	    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    Document doc = dBuilder.parse(fXmlFile);
	    doc.getDocumentElement().normalize();
	    /** 17/06/2010 Trujillo Comment
		 * En esta lista de nodos es que tenemos a los valores*/
	    NodeList nList = doc.getElementsByTagName("trkpt");
	    GPXFormat tmp = null;
		File fileError = new File(fileName+".error");
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileError, false));
	    
	    for (int i = 0; i < nList.getLength(); i++) {
			 	
    	   tmp = new GPXFormat();
	       Node nNode = nList.item(i);
	    	try{
	           tmp.setLongitude(nNode.getAttributes().getNamedItem("lon").getNodeValue());
	           tmp.setLatitude(nNode.getAttributes().getNamedItem("lat").getNodeValue());
		       Element eElement = (Element) nNode;
		       tmp.setHeight(getTagValue("ele",eElement));
		       tmp.setDate(getTagValue("time",eElement));
	    	}catch (Exception e) {
				writer.write("Error in node --> "+nNode.toString());
				writer.newLine();
				writer.write(e.getMessage());
				writer.newLine();
				writer.write(e.toString());
				writer.newLine();
				continue;
			}
	    	if (tree.containsKey(tmp.getTime())){
	    		//throw new InconsistencyTimeException(tmp.getDate());
	    	}
	    	tree.put(tmp.getTime(), tmp);
		}
	    writer.close();
	}

	 private static String getTagValue(String sTag, Element eElement){
		    NodeList nlList= eElement.getElementsByTagName(sTag).item(0).getChildNodes();
		    Node nValue = (Node) nlList.item(0); 
		    return nValue.getNodeValue();    
	 }
	 
	 public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		 parseXMLFile("D:\\Trabajo\\trayectories\\data\\GeoLife GPS Trajectories\\0001\\trajectory\\20090728.gpx");
	 }

		public static void parseGPXFiles(String folder, TreeMap<Long, GPSFormat> tree) throws IOException, ParserConfigurationException, SAXException{
			File f =  new File(folder);
			if (!f.isDirectory()) throw new IllegalArgumentException(folder+" deberia ser un directorio");
			File[] files = f.listFiles(new FileFilter(){
				@Override
				public boolean accept(File pathname) {
					if (pathname.getName().substring(pathname.getName().length()-4).equals(".gpx")) return true;
					return false;
				}
			});
			for (int i = 0; i < files.length; i++) {
				parseXMLFile(files[i].getAbsolutePath(), tree);
			}
		}
		
		public static void buildGpxFileWithUnixEpochTime(TreeMap<Long, GPSFormat> tree,
				String fileName, int number) throws IOException {
			File f = new File(fileName);
			BufferedWriter writer = new BufferedWriter(new FileWriter(f, false));
			writer.write("<?xml version=\"1.0\" standalone=\"yes\"?>");
			writer.newLine();
			writer.write("<gpx version=\"1.0\" creator=\"BeeLineGPS - http://www.VisualGPS.net\">");
			writer.newLine();
			writer.write("<trk>");
			writer.newLine();
			writer.write("<name>BeeLineGPS Track</name>");
			writer.newLine();
			writer.write("<trkseg>");
			writer.newLine();
			writer.write("<number>"+number+"</number>");
			writer.newLine();
			for (GPSFormat value : tree.values()){
				writeWithUnixEpochTime(value, writer);
			}
			writer.write("</trkseg>");
			writer.newLine();
			writer.write("</trk>");
			writer.newLine();
			writer.write("</gpx>");
			writer.close();
		}

		private static void writeWithUnixEpochTime(GPSFormat value, BufferedWriter writer) throws IOException {
			writer.write("<trkpt lat=\""+value.getLatitude()+"\" lon=\""+value.getLongitude()+"\">");
			writer.newLine();
			writer.write("<ele>"+value.getHeight()+"</ele>");
			writer.newLine();
			//System.out.println(value.getDate());
			SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss'Z'");
			//System.out.println(s.format(value.getDate()));
			writer.write("<time>"+s.format(value.getTime()*1000)+"</time>");
			writer.newLine();
			writer.write("<sym>Dot</sym>");
			writer.newLine();
			writer.write("</trkpt>");
			writer.newLine();
		}

}
