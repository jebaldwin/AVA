package rcpapp;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
//import org.eclipse.ui.internal.OpenPreferencesAction;
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
	private IWorkbenchAction perspectiveAction;
	private IWorkbenchAction aboutAction;
	
	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	protected void makeActions(final IWorkbenchWindow window) {
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
		
		perspectiveAction = ActionFactory.OPEN_PERSPECTIVE_DIALOG.create(window);
		register(perspectiveAction);
		
		aboutAction = ActionFactory.ABOUT.create(window);
		register(aboutAction);
	}

	public static URL makeIconFileURL(String name) throws MalformedURLException {
		String pathSuffix= "icons/"; //$NON-NLS-1$ 
		URL iconBaseURL= new URL(Activator.getDefault().getBundle().getEntry("/"), pathSuffix); //$NON-NLS-1$		
		return new URL(iconBaseURL, name);
	}
	
	protected void fillMenuBar(IMenuManager menuBar) {
		Separator separator = new Separator();
		
		MenuManager fileMenu = new MenuManager("&File",	IWorkbenchActionConstants.M_FILE);
		fileMenu.add(newAction);
		fileMenu.add(exitAction);
		menuBar.add(fileMenu);
		
		MenuManager editMenu = new MenuManager("&Edit",	IWorkbenchActionConstants.M_EDIT);
		editMenu.add(new PreferencesAction(getActionBarConfigurer().getWindowConfigurer().getWindow()));
		editMenu.add(separator);
		menuBar.add(editMenu);
		
		MenuManager helpMenu = new MenuManager("&Help",	IWorkbenchActionConstants.HELP_START);
		helpMenu.add(aboutAction);
		menuBar.add(helpMenu);
		
		MenuManager perspectiveMenu = new MenuManager("&Perspective", IWorkbenchActionConstants.M_WINDOW);
		perspectiveMenu.add(perspectiveAction);
		menuBar.add(perspectiveMenu);
	}
}
