package it.unitn.disi.logcompliance.prediction.tools.objects;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class AnalysisTraceList extends ArrayList<AnalysisTrace> {
	//List of analysis
	public AnalysisTraceList getAnalysisTrace(String traceId) {
		AnalysisTraceList result = new AnalysisTraceList();
		
		//IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		for(AnalysisTrace t:this) {
			if(t.getIds().contains(traceId)) {
				result.add(t);
			}
		}
		return result;
	}
}
