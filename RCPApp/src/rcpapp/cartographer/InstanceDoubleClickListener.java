package rcpapp.cartographer;

import java.util.Observable;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeSelection;

import cs.uvic.ca.ice.model.Function;

public class InstanceDoubleClickListener extends Observable implements IDoubleClickListener {
	public void doubleClick(DoubleClickEvent event) {
		Function f =  (Function) ((TreeSelection)event.getSelection()).getPaths()[0].getLastSegment();
		setChanged();
		notifyObservers(f);
	}
}
