package org.eclipse.ui.examples.navigator.actions;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.jface.action.Action;
import org.eclipse.zest.custom.sequence.assembly.editors.NodeProxy;
import org.eclipse.zest.custom.sequence.widgets.Activation;
import org.eclipse.zest.custom.uml.viewers.UMLSequenceViewer;

/**
 * Focuses on the parent of the current root activation.
 * @author jbaldwin
 */

public class FocusUpAction extends Action {
	
	private UMLSequenceViewer viewer;
	private Object element;
	
	public FocusUpAction(UMLSequenceViewer viewer) {
		this.viewer = viewer;
	}
	
	@Override
	public void run() {
		//Object root = viewer.getChart().getRootActivation();
		
		if (element instanceof NodeProxy) {
			NodeProxy a = (NodeProxy) element;
			NodeProxy m = a.getCallingNode();
			
			if(m != null){
				viewer.setRootActivation(m);
			}
			return;
		}
		if(element instanceof Activation){
			//Activation act = (Activation) element;
			Activation act = (Activation)element;
			Activation call = act.getSourceCall().getSource();
			viewer.getChart().setRootActivation(call);
			FigureCanvas canvas = (FigureCanvas) viewer.getChart().getSequenceControl();
			canvas.scrollSmoothTo(0, 0);
		}
	}

	/**
	 * @param element
	 */
	public void setFocusElement(Object element) {
		this.element = element;
	}
}
