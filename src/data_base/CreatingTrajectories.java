package data_base;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.TreeMap;


import clustering.MyTrajectoryFormat;
import distances.GPSDistance;

import util.Util;
import wrappers.GPSFormat;
import wrappers.Trajectory;

public class CreatingTrajectories {

	public static void main(String[] args) throws SQLException, IOException {
		String host = args[0];
		String pass = args[1];
		String scheme = args[2];
		DataBaseUtil.HOST = host;
		DataBaseUtil.PASS = pass;
		workingWithDataBase(scheme);
	}
	
	private static void workingWithDataBase(String scheme) throws SQLException, IOException{
		createTrajectoryDataSet(scheme);
	}

	
	private static void checkTimeStamps(String identifier, String scheme) throws SQLException {
		Connection connection = DataBaseUtil.getJDBCConnection();
		Statement s = connection.createStatement();
		ResultSet result = s.executeQuery("SELECT * FROM "+scheme+
		".trajectory where identifier = '"+identifier+"'");
		long lastTime = -1;
		int lastPos = -1;
		while (result.next()){
			long time  = result.getLong("timestamp");
			int pos = result.getInt("pos");
			if (time <= lastTime) throw new RuntimeException("time = "+time+" is lower than lastTime = "
					+ lastTime+" in trajectory = "+identifier);
			if (pos <= lastPos) throw new RuntimeException("pos = "+pos+" is lower than lastPos = "
					+ lastPos+" in trajectory = "+identifier);
		}
		s.close();
		connection.close();
	}

	public static void createTrajectoryDataSet(String scheme) throws IOException, SQLException{
		Connection connection = DataBaseUtil.getJDBCConnection();
		Statement s = connection.createStatement();
		s.executeUpdate("CREATE SCHEMA IF NOT EXISTS "+scheme);
		s.executeUpdate("DROP TABLE IF EXISTS "+scheme+".trajectory");
		String query = "CREATE TABLE IF NOT EXISTS "+scheme+".trajectory " +
		"(identifier CHAR(45), timestamp BIGINT, " +
		"latitude DOUBLE, longitude DOUBLE, pos INT, PRIMARY KEY(identifier, timestamp), INDEX(identifier, timestamp, pos)) ENGINE = MyISAM";
		System.out.println(query);
		s.executeUpdate(query);
		//Hashtable<String, Trajectory> dst = MyTrajectoryFormat.loadTrajecotries("real", "real_allTrajectories_sync.obj");
		Hashtable<String, Trajectory> dst = MyTrajectoryFormat.loadTrajecotries("real", "real_bestTrajectories_180.obj");
		DataBaseUtil.showProperties(dst, new GPSDistance());
		for (String trajectoryId : dst.keySet()) {
			Trajectory trajectory = dst.get(trajectoryId);
			int pos = 0;
			//System.out.println("Trajectory "+trajectory.getIdentifier()+" done");
			for (long time : trajectory.times()){
				GPSFormat location = trajectory.getPoint(time);
				s.execute("INSERT INTO "+scheme+".trajectory " +
						"(identifier, timestamp, latitude, longitude, pos) VALUES ('"+
						trajectoryId+"',"+time+",'"+location.getLatitude()+"','"+
						location.getLongitude()+"',"+pos+")");
				pos++;
			}
		}
		s.close();
		connection.close();
		System.out.println("Done!!!!!!!");
	}

}