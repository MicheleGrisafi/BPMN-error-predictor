package it.unitn.disi.logcompliance.prediction.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.*;
import org.deckfour.xes.model.impl.XAttributeBooleanImpl;
import org.deckfour.xes.model.impl.XAttributeDiscreteImpl;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.deckfour.xes.out.XesXmlSerializer;
import org.eclipse.bpmn2.*;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.di.BPMNDiagram;
import org.eclipse.bpmn2.modeler.core.utils.ModelUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.graphiti.ui.editor.DiagramEditor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;


import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import it.unitn.disi.logcompliance.main.analysis.utils.Trace;
import it.unitn.disi.logcompliance.prediction.PredictionLog;
import it.unitn.disi.logcompliance.prediction.tools.objects.AnalysisTrace;
import it.unitn.disi.logcompliance.prediction.tools.objects.AnalysisTraceList;
import it.unitn.disi.logcompliance.prediction.tools.objects.QueryConnection;
import it.unitn.disi.logcompliance.prediction.tools.objects.QueryPolicy;
import it.unitn.disi.logcompliance.prediction.tools.objects.QueryTask;

import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class LogEnricher {
	File analysisLogFile;
	File analysisResultFile;
	File partialTrace;
	IWorkbenchWindow window;
	public static String notationSeparator = ";:;";
	AnalysisTraceList analysisResult;
	ArrayList<QueryPolicy> allQueryPolicies;
	public static String notationKey = "prediction:notation";
	public static String placeholderKey = "prediction:placeholder";
	public static String keyHasBreach = "prediction:hasBreach";
	public static String notationSummary = "prediction:summary";
	private short wrong;
	private short right;

	
	public LogEnricher(File analysislogFile, File analysisResultFile, ArrayList<File> policies) {
		this.analysisLogFile = analysislogFile;
		this.analysisResultFile = analysisResultFile;
		analysisResult = readAnalysis(analysisResultFile);
		window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		wrong = right = 0;
		allQueryPolicies = readQueries(policies);
	}
	public LogEnricher() {}
	
	public XLog start() {
		XFactory factory = new XFactoryBufferedImpl();
		XLog newLog = factory.createLog();
		String data = "";
		//Read file and initialize parser
		List<XLog> logs = parse(analysisLogFile);
		XLog log = logs.get(0);
		for (XTrace trace : log) {
			newLog.add(enrichTrace(trace));
		}
		XAttributeMap newMap = new XAttributeMapImpl();
		newMap.put("isEnriched", new XAttributeBooleanImpl("isEnriched",true));
		newMap.put("wrongTraces", new XAttributeDiscreteImpl("wrongTraces",wrong));
		newMap.put("rightTraces", new XAttributeDiscreteImpl("rightTraces",right));
		newLog.setAttributes(newMap);
		
		XesXmlSerializer serializer;
		serializer = new XesXmlSerializer();
		
		try {
			OutputStream out = new FileOutputStream(new File("src/newLog.xes"));
			serializer.serialize(newLog, out);
			MessageDialog.openInformation(window.getShell(), "Log created", "The log was successful created and written to a file");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MessageDialog.openError(window.getShell(), "Error", e.toString());
		}
		return newLog;
	}
	/**
	 * Insert the notation in the given point
	 * @param tempTrace		trace to enrich
	 * @param annotation	String to annotate in the event
	 * @param e				Event to annotate, null if it's going to be z or z+1
	 * @param z				Wheter the event should be the last one of the trace
	 * @param z1			Whether the event should be the one after the lasat one of the trace, z+1
	 */
	private void insertNotation(XTrace tempTrace, String annotation, XEvent e, boolean z, boolean z1) {
		if(z1 && !tempTrace.get(tempTrace.size()-1).getAttributes().containsKey(placeholderKey)) {
			XAttributeMap fixMap = new XAttributeMapImpl();
			fixMap.put(placeholderKey, new XAttributeBooleanImpl(placeholderKey,true));
			XEvent fixEvent = new XEventImpl(fixMap);
			tempTrace.add(fixEvent);
		}
		String toInsert = "";
		if(z || z1)
			e = tempTrace.get(tempTrace.size()-1);
   		XAttributeMap map = e.getAttributes();
		XAttribute notationOld = map.get(notationKey);		   					
		if(notationOld != null)
			toInsert = notationOld.toString() + notationSeparator + annotation;
		else
			toInsert = annotation;
		XAttribute notation = new XAttributeLiteralImpl(notationKey,toInsert);
		map.put(notationKey,notation);
		e.setAttributes(map);
		
		//insert annotation to Trace summary
		XAttributeMap traceMap = tempTrace.getAttributes();
		if(traceMap == null)
			traceMap = new XAttributeMapImpl();
		String summary = "";
		XAttribute summ = traceMap.get(notationSummary);
		if(summ != null)
			summary += summ.toString() + notationSeparator + annotation;
		else
			summary += annotation;
		summ = new XAttributeLiteralImpl(notationSummary,summary);
		traceMap.put(notationSummary, summ);
		tempTrace.setAttributes(traceMap);
	}
	
	private XTrace enrichTrace(XTrace trace) {
		ArrayList<QueryPolicy> violated;
		
		XTrace tempTrace;
		//new logs: one for the right traces, one for the traces containing security breaches
		XAttributeMap mapTrace = trace.getAttributes();
		XAttribute id = mapTrace.get("concept:name");
		violated = checkForBreach(id.toString());
		if(violated != null) {
			//has security breach
			mapTrace.put(keyHasBreach, new XAttributeBooleanImpl(keyHasBreach,true));
			wrong++;
		}else {
			mapTrace.put(keyHasBreach, new XAttributeBooleanImpl(keyHasBreach,false));
			right++;
		}
		tempTrace = trace;
		tempTrace.setAttributes(mapTrace);
		int tempTraceSize = tempTrace.size();
		//tempTrace.setAttributes(mapTrace);
		
		//Scroll through the trace's events and check if there ought to be an annotation.
		if(violated != null ) {
			HashSet<QueryTask> tempTasks = new HashSet<>();
			String toInsert = "";
			for(QueryPolicy q : violated) {
				QueryTask tempTask = null;
				/******************************************************************	 NORMAL QURIES  **************************************************************/
				if(!q.isAntipattern()) {
					for(QueryConnection qc : q.getConnections()) {
						tempTask = qc.getA();
						if(tempTask.getName()!="@")
							tempTasks.add(tempTask);
						tempTask = qc.getB();
						if(tempTask.getName()!="@")
							tempTasks.add(tempTask);
						short indexA = -1, indexB = -1;
						XEvent a = null, b = null;   
						switch(qc.getConnection()) {
		     			   	case QueryConnection.WALK:
			     			   	boolean seq = true;
		     			   		// @ walk B
		     			   		if(qc.getA().getName() =="@") {
			     			   		for(short i = 0; i<tempTraceSize;i++) {
			     			   			XEvent e = tempTrace.get(i);
			     			   			XAttributeMap map = e.getAttributes();
			     			   			String name = map.get("concept:name").toString();
			     			   			if(name.equals(qc.getB().getName())) {
			     			   				b = e;
			     			   				indexB = i;  	
			     			   			}
			     			   		}
			     			   		if(b==null) {
			     			   			insertNotation(tempTrace,qc.toString(),null,false,true);
			     			   		}else if(indexB == 0) {
			     			   			insertNotation(tempTrace,qc.toString(),b,false,false);	
			     			   		}
		     			   		}else if(qc.getB().getName()=="@") {
		     			   			// A walk @
			     			   		for(short i = 0; i<tempTraceSize;i++) {
			     			   			XEvent e = tempTrace.get(i);
			     			   			XAttributeMap map = e.getAttributes();
			     			   			String name = map.get("concept:name").toString();
			     			   			if(name.equals(qc.getA().getName())) {
			     			   				a = e;
			     			   				indexA = i;  	
			     			   			}
			     			   		}
			     			   		if(a ==null) {
			     			   			insertNotation(tempTrace,qc.toString(),null,true,false);
			     			   		}else if(indexA == tempTraceSize-1) {
			     			   			insertNotation(tempTrace,qc.toString(),null,false,true);
			     			   		}
		     			   		}else {
			     			   		if(id.toString().equals("10"))
			     			   			System.out.println("b null for 10");
		     			   			for(short i = 0; i<tempTraceSize;i++) {
			     			   			XEvent e = tempTrace.get(i);
			     			   			XAttributeMap map = e.getAttributes();
			     			   			String name = map.get("concept:name").toString();
			     			   			if(name.equals(qc.getA().getName())) {
			     			   				a = e;
			     			   				indexA = i;
			     			   				if(b != null)
			     			   					seq=false;     	
			     			   			}else if(name.equals(qc.getB().getName())) {
			     			   				b = e;	
			     					   	}
			     			   		}
			     			   		if(b == null) {//|| (a null and b null)
			     			   			insertNotation(tempTrace,qc.toString(),null,false,true);
			     			   		}else if (!seq || a == null) {
			     			   			insertNotation(tempTrace,qc.toString(),b,false,false);
			 			   			}
		     			   		}
		     				   break;
		     			   	case QueryConnection.nWALK:
			     			   	seq = true;
			     			   	if(qc.getA().getName() =="@") {
			     			   		// @ Nwalk B
			     			   		for(short i = 0; i<tempTraceSize;i++) {
			     			   			XEvent e = tempTrace.get(i);
			     			   			XAttributeMap map = e.getAttributes();
			     			   			String name = map.get("concept:name").toString();
			     			   			if(name.equals(qc.getB().getName())) {
			     			   				b = e; 	
			     			   			}
			     			   		}
			     			   		if(b==null) {
			     			   			insertNotation(tempTrace,qc.toString(),null,false,true);
			     			   		}
		     			   		}else if(qc.getB().getName()=="@") {
		     			   			// A Nwalk @
			     			   		for(short i = 0; i<tempTraceSize;i++) {
			     			   			XEvent e = tempTrace.get(i);
			     			   			XAttributeMap map = e.getAttributes();
			     			   			String name = map.get("concept:name").toString();
			     			   			if(name.equals(qc.getA().getName())) {
			     			   				a = e;
			     			   			}
			     			   		}
			     			   		if(a == null) {
			     			   			insertNotation(tempTrace,qc.toString(),null,false,true);
			     			   		}
		     			   		}else {
			     			   		for(short i = 0; i<tempTraceSize;i++) {
			     			   			XEvent e = tempTrace.get(i);
			     			   			XAttributeMap map = e.getAttributes();
			     			   			String name = map.get("concept:name").toString();
			     			   			if(name.equals(qc.getA().getName())) {
			     			   				a = e;
			     			   			}else if(name.equals(qc.getB().getName())) {
			     			   				b = e;
			     			   				indexB = i;
			     			   				if(a != null)
			     			   					seq=false;     	
			     					   	}
			     			   		}
			     			   		if(a == null) {//|| (a null and b null)
			     			   			insertNotation(tempTrace,qc.toString(),null,false,true);
			     			   		}else if (!seq || b == null) {
			     			   			insertNotation(tempTrace,qc.toString(),a,false,false);
			 			   			}
		     			   		}
		     			   		break;
	     			   		case QueryConnection.FLOW:
		     			   		boolean ab = false,ba = false;
			     			   	if(qc.getB().getName() =="@") {
			     			   		// A flow @
			     			   		for(short i = 0; i<tempTraceSize;i++) {
			     			   			XEvent e = tempTrace.get(i);
			     			   			XAttributeMap map = e.getAttributes();
			     			   			String name = map.get("concept:name").toString();
			     			   			if(name.equals(qc.getB().getName())) {
			     			   				a = e; 	
			     			   				indexA = i;
			     			   			}
			     			   		}
			     			   		if(a==null) {
			     			   			insertNotation(tempTrace,qc.toString(),null,true,false);
			     			   		}else if(indexA == tempTraceSize-1) {
			     			   			insertNotation(tempTrace,qc.toString(),null,false,true);
			     			   		}
		     			   		}else if(qc.getA().getName()=="@") {
		     			   			// @ flow B
			     			   		for(short i = 0; i<tempTraceSize;i++) {
			     			   			XEvent e = tempTrace.get(i);
			     			   			XAttributeMap map = e.getAttributes();
			     			   			String name = map.get("concept:name").toString();
			     			   			if(name.equals(qc.getA().getName())) {
			     			   				b = e;
			     			   				indexB = i;
			     			   			}
			     			   		}
			     			   		if(b == null) {
			     			   			insertNotation(tempTrace,qc.toString(),null,false,true);
			     			   		}else if(indexB == tempTraceSize-1) {
			     			   			insertNotation(tempTrace,qc.toString(),b,false,false);
			     			   		}
		     			   		}else {
			     			   		for(short i = 0; i<tempTraceSize;i++) {
				   						XEvent e = tempTrace.get(i);
			     			   			XAttributeMap map = e.getAttributes();
			     			   			String name = map.get("concept:name").toString();
			     			   			if(name.equals(qc.getA().getName())) {
			     			   				a = e;
			     			   				indexA = i;
			     			   				if(b != null)
			     			   					ba=true;     	
			     			   			}else if(name.equals(qc.getB().getName())) {
			     			   				b = e;
				     			   			if(a != null && !a.equals(tempTrace.get(i-1)))
				     			   				ab =true;  
			     					   	}
			     			   		}
			     			   		if((a == null && b!=null) || ba) {
			     			   			insertNotation(tempTrace,qc.toString(),b,false,false);
			     			   		}else if (b == null && a == null) {
			     			   			insertNotation(tempTrace,qc.toString(),null,false,true);
			 			   			}else if(b == null || ab) {
			 			   				//create new fake event in case A is the last one
			 			   				if(indexA +1 == tempTraceSize && tempTrace.get(tempTraceSize-1).getAttributes().get(placeholderKey) == null) {
			 			   					XAttributeMap fixMap = new XAttributeMapImpl();
			 			   					fixMap.put(placeholderKey, new XAttributeBooleanImpl(placeholderKey,true));
			 			   					XEvent fixEvent = new XEventImpl(fixMap);
			 			   					tempTrace.add(fixEvent);
			 			   				}
				 			   			XEvent aPlusOne = tempTrace.get(indexA+1);
				 			   			insertNotation(tempTrace,qc.toString(),aPlusOne,false,false);
			 			   			}
		     			   		}
		     			   		break;
		     			   	case QueryConnection.nFLOW:
		     			   		ab = false;
			     			   	if(qc.getB().getName() =="@") {
			     			   		// A Nflow @
			     			   		for(short i = 0; i<tempTraceSize;i++) {
			     			   			XEvent e = tempTrace.get(i);
			     			   			XAttributeMap map = e.getAttributes();
			     			   			String name = map.get("concept:name").toString();
			     			   			if(name.equals(qc.getB().getName())) {
			     			   				a = e; 	
			     			   			}
			     			   		}
			     			   		if(a==null) {
			     			   			insertNotation(tempTrace,qc.toString(),null,false,true);
			     			   		}
		     			   		}else if(qc.getA().getName()=="@") {
		     			   			// @ Nflow B
			     			   		for(short i = 0; i<tempTraceSize;i++) {
			     			   			XEvent e = tempTrace.get(i);
			     			   			XAttributeMap map = e.getAttributes();
			     			   			String name = map.get("concept:name").toString();
			     			   			if(name.equals(qc.getA().getName())) {
			     			   				b = e;
			     			   			}
			     			   		}
			     			   		if(b == null) {
			     			   			insertNotation(tempTrace,qc.toString(),null,false,true);
			     			   		}
		     			   		}else {
			     			   		for(short i = 0; i<tempTraceSize;i++) {
				   						XEvent e = tempTrace.get(i);
			     			   			XAttributeMap map = e.getAttributes();
			     			   			String name = map.get("concept:name").toString();
			     			   			if(name.equals(qc.getA().getName())) {
			     			   				a = e;
			     			   				if(b != null)
			     			   					seq=false;     	
			     			   			}else if(name.equals(qc.getB().getName())) {
			     			   				b = e;
			     			   				indexB = i;
				     			   			if(a != null && a.equals(tempTrace.get(i-1)))
				     			   				ab =true;
			     					   	}
			     			   		}
			     			   		if(a == null) {//|| (a null and b null)
			     			   			insertNotation(tempTrace,qc.toString(),null,false,true);
			     			   		}else if (b== null || ab) {
			     			   			insertNotation(tempTrace,qc.toString(),a,false,false);
			 			   			}
		     			   		}
						}
					}
				}else {
					/******************************************************************	 ANTIPATTERN QUERIES  **************************************************************/
				}
			}
			//Check wheter a single task policy needs to be annotated because it was never used in other normal queries
			for(QueryPolicy q : violated) {
				if(!q.isAntipattern()) {
					for(QueryTask qt : q.getTasks()) {
						toInsert = "";
						if(!tempTasks.contains(qt)) {
							insertNotation(tempTrace,qt.getName(),null,false,true);
						}						
					}
				}		
			}
		}
		
		return tempTrace;
	}
	public AnalysisTraceList readAnalysis(File file) {
		AnalysisTraceList list = CSVUtils.parseTrace(file, ";");
		return list;
	}
	
	//Translate a query file in a query object
	private QueryPolicy readQuery(File queryFile) {
		String data = "";
		//MessageDialog.openError(window.getShell(), "File query:",queryFile.getName());
		boolean antipattern = false;
		HashSet<QueryTask> tasks = new HashSet<>();
		ArrayList<QueryConnection> connections = new ArrayList<>();
		try {
			//Decode bmpnq file for XML parsing
	        File inputFile = queryFile;
	        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	        Document doc = dBuilder.parse(inputFile);
	        doc.getDocumentElement().normalize();
	        
	        //Check whether antipattern is checked
	        String antiString = doc.getDocumentElement().getAttribute("antipattern");
	        if (antiString == "true")
	        	antipattern = true;
	        
	        //Analyze every process
	        NodeList nList = doc.getElementsByTagName("bpmn2:process");
	        for (short i = 0; i < nList.getLength(); i++) {
	        	//MessageDialog.openError(window.getShell(), "Connection:", "Found node");
	        	Node nNode = nList.item(i);
	        	if(nNode.getNodeType() == Node.ELEMENT_NODE) {
	        	   //Gather all the tasks and connections
	        		Element process = (Element)nNode;
	        		NodeList taskElements = process.getElementsByTagName("bpmn2:task");
	        		NodeList connectionElements = process.getElementsByTagName("bpmn2:sequenceFlow");
	        		for (short j = 0; j < taskElements.getLength(); j++) {
	        			//MessageDialog.openError(window.getShell(), "Connection:", "Found task");
	        			//Create tasks if it's not a wildcard
	        			if(taskElements.item(j).getNodeType() == Node.ELEMENT_NODE && !((Element)taskElements.item(j)).getAttribute("name").startsWith("@")) 
	        				tasks.add(new QueryTask(((Element)taskElements.item(j)).getAttribute("name")));
	        		}
	        		for (short j = 0; j < connectionElements.getLength(); j++) {
	        			//MessageDialog.openError(window.getShell(), "Connection:", "Found connection");
	        			//Create connections
	        			if(connectionElements.item(j).getNodeType() == Node.ELEMENT_NODE) {
	        				String typology = ((Element)connectionElements.item(j)).getAttribute("xsi:type");
	        				short type = 0;
	        				switch(typology) {
		        			   	case "secbpmn:negativeflow":
		        			   		type = QueryConnection.nFLOW;
		        			   		break;
		        			   	case "secbpmn:path":
		        			   		type = QueryConnection.WALK;
		        			   		break;
		        			   	case "secbpmn:negativepath":
		        			   		type = QueryConnection.nWALK;
		        			   		break;
		        			   	default:
		        			   		type= QueryConnection.FLOW;
	        				}
	        				//Retrieve source and target form bpmn id
	        				String source = "",target = "";
	        				for (short n = 0; n < taskElements.getLength(); n++) {
	        					if(taskElements.item(n).getNodeType() == Node.ELEMENT_NODE && ((Element)taskElements.item(n)).getAttribute("id").equals(((Element)connectionElements.item(j)).getAttribute("sourceRef"))) {
	        						source = ((Element)taskElements.item(n)).getAttribute("name");
	        						//MessageDialog.openError(window.getShell(), "Source:", source);
	        					} else if(taskElements.item(n).getNodeType() == Node.ELEMENT_NODE && ((Element)taskElements.item(n)).getAttribute("id").equals(((Element)connectionElements.item(j)).getAttribute("targetRef"))) {
	        						target = ((Element)taskElements.item(n)).getAttribute("name");
	        						//MessageDialog.openError(window.getShell(), "Tarfet:", target);
	        					}
	        				}
	        				QueryConnection newConnection = new QueryConnection(type,new QueryTask(source),new QueryTask(target));
	        				connections.add(newConnection);
	        				
	        			}
	        		}
	        	}
	        }
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
		return new QueryPolicy(queryFile.getName(),connections,tasks,antipattern);
	}
	private ArrayList<QueryPolicy> readQueries(ArrayList<File> files){
		ArrayList<QueryPolicy> queries = null;
		if(files.size()>0) {
			queries = new ArrayList<>(files.size());
			for(File f : files) {
				queries.add(readQuery(f));
			}
		}
		return queries;
	}
	/***
	 * Chek wether the given trace contains security breaches
	 * @param traceName : name of the trace to check
	 * @return array of security policies violated by the trace, null otherwise
	 */
	private ArrayList<QueryPolicy> checkForBreach(String traceName) {
		ArrayList<QueryPolicy> result = null;
		AnalysisTraceList analy = analysisResult.getAnalysisTrace(traceName);

		for(AnalysisTrace an:analy) {
			ArrayList<String> queries = (ArrayList<String>) an.getResources();
			for(QueryPolicy q : allQueryPolicies) {
				if(queries.contains(q.getId())) {
					if(result == null)
						result = new ArrayList<>();
					result.add(q);
				}
			}
		}
		
		return result;
	}
	
	public static List<XLog> parse(File file){
		XesXmlParser parser = new XesXmlParser();
		List<XLog> logs = new ArrayList();
		/*
		InputStream is = null;
		try {
			is = analysisLog.getContents();
		} catch (CoreException e1) {
			PredictionLog.logError("AnalysisLog reading error", e1);
			e1.printStackTrace();
		}
		try {
			if (is != null) {
				logs = parser.parse(is);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		try {
			logs = parser.parse(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return logs;
	}
}
