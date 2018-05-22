package it.unitn.disi.logcompliance.prediction.handlers;

import java.io.File;

import org.deckfour.xes.model.XLog;
import org.eclipse.bpmn2.modeler.core.utils.Messages;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import it.unitn.disi.logcompliance.prediction.PredictionActivator;
import it.unitn.disi.logcompliance.prediction.tools.LogEnricher;
import it.unitn.disi.logcompliance.prediction.tools.ModelTrainer;

public class TrainModelsHandler implements IHandler {
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
				MessageDialog.openError(window.getShell(), "No log found", "No log was found, cannot start the training");
				return null;
			}
		}
		InputDialog dialog = new InputDialog(window.getShell(), "Specify k", "In order to start the training a K value has to be set: ", null, null);
		dialog.open();
		String input = dialog.getValue();
		byte k = Byte.parseByte(input);
		
		ModelTrainer model = new ModelTrainer(k,dataset);
		model.getX();
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
