package it.unitn.disi.logcompliance.prediction.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Node;

import it.unitn.disi.logcompliance.prediction.tools.objects.QueryTask;
import it.unitn.disi.logcompliance.prediction.tools.objects.Valutation;

public class Predictor {
	XLog dataset;
	float X;
	IWorkbenchWindow window;
	public static String noAnnotation = "none";
	public static String breachReached = "A security breach has been reached";
	public Predictor(float X,XLog dataset) {
		this.dataset = dataset;
		this.X = X;
		window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}
	
	/**
	 * Returns the predictions for a given trace
	 * @param trace The trace to get prediction for
	 * @param fullPrediction: a boolean that indicate whether the prediction should include all the annotations
	 * @return A string containing all the annotations; if none is present the string "none" is returned;
	 */
	public String predict(XTrace trace,boolean fullPrediction) {
		Pair<XLog,XLog> newLogs = getRightWrongTraces(trace,true);
		String res = "";
		float P = (float)newLogs.right.size() / (newLogs.left.size() + newLogs.right.size());
		//MessageDialog.openError(window.getShell(), "ok", "Annotations: " + annotations.size() + "; P = " + newLogs.right.size() + "/("+newLogs.left.size()+"+"+newLogs.right.size()+")="+ P + "; X = " +X);
		if(P>=X) {
			ArrayList<Annotation> annotations = getSegnalation(trace,newLogs.right,fullPrediction);
			for(Annotation a:annotations) {
				res += a.annotation + "[" + a.minDist() +"]" + LogEnricher.notationSeparator;
			}
			if(annotations.size() == 0) {
				res = breachReached;
			}
		}else {
			res = noAnnotation;
		}
		return res;
	}
	
	/**
	 * Return the statistics for a given trace
	 * @param trace the trace to be tested
	 * @return Valutation object containing all the data
	 */
	public Valutation statsPredict(XTrace trace) {
		Valutation stats = new Valutation();
		XTrace partialTrace = new XTraceImpl(null);
		String res;
		boolean hasBreach = trace.getAttributes().get(LogEnricher.keyHasBreach).toString().equals("true");
		String summary = "";
		if(hasBreach) {
			System.out.println(trace.getAttributes().get("concept:name").toString());
			summary = trace.getAttributes().get(LogEnricher.notationSummary).toString();
		}
			
		//String debug = "";
		for(XEvent e:trace) {
			//Non aggiungo eventi fittizzi
			if(e.getAttributes().get("concept:name") != null) {
				partialTrace.add(e);
				//debug += e.getAttributes().get("concept:name").toString() + "-";
				res = predict(partialTrace,true);
				if(res.equals(breachReached))
					break;
				//Check where is a TP,FP....
				if(hasBreach) {
					if(res.equals(noAnnotation)) {
						//False negative
						stats.setFN();
					}else if(containNotation(summary,res)) {
						//True Positive
						stats.setTP();
					}else if(!res.equals(noAnnotation)) {
						//Wrong segnalation
						stats.setWrongAnnotation();
					}	
				}else {
					if(res.equals(noAnnotation)) {
						//True negative
						stats.setTN();
					}else {
						//False positive
						stats.setFP();
					}
				}
			}
			//MessageDialog.openError(window.getShell(), "Testing", "Trace: " + debug + "\n" + Arrays.toString(stats) );
		}
		return stats;
	}
	
	private boolean containNotation(String summary, String prediction) {
		boolean res = false;
		//MessageDialog.openError(window.getShell(), "Contain", "summ: "+summary + "- Pred:" + prediction );
		String[] pred = prediction.split(LogEnricher.notationSeparator);
		//MessageDialog.openError(window.getShell(), "PRediction", Arrays.toString(pred));
		for(String s:pred) {
			s = s.substring(0, s.indexOf("["));
			if(summary.contains(s))
				res = true;
		}
		//MessageDialog.openError(window.getShell(), "Contain", "summ: "+summary + "- Pred:" + prediction + "\n Res = " + res);
		return res;
	}
	
	private HashMap<String,Annotation> elaborateSegnalation(XTrace trace,XLog wrongLogs) {
		//Could use Map<String,int> 
		HashMap<String,Annotation> annotations = new HashMap<>();
		String attr;
		Annotation temp;
		XAttribute attrTemp;
		//Gather all annotations
		for(XTrace t:wrongLogs) {
			for(int i = 0;i<t.size();i++) {
				XEvent e = t.get(i);
				attrTemp = e.getAttributes().get(LogEnricher.notationKey);
				if(attrTemp != null) {
					String[] annString = attrTemp.toString().split(LogEnricher.notationSeparator);
					//MessageDialog.openError(window.getShell(), "ok", annString.toString());
					for(String s:annString) {
						temp = annotations.get(s);
						if(temp != null) {
							annotations.get(s).distances.add(i);
						}else {
							annotations.put(s, new Annotation(s,i));
						}
					}
				}
			}
		}
		return annotations;
	}
	private ArrayList<Annotation> getSegnalation(XTrace trace,XLog wrongLogs, boolean fullPrediction){
		HashMap<String,Annotation> annotations = elaborateSegnalation(trace,wrongLogs);
		//Decide which ones ought to be in the output
		//Criteria: closest one!
		int dist = 999999;
		int tmp = 0;
		ArrayList<Annotation> output = new ArrayList<>();
		for (Map.Entry<String, Annotation> entry : annotations.entrySet()){
		    if(!fullPrediction) {
		    	tmp = entry.getValue().minDist();
				if(tmp < dist) {
			    	dist = tmp;
			    	output.clear();
			    	output.add((Annotation) entry.getValue());
			    }else if(tmp == dist) {
			    	output.add((Annotation) entry.getValue());
			    }
		    }else {
		    	output.add((Annotation) entry.getValue());
		    }
		}
		output.sort(new Comparator<Annotation>() {
			@Override
			public int compare(Annotation o1, Annotation o2) {
				int res = o2.frequency - o1.frequency;
				return res*-1;
			}
		});	
		return output;
	}
	/**
	 * Returns two logs containing the right and the wrong traces
	 * @param trace the partial trace
	 * @param cutTraces whether to return the entire trace or just the last part
	 * @return Pair<RIGHTtraces,WRONGtrace>
	 */
	private Pair<XLog,XLog> getRightWrongTraces(XTrace trace,boolean cutTraces){
		XLog rightTraces = new XLogImpl(null);
		XLog wrongTraces = new XLogImpl(null);
		for(XTrace t:dataset) {
			if(isPartiallyEqual(t, trace)) {
				if(t.getAttributes().get(LogEnricher.keyHasBreach).toString().equals("true")) {
					if(cutTraces)
						wrongTraces.add(cutTrace(t,trace.size()));
					else
						wrongTraces.add(t);
				}else {
					if(cutTraces)
						rightTraces.add(cutTrace(t,trace.size()));
					else
						rightTraces.add(t);
				}
			}
		}
		//MessageDialog.openError(window.getShell(), "ok", "Right: " + rightTraces.size() + "; Wrong = " + wrongTraces.size() );
		return new Pair<XLog,XLog>(rightTraces,wrongTraces);
	}
	
	private XTrace cutTrace(XTrace trace,int start) {
		XTrace newTrace = new XTraceImpl(trace.getAttributes());
		for(int i = start; i < trace.size();i++) {
			newTrace.add(trace.get(i));
		}
		return newTrace;
	}
	

	/**
	 * Returns wheter the two traces are partially equal, meaning that the first one contains the second one
	 * @param complete full trace
	 * @param partial partial trace
	 * @return true if Full contains Partial, false otherwise
	 */
	public boolean isPartiallyEqual(XTrace complete, XTrace partial) {
		boolean res = true;
		XEvent ep,ec;
		for(short i=0,j=0;i<partial.size() && j<complete.size();) {
			ep = partial.get(i);
			ec = complete.get(j);
			if(ec.getAttributes().get(LogEnricher.placeholderKey) != null)
				j++;
			else if(ep.getAttributes().get(LogEnricher.placeholderKey) != null)
				i++;
			else {
				if(!ep.getAttributes().get("concept:name").equals(ec.getAttributes().get("concept:name")))
					res = false;
				i++;
				j++;
			}
		}	
		return res;
	}

	private class Pair<L,R> {
		private final L left;
		private final R right;

		public Pair(L left, R right) {
			this.left = left;
			this.right = right;
		}

		public L getLeft() { return left; }
		public R getRight() { return right; }

		@Override
		public int hashCode() { return left.hashCode() ^ right.hashCode(); }

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Pair)) return false;
				Pair pairo = (Pair) o;
				return this.left.equals(pairo.getLeft()) && this.right.equals(pairo.getRight());
			}
	}
	private class Annotation{
		String annotation;
		ArrayList<Integer> distances;
		int frequency;
		public Annotation(String annotation,int distance) {
			this.annotation =annotation;
			this.distances = new ArrayList<>();
			distances.add(distance);
		}
		public int minDist() {
			int res = distances.get(0);
			for(Integer i:distances) {
				if(i<res)
					res = i;
			}
			return res;
		}
		@Override
	    public int hashCode() {
	        return new HashCodeBuilder(17, 31).append(annotation).toHashCode();
	    }

	    @Override
	    public boolean equals(Object obj) {
	       if (!(obj instanceof Annotation))
	            return false;
	        if (obj == this)
	            return true;
	        Annotation rhs = (Annotation) obj;
	        return new EqualsBuilder().append(annotation, rhs.annotation).isEquals();
	    }
	}
	@Deprecated
	private XLog getRightTraces(XTrace trace) {
		return getTraces(trace,false);
	}
	@Deprecated
	private XLog getWrongTraces(XTrace trace) {
		return getTraces(trace,true);
	}
	@Deprecated
	private XLog getTraces(XTrace trace, boolean hasBreach) {
		XLog newLog = new XLogImpl(null);
		for(XTrace t:dataset) {
			if(isPartiallyEqual(t, trace) && t.getAttributes().get(LogEnricher.keyHasBreach).equals(hasBreach))
				newLog.add(t);
		}
		return newLog;
	}
	
}
