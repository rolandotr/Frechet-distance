package compression;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.acl.LastOwnerException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import clustering.MyTrajectoryFormat;

import parsers.TxtParser;

import distances.Distance;
import distances.EuclideanDistance;
import trajectory.SimpleTrajectory;
import trajectory.Trajectory;
import util.Geometry;
import util.Util;
import wrappers.GPSFormat;
import wrappers.SimpleFormat;

public class BasicCompression {

	public static void main(String[] args) throws NumberFormatException, IOException {
		GPSFormat initialPoint = new SimpleFormat(0, 0, 0);
		GPSFormat endingPoint = new SimpleFormat(0, 1, 1);
		double[][] bands = Geometry.computeTangentsFromPointToCircle2D(initialPoint, endingPoint, 1d,
				new EuclideanDistance());
		System.out.println(bands[0][0]+","+bands[0][1]);
		System.out.println(bands[1][0]+","+bands[1][1]);
		
		File file = new File("1449.txt"); 
		Trajectory t = new SimpleTrajectory("tCompressed");
		//GPXParser.parseXMLFile(file.getAbsolutePath(), ((SimpleTrajectory)trajectory1).getTree());
		TxtParser.parseTxtFile(file, ((SimpleTrajectory)t).getTree());
		System.out.println(t.size());
		//aqui ya tenemos la trayectoria
		Trajectory compressed = compress(t, 25, new EuclideanDistance());
		Util.printTrajectory(compressed);
		System.out.println(compressed.size());
	}
	

	public static Trajectory compress(Trajectory trajectory, double threshold, Distance distance){
		if (trajectory.size() <= 2) return (Trajectory)trajectory.clone();
		GPSFormat initialPoint = trajectory.getPoint(trajectory.getTime(0));
		GPSFormat endingPoint = trajectory.getPoint(trajectory.getTime(1));
		double angle = Math.asin(threshold/distance.distance(endingPoint, initialPoint));
		int cont = 0;
		SimpleTrajectory result = new SimpleTrajectory(trajectory.getIdentifier());
		//annadimos el primer punto siempre
		result.addPoint(initialPoint);
		for (GPSFormat point : trajectory.points()) {
			cont++;
			if (cont <= 2) continue;//obviamos los dos primeros puntos
			if (trajectory.size() == cont){
				//en este caso es porque este es el ultimo punto, de modo que va
				result.addPoint(point);
				return result;
			}
			//calculamos el angulo entre point - initialPoint - endingPoint
			double newAngle = Geometry.computeAngleBetweenVectors2D(point, initialPoint, endingPoint, distance);
			//verificamos que el punto este dentro de las bandas.
			if (newAngle <= angle || distance.distance(initialPoint, endingPoint) <= threshold){
				//en caso de estar pues esta pasa a ser la nueva referencia
				endingPoint = point;
			}
			else {
				//dado que no cumple con las restriccion, entonces insertamos el ultimo que teniamos
				result.addPoint(endingPoint);
				initialPoint = endingPoint;
				endingPoint = point;
			}
			angle = Math.asin(threshold/distance.distance(endingPoint, initialPoint));
		}
		return result;
	}

	public static List<Trajectory> compress(List<Trajectory> list, double threshold, 
			Distance distance) {
		List<Trajectory> result = new LinkedList<Trajectory>();
		for (Trajectory trajectory : list) {
			result.add(compress(trajectory, threshold, distance));
		}
		return result;
	}
	
	public static List<Trajectory> compressGPS(List<Trajectory> list, double threshold, 
			Distance distance) {
		List<Trajectory> result = new LinkedList<Trajectory>();
		for (Trajectory trajectory : list) {
			result.add(compressGPS(trajectory, threshold, distance));
		}
		return result;
	}


	public static Trajectory compressGPS(Trajectory trajectory, double threshold, Distance distance){
		if (trajectory.size() <= 2) return (Trajectory)trajectory.clone();
		GPSFormat initialPoint = trajectory.getPoint(trajectory.getTime(0));
		GPSFormat endingPoint = trajectory.getPoint(trajectory.getTime(1));
		//se han cogido los primeros puntos
		double angle = Math.asin(threshold/distance.distance(endingPoint, initialPoint));
		//este es el angulo formado, entre los dos puntos, y el punto interseccion
		//de la tangente que parte desde initialPoint hasta la circunferencia
		//de rafio threshold y centrada en endingpoint.
		int cont = 0;
		SimpleTrajectory result = new SimpleTrajectory(trajectory.getIdentifier());
		//annadimos el primer punto siempre
		result.addPoint(initialPoint);
		for (GPSFormat point : trajectory.points()) {
			cont++;
			if (cont <= 2) continue;//obviamos los dos primeros puntos
			if (trajectory.size() == cont){
				//en este caso es porque este es el ultimo punto, de modo que va
				result.addPoint(endingPoint);
				result.addPoint(point);
				return result;
			}
			//calculamos el angulo entre point - initialPoint - endingPoint
			double newAngle = Geometry.computeAngleBetweenVectors3D(point, initialPoint, endingPoint, distance);
			//verificamos que el punto este dentro de las bandas.
			if (newAngle <= angle || distance.distance(initialPoint, endingPoint) <= threshold){
				//en caso de estar pues esta pasa a ser la nueva referencia
				endingPoint = point;
			}
			else {
				//dado que no cumple con las restriccion, entonces insertamos el ultimo que teniamos
				result.addPoint(endingPoint);
				initialPoint = endingPoint;
				endingPoint = point;
			}
			angle = Math.asin(threshold/distance.distance(endingPoint, initialPoint));
		}
		return result;
	}
}
