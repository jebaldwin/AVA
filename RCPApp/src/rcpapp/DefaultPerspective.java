package rcpapp;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class DefaultPerspective implements IPerspectiveFactory {
	public final static String ID = "rcpapp.perspective.ICE";
	
	@Override
	public void createInitialLayout(IPageLayout layout) {
        String editorAreaId = layout.getEditorArea();
        layout.setFixed(false);
        	
        layout.addStandaloneView("rcpapp.DefaultView",  true, IPageLayout.LEFT, 1.0f, editorAreaId);
        layout.setEditorAreaVisible(false);
	}

}
