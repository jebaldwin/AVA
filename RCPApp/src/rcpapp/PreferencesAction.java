package rcpapp;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;

public class PreferencesAction extends Action {

	/**
	 * The workbench window; or <code>null</code> if this action has been
	 * <code>dispose</code>d.
	 */
	private IWorkbenchWindow workbenchWindow;

	/**
	 * Create a new <code>OpenPreferenceAction</code> This default constructor
	 * allows the the action to be called from the welcome page.
	 */
	public PreferencesAction() {
		workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}

	public PreferencesAction(IWorkbenchWindow window) {
		super(WorkbenchMessages.OpenPreferences_text);
		if (window == null) {
			throw new IllegalArgumentException();
		}
		this.workbenchWindow = window;
		// @issue action id not set
		setToolTipText(WorkbenchMessages.OpenPreferences_toolTip);
		window.getWorkbench().getHelpSystem().setHelp(this, IWorkbenchHelpContextIds.OPEN_PREFERENCES_ACTION);
	}

	public void run() {
		if (workbenchWindow == null) {
			// action has been disposed
			return;
		}
		String[] ids = { "Assembly", "Visualiser" };
		PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(null, "Assembly", ids, null);
		dialog.open();
	}

}
