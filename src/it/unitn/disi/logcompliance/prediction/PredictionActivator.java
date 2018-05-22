package it.unitn.disi.logcompliance.prediction;

import org.deckfour.xes.model.XLog;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class PredictionActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "it.unitn.disi.logcompliance.prediction"; //$NON-NLS-1$

	// The shared instance
	private static PredictionActivator plugin;
	
	private static XLog dataset;
	private static float X =-1;
	
	/**
	 * The constructor
	 */
	public PredictionActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static PredictionActivator getDefault() {
		return plugin;
	}
	
	public static XLog getDataset() {
		return dataset;
	}
	public static void setDataset(XLog data) {
		dataset = data;
	}

	public static float getX() {
		return X;
	}

	public static void setX(float x) {
		X = x;
	}
}
