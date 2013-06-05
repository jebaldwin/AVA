package rcpapp;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.internal.OpenPreferencesAction;
import org.eclipse.ui.internal.about.AboutAction;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of
 * the actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	// Actions - important to allocate these only in makeActions, and then use
	// them
	// in the fill methods. This ensures that the actions aren't recreated
	// when fillActionBars is called with FILL_PROXY.
	private IWorkbenchAction newAction;
	private IWorkbenchAction exitAction;
	private IWorkbenchAction deleteAction;
	private IWorkbenchAction copyAction;
	private IWorkbenchAction pasteAction;
	private IWorkbenchAction refreshAction;
	private IWorkbenchAction saveAction;
	
	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	protected void makeActions(final IWorkbenchWindow window) {
		// Creates the actions and registers them.
		// Registering is needed to ensure that key bindings work.
		// The corresponding commands keybindings are defined in the plugin.xml
		// file.
		// Registering also provides automatic disposal of the actions when
		// the window is closed.

		newAction = ActionFactory.NEW_WIZARD_DROP_DOWN.create(window);
		register(newAction);
		
		exitAction = ActionFactory.QUIT.create(window);
		register(exitAction);
		
		deleteAction = ActionFactory.DELETE.create(window);
		register(deleteAction);
		
		copyAction = ActionFactory.COPY.create(window);
		register(copyAction);
		
		pasteAction = ActionFactory.PASTE.create(window);
		register(pasteAction);
		
		refreshAction = ActionFactory.REFRESH.create(window);
		register(refreshAction);
		
		saveAction = ActionFactory.SAVE.create(window);
		register(saveAction);
	}

	public static URL makeIconFileURL(String name) throws MalformedURLException {
		String pathSuffix= "icons/"; //$NON-NLS-1$ 
		URL iconBaseURL= new URL(Activator.getDefault().getBundle().getEntry("/"), pathSuffix); //$NON-NLS-1$		
		return new URL(iconBaseURL, name);
	}
	
	protected void fillMenuBar(IMenuManager menuBar) {
		Separator separator = new Separator();
		
		MenuManager fileMenu = new MenuManager("&File",	IWorkbenchActionConstants.CLOSE);
		fileMenu.add(newAction);
		fileMenu.add(saveAction);
		fileMenu.add(exitAction);
		menuBar.add(fileMenu);
		
		MenuManager editMenu = new MenuManager("&Edit",	IWorkbenchActionConstants.M_EDIT);
		editMenu.add(new GroupMarker(IWorkbenchActionConstants.FIND_EXT));
		
		editMenu.add(new OpenPreferencesAction(getActionBarConfigurer().getWindowConfigurer().getWindow()));
		//editMenu.add(new PreferencesAction(getActionBarConfigurer().getWindowConfigurer().getWindow()));
		editMenu.add(separator);
		menuBar.add(editMenu);
		
		MenuManager helpMenu = new MenuManager("&Help",	IWorkbenchActionConstants.HELP_START);
		helpMenu.add(new AboutAction(getActionBarConfigurer().getWindowConfigurer().getWindow()));
		menuBar.add(helpMenu);
	}
}
