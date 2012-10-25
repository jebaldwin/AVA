package rcpapp.rails;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

import cs.uvic.ca.ice.bridge.CommCenter;
import cs.uvic.ca.ice.bridge.Message;
import cs.uvic.ca.ice.model.Instance;
import cs.uvic.ca.ice.model.InstanceMap;

public class DataSourceView extends ViewPart implements Observer {
	public static final String ID = "rcpapp.rails.DataSource";
	ListViewer instanceList;
	
	public DataSourceView() {
		super();
		
		this.instanceList = null;
		InstanceMap.getModel().addObserver(this);
	}
	
	public void createPartControl(Composite parent) {
		 this.instanceList = new ListViewer(parent);
		 
		 this.instanceList.setLabelProvider(new LabelProvider() {
			 public String getText(Object element) {
				 Instance i = (Instance) element;
				 return i.getName();
			 }
		 });
		 
		 getSite().setSelectionProvider(this.instanceList);
		 hookDoubleClickCommand();
	}
	
	private void hookDoubleClickCommand() {
		this.instanceList.addDoubleClickListener(new InstanceDoubleClickListener());
	}
	
	public void setFocus() {
		
	}
	
	public void update(Observable obs, Object arg) {
		Display mainDisplay = Display.getDefault();
		Instance iceInstance = (Instance)arg;
			
		mainDisplay.asyncExec(new InstanceAdd(this.instanceList, iceInstance));
	}
	
	private class InstanceDoubleClickListener implements IDoubleClickListener {
		public void doubleClick(DoubleClickEvent event) {
			IHandlerService handlerService;
			
			handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
			try {
				handlerService.executeCommand("rcpapp.rails.openEditor", null);
			} catch (ExecutionException | NotDefinedException
					| NotEnabledException | NotHandledException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private class InstanceAdd implements Runnable {
		ListViewer list = null;
		Instance ins = null;
		
		public InstanceAdd(ListViewer list, Instance iceInstance) {
			this.list = list;
			this.ins = iceInstance;
		}
		
		public void run() {
			Object oins = this.list.getElementAt(0);
			if(oins != null)
				this.list.remove(oins);
				
			this.list.add(this.ins);
		}
	}
}
