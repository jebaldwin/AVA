package rcpapp.cartographer;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
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
import org.eclipse.zest.core.viewers.IEntityStyleProvider;
import org.eclipse.zest.core.viewers.IGraphContentProvider;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.algorithms.RadialLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutStyles;


import cs.uvic.ca.ice.model.CallSite;
import cs.uvic.ca.ice.model.Function;
import cs.uvic.ca.ice.model.IRefreshPart;
import cs.uvic.ca.ice.model.Instance;
import cs.uvic.ca.ice.model.InstanceMap;

public class MapView extends ViewPart implements IRefreshPart {
	public static final String ID = "rcpapp.cartographer.MapView";
	private GraphViewer viewer;
	private int layout = 1;

	public void createPartControl(Composite parent) {
		this.viewer = new GraphViewer(parent, SWT.NONE);
		
		this.viewer.setContentProvider(new MapViewContentProvider());
		this.viewer.setLabelProvider(new MapViewLabelProvider());
		this.viewer.setInput(InstanceMap.getModel().getFirstInstance());
		this.viewer.setLayoutAlgorithm(new RadialLayoutAlgorithm(LayoutStyles.NONE), true);
	}

	public void setFocus() {
	}
	
	public void refreshPart() {
		this.viewer.setInput(InstanceMap.getModel().getFirstInstance());
	}
	
	private class MapViewContentProvider implements IGraphContentProvider {
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
		
		public double getWeight(Object connection) {
			return 1.0;
		}
		
		public Object[] getElements(Object input) {
			if(!(input instanceof Instance))
				return null;
	
			Instance ins = (Instance)input;
			Collection<Function> functions = ins.getFunctions();
			Iterator<Function> func_iter = functions.iterator();
			Function f, entry;
			
			entry = null;
			while(func_iter.hasNext()) {
				f = func_iter.next();
				
				if(f.getEntryPoint()) {
					entry = f;
					break;
				}
			}
			
			if(entry != null) {
				System.out.println("returning entry calls");
				Collection<CallSite> calls = entry.getCalls();
				return calls.toArray();
			}
			
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
		
		public Color getNodeHighlightColor(Object entity) {
			return new Color(null, 255, 0, 0);
		}

		public Color getBorderColor(Object entity) {
			return null;
		}

		public Color getBorderHighlightColor(Object entity) {
			return null;
		}

		public int getBorderWidth(Object entity) {
			return 5;
		}

		public Color getBackgroundColour(Object entity) {
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
			return 0;
		}

		public Color getColor(Object rel) {
			return new Color(null, 0, 255, 255);
		}

		public Color getHighlightColor(Object rel) {
			return null;
		}

		public int getLineWidth(Object rel) {
			return 10;
		}
	}
}
