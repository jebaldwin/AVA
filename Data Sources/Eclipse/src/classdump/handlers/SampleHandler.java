package classdump.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;

public class SampleHandler extends AbstractHandler {

	public SampleHandler() {
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject[] iprojects = workspace.getRoot().getProjects();

		for (IProject ip : iprojects)
		{
		    if (ip.isOpen() == true)
		    {
		        IJavaProject javaProject = JavaCore.create(ip);

		        IPackageFragment[] packages;
		        try
		        {
		        	packages = javaProject.getPackageFragments();

		        	for (IPackageFragment mypackage : packages)
		        	{
		        		if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE)
		        		{
		        			if(mypackage.getCompilationUnits().length > 0)
		        				System.out.println("Source Name " + mypackage.getElementName());
		        		}
		        		else if (mypackage.getKind() == IPackageFragmentRoot.K_BINARY)
		        		{
		        			if(mypackage.getCompilationUnits().length > 0)
		        				System.out.println("Binary Name " + mypackage.getElementName());
		        		}
		        	}
		        } catch (JavaModelException e) {
		        	e.printStackTrace();
		        }
		    }
		}
		    
		return null;
	}
}
