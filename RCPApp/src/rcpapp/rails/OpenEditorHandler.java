package rcpapp.rails;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import cs.uvic.ca.ice.model.Instance;

public class OpenEditorHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
	    System.out.println("called");

	    // Get the view
	    IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
	    IWorkbenchPage page = window.getActivePage();
	    DataSourceView view = (DataSourceView) page.findView(DataSourceView.ID);
	     
   
	    // Get the selection
	    ISelection selection = view.getSite().getSelectionProvider().getSelection();
	    if (selection != null && selection instanceof IStructuredSelection) {
	      Object obj = ((IStructuredSelection) selection).getFirstElement();
	      
	      // If we had a selection lets open the editor
	      if (obj != null) {
	        Instance inst = (Instance) obj;
	        DataSourceInstanceEditorInput input = new DataSourceInstanceEditorInput(inst.getId());
	        try {
	          page.openEditor(input, DataSourceInstanceEditor.ID);

	        } catch (PartInitException e) {
	          throw new RuntimeException(e);
	        }
	      }
	    }
	    return null;
	}

}
