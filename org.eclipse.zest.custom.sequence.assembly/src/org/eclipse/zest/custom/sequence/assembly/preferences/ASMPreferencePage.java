package org.eclipse.zest.custom.sequence.assembly.preferences;

import java.awt.Composite;
import java.util.Iterator;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.zest.custom.sequence.assembly.Activator;
import org.eclipse.zest.custom.sequence.assembly.Startup;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class ASMPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static StringFieldEditor field2;
	
	public ASMPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		// setDescription("A demonstration of a preference page implementation");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {
		Label listLabel = new Label(getFieldEditorParent(), SWT.NONE);
		listLabel.setText("");
		
		listLabel = new Label(getFieldEditorParent(), SWT.NONE);
		listLabel.setText("");
		
		
		field2 = new StringFieldEditor(PreferenceConstants.P_PREF_COUNT, "Obfuscation Loop Count ( >= 3 ):", 4, getFieldEditorParent());
		field2.setTextLimit(4);
		addField(field2);

		listLabel = new Label(getFieldEditorParent(), SWT.NONE);
		listLabel.setText("");

		addField(new RadioGroupFieldEditor(PreferenceConstants.P_DEBUG, "Debugging - Log Calls", 1, new String[][] { { "&Stepped Into Calls", PreferenceConstants.P_DEBUG_STEP },
				{ "&All Calls", PreferenceConstants.P_DEBUG_ALL } }, getFieldEditorParent(), true));

		listLabel = new Label(getFieldEditorParent(), SWT.NONE);
		listLabel.setText("");
		
		addField(new RadioGroupFieldEditor(PreferenceConstants.P_LOG, "Debugging - Log External Inner Calls", 1, new String[][] { { "&Yes", PreferenceConstants.P_LOG_INNER}, { "&No", PreferenceConstants.P_LOG_OUTER } },
				getFieldEditorParent(), true));

		listLabel = new Label(getFieldEditorParent(), SWT.NONE);
		listLabel.setText("");
		
		
		/*addField(new RadioGroupFieldEditor(PreferenceConstants.P_RETURN, "Debugging and Navigation - Return Calls", 1, new String[][] { { "&Show Return Calls as Dotted Lines", PreferenceConstants.P_SHOW_RETURN}, { "&Show Return Calls as Solid Lines", PreferenceConstants.P_NO_SHOW_RETURN} },
				getFieldEditorParent(), true));

		listLabel = new Label(getFieldEditorParent(), SWT.NONE);
		listLabel.setText("");*/
		
		/*addField(new RadioGroupFieldEditor(PreferenceConstants.P_STATIC, "Static Preferences", 1, new String[][] { { "&Retrieve Call Information", PreferenceConstants.P_STATIC_RET },
				{ "&Use Stored Information", PreferenceConstants.P_STATIC_STOR } }, getFieldEditorParent(), true));

		listLabel = new Label(getFieldEditorParent(), SWT.NONE);
		listLabel.setText("");*/

		RadioGroupFieldEditor field = new RadioGroupFieldEditor(PreferenceConstants.P_GENERAL, "General - Navigation", 1, new String[][] { { "&Always Synchronize Navigation", PreferenceConstants.P_GEN_ALL },
				{ "&Only Double Click for Navigation", PreferenceConstants.P_GEN_CLICK } }, getFieldEditorParent(), true);
		addField(field);
		
		/*listLabel = new Label(getFieldEditorParent(), SWT.NONE);
		listLabel.setText("");
		
		RadioGroupFieldEditor field2 = new RadioGroupFieldEditor(PreferenceConstants.P_COMMENTS, "General - Documentation", 1, new String[][] { { "&Do Not Add Comment Flags", PreferenceConstants.P_NO_COMMENTS},
				{ "&Add Comment Flags", PreferenceConstants.P_RET_COMMENTS} }, getFieldEditorParent(), true);
		addField(field2);*/
		
		/*listLabel = new Label(getFieldEditorParent(), SWT.NONE);
		listLabel.setText("");
		
		listLabel = new Label(getFieldEditorParent(), SWT.NONE);
		listLabel.setText("");*/
		
		//Group urlGroup = new Group(getFieldEditorParent(), SWT.NONE);
		//urlGroup.setText("Obfuscation Loop Count");
		
		// field2 = new StringFieldEditor(PreferenceConstants.P_PREF_COUNT, "Obfuscation Loop Count:", 4, getFieldEditorParent());
		//field2.setTextLimit(4);
		//addField(field2);

		/*listLabel = new Label(getFieldEditorParent(), SWT.NONE);
		listLabel.setText("");

		addField(new RadioGroupFieldEditor("BUG_USER_KEY", "Warning Preferences", 1, new String[][] { { "&Show Warning Messages", "false" }, { "&Hide Warning Messages", "true" } },
				getFieldEditorParent(), true));*/	

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	public boolean performOk() {
		super.performOk();
		if (getPreferenceStore().getString(PreferenceConstants.P_DEBUG).equals(PreferenceConstants.P_DEBUG_ALL)) {
			// enable tracing
			Startup.send(null, "enableTracing");
		} else {
			// disable tracing
			Startup.send(null, "disableTracing");
		}
		
		if(getPreferenceStore().getString(PreferenceConstants.P_LOG).equals(PreferenceConstants.P_LOG_INNER)){
			Startup.send(null, "enableInner");
	    } else {
	    	Startup.send(null, "disableInner");
	    }	

		int val = new Integer(field2.getStringValue());
		if(val < 3){
			getPreferenceStore().setValue(PreferenceConstants.P_PREF_COUNT, "3");
			val = 3;
		}
	    Startup.send(null, "prefCount " + val);//(val * 2));
	    	
		return true;
	}
}