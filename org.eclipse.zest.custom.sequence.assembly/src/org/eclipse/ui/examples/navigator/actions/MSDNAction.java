package org.eclipse.ui.examples.navigator.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.MSDN.MSDNView;
import org.eclipse.zest.custom.sequence.assembly.editors.NodeProxy;
import org.eclipse.zest.custom.sequence.widgets.Activation;
import org.eclipse.zest.custom.sequence.widgets.Call;
import org.eclipse.zest.custom.uml.viewers.UMLSequenceViewer;

/**
 * Collapses all activations under the focused root.
 * @author jbaldwin
 */

public class MSDNAction extends Action {
	
	private UMLSequenceViewer viewer;
	private Object element;
	
	public MSDNAction(UMLSequenceViewer viewer) {
		this.viewer = viewer;
	}
	
	@Override
	public void run() {
		String name = "";
		
		if(element instanceof Call){
			element = (Call)element;
			name = ((Call) element).getText();
		}
		if(element instanceof Activation){
			element = (Activation)element;
			name = ((Call) element).getText();
		}
		if(element instanceof NodeProxy) {
			name = ((NodeProxy)element).targetName;
		}
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("avadocumentationframe.views.MSDNView");
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		MSDNView.changeURLTerms(name + " site:msdn.microsoft.com");
	}
	
	/**
	 * @param element
	 */
	public void setFocusElement(Object element) {
		this.element = element;
	}

}
