package org.eclipse.ui.examples.navigator.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.zest.custom.sequence.assembly.Activator;
import org.eclipse.zest.custom.sequence.assembly.preferences.PreferenceConstants;
import org.eclipse.zest.custom.uml.viewers.UMLSequenceViewer;

/**
 * Expands the activations in the viewer starting at the focused root.
 * @author Jennifer Baldwin
 */

public class ExpandAllAction extends Action {
	
	private UMLSequenceViewer viewer;
	private Object element;
	
	public ExpandAllAction(UMLSequenceViewer viewer) {
		this.viewer = viewer;
	}
	
	@Override
	public void run() {
		if (element != null) {
			if(Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_STATIC).equals(PreferenceConstants.P_STATIC_RET)){
				MessageDialogWithToggle dialog = MessageDialogWithToggle.openWarning(Activator.getDefault().getWorkbench().getDisplay().getActiveShell(), "Cannot Load Expanded Elements", "When retrieving function names, you cannot expand all elements. If you want to expand the elements, please switch to stored information and try again.",
					"Don't display this question again", false,
					 Activator.getDefault().getPreferenceStore(), "BUG_USER_KEY");
				boolean result = dialog.getToggleState();
				String res = new Boolean(result).toString();
				Activator.getDefault().getPreferenceStore().setValue("BUG_USER_KEY", res);
			} else {
				try{
					viewer.expandActivationsUnder(element, true);
				} catch(NullPointerException npe){
					//happens because we don't have a content provider, don't need one
				}
			}
		}
	}
	
	/**
	 * @param element
	 */
	public void setFocusElement(Object element) {
		this.element = element;
	}

}
