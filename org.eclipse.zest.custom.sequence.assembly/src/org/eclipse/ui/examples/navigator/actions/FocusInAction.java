package org.eclipse.ui.examples.navigator.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.zest.custom.sequence.widgets.Activation;
import org.eclipse.zest.custom.uml.viewers.UMLSequenceViewer;
import org.eclipse.draw2d.FigureCanvas;

/**
 * Focuses on an element in a sequence viewer.
 * @author jbaldwin
 */

public class FocusInAction extends Action {
	
	private UMLSequenceViewer viewer;
	private Object element;
	
	/**
	 * Creates this action to work with the given viewer.
	 * @param viewer
	 */
	public FocusInAction(UMLSequenceViewer viewer) {
		this.viewer = viewer;
	}
	
	public FocusInAction(UMLSequenceViewer viewer, Activation act) {
		this.viewer = viewer;
	}
	
	@Override
	public void run() {
		if(element != null && element instanceof Activation){
			viewer.getChart().setRootActivation((Activation) element);
			FigureCanvas canvas = (FigureCanvas) viewer.getChart().getSequenceControl();
			canvas.scrollSmoothTo(0, 0);
			return;
		} 
		if (element != null) {
			viewer.setRootActivation(element);
		}
		return;
	}

	/**
	 * @param element
	 */
	public void setFocusElement(Object element) {
		this.element = element;
	}
}
