package rcpapp.cartographer;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

import cs.uvic.ca.ice.model.Function;
import cs.uvic.ca.ice.model.Instance;
import cs.uvic.ca.ice.model.InstanceMap;

public class InstanceView extends ViewPart implements Observer {
	public final static String ID = "rcpapp.cartographer.InstanceView";
	private final static InstanceDoubleClickListener doubleClickListener = new InstanceDoubleClickListener();
	private TreeViewer instanceTree;
	
	public InstanceView() {
		this.instanceTree = null;
	}

	public static InstanceDoubleClickListener getDoubleClickListener() {
		return doubleClickListener;
	}
	
	/* ------ Extends ViewPart ------ */
	
	public void createPartControl(Composite parent) {
		this.instanceTree = new TreeViewer(parent);
		 
		this.instanceTree.setLabelProvider(new InstanceLabelProvider());
		this.instanceTree.setContentProvider(new InstanceContentProvider());
		this.instanceTree.setInput(InstanceMap.getModel());
		this.instanceTree.addDoubleClickListener(this.doubleClickListener);
	}

	public void setFocus() {
		
	}


	/* ------ Instance Map Observer ------ */
	
	public void update(Observable arg0, Object arg1) {
		
	}
	
	
	/* ------ Instance Label Provider ------ */
	
	private class InstanceLabelProvider extends LabelProvider {
		public String getText(Object element) {
			if(element instanceof Instance) {
				return ((Instance)element).getName();
			} else if(element instanceof Function) {
				return ((Function)element).getName();
			} else {
				System.out.println("Cartographer::InstanceView - label provider receieved: " + element.getClass());
			}
			
			return null;
		}
	}
	
	
	/* ------ Instance Content Provider ------ */
	
	private class InstanceContentProvider implements ITreeContentProvider {
		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		// called in response to treeViewer.setInput()
		public Object[] getElements(Object inputElement) {
			if(inputElement instanceof InstanceMap) {
				return ((InstanceMap)inputElement).getArrayOfInstances();
			} else {
				System.out.println("Cartographer::InstanceView - getElements received: " + inputElement.getClass());
			}
			
			return null;
		}

		// called when an Instance is expanded in the UI
		public Object[] getChildren(Object parentElement) {
			if(parentElement instanceof Instance) {
				return ((Instance)parentElement).getFunctions().toArray();
			} else {
				System.out.println("Cartographer::InstanceView - getChildren received: " + parentElement.getClass());
			}
			
			return null;
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			if(element instanceof Instance) {
				 return (((Instance)element).getFunctions().size() == 0 ? false : true);
			}
			
			return false;
		}
	}
}
