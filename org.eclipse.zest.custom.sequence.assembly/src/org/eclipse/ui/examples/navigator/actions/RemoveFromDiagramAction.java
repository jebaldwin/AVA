package org.eclipse.ui.examples.navigator.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.zest.custom.sequence.widgets.Activation;
import org.eclipse.zest.custom.sequence.widgets.Call;
import org.eclipse.zest.custom.sequence.widgets.Message;
import org.eclipse.zest.custom.sequence.widgets.UMLItem;
import org.eclipse.zest.custom.uml.viewers.UMLSequenceViewer;
import org.eclipse.draw2d.FigureCanvas;

/**
 * Focuses on an element in a sequence viewer.
 * @author jbaldwin
 */

public class RemoveFromDiagramAction extends Action {
	
	private UMLSequenceViewer viewer;
	private Object element;
	
	/**
	 * Creates this action to work with the given viewer.
	 * @param viewer
	 */
	public RemoveFromDiagramAction(UMLSequenceViewer viewer) {
		this.viewer = viewer;
	}
	
	public RemoveFromDiagramAction(UMLSequenceViewer viewer, Activation act) {
		this.viewer = viewer;
	}
	
	@Override
	public void run() {
		if(element != null && element instanceof Activation){
			Activation act = (Activation) element;
			Call source = act.getSourceCall();
			source.getSource().removeMessage(source);
			
			viewer.getChart().setRootActivation((Activation) element);
			FigureCanvas canvas = (FigureCanvas) viewer.getChart().getSequenceControl();
			canvas.scrollSmoothTo(0, 0);
			
			UMLItem[] items = viewer.getChart().getItems();
			for (int i = 0; i < items.length; i++) {
				UMLItem item = items[i];
				
				//reached the current position in the graph
				if(item.equals(element)){
					break;
				} else {
					item.setData("todelete", "true");
				}
			}
		} 
	}

	/**
	 * @param element
	 */
	public void setFocusElement(Object element) {
		this.element = element;
	}
}
