package rcpapp.cartographer;

import java.util.Collection;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IConnectionStyleProvider;
import org.eclipse.zest.core.viewers.IEntityConnectionStyleProvider;
import org.eclipse.zest.core.viewers.IEntityStyleProvider;
import org.eclipse.zest.core.viewers.IGraphContentProvider;
import org.eclipse.zest.core.viewers.IGraphEntityRelationshipContentProvider;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutStyles;

import cs.uvic.ca.ice.model.CallSite;
import cs.uvic.ca.ice.model.Function;
import cs.uvic.ca.ice.model.IRefreshPart;
import cs.uvic.ca.ice.model.Instance;
import cs.uvic.ca.ice.model.InstanceMap;

public class MapView extends ViewPart implements IRefreshPart, Observer {
	public static final String ID = "rcpapp.cartographer.MapView";
	private final static GraphDoubleClickListener doubleClickListener = new GraphDoubleClickListener();
	private GraphViewer viewer;
	private int layout = 1;

	public MapView() {
		InstanceDoubleClickListener dbl = InstanceView.getDoubleClickListener();
		dbl.addObserver(this);
		
		CallStackDoubleClickListener csdbl = CallStackView.getDoubleClickListener();
		csdbl.addObserver(this);
	}
	
	public void createPartControl(Composite parent) {
		this.viewer = new GraphViewer(parent, SWT.NONE);
		
		this.viewer.setContentProvider(new MapViewContentProvider());
		this.viewer.setLabelProvider(new MapViewLabelProvider());

		TreeLayoutAlgorithm treeLayout = new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
		//treeLayout.setDirection(1);
		this.viewer.setLayoutAlgorithm(treeLayout, true);

		this.viewer.addDoubleClickListener(doubleClickListener);
		this.viewer.addDoubleClickListener(new InternalDoubleClickListener());
		
		final CommentAction sc = new CommentAction("Set Comment") {};
		final RenameAction rn = new RenameAction("Rename") {};
		final MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		
		mgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if(!selection.isEmpty()) {
					mgr.add(sc);
					mgr.add(rn);
				}
			}
		});
		
		this.viewer.getGraphControl().setMenu(mgr.createContextMenu(this.viewer.getGraphControl()));
	}

	public static GraphDoubleClickListener getDoubleClickListener() {
		return doubleClickListener;
	}
	
	public void setFocus() {
	}
	
	public void refreshPart() {
	}
	
	public void update(Observable o, Object arg) {
		if(arg instanceof Function) {
			this.viewer.setInput((Function) arg);
		} else {
			System.out.println("MapView update: " + arg.getClass());
		}
	}
	
	private class InternalDoubleClickListener implements IDoubleClickListener {
		public void doubleClick(DoubleClickEvent event) {
			Viewer viewer = event.getViewer();
			Function func = (Function) ((StructuredSelection)event.getSelection()).getFirstElement();
			
			viewer.setInput(func);
		}
	}
	
	private class CommentAction extends Action {
		public CommentAction(String string) {
			super(string);
		}

		public void run() {
			System.out.println("CommentAction run");
			
			String oldComment = "";
			IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			if(!selection.isEmpty()) {
				Function f = (Function) selection.getFirstElement();
				oldComment = f.getComment();
			}
			
			CommentDialog cd = new CommentDialog(null, "Set Comment", "Comment:", oldComment, null);
			cd.open();
		}
	}
	
	private class CommentDialog extends InputDialog {

		public CommentDialog(Shell parentShell, String dialogTitle,
				String dialogMessage, String initialValue,
				IInputValidator validator) {
			super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
		}
		
		public void okPressed() {
			System.out.println("Comment: " + this.getValue());
			this.close();
		}
	}
	
	private class RenameAction extends Action {
		public RenameAction(String string) {
			super(string);
		}

		public void run() {
			System.out.println("RenameAction run");
			
			String oldName = "";
			IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			if(!selection.isEmpty()) {
				Function f = (Function) selection.getFirstElement();
				oldName = f.getName();
			}
			
			RenameDialog rd = new RenameDialog(null, "Rename Function", "Name:", oldName, null);
			rd.open();
		}
	}
	
	private class RenameDialog extends InputDialog {

		public RenameDialog(Shell parentShell, String dialogTitle,
				String dialogMessage, String initialValue,
				IInputValidator validator) {
			super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
		}
		
		public void okPressed() {
			System.out.println("Rename: " + this.getValue());
			this.close();
		}
	}
	
	private class MapViewContentProvider implements IGraphContentProvider, IGraphEntityRelationshipContentProvider  {
		public Object getSource(Object rel) {
			if(!(rel instanceof CallSite))
				return null;
			
			CallSite cs = (CallSite)rel;
			Instance i = InstanceMap.getModel().getInstanceById(cs.getInstanceId());
			Function f = i.getFunctionByAddress(cs.callee());
			return f;
		}
		
		public Object getDestination(Object rel) {
			if(!(rel instanceof CallSite))
				return null;
			
			CallSite cs = (CallSite)rel;
			return cs.target();
		}
		
		public Object[] getRelationships(Object source, Object dest) {
			return null;
		}
		
		public double getWeight(Object connection) {
			return 1.0;
		}
		
		public Object[] getElements(Object input) {
			Function entry = null;
			if(!(input instanceof Function)) {
				return null;
			} else {
				entry = (Function) input;
			}
			
			if(entry != null) {
				Collection<CallSite> calls = entry.getCalls();
				return calls.toArray();
			}
			
			System.out.println("Cartographer: entry is null");
			return null;
		}
		
		public void dispose() {
			
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			
		}
	}
	
	private class MapViewLabelProvider extends LabelProvider implements IEntityStyleProvider, IConnectionStyleProvider {

		/* -------- LabelProvider methods -------- */
		
		public String getText(Object element) {
			if(!(element instanceof Function))
				return null;

			Function f = (Function)element;
			
			return f.getName();
		}
		
		public Image getImage(Object element) {
			return null;
		}

		
		/* -------- IEntityStyleProvider methods -------- */
		
		/* Color during selection */
		public Color getNodeHighlightColor(Object entity) {
			return null;
		}

		public Color getBorderColor(Object entity) {
			return null;
		}

		public Color getBorderHighlightColor(Object entity) {
			return null;
		}

		public int getBorderWidth(Object entity) {
			return 1;
		}

		/* Unselected color */
		public Color getBackgroundColour(Object entity) {
			if(entity instanceof Function && ((Function)entity).isExternal() == false) {
				Function f = (Function) entity;
				int s = f.getSize().intValue();
				
				if(s < 0) {
					s = 0;
				} else if(s > 255) {
					s = 255;
				}
				
				int r = s;
				int g = 255 - s;
				int b = 0;
				
				System.out.println("--> [" + s + "] (" + r + ", " + g + ", " + b + ")");
				
				Color c = new Color(null, r, g, b);
				
				return c;
			}
			return null;
		}

		public Color getForegroundColour(Object entity) {
			return null;
		}

		public IFigure getTooltip(Object entity) {
			return null;
		}

		public boolean fisheyeNode(Object entity) {
			return false;
		}

		/* -------- IConnectionStyleProvider methods -------- */
		
		public int getConnectionStyle(Object rel) {
			return ZestStyles.CONNECTIONS_DIRECTED;
		}

		public Color getColor(Object rel) {
			return new Color(null, 0, 255, 255);
		}

		public Color getHighlightColor(Object rel) {
			return null;
		}

		public int getLineWidth(Object rel) {
			return 0;
		}
	}
}
