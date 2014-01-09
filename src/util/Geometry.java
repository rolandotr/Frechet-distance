package util;

import distances.Distance;
import distances.EuclideanDistance;
import wrappers.GPSFormat;
import wrappers.SimpleFormat;

public abstract class Geometry {
	
	
	/** 14/03/2012 Trujillo Comment
	 * Computes the proyectio of p over the segment p1-p2. If the proyection is not wihtin p1-p2, 
	 * the method return null*/
	public static GPSFormat computeProyectionIfEexist2D(GPSFormat p, GPSFormat p1, GPSFormat p2){
		GPSFormat intersection = computeProyection2D(p, p1, p2);
		if (intersection == null) return null;
		if (isInsideSegment2D(intersection, p1, p2)) return intersection;
		else return null;
	}

	private static boolean isInsideSegment2D(GPSFormat intersection,
			GPSFormat p1, GPSFormat p2) {
		if (p2.getLatitude() == p1.getLatitude() && p2.getLongitude() == p1.getLongitude())
			return intersection.getLatitude() == p1.getLatitude() &&
					intersection.getLongitude() == p1.getLongitude();
		else if (p2.getLatitude() == p1.getLatitude() || p2.getLongitude() == p1.getLongitude()){
			return false;
		}
		else{
			double x = (intersection.getLatitude()-p1.getLatitude())/
			(p2.getLatitude()-p1.getLatitude());
			double y = (intersection.getLongitude()-p1.getLongitude())/
			(p2.getLongitude()-p1.getLongitude());
			return x == y;
		}
	}

	public static double[] computeProyectionIfEexistGPS(GPSFormat p, GPSFormat p1, GPSFormat p2){
		double[] intersection = computeProyectionGPS(p, p1, p2);
		if (intersection == null) return null;
		if (isInsideSegment3D(intersection, p1, p2)) return intersection;
		else return null;
	}

	private static boolean isInsideRectangle2D(GPSFormat p,
			GPSFormat p1, GPSFormat p2) {
		double min = Math.min(p1.getLatitude(), p2.getLatitude());
		double max = Math.max(p1.getLatitude(), p2.getLatitude());
		if (p.getLatitude() < min || p.getLatitude() > max) return false;
		min = Math.min(p1.getLongitude(), p2.getLongitude());
		max = Math.max(p1.getLongitude(), p2.getLongitude());
		if (p.getLongitude() < min || p.getLongitude() > max) return false;
		return true;
	}

	private static boolean isInsideSegment3D(double[] point,
			GPSFormat point1, GPSFormat point2) {
		double[] p1 = Converter.degreesToXYZ(point1); 
		double[] p2 = Converter.degreesToXYZ(point2); 
		//ya tenemos los puntos en 3D
		//ahora verificamos que p1 y p2 no sean iguales
		//porque en este caso no hay proyeccion
		if (p2[0] == p1[0] && p2[1] == p1[1] && p2[2] == p1[2]) 
			return point[0] == p1[0] && point[1] == p1[1] && point[2] == p1[2];
		else if (p2[0] == p1[0] || p2[1] == p1[1] || p2[2] == p1[2]){
			return false;
		}
		else{
			double x = (point[0]-p1[0])/(p2[0]-p1[0]);
			double y = (point[1]-p1[1])/(p2[1]-p1[1]);
			double z = (point[2]-p1[2])/(p2[2]-p1[2]);
			return x == y && y == z;
		}
	}

	/** 14/03/2012 Trujillo Comment
	 * Computes the proyectio of p over the segment p1-p2. */
	public static GPSFormat computeProyection2D(GPSFormat p, GPSFormat p1, GPSFormat p2){
		double[] lineEq = computeLineEquation(p1, p2);
		double[] proyectionLine = computeProyectionLine2D(p, lineEq);
		if (lineEq[1] == proyectionLine[1] && lineEq[1] == 0){
			//both lines are parallel and ortogonal to y-axis 
			throw new RuntimeException();
		}
		if (lineEq[1] == 0){
			return new SimpleFormat(0, p1.getLatitude(), p.getLongitude());
		}
		if (proyectionLine[1] == 0){
			return new SimpleFormat(0, p.getLatitude(), p1.getLongitude());
		}
		lineEq = normalizeLineEquation2D(lineEq);
		proyectionLine = normalizeLineEquation2D(proyectionLine);
		if (lineEq[0] == proyectionLine[0]) {
			//both lines are parallel and ortogonal to y-axis 
			throw new RuntimeException();
		}
		double x = (proyectionLine[1] - lineEq[1])/(lineEq[0]-proyectionLine[0]);
		double y = x*proyectionLine[0] + proyectionLine[1];
		
		return new SimpleFormat(0, x, y);
	}
	
	public static double[] computeProyectionGPS(GPSFormat reference, 
			GPSFormat point1, GPSFormat point2){
		double[] p1 = Converter.degreesToXYZ(point1); 
		double[] p2 = Converter.degreesToXYZ(point2); 
		double[] p3 = Converter.degreesToXYZ(reference);
		//ya tenemos los puntos en 3D
		//ahora verificamos que p1 y p2 no sean iguales
		//porque en este caso no hay proyeccion
		if (p2[0] == p1[0] && p2[1] == p1[1] && p2[2] == p1[2]) return null;
		double denominator = Math.pow(p2[0]-p1[0], 2)+Math.pow(p2[1]-p1[1], 2)+
			Math.pow(p2[1]-p1[1], 2);
		double numerator = (p2[0]-p1[0])*(p3[0]-p1[0])+(p2[1]-p1[1])*(p3[1]-p1[1])+
			(p2[2]-p1[2])*(p3[2]-p1[2]);
		double alpha = numerator/denominator;
		//dado que ya tenemos alpha, entonces podemos calcular el punto
		double x = p1[0]+alpha*(p2[0]-p1[0]);
		double y = p1[1]+alpha*(p2[1]-p1[1]);
		double z = p1[2]+alpha*(p2[2]-p1[2]);
		return new double[]{x,y,z};
	}
	
	public static double[] computeProyectionLine2D(GPSFormat p, double[] line){
			if (line[1] == 0){
				return new double[]{0,1,p.getLongitude()};
			}
			line = normalizeLineEquation2D(line);
			double angle = Math.atan(line[0]);
			double ortogonalPendent = Math.tan(angle + Math.PI/2);
			double n = p.getLongitude() - ortogonalPendent*p.getLatitude();
			return new double[]{-ortogonalPendent, 1, n};
		}
		
	public static double[] normalizeLineEquation2D(double[] line) {
		return new double[]{-line[0]/line[1], line[2]/line[1]};
	}
	
	public static double[] computeLineEquation(GPSFormat p1, GPSFormat p2) {
		double m;
		if (p1.getLatitude() == p2.getLatitude()){
			return new double[]{1, 0, p1.getLatitude()};
		}
		else{
			m = (p2.getLongitude()-p1.getLongitude())/(p2.getLatitude()-p1.getLatitude());
		}
		double n = p1.getLongitude()-m*p1.getLatitude();
		return new double[]{-m, 1, n};
	}

	public static void main(String[] args) {
		double m = 0;
		/*double radian = Math.atan(m);
		System.out.println(radian);
		System.out.println(Math.tan(radian + 0));
		System.out.println(Math.tan(radian + Math.PI/4));
		System.out.println(Math.tan(radian + Math.PI/2));
		System.out.println(Math.tan(radian + Math.PI));*/
		GPSFormat p = new SimpleFormat(0, 0, 0);
		GPSFormat p1 = new SimpleFormat(0, 1, 0);
		GPSFormat p2 = new SimpleFormat(0, -2, 2);
		System.out.println("angle = "+computeAngleBetweenVectors2D(p1, p, p2, new EuclideanDistance()));
	}

	public static double[] computeMediatriz2D(GPSFormat p1, GPSFormat p2) {
		GPSFormat middle = new SimpleFormat(0, (p1.getLatitude()+p2.getLatitude())/2, 
				(p1.getLongitude()+p2.getLongitude())/2);
		double[] line = computeLineEquation(p1, p2);
		return computeProyectionLine2D(middle, line);
	}

	public static double[] computeMediatrizIntersectionGPS(GPSFormat point1, 
			GPSFormat point2, GPSFormat point3, GPSFormat point4) {
		double[] p1 = Converter.degreesToXYZ(point1);
		double[] p2 = Converter.degreesToXYZ(point2);
		double[] p3 = Converter.degreesToXYZ(point3);
		double[] p4 = Converter.degreesToXYZ(point4);
		//ya los tenemos en 3D
		double mx = (p1[0]+p2[0])/2;
		double my = (p1[1]+p2[1])/2;
		double mz = (p1[2]+p2[2])/2;
		double numerator = (p3[0]-mx)*(p2[0]-p1[0])
		  +(p3[1]-my)*(p2[1]-p1[1])
		  +(p3[2]-mz)*(p2[2]-p1[2]);
		double denominator = (p4[0]-p3[0])*(p2[0]-p1[0])
		  +(p4[1]-p3[1])*(p2[1]-p1[1])
		  +(p4[2]-p3[2])*(p2[2]-p1[2]);
		if (denominator == 0)
			return null; // es por los dos segmentos son ortoganales, por tanto su mediatriz no se corta
		double alpha = -numerator/denominator;
		double a = p3[0]+alpha*(p4[0]-p3[0]);
		double b = p3[1]+alpha*(p4[1]-p3[1]);
		double c = p3[2]+alpha*(p4[2]-p3[2]);
		double[] result = new double[]{a,b,c};
		if (isInsideSegment3D(result, point3, point4)) return result;
		else return null;
	}

	public static GPSFormat computeIntersectionIfExist2D(double[] line,
			GPSFormat p1, GPSFormat p2) {
		double[] line2 = computeLineEquation(p1, p2);
		GPSFormat p = computeIntersection2D(line, line2);
		if (p == null) return null;
		if (isInsideRectangle2D(p, p1, p2)) return p;
		else return null;
	}

	private static GPSFormat computeIntersection2D(double[] line1, double[] line2) {
		double m;
		if (line1[1] == line2[1] && line1[1] == 0){
			//are parallel
			return null;
		}
		if (line1[1] == 0){
			double x = line1[2]/line1[0];
			return new SimpleFormat(0, x, (line2[2]-line2[0]*x)/line2[1]);
		}
		if (line2[1] == 0){
			double x = line2[2]/line2[0];
			return new SimpleFormat(0, x, (line1[2]-line1[0]*x)/line1[1]);
		}
		line1 = normalizeLineEquation2D(line1);
		line2 = normalizeLineEquation2D(line2);
		if (line1[0] == line2[0]){
			//are parallel
			return null;
		}
		double x = (line2[1]-line1[1])/(line1[0]-line2[0]);
		double y = line1[0]*x + line1[1];
		return new SimpleFormat(0, x, y);
	}

	
	/***Trujillo- Sep 26, 2012
	 *  Computa la ecuacion de las dos rectas que son tangentes a la circuferencia.
	 */
	public static double[][] computeTangentsFromPointToCircle2D(
			GPSFormat point, GPSFormat center, double radius, distances.Distance distance) {
		//primero verficamos que el punto no este dentro de la circunferencia.
		//en cuyo caso no hay tangentes
		if (isPointInsideCircle2D(point, center, radius)) return null;
		double hipotenusa = distance.distance(point, center);
		double alpha = Math.asin(radius/hipotenusa);
		double[] middleLine = computeLineEquation(point, center);
		middleLine = normalizeLineEquation2D(middleLine);
		double middleLineAngle = Math.atan(middleLine[0]);
		double angle1 = middleLineAngle + alpha;
		double angle2 = middleLineAngle - alpha;
		System.out.println(angle1);
		System.out.println(angle2);
		double[] line1 = computeNormalizedLine2D(point, angle1);
		double[] line2 = computeNormalizedLine2D(point, angle2);
		return new double[][]{line1, line2};
	}

	private static double[] computeNormalizedLine2D(GPSFormat point, double angle) {
		double m = Math.tan(angle);
		double n = point.getLongitude()-m*point.getLatitude();
		return new double[]{m, n};
	}

	private static boolean isPointInsideCircle2D(GPSFormat point,
			GPSFormat center, double radius) {
		return Math.pow(point.getLatitude()-center.getLatitude(), 2)+
				Math.pow(point.getLongitude()-center.getLongitude(), 2) <= Math.pow(radius, 2);
	}

	public static boolean isPointInsideBands2D(GPSFormat point, double[][] bands) {
		double r1 = point.getLongitude() - bands[0][0]*point.getLatitude() - bands[0][1];
		double r2 = point.getLongitude() - bands[1][0]*point.getLatitude() - bands[1][1];
		return r1*r2 <= 0;
	}

	public static double computeAngleBetweenVectors2D(GPSFormat p1,
			GPSFormat midle, GPSFormat p2, Distance distance) {
		double numerator = (p2.getLatitude() - midle.getLatitude())*(p1.getLatitude() - midle.getLatitude())
			+ (p2.getLongitude() - midle.getLongitude())*(p1.getLongitude() - midle.getLongitude());
		double d1 =  distance.distance(p1, midle);
		double d2 =  distance.distance(p2, midle);
		return Math.acos(numerator/(d1*d2));
	}
	
	public static double computeAngleBetweenVectors3D(GPSFormat p1,
			GPSFormat midle, GPSFormat p2, Distance distance) {
		double[] point1 = Converter.degreesToXYZ(p1);
		double[] point2 = Converter.degreesToXYZ(p2);
		double[] pointMidle = Converter.degreesToXYZ(midle);
		double numerator = (point2[0] - pointMidle[0])*(point1[0] - pointMidle[0])
			+ (point2[1] - pointMidle[1])*(point1[1] - pointMidle[1])
			+ (point2[2] - pointMidle[2])*(point1[2] - pointMidle[2]);
		double d1 =  distance.distance(p1, midle);
		double d2 =  distance.distance(p2, midle);
		return Math.acos(numerator/(d1*d2));
	}
}
