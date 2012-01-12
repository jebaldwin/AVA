package org.eclipse.zest.custom.sequence.assembly.popup.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.zest.custom.sequence.assembly.model.XMLUtils;

/**
 * @author jbaldwin
 */
public class TransformAndGenerateAction implements IObjectActionDelegate {

	private Shell shell;
	private IWorkbenchPart part;
	//private IMethod method;
	private ArrayList<IFile> files = new ArrayList<IFile>();
	
	/**
	 * Constructor for Action1.
	 */
	public TransformAndGenerateAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.part = targetPart;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		//transform XML file first
		for(int i = 0; i < files.size(); i++){
			IFile file = files.get(i);
			File newFile = null;
			IContainer folder = file.getParent();

			if(!file.isLinked() && !folder.isLinked()){
				File inputFile = file.getLocation().toFile();
				newFile = XMLUtils.parseTextToXML(inputFile, null, null);
			} else {
				if(file.isLinked() && !folder.isLinked()){
					String path = file.getLocation().toOSString();//.getLocationURI().toString();
					//path = path.replace("file:/", "");
					//path = path.replace("%20", " ");
					File inputFile = new File(path);
					newFile = XMLUtils.parseTextToXML(inputFile, file, null);
				} else {
					String path = file.getLocation().toOSString();//file.getLocationURI().toString();
					//path = path.replace("file:/", "");
					//path = path.replace("%20", " ");
					File inputFile = new File(path);
					path = folder.getLocation().toOSString();//folder.getLocationURI().toString();
					//path = path.replace("file:/", "");
					//path = path.replace("%20", " ");
					String filename = path + File.separator + file.getName();
					filename = filename.substring(0, filename.indexOf(".ose"));
		        	filename = filename.replace(".exe", "") + ".axml";
					File ofile = new File(filename);
					newFile = XMLUtils.parseTextToXML(inputFile, file, ofile);	
				}
			}
		
			IWorkspace workspace= ResourcesPlugin.getWorkspace();
			IPath location= Path.fromOSString(newFile.getAbsolutePath());
			IFile newIFile= workspace.getRoot().getFileForLocation(location); 

			if(newIFile != null){
				try {
					//workspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
					newIFile.refreshLocal(IResource.DEPTH_ZERO, null);
				} catch (CoreException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} else {
				try {
					folder.getParent().refreshLocal(IResource.DEPTH_INFINITE, null);
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			/*try {
				IDE.openEditor(
						part.getSite().getPage(), 
						newIFile, 
						"org.eclipse.zest.custom.sequence.assembly.editors.AssemblySequenceEditor"
				);
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		action.setEnabled(false);
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			files.clear();
			for (Iterator<?> i = ss.iterator(); i.hasNext();) {
				Object o = i.next();

				if (o instanceof IAdaptable) {
					IFile file = (IFile)((IAdaptable)o).getAdapter(IFile.class);
					files.add(file);
				}
			}
			action.setEnabled(files.size() > 0);
		}
	}

}
