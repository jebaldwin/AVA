package org.eclipse.zest.custom.statediagram.assembly.views;

import java.util.Set;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.zest.custom.sequence.assembly.Startup;

/**
 * This class demonstrates how to create your own dialog classes. It allows
 * users to input a String
 */
public class RecordDialog extends TitleAreaDialog { // InputDialog {
	// IDs for MailDialog buttons
	// We use large integers because we don't want
	// to conflict with system constants
	public static final int OPEN = 9999;
	public static final int DELETE = 9998;
	private Combo idaKeys = null;
	private Text ti = null;
	private String lastRun;
	public String ida;
	public String name;

	NodeModelContentProvider model;

	public RecordDialog(Shell shell, NodeModelContentProvider model, String lastRun) {
		super(shell);
		this.model = model;
		this.lastRun = lastRun;
		setHelpAvailable(false);
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Record Session");
	}

	/**
	 * @see org.eclipse.jface.window.Window#create() We complete the dialog with
	 *      a title and a message
	 */
	public void create() {
		super.create();
		setMessage("Select an Available IDAPro Instance.");
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

		idaKeys = new Combo(area, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		
		Set<String> keys = Startup.disassemblerIFs.keySet();
		Object[] keyArray = keys.toArray();
		int toSelect = 0;
		for (int i = 0; i < keyArray.length; i++) {
			String name = (String) keyArray[i];
			if(!name.equals("first")){
				idaKeys.add(name);
				if(name.equals(lastRun)){
					toSelect = i;
				}
			}
		}
		idaKeys.select(toSelect);

		Label nameLabel = new Label(area, SWT.LEFT);
		nameLabel.setText("State Name:");
		ti = new Text(area, SWT.LEFT | SWT.BORDER);
		GridData data = new GridData();
		data.widthHint = 150;
		ti.setLayoutData(data);

		return area;
	}

	protected void okPressed() {
		if (idaKeys.getSelectionIndex() == -1) {
			setErrorMessage("Select an IDAPro Executable.");
		} else {
			ida = idaKeys.getText();
			name = ti.getText();
			if(name.length() == 0){
				setErrorMessage("Give a Name (You can Rename it Later).");
			}
			super.okPressed();
		}
	}
}