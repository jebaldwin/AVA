package org.eclipse.zest.custom.sequence.assembly.editors;

import java.util.LinkedList;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.zest.custom.sequence.widgets.Activation;
import org.eclipse.zest.custom.sequence.widgets.UMLItem;
import org.eclipse.zest.custom.uml.viewers.UMLSequenceViewer;

/**
 * Gives a path to the root from a JavaActivation
 * @author jbaldwin
 */

public class DynamicSequenceBreadCrumbContentProvider implements
		IStructuredContentProvider {

	//private AssemblySequenceContentProvider acsp;
	private UMLSequenceViewer viewer;
	
	public DynamicSequenceBreadCrumbContentProvider(UMLSequenceViewer viewer){
		//this.acsp = acsp;
		this.viewer = viewer;
	}
	
	public Object[] getElements(Object inputElement) {
		//LinkedList<NodeProxy> elements = new LinkedList<NodeProxy>();
		LinkedList<Activation> elements = new LinkedList<Activation>();
		Activation a = (Activation) inputElement;
		
		if(inputElement == null){
			//elements.addFirst(viewer.getChart().getRootActivation().getLifeline().getText());
			elements.addFirst(viewer.getChart().getRootActivation());
			return elements.toArray();
		}
		
		if (inputElement instanceof Activation) {
			
			if(a.getSourceCall() == null){
				elements.add(a);
				return elements.toArray();
			}
			Activation m = a.getSourceCall().getSource();
			
			if(a == null){
				return new Object[0];
			}
			
			while (a != null) {
				elements.addFirst(a);
				
				if (m != null) {
					if(m.getLifeline().getText().equals("User") || m.getSourceCall() == null){
						elements.addFirst(m);
						a = null;
					} else {
						Activation temp = m;
						a = m;
						m = temp.getSourceCall().getSource();					
					}
				} else {
					a = null;
				}
			}
			return elements.toArray();
		}
		return new Object[0];
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		
	}

	
}
