package it.unitn.disi.logcompliance.prediction.handlers;
import java.io.File;
import java.util.ArrayList;

import org.deckfour.xes.model.XLog;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import it.unitn.disi.logcompliance.prediction.PredictionActivator;
import it.unitn.disi.logcompliance.prediction.tools.LogEnricher;

public class enrichLogHandler implements IHandler {

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ArrayList<File> policies = new ArrayList<>();
		policies.add(new File("src/debug intero/policy1.bpmnq"));
		policies.add(new File("src/debug intero/policy2.bpmnq"));
		policies.add(new File("src/debug intero/policy3.bpmnq"));
		policies.add(new File("src/debug intero/policy4.bpmnq"));
		policies.add(new File("src/debug intero/policy5.bpmnq"));
		policies.add(new File("src/debug intero/policy6.bpmnq"));
		LogEnricher enricher = new LogEnricher(new File("src/debug intero/debug.xes"),new File("src/debug intero/analDebug.csv"),policies);
		XLog dataset = enricher.start(); 
		PredictionActivator.setDataset(dataset);
		return null;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isHandled() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

}
