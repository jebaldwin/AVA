package org.eclipse.ui.examples.navigator.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.zest.custom.sequence.widgets.Activation;
import org.eclipse.zest.custom.sequence.widgets.Call;
import org.eclipse.zest.custom.uml.viewers.UMLSequenceViewer;

/**
 * Collapses all activations under the focused root.
 * @author jbaldwin
 */

public class CollapseAllAction extends Action {
	
	private UMLSequenceViewer viewer;
	private Object element;
	
	public CollapseAllAction(UMLSequenceViewer viewer) {
		this.viewer = viewer;
	}
	
	@Override
	public void run() {
		if(element instanceof Call){
			element = (Activation)element;
		}
		if(element instanceof Activation){
			
		}
		viewer.collapseActivationsUnder(element);
	}
	
	/**
	 * @param element
	 */
	public void setFocusElement(Object element) {
		this.element = element;
	}

}
