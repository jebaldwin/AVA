package org.eclipse.zest.custom.sequence.assembly;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.zest.custom.sequence.assembly.editors.AssemblySequenceEditor;
import org.eclipse.zest.custom.sequence.assembly.editors.DynamicAssemblySequenceEditor;
import org.eclipse.zest.custom.sequence.assembly.editors.NavigationAssemblySequenceEditor;
import org.eclipse.zest.custom.sequence.assembly.editors.TraceEditor;
import org.eclipse.zest.custom.statediagram.assembly.editors.StateEditor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author jbaldwin
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.zest.custom.sequence.assembly";
	static final String VERSION_STRING[] = { "0.046", "2.0" // from Workbench
															// class in Eclipse
															// 3.3
	};

	// The shared instance
	private static Activator plugin;
	public DynamicAssemblySequenceEditor dynamicEditor;
	public NavigationAssemblySequenceEditor navEditor;
	public AssemblySequenceEditor activeeditor;
	//public TraceAssemblySequenceEditor tactiveeditor;
	public static LinkedList<AssemblySequenceEditor> editors = new LinkedList<AssemblySequenceEditor>();
	public static HashMap<String, StateEditor> stateeditors = new HashMap<String, StateEditor>();
	//public static LinkedList<TraceAssemblySequenceEditor> teditors = new LinkedList<TraceAssemblySequenceEditor>();
	//public static LinkedList<TraceAssemblySequenceEditor> traceEditors = new LinkedList<TraceAssemblySequenceEditor>();

	public TraceEditor tactiveeditor;
	public static LinkedList<TraceEditor> teditors = new LinkedList<TraceEditor>();
	public static LinkedList<TraceEditor> traceEditors = new LinkedList<TraceEditor>();
	/**
	 * The constructor
	 */
	public Activator() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {

		plugin = this;
		/*
		 * try { Location installLoc = Platform.getInstallLocation(); // fetch
		 * the Location that we will be modifying Location instanceLoc =
		 * Platform.getInstanceLocation(); // set location to c:\temp
		 * instanceLoc.set(installLoc.getURL(), false); } catch (Exception err)
		 * { err.printStackTrace(); }
		 */
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		// TODO shut down dynamic editor because it never reopens properly
		// editor.dispose();
		plugin = null;
		super.stop(context);
		Startup.send(null, "bye");
	}

	private IStatus saveState(final IMemento memento) {
		MultiStatus result = new MultiStatus(PlatformUI.PLUGIN_ID, IStatus.OK, WorkbenchMessages.Workbench_problemsSaving, null);
		// Save the version number.
		memento.putString(IWorkbenchConstants.TAG_VERSION, VERSION_STRING[1]);
		// Save how many plug-ins were loaded while restoring the workbench
		memento.putInteger(IWorkbenchConstants.TAG_PROGRESS_COUNT, 10); // we
																		// guesstimate
																		// this
		// Save the advisor state.
		result.add(Status.OK_STATUS);
		// Save the workbench windows.
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
		for (int nX = 0; nX < windows.length; nX++) {
			WorkbenchWindow window = (WorkbenchWindow) windows[nX];
			IMemento childMem = memento.createChild(IWorkbenchConstants.TAG_WINDOW);
			result.merge(window.saveState(childMem));
		}
		result.add(((Workbench) workbench).getEditorHistory().saveState(memento.createChild(IWorkbenchConstants.TAG_MRU_LIST)));
		return result;
	}

	private void saveMementoToFile(XMLMemento memento) {
		File stateFile = getWorkbenchStateFile();
		if (stateFile != null) {
			try {
				FileOutputStream stream = new FileOutputStream(stateFile);
				OutputStreamWriter writer = new OutputStreamWriter(stream, "utf-8"); //$NON-NLS-1$
				memento.save(writer);
				writer.close();
			} catch (IOException ioe) {
				stateFile.delete();
				// Activator.log( ioe );
			}
		}
	}

	private File getWorkbenchStateFile() {
		IPath path = WorkbenchPlugin.getDefault().getDataLocation();
		if (path == null) {
			return null;
		}
		path = path.append("workbench.xml");
		return path.toFile();
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	public static void sendMessage(String message){
		System.out.println("message to sent to comment thread " + message);
		Plugin plug = Platform.getPlugin("AVADocumentationFrame");

	}
}
