package rcpapp.cartographer;

import java.util.Collection;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
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
	
	private class MapViewContentProvider implements IGraphContentProvider, IGraphEntityRelationshipContentProvider  {
		public Object getSource(Object rel) {
			if(!(rel instanceof CallSite))
				return null;
			
			CallSite cs = (CallSite)rel;
			Instance i = InstanceMap.getModel().getInstanceById(cs.getInstanceId());
			Function f = i.getFunctionByAddress(cs.callee());
			System.out.println("Source: " + f.getName());
			return f;
		}
		
		public Object getDestination(Object rel) {
			if(!(rel instanceof CallSite))
				return null;
			
			CallSite cs = (CallSite)rel;
			System.out.println("Getting destination...");
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
				System.out.println("MapViewContentProvider getElements: " + input.getClass());
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
			if(entity instanceof Function) {
				return (((Function)entity).isExternal() ? new Color(null, 0, 255, 255) : null);
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
