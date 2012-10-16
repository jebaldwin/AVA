package rcpapp.rails;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

import cs.uvic.ca.ice.bridge.CommCenter;
import cs.uvic.ca.ice.bridge.Message;

public class DataSourceView extends ViewPart implements Observer {
	ListViewer instanceList;
	
	public DataSourceView() {
		super();
		
		this.instanceList = null;
		CommCenter.getCommCenter().addObserver(this);
	}
	
	public void createPartControl(Composite parent) {
		 this.instanceList = new ListViewer(parent);
	}
	
	public void setFocus() {
		
	}
	
	public void update(Observable obs, Object arg) {
		ConcurrentLinkedQueue<Message> msgQ = (ConcurrentLinkedQueue<Message>)arg;
		Display mainDisplay = Display.getDefault();
		
		Message m = msgQ.peek();
		
		mainDisplay.asyncExec(new InstanceAdd(this.instanceList, m));
	}
	
	private class InstanceAdd implements Runnable {
		ListViewer list = null;
		Message msg = null;
		
		public InstanceAdd(ListViewer list, Message m) {
			this.list = list;
			this.msg = m;
		}
		
		public void run() {
			this.list.add(this.msg.origin());
		}
	}
}
