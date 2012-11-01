package rcpapp;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PerspectiveAdapter;

import cs.uvic.ca.ice.model.IRefreshPart;

public class ApplicationPerspectiveAdapter extends PerspectiveAdapter {
	public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		System.out.println("activated: " + perspective.getId());
		
		IViewReference[] viewReferences = page.getViewReferences();
		for(IViewReference viewRef : viewReferences) {
			IViewPart view = viewRef.getView(false);
			if(view instanceof IRefreshPart)
				((IRefreshPart)view).refreshPart();
		}
				
		IEditorReference[] editorReferences = page.getEditorReferences();
		for(IEditorReference editorRef : editorReferences) {
			IEditorPart editor = editorRef.getEditor(false);
			if(editor instanceof IRefreshPart)
				((IRefreshPart)editor).refreshPart();
		}
	}
}
