package org.eclipse.ui.examples.navigator.actions;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.zest.custom.sequence.assembly.Activator;
import org.eclipse.zest.custom.sequence.assembly.Startup;
import org.eclipse.zest.custom.sequence.assembly.editors.AssemblySequenceEditor;
import org.eclipse.zest.custom.sequence.assembly.editors.ReversedAssemblySequenceEditor;
import org.eclipse.zest.custom.sequence.assembly.preferences.PreferenceConstants;
import org.w3c.dom.Element;

/**
 * An action to open the method sequence
 * 
 * @author jbaldwin
 * @since 3.2
 */
public class OpenReversedFunctionAction extends ActionDelegate {

	private IStructuredSelection selection = StructuredSelection.EMPTY;
	
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
					 path = path + "$" + funcName;
					 location = new Path(path);
					 
					 IWorkspace ws = ResourcesPlugin.getWorkspace();
					 IFile test = ws.getRoot().getFileForLocation(location);
	
					 if(test != null && !test.isLinked()){
						 newIFile = test;
						 
						 try {
							 ReversedAssemblySequenceEditor editor = (ReversedAssemblySequenceEditor) IDE.openEditor(
									 Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage(), 
										newIFile, 
										"org.eclipse.zest.custom.sequence.assembly.editors.ReversedAssemblySequenceEditor"
								);				 
							 Activator.getDefault().editors.add(editor);
						} catch (PartInitException e) {
								e.printStackTrace();
						}
					 } else{
						 path = path + "$" + passPath;
					
						 IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(path));
						 
						 IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
						 try {
							 ReversedAssemblySequenceEditor editor = (ReversedAssemblySequenceEditor) page.openEditor(new FileStoreEditorInput(fileStore), "org.eclipse.zest.custom.sequence.assembly.editors.ReversedAssemblySequenceEditor");
							 Activator.getDefault().editors.add(editor);
						} catch (PartInitException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					 }
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
