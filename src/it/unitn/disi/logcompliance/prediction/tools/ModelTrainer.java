package it.unitn.disi.logcompliance.prediction.tools;

import java.util.ArrayList;
import java.util.Arrays;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeBooleanImpl;
import org.deckfour.xes.model.impl.XAttributeDiscreteImpl;
import org.deckfour.xes.model.impl.XLogImpl;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import it.unitn.disi.logcompliance.prediction.tools.objects.Valutation;

public class ModelTrainer {
	XLog dataset;
	byte k;
	IWorkbenchWindow window;
	public ModelTrainer(byte k,XLog dataset) {
		this.k =k;
		this.dataset = dataset;
		window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}
	public float getX() {
		float X = 0f;
		float increment = 0.1f;
		ArrayList<Valutation> result = new ArrayList<>();
		for(float i = X; i<=1;i+=increment) {
			result.add(trainModel(i));
		}
		
		return X;
	}
	/**
	 * Train a model and get a valutation of it
	 * @param X the X of the model
	 * @return	an objhect containig all the data
	 */
	private Valutation trainModel(float X) {
		XLog[] logs = partitionDataset(dataset);
		ArrayList<XLog> newData;// = new ArrayList<>(k-1);
		Predictor predictor;
		Valutation stats = new Valutation();
		Valutation tempStats;
		for(int i = 0; i<k;i++) {
			newData = new ArrayList<XLog>(Arrays.asList(logs));
			predictor = new Predictor(X,mergeLogs(newData));
			for(XTrace t:logs[i]) {
				tempStats = predictor.statsPredict(t);
				stats.plus(tempStats);
			}
			//MessageDialog.openInformation(window.getShell(), "k: " + i, Arrays.toString(stats));
		}
		//MessageDialog.openInformation(window.getShell(), "X: " + X, "TP: " + stats[0]+"\nTN: " +stats[1]+"\nFN: "+stats[2]+"\nFP:" + stats[3]+"\nFalse annotation:" + stats[4]);
		return stats;
	}
	private XLog mergeLogs(ArrayList<XLog> logs) {
		XLog whole = new XLogImpl(null);
		for(XLog l:logs) {
			for(XTrace t:l) {
				whole.add(t);
			}
		}
		return whole;
	}
	private XLog[] partitionDataset(XLog dataset) {
		int size = dataset.size();
		int kSize = size / k;
		int residual = size%k;
		short rightPerTrace = (short) (((XAttributeDiscreteImpl) dataset.getAttributes().get("rightTraces")).getValue()/k);
		short rightResidual = (short) (((XAttributeDiscreteImpl) dataset.getAttributes().get("rightTraces")).getValue()%k);
		short wrongPerTrace =(short)(((XAttributeDiscreteImpl) dataset.getAttributes().get("wrongTraces")).getValue()/k);
		short wrongResidual =(short)(((XAttributeDiscreteImpl) dataset.getAttributes().get("wrongTraces")).getValue()%k);
		
		String stats = "RightxTrace: "+rightPerTrace+ "; RightResidual: " +rightResidual+";WrongXTrace: "+wrongPerTrace+"; WrongResidual: "+wrongResidual;
				
		short tempRight = 0;
		short tempWrong = 0;
		int indexRight = 0;
		int indexWrong = 0;
		XLog[] partitions = new XLogImpl[k];
		for(int i = 0; i<k;i++) {
			partitions[i] = new XLogImpl(null);
		}
		//Stratification!
		for(int i = 0; i<size; i++) {
			XTrace t = dataset.get(i);
			if((t.getAttributes().get(LogEnricher.keyHasBreach)).toString().equals("true")){
				//has breach
				indexWrong = tempWrong/wrongPerTrace;
				//check for residual
				if(indexWrong == k) {
					partitions[wrongResidual--].add(t);
					//MessageDialog.openError(window.getShell(), "Wrong", "TempWrong: "+tempWrong+"; Index:"+wrongResidual+1);
				}
				else {
					partitions[indexWrong].add(t);
					//MessageDialog.openError(window.getShell(), "Wrong", "TempWrong: "+tempWrong+"; Index:"+indexWrong);
				}
				tempWrong++;
			}else {
				indexRight = tempRight/rightPerTrace;
				//MessageDialog.openError(window.getShell(), "Right", "TempRight: "+tempRight+"; Index:"+indexRight);
				if(indexRight == k) {
					partitions[rightResidual--].add(t);
					//MessageDialog.openError(window.getShell(), "Right", "TempRight: "+tempRight+"; Index:"+rightResidual+1);
				}
				else {
					partitions[indexRight].add(t);
					//MessageDialog.openError(window.getShell(), "Right", "TempRight: "+tempRight+"; Index:"+indexRight);
				}
				tempRight++;
			}
		}
		String partString = "";
		for(int i = 0; i<partitions.length;i++) {
			partString += "Par: " + i + " :"+ partitions[i].size() + ";\n";
		}
		//MessageDialog.openError(window.getShell(), "Partition", partString);
		return partitions;
	}
	
}
