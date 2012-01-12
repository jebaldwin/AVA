package org.eclipse.zest.custom.sequence.assembly.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.zest.custom.sequence.assembly.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_DEBUG, PreferenceConstants.P_DEBUG_STEP);
		store.setDefault(PreferenceConstants.P_STATIC, PreferenceConstants.P_STATIC_STOR);
		store.setDefault(PreferenceConstants.P_GENERAL, PreferenceConstants.P_GEN_CLICK);
		store.setDefault(PreferenceConstants.P_LOG, PreferenceConstants.P_LOG_OUTER);
		store.setDefault(PreferenceConstants.P_PREF_COUNT, "10");
		store.setDefault(PreferenceConstants.P_RET_COMMENTS, PreferenceConstants.P_NO_COMMENTS);
		store.setDefault(PreferenceConstants.P_RETURN, PreferenceConstants.P_SHOW_RETURN);
		store.setDefault("BUG_USER_KEY", "false");
	}

}
