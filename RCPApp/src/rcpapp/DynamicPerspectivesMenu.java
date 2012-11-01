package rcpapp;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import cs.uvic.ca.ice.model.InstanceMap;

public class DynamicPerspectivesMenu extends ContributionItem {
	private static final String KEY_PERSPECTIVE_DESCR = "k_p_descr";
	
	private final SelectionListener menuItemSelectionListener = new SwitchPerspectiveMenuListener();
	
	private static IWorkbenchPage getActivePage() {
		IWorkbenchPage result = null;
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if(window != null) {
			result = window.getActivePage();
		}
		
		return result;
	}
	
	private static String getPerspectiveId() {
		String result = null;
		IWorkbenchPage page = getActivePage();
		if(page != null) {
			IPerspectiveDescriptor descriptor = page.getPerspective();
			if(descriptor != null) {
					result = descriptor.getId();
			}
		}
		
		return result;
	}
	
	public DynamicPerspectivesMenu() {
	}

	public DynamicPerspectivesMenu(String id) {
		super(id);
	}

	public void fill(Menu menu, int index) {
		String activePerspective = getPerspectiveId();
		IPerspectiveDescriptor[] perspectives = PlatformUI.getWorkbench().getPerspectiveRegistry().getPerspectives();
		for(IPerspectiveDescriptor descriptor : perspectives) {
			String pid = descriptor.getId();
			if(pid.equals(rcpapp.tracks.Perspective.PERSPECTIVE_ID) || 
					pid.equals(rcpapp.rails.Perspective.ID) || 
					pid.equals(rcpapp.cartographer.Perspective.ID)) {
				MenuItem item = new MenuItem(menu, SWT.RADIO);
				item.setData(KEY_PERSPECTIVE_DESCR, descriptor);
				item.setText(descriptor.getLabel());
				item.setEnabled(InstanceMap.getModel().ready());
				item.addSelectionListener(menuItemSelectionListener);
				if(descriptor.getId().equals(activePerspective)) {
					item.setSelection(true);
				}
			}
		}
	}
	
	public final boolean isDynamic() {
		return true;
	}
	
	private static final class SwitchPerspectiveMenuListener implements SelectionListener {
		public void widgetSelected(SelectionEvent e) {
			MenuItem item = (MenuItem) e.widget;
			if(item.getSelection()) {
				IWorkbenchPage page = getActivePage();
				if(page != null) {
					IPerspectiveDescriptor descriptor = (IPerspectiveDescriptor) item.getData(KEY_PERSPECTIVE_DESCR);
					page.setPerspective(descriptor);
				}
			}
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			
		}
	}
}
