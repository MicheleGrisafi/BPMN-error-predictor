package it.unitn.disi.logcompliance.prediction;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class PredictionLog {
	public static void logInfo(String message) {
		log(IStatus.INFO, IStatus.OK, message, null);
	}
	public static void logError(Throwable exception) {
		logError("Unexpected Exception", exception);
	}
	public static void logError(String message, Throwable exception) {
		log(IStatus.ERROR, IStatus.OK, message, exception);
	}
	public static void log(int severity, int code, String message,Throwable exception) {
		log(createStatus(severity, code, message, exception));
	}
	public static IStatus createStatus(int severity, int code,String message, Throwable exception) {
		return new Status(severity, PredictionActivator.PLUGIN_ID, code,message, exception);
	}
	public static void log(IStatus status) {
		PredictionActivator.getDefault().getLog().log(status);
	}
}
