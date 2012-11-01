package org.eclipse.zest.custom.sequence.assembly;

import java.io.File;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Semaphore;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.zest.custom.sequence.assembly.model.XMLUtils;
//import org.eclipse.zest.custom.sequence.assembly.editors.AssemblySequenceEditor;
//import org.eclipse.zest.custom.sequence.assembly.model.XMLUtils;
//import org.eclipse.zest.custom.statediagram.assembly.editors.StateEditor;

import cs.uvic.ca.ice.model.Instance;
import cs.uvic.ca.ice.model.InstanceMap;
import cs.uvic.ca.idaplugin.comm.DisassemblerSocketComms;

public class Startup implements IStartup, Observer {

	/* String disassembledFilename
	 * 
	 * Used for dynamic and navigation.
	 */
	public static String disassembledFilename = "";
	
	/* Semaphore available
	 * 
	 * Accessed by AssemblySequenceEditor and TraceAssemblySequenceEditor.
	 */
	public final static Semaphore available = new Semaphore(1, true);

	/* Display display
	 * 
	 * Used as a way to get at the display thread.
	 */
	public static Display display = PlatformUI.getWorkbench().getDisplay();
	
	/* boolean done
	 * 
	 * Usage is unclear, referenced in some of the editors and comm code.
	 */
	public static boolean done = false;
	
	/* DisassemblerSocketComms disassemblerIF
	 * 
	 * Referenced by both dynamic and static editors.
	 */
	public static DisassemblerSocketComms disassemblerIF;

	private Instance instance;
	private final static Semaphore updaterSemaphore = new Semaphore(1, true);
	
	public void update(Observable obs, Object arg) {
		Instance ins = (Instance)arg;
		
		//if(done == false)
		this.instance = ins;
		load(ins);
	}
	
	public void load(Instance main_instance) {
		Instance ins;
		
		if(main_instance == null)
			if(this.instance != null)
				ins = this.instance;
			else
				return;
		else
			ins = main_instance;
		
		try {
			updaterSemaphore.acquire();
		} catch (InterruptedException e3) {
			e3.printStackTrace();
		}
		
		IProgressMonitor pm = null;
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("IDAPlugin");

		if(!project.exists())
			try {
				project.create(pm);
			} catch (CoreException e2) {
				e2.printStackTrace();
			}
		
		try {
			project.open(pm);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		
		disassembledFilename = ins.getName();
		
		File dir = new File(project.getLocation().toOSString());
		File axmlFile = new File(dir.getAbsolutePath() + File.separator + disassembledFilename + ".axml");
		System.out.println("axmlFile: " + axmlFile);
		
		XMLUtils.dumpInstanceToAXML(ins, axmlFile);
		
		try {
			project.refreshLocal(IResource.DEPTH_ONE, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		done = true;
		
		System.out.println("did fakeHello");
		
		updaterSemaphore.release();
	}
	
	public void earlyStartup() {
		System.out.println("Tracks earlyStartup");
		
		InstanceMap im = InstanceMap.getModel();
		im.addObserver(this);
	}
		
	public static void send(String ID, String message) {
		System.out.println("Tracks send: (" + ID + ") :: " + message);
	}
}
