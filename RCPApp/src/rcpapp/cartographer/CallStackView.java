package rcpapp.cartographer;

import java.util.Observable;
import java.util.Observer;
import java.util.Stack;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import cs.uvic.ca.ice.model.Function;
import cs.uvic.ca.ice.model.Instance;

public class CallStackView extends ViewPart implements Observer {
	public final static String ID = "rcpapp.cartographer.CallStackView";
	private ListViewer viewer;
	private Stack callStack;
	
	public CallStackView() {
		MapView.getDoubleClickListener().addObserver(this);
		this.callStack = new Stack();
	}

	public void createPartControl(Composite parent) {
		this.viewer = new ListViewer(parent);
		
		this.viewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				if(element instanceof Function) {
					return ((Function)element).getName();
				} else {
					System.out.println("Cartographer::CallStackView - label provider receieved: " + element.getClass());
				}
				
				return null;
			}	
		});
		this.viewer.setContentProvider(new CallStackContentProvider());
	}

	public void setFocus() {
	}

	public void update(Observable o, Object arg) {
		System.out.println("Call stack push: " + ((StructuredSelection)arg).getFirstElement());
		this.callStack.push((Function) ((StructuredSelection)arg).getFirstElement());
		this.viewer.setInput(this.callStack);
	}
	
	
	/* ------ Call Stack Content Provider ------ */
	
	private class CallStackContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			return ((Stack)inputElement).toArray();
		}
		
		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
}
