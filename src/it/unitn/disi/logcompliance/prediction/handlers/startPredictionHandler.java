package it.unitn.disi.logcompliance.prediction.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.deckfour.xes.model.XLog;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import it.unitn.disi.logcompliance.prediction.PredictionActivator;
import it.unitn.disi.logcompliance.prediction.PredictionLog;
import it.unitn.disi.logcompliance.prediction.tools.LogEnricher;
import it.unitn.disi.logcompliance.prediction.tools.ModelTrainer;
import it.unitn.disi.logcompliance.prediction.tools.Predictor;

public class startPredictionHandler implements IHandler {

	IWorkbenchWindow window;

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
		// TODO Auto-generated method stub
		window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		
		XLog dataset = PredictionActivator.getDataset();
		if(dataset == null) {
			File logFile = new File("src/newLog.xes");
			if(logFile.exists() && !logFile.isDirectory()) {
				dataset = LogEnricher.parse(logFile).get(0);
			}else {
				MessageDialog.openError(window.getShell(), "No log found", "No log was found, cannot start the prediction");
				return null;
			}
		}
		float X = PredictionActivator.getX();
		if(X == -1) {
			X = 0.5f;
			MessageDialog.openInformation(window.getShell(), "No X found", "No X was defined, the standard 0.5 will be used");
		}
		Predictor predictor = new Predictor(X,dataset);
		List<XLog> logs = LogEnricher.parse(new File("src/debug intero/partial.xes"));
		MessageDialog.openInformation(window.getShell(), "Prediction", predictor.predict(logs.get(0).get(0),false)); 	
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
