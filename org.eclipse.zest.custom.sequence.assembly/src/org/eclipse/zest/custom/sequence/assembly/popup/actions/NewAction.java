package org.eclipse.zest.custom.sequence.assembly.popup.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

/**
 * @author jbaldwin
 */
public class NewAction implements IObjectActionDelegate {

	private Shell shell;
	private IWorkbenchPart part;
	//private IMethod method;
	private IFile file;
	
	/**
	 * Constructor for Action1.
	 */
	public NewAction() {
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
			IEditorPart ep = IDE.openEditor(
					part.getSite().getPage(), 
					file, 
					"org.eclipse.zest.custom.sequence.assembly.editors.AssemblySequenceEditor"
			);
			
			System.out.println("class " + ep.getClass());
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
