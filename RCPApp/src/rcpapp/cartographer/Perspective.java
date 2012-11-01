package rcpapp.cartographer;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {
	public final static String ID = "rcpapp.cartographer.Perspective";
	
	public void createInitialLayout(IPageLayout layout) {
        String editorAreaId = layout.getEditorArea();
        layout.setFixed(false);
        	
        layout.addStandaloneView("rcpapp.cartographer.MapView",  true, IPageLayout.LEFT, 1.0f, editorAreaId);
        layout.setEditorAreaVisible(false);
	}
}
