package rcpapp.cartographer;

import java.util.Observable;
import java.util.Observer;
import java.util.Stack;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.part.ViewPart;

import cs.uvic.ca.ice.model.Function;
import cs.uvic.ca.ice.model.Instance;

public class CallStackView extends ViewPart implements Observer {
	public final static String ID = "rcpapp.cartographer.CallStackView";
	private TableViewer viewer;
	private Stack callStack;
	private Integer next_frame;
	
	public CallStackView() {
		InstanceDoubleClickListener dbl = InstanceView.getDoubleClickListener();
		dbl.addObserver(this);
		
		MapView.getDoubleClickListener().addObserver(this);
		this.callStack = new Stack();
		this.next_frame = 1;
	}

	public void createPartControl(Composite parent) {
		this.viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		
		final Table table = this.viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		/* Column 1 */
		TableViewerColumn col_frame = new TableViewerColumn(this.viewer, SWT.NONE);
		col_frame.getColumn().setWidth(50);
		col_frame.getColumn().setText("Frame");
		col_frame.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				return ((StackFrame)element).getFrameNumber().toString();
			}
		});
		
		/* Column 2 */
		TableViewerColumn col_module = new TableViewerColumn(this.viewer, SWT.NONE);
		col_module.getColumn().setWidth(150);
		col_module.getColumn().setText("Module");
		col_module.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				return ((StackFrame)element).getFunction().getModule();
			}
		});
		
		/* Column 3 */
		TableViewerColumn col_addr = new TableViewerColumn(this.viewer, SWT.NONE);
		col_addr.getColumn().setWidth(100);
		col_addr.getColumn().setText("Address");
		col_addr.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				return "0x" + Integer.toHexString(((StackFrame)element).getFunction().getStart());
			}
		});
		
		/* Column 4 */
		TableViewerColumn col_name = new TableViewerColumn(this.viewer, SWT.NONE);
		col_name.getColumn().setWidth(200);
		col_name.getColumn().setText("Function");
		col_name.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				return ((StackFrame)element).getFunction().getName();
			}
		});
		
		/* Column 5 */
		TableViewerColumn col_comment = new TableViewerColumn(this.viewer, SWT.NONE);
		col_comment.getColumn().setWidth(400);
		col_comment.getColumn().setText("Comment");
		col_comment.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				//return ((StackFrame)element).getFunction().getName();
				return ((StackFrame)element).getFunction().getComment();
			}
		});		

		this.viewer.setContentProvider(new CallStackContentProvider());
	}

	public void setFocus() {
	}

	public void update(Observable o, Object arg) {
		Function f = null;
		
		if(o instanceof InstanceDoubleClickListener) {
			this.callStack.clear();
			this.next_frame = 1;
			f = (Function) arg;
		} else {
			f = (Function) ((StructuredSelection)arg).getFirstElement();
		}
		
		this.callStack.push(new StackFrame(this.next_frame, f));
		this.viewer.setInput(this.callStack);
		this.next_frame += 1;
	}
	
	
	/* ------ Call Stack Content Provider ------ */
	
	private class CallStackContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			return ((Stack)inputElement).toArray();
		}
		
		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	private class StackFrame {
		private Integer fn;
		private Function f;
		
		public StackFrame(Integer frame_number, Function f) {
			this.fn = frame_number;
			this.f = f;
		}
		
		public Integer getFrameNumber() {
			return this.fn;
		}
		
		public Function getFunction() {
			return this.f;
		}
	}
}
