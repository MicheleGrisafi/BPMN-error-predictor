package it.unitn.disi.logcompliance.prediction.tools.objects;

import java.util.ArrayList;
import java.util.HashSet;

public class QueryPolicy {
	ArrayList<QueryConnection> connections;
	HashSet<QueryTask> tasks;
	String id;
	boolean antipattern = false;
	
	public QueryPolicy(String id, ArrayList<QueryConnection> connections, HashSet<QueryTask> tasks, boolean antipattern) {
		this.connections = connections;
		this.tasks = tasks;
		this.id = id;
		this.antipattern = antipattern;
		refine();
	}
	
	public boolean hasEvent(String name) {
		boolean result = false;
		for(QueryTask q : tasks) {
			if(q.getName()==name)
				result = true;
		}
		return result;
	}
	
	private void refine() {
		if(!antipattern) {
			for(QueryConnection q :connections) {
				if(tasks.contains(q.getA()))
					tasks.remove(q.getA());
				else if(tasks.contains(q.getB()))
					tasks.remove(q.getB());
			}
		}
	}
	
	public ArrayList<QueryConnection> getConnections() {
		return connections;
	}
	public void setConnections(ArrayList<QueryConnection> connections) {
		this.connections = connections;
	}
	public HashSet<QueryTask> getTasks() {
		return tasks;
	}
	public void setTasks(HashSet<QueryTask> tasks) {
		this.tasks = tasks;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public boolean isAntipattern() {
		return antipattern;
	}
	public void setAntipattern(boolean antipattern) {
		this.antipattern = antipattern;
	}
	@Override
	public String toString() {
		HashSet<QueryTask> temp = (HashSet<QueryTask>) tasks.clone();
		String result = "The following policies might be violated: \n";
		for(QueryConnection q : connections) {
			if(isAntipattern())
				result+="NOT: ";
			result+= q.getA().getName();
			switch(q.getConnection()) {
				case QueryConnection.FLOW:
					result += "flow to";
					break;
				case QueryConnection.nFLOW:
					result += "negative flow to";
					break;
				case QueryConnection.WALK:
					result += "walk to";
					break;
				case QueryConnection.nWALK:
					result += "negative walk to";
					break;
			}
			result+= q.getB().getName() + ";\n";
		}
		return result;
	}
}
