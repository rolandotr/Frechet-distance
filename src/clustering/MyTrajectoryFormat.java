package clustering;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;


import trajectory.Trajectory;

public abstract class MyTrajectoryFormat{

	public static Hashtable<String, Trajectory> loadTrajecotries(String folder, String fileName) {
		ObjectInputStream input;
		try {
			input = new ObjectInputStream(new FileInputStream("./"+folder+"/"+fileName));
			try {
				return (Hashtable<String, Trajectory>)input.readObject();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			input.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new RuntimeException();
	}
	
	public static List<Trajectory> loadTrajecotriesInList(String folder, String fileName) {
		ObjectInputStream input;
		try {
			input = new ObjectInputStream(new FileInputStream("./"+folder+"/"+fileName));
			try {
				return (List<Trajectory>)input.readObject();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			input.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new RuntimeException();
	}


	public static Hashtable<String, Trajectory> loadTrajecotries(String preffix) {
		return loadTrajecotries(preffix, preffix+"_allTrajectories.obj");
	}

	public static Hashtable<String, Trajectory> loadCompressedTrajecotries(String preffix) {
		return loadTrajecotries(preffix, preffix+"_allTrajectories_compressed.obj");
	}

	public static List<Trajectory> loadTrajecotriesInList(String preffix) {
		return loadTrajecotriesInList(preffix, preffix+"_allTrajectoriesList.obj");
	}
	
	public static List<Trajectory> loadCompressedTrajecotriesInList(String preffix) {
		return loadTrajecotriesInList(preffix, preffix+"_allTrajectoriesList_compressed.obj");
	}
	


}
