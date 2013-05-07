package org.eclipse.zest.custom.sequence.assembly.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import java.io.*;

import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;
import org.eclipse.zest.custom.sequence.assembly.Activator;
import org.eclipse.zest.custom.sequence.assembly.Startup;
import org.eclipse.zest.custom.sequence.assembly.editors.DynamicAssemblySequenceEditor;
import org.eclipse.zest.custom.sequence.assembly.preferences.PreferenceConstants;

/**
 * This is a sample new wizard. Its role is to create a new file 
 * resource in the provided container. If the container resource
 * (a folder or a project) is selected in the workspace 
 * when the wizard is opened, it will accept it as the target
 * container. The wizard creates one file with the extension
 * "axml". If a sample multi-page editor (also available
 * as a template) is registered for the same extension, it will
 * be able to open it.
 */

public class DebugNewWizard extends Wizard implements INewWizard {
	private DebugNewWizardPage page;
	private ISelection selection;

	/**
	 * Constructor for AssemblyNewWizard.
	 */
	public DebugNewWizard() {
		super();
		setNeedsProgressMonitor(true);
	}
	
	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page = new DebugNewWizardPage(selection);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() {
		final String containerName = page.getContainerName();
		final String fileName = page.getFileName();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(containerName, fileName, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
			System.out.println("here");
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 */

	private void doFinish(
		String containerName,
		final String fileName,
		IProgressMonitor monitor)
		throws CoreException {
		// create a sample file
		monitor.beginTask("Creating " + fileName, 2);
		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		final IContainer container = (IContainer) resource;
		if (fileName.length() == 0) {
			Calendar cal = Calendar.getInstance();
			String DATE_FORMAT_NOW = "yyyyMMdd-HHmmss";
		    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		    String timestamp =  sdf.format(cal.getTime());
		    
			//fileName = timestamp;
		}
		/*final IFile file = container.getFile(new Path(fileName + ""));
		System.out.println(file.getFullPath());
		try {
			InputStream stream = openContentStream();
			if (file.exists()) {
				file.setContents(stream, true, true, monitor);
			} else {
				file.create(stream, true, monitor);
			}
			stream.close();
		} catch (IOException e) {
		}
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");*/
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				/*IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IWorkspace ws = ResourcesPlugin.getWorkspace();
				Path location = new Path(container.getFullPath().toString() + File.separator + "temp.daxml");
				IFile newIFile = ws.getRoot().getFile(location);*/
				
				//IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IWorkspace ws = ResourcesPlugin.getWorkspace();
				IResource firstProject = ws.getRoot().getProjects()[0];
				Path location = new Path(firstProject.getFullPath().toString() + File.separator + "temp.daxml");
				IFile newIFile = ws.getRoot().getFile(location);
				
				/*File file = new File(location.toString());
				try {
					file.createNewFile();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}*/
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
						//IDE.openEditor(page, file, true);
						DynamicAssemblySequenceEditor editor = (DynamicAssemblySequenceEditor) IDE.openEditor(
							 	Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage(), 
							 	newIFile, 
								"org.eclipse.zest.custom.sequence.assembly.editors.DynamicAssemblySequenceEditor"
						);		
						if(fileName.length() > 0){
							editor.setPartName(fileName, null);
						} else {
							editor.setPartName("Debugging Diagram", null);
						}				
						editor.saveLocation = root.getLocation().toPortableString() + resource.getFullPath().toPortableString();
						//editor.localFile = Startup.disassembledFilename;//newIFile.getName().replace("axml", "exe");
						Activator.getDefault().dynamicEditor = editor;
						
						if(Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_DEBUG).equals(PreferenceConstants.P_DEBUG_ALL)){
							Startup.send(null, "enableTracing");
					    } else {
					    	Startup.send(null, "disableTracing");
					    }
						
						if(Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_LOG).equals(PreferenceConstants.P_LOG_INNER)){
							Startup.send(null, "enableInner");
					    } else {
					    	Startup.send(null, "disableInner");
					    }
					 }
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		});
		monitor.worked(1);
	}
	
	/**
	 * We will initialize file contents with a sample text.
	 */

	private InputStream openContentStream() {
		String contents =
			"<dynamicTrace filename=\"\">\n<functionEntryPoint address=\"\" index=\"0\" module=\"\" name=\"\"><function address=\"\" index=\"1\" name=\"User\" stereotype=\"actor\"/>\n</functionEntryPoint>\n</dynamicTrace>";
		return new ByteArrayInputStream(contents.getBytes());
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status =
			new Status(IStatus.ERROR, "org.eclipse.zest.custom.sequence.assembly", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
	
	public boolean canFinish() {
        return true;
    }
}