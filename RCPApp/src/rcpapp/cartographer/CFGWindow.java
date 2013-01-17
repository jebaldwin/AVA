package rcpapp.cartographer;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IConnectionStyleProvider;
import org.eclipse.zest.core.viewers.IEntityStyleProvider;
import org.eclipse.zest.core.viewers.IGraphContentProvider;
import org.eclipse.zest.core.viewers.IGraphEntityRelationshipContentProvider;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.RadialLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;

import cs.uvic.ca.ice.model.CallSite;
import cs.uvic.ca.ice.model.Function;
import cs.uvic.ca.ice.model.Instance;
import cs.uvic.ca.ice.model.InstanceMap;
import cs.uvic.ca.ice.model.Instruction;

public class CFGWindow extends ApplicationWindow {
	private Function func;
	private GraphViewer viewer;
	
	public CFGWindow(Function f) {
		super(null);
		
		this.func = f;
		System.out.println("CFGWindow: " + f + "(function: " + f.getName() + ")"); 
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Control Flow Graph: " + this.func.getName());
	}
	
	protected Point getInitialSize() {
		return new Point(1024, 768);
	}
	
    protected Control createContents(Composite parent) {
		this.viewer = new GraphViewer(parent, SWT.NONE);
		
		this.viewer.setContentProvider(new CFGContentProvider());
		this.viewer.setLabelProvider(new CFGLabelProvider());

		TreeLayoutAlgorithm treeLayout = new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
		//treeLayout.setDirection(1);
		//this.viewer.setLayoutAlgorithm(new RadialLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
		this.viewer.setLayoutAlgorithm(treeLayout, true);
		
		this.viewer.setInput(this.func);
		
		return parent;
    }
    
	private class CFGContentProvider implements IGraphEntityRelationshipContentProvider  {
		public Object[] getRelationships(Object source, Object dest) {
			if(!(source instanceof Instruction) || !(dest instanceof Instruction))
				return null;
			
			Instruction si = (Instruction)source;
			Instruction di = (Instruction)dest;
			
			Object[] rel;
			Iterator<Long> ni = si.getNextAddresses().iterator();
			while(ni.hasNext()) {
				Long next = ni.next();
				if(next.longValue() == di.getAddress().longValue()) {
					System.out.println(si.getAddress() + " -> " + di.getAddress());
					rel = new Object[1];
					rel[0] = si;
					return rel;
				}
			}
			
			return null;
		}
		
		public Object[] getElements(Object input) {
			Function entry = null;
			if(!(input instanceof Function)) {
				return null;
			} else {
				entry = (Function) input;
			}
			
			System.out.println("getElements function");
			if(entry != null) {
				Collection<Instruction> instructions = entry.getInstructions();
				return instructions.toArray();
			}
			
			System.out.println("CFGWindow: entry is null");
			return null;
		}
		
		public void dispose() {
			
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			
		}
	}
	
	private class CFGLabelProvider extends LabelProvider implements IEntityStyleProvider, IConnectionStyleProvider {

		/* -------- LabelProvider methods -------- */
		
		public String getText(Object element) {
			if(!(element instanceof Instruction))
				return null;

			Instruction i = (Instruction)element;
			
			return i.getAddress().toString();
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
