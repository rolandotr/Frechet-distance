package algorithms.generalization;

import java.util.LinkedList;
import java.util.List;

import wrappers.GeneralizedPoint;

public class GeneralizedTrajectory {

	private List<GeneralizedPoint> trajectory;
	private String identifier;
	
	public GeneralizedTrajectory(String identifier){
		this.identifier = identifier;
		trajectory = new LinkedList<GeneralizedPoint>();
	}
	
	public int size() {
		return trajectory.size();
	}

	public GeneralizedPoint[] toArray() {
		GeneralizedPoint[] result = new GeneralizedPoint[trajectory.size()];
		return trajectory.toArray(result);
	}

	public String getIdentifier() {
		return identifier;
	}

	public void addPoint(GeneralizedPoint point) {
		trajectory.add(point);
	}
	
	@Override
	public String toString() {
		String result = "";
		for (GeneralizedPoint point : trajectory) {
			result += "("+point.toString()+")";
		}
		return result;
	}

	public List<GeneralizedPoint> points() {
		return trajectory;
	}
}
