package rcpapp.rails;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import cs.uvic.ca.ice.model.Function;
import cs.uvic.ca.ice.model.Instance;
import cs.uvic.ca.ice.model.InstanceMap;

public class DataSourceInstanceEditor extends EditorPart {
	public static final String ID = "rcpapp.rails.InstanceEditor";
	private Instance inst;
	
	public DataSourceInstanceEditor() {
		System.out.println("DataSourceInstanceEditor ctor");
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setInput(input);
		setSite(site);
		
		if (!(input instanceof DataSourceInstanceEditorInput)) {
			throw new RuntimeException("wrong input");
		}
		
		DataSourceInstanceEditorInput dsiInput = (DataSourceInstanceEditorInput)input;
		this.inst = InstanceMap.getModel().getInstanceById(dsiInput.getId());
		
		if(this.inst == null) {
			throw new RuntimeException("instance does not exist in model");
		}
			
		setPartName(this.inst.getName());
		
		firePropertyChange(IWorkbenchPartConstants.PROP_INPUT);
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	public void createPartControl(Composite parent) {
	    GridLayout layout = new GridLayout();
	    layout.numColumns = 2;
	    parent.setLayout(layout);
	    
	    Label label1 = new Label(parent, SWT.NONE);
	    label1.setText("Instance");
	    
	    Text text = new Text(parent, SWT.BORDER);
	    text.setText(inst.getName());
	    
	    Label label2 = new Label(parent, SWT.NONE);
	    label2.setText("Functions");
	    
	    ListViewer funcList = new ListViewer(parent);
		funcList.setLabelProvider(new LabelProvider() {
			 public String getText(Object element) {
				 Function f = (Function) element;
				 return f.getName();
			 }
		});
	  
		Collection<Function> funcs = inst.getFunctions();
		Iterator<Function> func_iter = funcs.iterator();
		while(func_iter.hasNext()) {
			Function f = func_iter.next();
			funcList.add(f);
		}
		System.out.println("num funcs: " + funcs.size());
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
