package rcpapp.cartographer;

import java.util.Observable;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeSelection;

import cs.uvic.ca.ice.model.Function;

public class GraphDoubleClickListener extends Observable implements IDoubleClickListener {
	public void doubleClick(DoubleClickEvent event) {
		setChanged();
		notifyObservers(event.getSelection());
	}
}
