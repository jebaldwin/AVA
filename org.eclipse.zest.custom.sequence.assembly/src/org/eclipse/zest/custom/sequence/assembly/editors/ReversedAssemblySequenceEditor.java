package org.eclipse.zest.custom.sequence.assembly.editors;

import java.util.Set;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.zest.custom.sequence.assembly.model.XMLUtils;
import org.eclipse.zest.custom.sequence.widgets.Activation;
import org.eclipse.zest.custom.sequence.widgets.Message;
import org.eclipse.zest.custom.sequence.widgets.UMLItem;
import org.eclipse.zest.custom.uml.viewers.BreadCrumbViewer;
import org.eclipse.zest.custom.uml.viewers.UMLSequenceViewer;
import org.w3c.dom.Document;

/**
 * @author jbaldwin
 */
public class ReversedAssemblySequenceEditor extends AssemblySequenceEditor {

	
	@Override
	public void createPartControl(Composite parent) {
		
		System.out.println("create reverse part control");
		
		control = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 3;
		control.setLayout(layout);

		ascp = new ReversedAssemblySequenceContentProvider(getEditorInput(), methodToExpand, localPath, this);
		breadcrumb = new BreadCrumbViewer(control, SWT.BORDER);

		viewer = new UMLSequenceViewer(control, SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL);
		viewer.setContentProvider(ascp);
		viewer.setLabelProvider(new AssemblySequenceLabelProvider(ascp, viewer));
	    //viewer.setMessageGrouper(new AssemblyMessageGrouper());

		Document doc = document;
		if (doc == null)
			doc = XMLUtils.createSequenceFromXML(inputFile);

		viewer.setInput(doc);
		viewer.getChart().addMouseListener(new NavigateToCodeListener());
		viewer.getChart().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.addSequenceListener(new SequenceViewerListener());
		viewer.getChart().setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		asbcp = new AssemblySequenceBreadCrumbContentProvider(ascp);
		breadcrumb.setContentProvider(asbcp);
		breadcrumb.setLabelProvider(new AssemblySequenceLabelProvider(ascp, viewer));
		breadcrumb.addSelectionChangedListener(new BreadCrumbSelectionListener());
		breadcrumb.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		MenuManager manager = new MenuManager("ASTSequenceEditor", "#ASTSequenceEditorContext");
		manager.setRemoveAllWhenShown(true);
		Menu contextMenu = manager.createContextMenu(viewer.getChart());
		manager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				Point location = Display.getCurrent().getCursorLocation();
				location = viewer.getChart().toControl(location);
				Object element = viewer.elementAt(location.x, location.y);
				Widget widget = viewer.getChart().getItemAt(location.x, location.y);
				Activation act = null;
				if (widget instanceof Activation) {
					act = (Activation) widget;
				}
				if (element instanceof NodeProxy) {
					String activationName = ((ILabelProvider) viewer.getLabelProvider()).getText(element);
					focusIn.setFocusElement(element);
					focusIn.setText("Focus On " + activationName);
					manager.add(focusIn);
					expandAll.setText("Expand All Activations Under " + activationName);
					expandAll.setFocusElement(element);
					manager.add(expandAll);
					collapseAll.setText("Collapse All Activations Under " + activationName);
					collapseAll.setFocusElement(element);
					manager.add(collapseAll);

					NodeProxy np = (NodeProxy) element;
					// focus on caller
					if (np.getCallingNode() != null && !viewer.getRootActivation().equals(ascp.rootNode)) {
						manager.add(focusUp);
					}
				} else {
					// String activationName = (String) element;

					// if (act.getText().equals(activationName)) {
					String activationName = ((ILabelProvider) viewer.getLabelProvider()).getText(element);
					focusIn.setFocusElement(act);
					focusIn.setText("Focus On " + activationName);
					manager.add(focusIn);

					expandAll.setText("Expand All Activations Under " + activationName);
					expandAll.setFocusElement(act);
					manager.add(expandAll);

					collapseAll.setText("Collapse All Activations Under " + activationName);
					collapseAll.setFocusElement(act);
					manager.add(collapseAll);

					// focus on caller
					if (!element.equals("Start")) {
						focusUp.setFocusElement(act);
						manager.add(focusUp);
					}

					asbcp.currAct = act;
					// if (np.getCallingNode() != null &&
					// !viewer.getRootActivation().equals(ascp.rootNode))
					// { //
					// manager.add(focusUp);
					// }
				}
				manager.add(cloneAction);
			}
		});
		viewer.getChart().setMenu(contextMenu);
		viewer.getChart().setCloneVisible(false);

		makeActions();
		getEditorSite().getActionBars().getToolBarManager().add(cloneAction);
		// load persistence
		loadSaved();
		
	}	
	
	protected void updateExpanded() {
		super.updateExpanded();
		
		UMLItem[] items = viewer.getChart().getItems();
		for (int i = 0; i < items.length; i++) {
			UMLItem item = items[i];
			if (item instanceof Message){
				Message mess  = (Message)item;
				mess.setSourceStyle(Message.CLOSED_ARROW | Message.FILL_MASK); 
				mess.setTargetStyle(Message.NONE);
			}
		}
	}
	
	public void setDirty(boolean dirty){
		//don't save reversed editor
		this.dirty = false;
		firePropertyChange(PROP_DIRTY);
	}
}
