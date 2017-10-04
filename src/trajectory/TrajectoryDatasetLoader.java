package trajectory;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.List;

import wrappers.Trajectory;



/*Trujillo- Jan 15, 2014
 * We do not consider a trajectory dataset as a single list of trajectories. Instead, 
 * we consider a dataset as a set of list of trajectories. This gives us more flexibility 
 * since, obviously, it not is able to represent a single list of trajectories, but it 
 * also improves performance when the dataset is too large and it is smartly split. The 
 * idea here is first creating large clusters, which them can be processed again to find smaller
 * clusters.*/

/*Trujillo- Jan 15, 2014
 * This class has been designed to load each of these lists iteratively.*/
public class TrajectoryDatasetLoader implements Iterator<List<Trajectory>>{

	private File[] files;
	private int index;
	
	public TrajectoryDatasetLoader(String pathToTrajectoryDataset){
		File f = new File(pathToTrajectoryDataset);
		files = f.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".obj");
			}
		});
		index = 0;
	}
	@Override
	public boolean hasNext() {
		return (index < files.length);
	}

	@Override
	public List<Trajectory> next() {
		try{
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(files[index++]));
			return (List<Trajectory>)in.readObject();
		}catch(FileNotFoundException e1){
			e1.printStackTrace();
			return null;
		}catch(ClassNotFoundException e2){
			e2.printStackTrace();
			return null;
			
		}catch(IOException e3){
			e3.printStackTrace();
			return null;
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
