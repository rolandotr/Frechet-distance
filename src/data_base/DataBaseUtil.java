package data_base;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Hashtable;

import trajectory.Trajectory;

import distances.Distance;

/** 16/03/2011 Trujillo Comment
 * Aqui pondremos metodos que seran utiles para la conexion con la abse de datos*/
public abstract class DataBaseUtil {
	
	public static String HOST = "";
	//public static final String HOST = "10.30.102.178";
	//public static final String PASS = "rolandotr";
	public static String PASS = "";
	public static String USER = "root";
	public static String SCHEMA_NAME = "frechet";
	
	public static Connection getJDBCConnection(String host, String userName, String password){
		try
		{
		   Class.forName("com.mysql.jdbc.Driver");
		   Connection connection = DriverManager.getConnection ("jdbc:mysql://"+host
				   +"/"+SCHEMA_NAME,userName, password);
		   return connection;
		   
		} catch (Exception e)
		{
		   e.printStackTrace();
		   return null;
		} 	

	}
	
	public static Connection getJDBCConnection(){
		try
		{
		   Class.forName("com.mysql.jdbc.Driver");
		   Connection connection = DriverManager.getConnection ("jdbc:mysql://"+HOST+"/"+SCHEMA_NAME,USER, PASS);
		   return connection;
		   
		} catch (Exception e)
		{
		   e.printStackTrace();
		   return null;
		} 	

	}
	
	public static void showProperties(Hashtable<String, Trajectory> dst, Distance dist) {
		int points = 0;
		double distance = 0;
		long time = 0;
		for (Trajectory t: dst.values()){
			points += t.size();
			distance += dist.length(t);
			time += t.lastTime()-t.firstTime();
			//System.out.println(t.lastTime());
		}
		System.out.println("Number of trajectories "+dst.size());
		System.out.println("Points average "+points/dst.size());
		System.out.println("Length average "+distance/dst.size());
		System.out.println("Time average "+time/dst.size());
	}

	
}
