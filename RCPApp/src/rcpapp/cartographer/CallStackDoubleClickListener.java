package rcpapp.cartographer;

import java.util.Observable;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;

import cs.uvic.ca.ice.model.Function;

public class CallStackDoubleClickListener extends Observable implements IDoubleClickListener {
	public void doubleClick(DoubleClickEvent event) {
			System.out.println("call stack dbl click: " + event.getSelection());
			Function f = ((StackFrame)((StructuredSelection)event.getSelection()).getFirstElement()).getFunction();
			setChanged();
			notifyObservers(f);
	}
}
