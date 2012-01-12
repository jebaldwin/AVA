package org.eclipse.zest.custom.statediagram.assembly.views;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * This class demonstrates how to create your own dialog classes. It allows
 * users to input a String
 */
public class ConnectionDialog extends TitleAreaDialog { // InputDialog {
	// IDs for MailDialog buttons
	// We use large integers because we don't want
	// to conflict with system constants
	public static final int OPEN = 9999;
	public static final int DELETE = 9998;
	private Combo sourceConn = null;
	private Combo targetConn = null;
	public String source;
	public String target;

	NodeModelContentProvider model;

	public ConnectionDialog(Shell shell, NodeModelContentProvider model) {
		super(shell);
		this.model = model;
		setHelpAvailable(false);
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Create a Connection");
	}

	/**
	 * @see org.eclipse.jface.window.Window#create() We complete the dialog with
	 *      a title and a message
	 */
	public void create() {
		super.create();
		setMessage("Create a new directed connection.");
		setHelpAvailable(false);
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#
	 *      createDialogArea(org.eclipse.swt.widgets.Composite) Here we fill the
	 *      center area of the dialog
	 */
	protected Control createDialogArea(Composite parent) {
		// Create new composite as container
		final Composite area = new Composite(parent, SWT.NULL);
		// We use a grid layout and set the size of the margins
		final GridLayout gridLayout = new GridLayout(2, true);
		gridLayout.marginWidth = 15;
		gridLayout.marginHeight = 10;
		area.setLayout(gridLayout);

		Label toLabel = new Label(area, SWT.LEFT);
		toLabel.setText("To:");

		sourceConn = new Combo(area, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		
		Label fromLabel = new Label(area, SWT.LEFT);
		fromLabel.setText("From:");

		targetConn = new Combo(area, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		Set<String> keys = model.nodes.keySet();

		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
			String name = (String) iterator.next();
			sourceConn.add(name);
			targetConn.add(name);
		}

		return area;
	}

	protected void okPressed() {
		//if (sourceConn.getSelectionIndex() >= targetConn.getSelectionIndex()) {
		//	setErrorMessage("Connections cannot go backwards.");
		//} else {
			if (sourceConn.getSelectionIndex() == -1 || targetConn.getSelectionIndex() == -1) {
				setErrorMessage("Both source and target must be set.");
			} else {
				source = sourceConn.getText();
				target = targetConn.getText();
				super.okPressed();
			}
		//}
	}
}