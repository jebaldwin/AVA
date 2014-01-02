package assemblyvisualiser.actions;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.eclipse.contribution.visualiser.core.ProviderManager;
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
import org.eclipse.ui.PartInitException;

import assemblyvisualiser.AssemblyVisualiserPlugin;
import assemblyvisualiser.convertxml.ConvertXMLtoMVIS;
import assemblyvisualiser.provider.AssemblyContentProvider;

/**
 * @author jbaldwin
 */
public class TransformAndVisualiseAction implements IObjectActionDelegate {

	private Shell shell;
	private IWorkbenchPart part;
	//private IMethod method;
	private IFile file;
	
	/**
	 * Constructor for Action1.
	 */
	public TransformAndVisualiseAction() {
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

		try {
			part.getSite().getPage().showView("org.eclipse.contribution.visualiser.views.Visualiser");
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		File inputFile = file.getLocation().toFile();
		AssemblyVisualiserPlugin.file = file;
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		File contentFile = null, markupFile = null;
		
		//create vis files, if not there
		try{
			contentFile = new File(inputFile.getParent() + Path.SEPARATOR + "content.vis" );
			contentFile.createNewFile();
			markupFile = new File(inputFile.getParent() + Path.SEPARATOR + "markup.vis");
			markupFile.createNewFile();
			
			IPath location= Path.fromOSString(contentFile.getAbsolutePath());
			IFile newIFile= workspace.getRoot().getFileForLocation(location); 
			newIFile.refreshLocal(IResource.DEPTH_ZERO, null);
			
			location= Path.fromOSString(markupFile.getAbsolutePath());
			newIFile= workspace.getRoot().getFileForLocation(location); 
			newIFile.refreshLocal(IResource.DEPTH_ZERO, null);
		} catch(IOException ioe){
			ioe.printStackTrace();
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//transform vis files
		ConvertXMLtoMVIS.convertContentVis(contentFile, inputFile);
		ConvertXMLtoMVIS.convertMarkupVis(markupFile, inputFile);
		
		//refresh visualiser
		((AssemblyContentProvider)ProviderManager.getContentProvider()).resetVisualiser(contentFile, markupFile);
		//((AssemblyMarkupProvider)ProviderManager.getMarkupProvider()).resetVisualiser(markupFile);
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		action.setEnabled(false);
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			this.file = null;
			for (Iterator<?> i = ss.iterator(); i.hasNext();) {
				Object o = i.next();

				if (o instanceof IAdaptable) {
					IFile file = (IFile)((IAdaptable)o).getAdapter(IFile.class);
					this.file = file;
					break;
				}
			}
			action.setEnabled(file != null);
		}
	}

}
