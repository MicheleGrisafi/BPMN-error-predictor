package it.unitn.disi.logcompliance.prediction.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import it.unitn.disi.logcompliance.prediction.tools.objects.AnalysisTrace;
import it.unitn.disi.logcompliance.prediction.tools.objects.AnalysisTraceList;


public class CSVUtils {
	 public static AnalysisTraceList parseTrace(File file,String separator) {
		 AnalysisTraceList result = new AnalysisTraceList();
		 AnalysisTrace temp;
		 String line = "";
	     String cvsSplitBy = separator;
	     try (BufferedReader br = new BufferedReader(new FileReader(file))) {
	    	 while ((line = br.readLine()) != null) {
                // use comma as separator
	    		 String[] values = line.split(cvsSplitBy);
	    		 
	    		 temp = new AnalysisTrace();
	    		 temp.setNames(parse(values[2],"-"));
	    		 temp.setMoves(parse(values[3],","));
	    		 temp.setIds(parse(values[4],","));
	    		 temp.setResources(parse(values[5],","));
	    		 result.add(temp);
	    		 //MessageDialog.openError(window.getShell(), "First id", temp.getIds().get(0).toString());
             }
	     } catch (IOException e) {
	    	 e.printStackTrace();
	     }
	     //remove the trace with the coloumns names
	     if(result.size()>0)
	    	 result.remove(0);
	     return result;
    }
	public static List<String> parse(String list,String separator) {
		List<String> result = new ArrayList<>();
		String line = "";
		String cvsSplitBy = separator;
		String[] values = list.split(cvsSplitBy);
		for (int i = 0; i < values.length; i++) {
			result.add(values[i]);
		}
		return result;
	 }


}