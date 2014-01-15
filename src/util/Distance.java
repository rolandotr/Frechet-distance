package util;

import java.awt.Point;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;


//import sun.font.EAttribute;

import clustering.Cluster.PointInsideTrajectory;
import distances.EuclideanDistance;

import trajectory.SimpleTrajectory;
import trajectory.Trajectory;
import wrappers.GPSFormat;
import wrappers.GPXFormat;
import wrappers.SimpleFormat;

public class Distance {

	public static Hashtable<Vertex, Hashtable<Vertex, Double>> graphDistances;

	public static double graphDistance(double x1, double y1, double x2,
			double y2) {
		Vertex v1 = new Vertex(null, 0, x1, y1);
		Vertex v2 = new Vertex(null, 0, x2, y2);
		return graphDistances.get(v1).get(v2);
	}

	public static double euclideanDistance(Point p1, Point p2) {
		return Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2)
				+ Math.pow(p1.getY() - p2.getY(), 2));
	}

	public static double euclideanDistance(MinutesCoordinates lat1,
			MinutesCoordinates lon1, MinutesCoordinates lat2,
			MinutesCoordinates lon2) {
		return gpsDistance(Converter.minutesToDegrees(lat1),
				Converter.minutesToDegrees(lon1),
				Converter.minutesToDegrees(lat2),
				Converter.minutesToDegrees(lon2));
	}

	public static double euclideanDistance(double x1, double y1, double x2,
			double y2) {
		return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
	}

	public static double gpsDistance(double lat1, double lon1, double lat2,
			double lon2) {
		/**
		 * 21/06/2010 Trujillo Comment primero debemos obtener las coordenadas
		 * cartesianas
		 */
		/*
		 * CartesianCoordinates p1 = cartesianCord(lat1, lon1);
		 * CartesianCoordinates p2 = cartesianCord(lat2, lon2); return
		 * Math.sqrt(Math.pow(p1.lat-p2.lat, 2)+Math.pow(p1.lon-p2.lon, 2));
		 */
		// return Math.sqrt(Math.pow(69.1*(lat1-lat2),
		// 2)+Math.pow(53*(lon1-lon2),2));
		// para convertir en radianes se multiplica por 57.29577951
		double rLat1 = lat1 / 57.29577951;
		double rLon1 = lon1 / 57.29577951;
		double rLat2 = lat2 / 57.29577951;
		double rLon2 = lon2 / 57.29577951;
		// el radio de la esfera es mas o menos 6,378.8 kilometers
		return 6378.8 * Math.acos(Math.sin(rLat1) * Math.sin(rLat2)
				+ Math.cos(rLat1) * Math.cos(rLat2) * Math.cos(rLon2 - rLon1));
	}

	private static CartesianCoordinates cartesianCord(double lat1, double lon1) {
		/**
		 * 21/06/2010 Trujillo Comment La tierra por la latitud tiene un semi
		 * diametro de 10000 Km, lo que quiere decir que los 90 grados son 10000
		 */
		double lat = lat1 * 10000 / (double) 90;
		/**
		 * 21/06/2010 Trujillo Comment mientras que en la longitud 360 grados se
		 * reparten 40030 kilometros
		 */
		double lon = lat1 * 40030 / (double) 360;
		return new CartesianCoordinates(lat, lon);
	}

	public static boolean isInRadius(double latCenter, double lonCenter,
			double lat, double lon, double radius) {
		return (latCenter - radius <= lat && latCenter + radius >= lat
				&& lonCenter - radius <= lon && lonCenter + radius >= lon);
	}

	public static boolean isInRadius(GPSFormat p1, GPSFormat p2, double radius) {
		double distancePoints = Distance.euclideanDistance(p1, p2);

		if (distancePoints - radius <= 0.000000001) return true;
		else return false;
		
		//return (distancePoints <= radius);
	}
	
	public static void main(String[] args) {
		double lat1 = 37.77333;
		double lon1 = -122.2284;
		double lat2 = 37.7883;
		double lon2 = -122.40804;
		System.out.println(gpsDistance(lat1, lon1, lat2, lon2));
	}

	public static double euclideanDistance(GPSFormat p1, GPSFormat p2) {
		return Math.sqrt(Math.pow(p1.getLatitude() - p2.getLatitude(), 2)
				+ Math.pow(p1.getLongitude() - p2.getLongitude(), 2));
	}

	/**
	 * 17/08/2010 Trujillo Comment Computa para cada punto la distancia entre
	 * ellos y entonces devuelve el promedio
	 */
	public static double intraClusterAverageDistance(
			List<PointInsideTrajectory> cluster) {
		double distance = 0;
		for (PointInsideTrajectory p1 : cluster) {
			for (PointInsideTrajectory p2 : cluster) {
				distance += Distance.euclideanDistance(p1.p, p2.p);
			}
		}
		return distance / (cluster.size() * 2);
	}
	
	public static double intraClusterAverageDistance(
			List<Trajectory> cluster, distances.Distance dis) {
		double distance = 0;
		for (Trajectory p1 : cluster) {
			for (Trajectory p2 : cluster) {
				distance += dis.distance(p1, p2);
			}
		}
		return distance/(cluster.size()*(cluster.size()-1));
	}
	/**
	 * 23/11/2011 GRufian Comment Computa la distancia maxima entre pivote
	 * inicial y lista de trayectorias --> se devuelve nuevo pivote
	 */

	public static Trajectory maxTrajectDistanced(Trajectory pivot,
			List<Trajectory> listTrajectories) {
		
		Trajectory newPivot = null;
		double distance = -1.0;

		for (Trajectory tr : listTrajectories) {
			if (Distance.distance(tr, pivot) > distance) {
				distance = Distance.distance(tr, pivot);
				newPivot = tr;
			}
		}

		return newPivot;
	}

	public static double distance(Trajectory t1, Trajectory t2) {
		if (t1.size() != t2.size()) throw new RuntimeException();
		double distance = 0;
		for (long time : t1.times()) {
			distance += Distance.euclideanDistance(t1.getPoint(time), t2.getPoint(time));
		}
		return distance;
	}

	/**
	 * 23/11/2011 GRufian Comment Computa la distancia maxima entre pivote
	 * inicial y lista de trayectorias --> se devuelve maxima distancia
	 */

	public static double maxTrajectDistanceDouble(Trajectory pivot,
			List<Trajectory> listTrajectories) {
		double distance = -1.0;

		for (Trajectory tr : listTrajectories) {
			if (Distance.distance(tr, pivot) > distance) {
				distance = Distance.distance(tr, pivot);
			}
		}

		return distance;
	}

	public static List<Trajectory> minDistancedNeighbours(
			Trajectory pivot, List<Trajectory> listTrajectories,
			int numNeighbours) {
		// Creating distances and sorting them by value.
		Map<Trajectory, Double> mapDistances= new HashMap<Trajectory, Double>();
		
		for (Trajectory tr : listTrajectories) {
			mapDistances.put(tr, Distance.distance(tr, pivot));
		}
		
		mapDistances.remove(pivot);
		// Obtaining the nearest k-1
		List<Trajectory> nearest = new ArrayList<Trajectory>();
		
		Map<Trajectory, Double> sortedDistances = sortByAscendingValues(mapDistances);

		int n=0;
		for (Entry<Trajectory, Double> e : sortedDistances.entrySet()){
			nearest.add(e.getKey());
			n++;
			
			if (n == numNeighbours) break;
		}
		
		return nearest;
	}


	
	/**
	 * 23/11/2011 GRufian Comment Computa la distancia minima entre pivote y
	 * lista de trayectorias --> se devuelve nuevo pivote
	 */

	public static Trajectory minTrajectDistanced(Trajectory pivot,
			List<Trajectory> listTrajectories) {
		Trajectory newPivot = null;
		double distance = Double.MAX_VALUE;

		for (Trajectory tr : listTrajectories) {
			if (Distance.distance(tr, pivot) < distance) {
				distance = Distance.distance(tr, pivot);
				newPivot = tr;
			}
		}
		
		return newPivot;
	}


	public static GPSFormat minSpaceDistortion(long time, GPSFormat center, GPSFormat point, double radius) {
		if (radius > 0) {
			//If radius > 0 --> Move point to the disk center along the direction (line) between point and center.
			//Radius of the disk = delta/2.
			//New point = Coincidence of the line between two points and the disk's circunference of the point in trajectory center.
			
			double centerX = center.getX();
			double centerY = center.getY();
			double x = point.getX();
			double y = point.getY();
			
			if (x != centerX) {
				double m = (centerY - y)/(centerX - x);
				
				double n = y - (m*x); 
				
				double a = 1 + (m*m);
				double b = 2*m*n - (2*centerX) - (2*m*centerY);
				double c = (centerX*centerX) + (n*n) + (centerY*centerY) - (2*centerY*n) - (radius*radius);

				/*Antiguo discriminante!!*/ 
				/* 
				double term1 = (b*b);
				double term2 = (4*a*c);
							
				double discr = term1 - term2;

				double x1 = (-b + Math.sqrt(discr))/(2*a);
				double x2 = (-b - Math.sqrt(discr))/(2*a);
				*/
				
				//Nuevo cálculo de discriminante, siguiendo fórmula Rolando x = b/2a +/- sqrt((1/4)*(b/a)^2 - c/a)
								
				double discrRolando = (0.25*Math.pow((b/a), 2)) - (c/a);
				
				if (discrRolando < 0 && discrRolando > -1) {
					System.out.println("WARNING! Se ha redondeado a 0 valor discr: " + discrRolando);
					discrRolando = 0;
				} else if (discrRolando < -1) {
					throw new RuntimeException("Discriminante < -1. Fallo en cálculo de punto distorsionado.");
				}
				
				double x1 =  - ((b/(2*a)) + Math.sqrt(discrRolando));
				double x2 =  - ((b/(2*a)) - Math.sqrt(discrRolando));
				
				double y1 = m*x1 + n;
				double y2 = m*x2 + n;
				
				//Detecting the minimum Distance between the two points in which the line "confluye" with the circunference.
				if (Distance.euclideanDistance(x, y, x1, y1) <= Distance.euclideanDistance(x, y, x2, y2)) {
					return new SimpleFormat(time, x1, y1);
				} else {
					return new SimpleFormat(time, x2, y2);
				}
			} else {
				//Spetial case x == centerX. NO m.
				double x1 = centerX;
				double x2 = centerX;
				
				double y1 = centerY+500;
				double y2 = centerY-500;
				
				if (Distance.euclideanDistance(x, y, x1, y1) <= Distance.euclideanDistance(x, y, x2, y2)) {
					return new SimpleFormat(time, x1, y1);
				} else {
					return new SimpleFormat(time, x2, y2);
				}
			}
		} else {
			//If radius = 0 --> Move point to arithmetic mean of the cluster.
			return new SimpleFormat(time, center.getX(), center.getY());
		}
	}
	
	
    /**
     * Sort a map by values in ascending order keeping the duplicate entries.
     * @param map map to be sorted.
     */
	public static Map sortByAscendingValues(Map unsortMap) {
		 
        List list = new LinkedList(unsortMap.entrySet());
 
        //sort list based on comparator
        Collections.sort(list, new Comparator() {
             public int compare(Object o1, Object o2) {
	           return ((Comparable) ((Map.Entry) (o1)).getValue())
	           .compareTo(((Map.Entry) (o2)).getValue());

             }
        });
 
        //put sorted list into map again
		Map sortedMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
		     Map.Entry entry = (Map.Entry)it.next();
		     sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
   }

	public double distance(GPSFormat p, GPSFormat first) {
		// TODO Auto-generated method stub
		return 0;
	}

	
}