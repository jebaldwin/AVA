package org.eclipse.ui.examples.navigator.actions;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.ide.IDE;
import org.eclipse.zest.custom.sequence.assembly.Activator;
import org.eclipse.zest.custom.sequence.assembly.Startup;
import org.eclipse.zest.custom.sequence.assembly.editors.DynamicAssemblySequenceEditor;
import org.eclipse.zest.custom.sequence.assembly.preferences.PreferenceConstants;
import org.w3c.dom.Element;

/**
 * An action to open the method sequence
 * 
 * @author jbaldwin
 * @since 3.2
 */
public class OpenDynamicFunctionAction extends ActionDelegate {

	private IStructuredSelection selection = StructuredSelection.EMPTY;
	private DynamicAssemblySequenceEditor editor;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.actions.ActionDelegate#selectionChanged(org.eclipse.jface
	 * .action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection)
			selection = (IStructuredSelection) sel;
		else
			selection = StructuredSelection.EMPTY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.actions.ActionDelegate#run(org.eclipse.jface.action.IAction
	 * )
	 */
	public void run(IAction action) {
		


		Object firstElement = selection.getFirstElement();
		 
		if(firstElement instanceof Element) {
			 String funcName = ((Element) firstElement).getAttribute("name"); //$NON-NLS-1$
			 String funcAddr = ((Element) firstElement).getAttribute("address");
			 String path = ((Element) firstElement).getBaseURI();
			 IFile newIFile = null;
			 
			 if(selection.size() == 1) {
				 
				 TreeSelection treeSel = (TreeSelection) selection;
				 String stuff = treeSel.getFirstElement().toString();
				 if(stuff.indexOf("[module: ") < 0){				 
					 TreePath[] paths = treeSel.getPaths();
					 TreePath sel = paths[0];
					 String passPath = "";
					 for(int i = 0; i < sel.getSegmentCount(); i++){
					 	if(sel.getSegment(i).toString().startsWith("L/")){
					 		passPath = sel.getSegment(i).toString().replace("L/", "");
					 		break;
					 	}
					 }
					 IPath location = null;
					 
					 path = path.replace("file:/", "");
					 path = path.replace("%20", " ");
					 //path = path.replace("axml", "dxml");
					 //path = path + "$" + funcName;
					 
					 if(path.endsWith(".trace")){
						 MessageBox messageBox = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_WARNING);
					     messageBox.setText("Warning");
					     messageBox.setMessage("Cannot view dynamic diagram for a trace file, please open with the static viewer.");
					     messageBox.open();
					     return;
					 }
					 
					 //set to parent directory
					 File input = new File(path);
					 path = path.substring(0, path.lastIndexOf("/") + 1);
					 String saveLocation = path;
					 
					 //File dir = new File(path);
					 //String[] files = dir.list();
					 //path += files[0];
					 
					 //location = new Path(path);
					 
					 //this gives the wrong path on purpose, so only one dynamic editor can be opened?
					 IWorkspace ws = ResourcesPlugin.getWorkspace();
					 
					// location = new Path(ws.getRoot().getFullPath().toString() + "temp.dxml");
					// newIFile = ws.getRoot().getFileForLocation(location);
					 
					//IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					ws = ResourcesPlugin.getWorkspace();
					IResource firstProject = ws.getRoot().getProjects()[0];
					location = new Path(firstProject.getFullPath().toString() + File.separator + Startup.disassembledFilename + ".daxml");
					newIFile = ws.getRoot().getFile(location);
						
					// if(newIFile != null && !newIFile.isLinked()){
						
						 try {
							 boolean openNew = false;
							 
							 if(Activator.getDefault().dynamicEditor != null && Activator.getDefault().dynamicEditor.isDirty()){
								 MessageBox messageBox = new MessageBox(Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
						        
						         messageBox.setText("Save Resource");
						         messageBox.setMessage("Diagram has been modified. Save changes?");
						         int buttonID = messageBox.open();
						         switch(buttonID) {
						         case SWT.YES:
						            // saves changes ...
						        	Activator.getDefault().dynamicEditor.doSave(new NullProgressMonitor());
						        	openNew = true;
						        	break;
						         case SWT.NO:
						            // exits here ...
						        	openNew = true;
						            break;
						         case SWT.CANCEL:
						            // does nothing ...
						        	openNew = false;
						        	break;
						        }
						        
							 } else {
								 openNew = true;
							 }
							 
							 if(openNew){
								 editor = (DynamicAssemblySequenceEditor) IDE.openEditor(
										 	Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage(), 
											newIFile, 
											"org.eclipse.zest.custom.sequence.assembly.editors.DynamicAssemblySequenceEditor"
									);	
								
								 editor.saveLocation = saveLocation;
							     editor.setPartName(funcName);
								 editor.localFile = input.getName().replace("axml", "exe");
								 Activator.getDefault().dynamicEditor = editor;
								 
								 //send message to IDAPro to set a breakpoint at selected function
								 Long position = Long.parseLong(funcAddr, 16);
								 Startup.send(editor.localFile, "setBreakpoint " + position);// + " " + editor.localFile);
								 
								 /*if(Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_DEBUG).equals(PreferenceConstants.P_DEBUG_ALL)){
									 Startup.send(null, "enableTracing");
							     } else {
							    		Startup.send(null, "disableTracing");
							     }*/
							 }
						} catch (PartInitException e) {
								e.printStackTrace();
						}
					/* } else{
						 //path = path + "$" + passPath;
						 IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(path));
						 
						 IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
						 try {
							 editor = (DynamicAssemblySequenceEditor) page.openEditor(
									 	new FileStoreEditorInput(fileStore), 
									 	"org.eclipse.zest.custom.sequence.assembly.editors.DynamicAssemblySequenceEditor");
							 editor.setPartName(funcName);
							 editor.localFile = input.getName().replace("axml", "exe");
							 Activator.getDefault().dynamicEditor = editor;
							 
							 //send message to IDAPro to set a breakpoint at selected function
							 Long position = Long.parseLong(funcAddr, 16);
							 Startup.disassemblerIF.send("setBreakpoint " + position);
						 } catch (PartInitException e) {
							e.printStackTrace();
						}
					 }*/
				 } else {
					 MessageBox messageBox = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_WARNING);
				     messageBox.setText("Warning");
				     messageBox.setMessage("Cannot view call graph for module, please select a child function.");
				     messageBox.open();
				 }
			 }
		} 
	}
	
	
}
