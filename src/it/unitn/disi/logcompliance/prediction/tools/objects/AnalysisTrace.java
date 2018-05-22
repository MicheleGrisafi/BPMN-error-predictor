package it.unitn.disi.logcompliance.prediction.tools.objects;

import java.util.ArrayList;
import java.util.List;

public class AnalysisTrace {
	List<String> names=new ArrayList<String>();
	List<String> moves=new ArrayList<String>();
	List<String> ids= new ArrayList<String>();
	List<String> resources = new ArrayList<String>();
	
	public AnalysisTrace(List<String> names, List<String> moves, List<String> ids, List<String> resources) {
		this.names = names;
		this.moves = moves;
		this.ids = ids;
		this.resources = resources;
	}
	public AnalysisTrace() {
		
	}
	
	public List<String> getNames() {
		return names;
	}
	public void setNames(List<String> names) {
		this.names = names;
	}
	public List<String> getMoves() {
		return moves;
	}
	public void setMoves(List<String> moves) {
		this.moves = moves;
	}
	public List<String> getIds() {
		return ids;
	}
	public void setIds(List<String> ids) {
		this.ids = ids;
	}
	public List<String> getResources() {
		return resources;
	}
	public void setResources(List<String> resources) {
		this.resources = resources;
	}
	@Override
	public String toString() {
		return names.size() + " " + ids.size() + " " + moves.size();
	}
}
