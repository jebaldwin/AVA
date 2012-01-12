package org.eclipse.zest.custom.sequence.assembly.editors;

import java.util.LinkedList;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.zest.custom.sequence.widgets.Activation;
import org.eclipse.zest.custom.sequence.widgets.Call;

/**
 * Gives a path to the root from a JavaActivation
 * @author jbaldwin
 */

public class AssemblySequenceBreadCrumbContentProvider implements
		IStructuredContentProvider {

	private AssemblySequenceContentProvider acsp;
	public Activation currAct = null;
	
	public AssemblySequenceBreadCrumbContentProvider(AssemblySequenceContentProvider acsp){
		this.acsp = acsp;
	}
	
	public Object[] getElements(Object inputElement) {
		
		LinkedList<Object> elements = new LinkedList<Object>();
		NodeProxy a = (NodeProxy) inputElement;
				
		if(inputElement == null){
			elements.addFirst(acsp.rootNode);
			return elements.toArray();
		}
		
		if(currAct != null){
			Activation act = currAct;
			if(act.getSourceCall() == null){
				elements.add(act);
				return elements.toArray();
			}
			Activation m = currAct.getSourceCall().getSource();
			
			if(act == null){
				return new Object[0];
			}
			
			while (act != null) {
				elements.addFirst(act);
				
				if (m != null) {
					if(m.getLifeline().getText().equals("User")){
						elements.addFirst(m);
						act = null;
					} else {
						Activation temp = m;
						act = m;
						Call call = temp.getSourceCall();
						if(call == null){
							m = null;
						} else {
							m = temp.getSourceCall().getSource();	
						}
					}
				} else {
					act = null;
				}
			}
			return elements.toArray();
		} else {
			if (inputElement instanceof NodeProxy) {
				
				NodeProxy m = a.getCallingNode();
				
				if(a == null){
					return new Object[0];
				}
				
				while (a != null) {
					elements.addFirst(a);
					
					if (m != null) {
						if(m.equals(acsp.rootNode)){
							elements.addFirst(m);
							a = null;
						} else {
							NodeProxy temp = m;
							a = m;
							m = temp.getCallingNode();					
						}
					} else {
						a = null;
					}
				}
				return elements.toArray();
			}
		}
		return new Object[0];
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	
}
