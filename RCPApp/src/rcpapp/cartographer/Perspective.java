package rcpapp.cartographer;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {
	public final static String ID = "rcpapp.cartographer.Perspective";
	
	public void createInitialLayout(IPageLayout layout) {
        String editorAreaId = layout.getEditorArea();
        layout.setFixed(false);

        layout.addStandaloneView("rcpapp.cartographer.InstanceView", true, IPageLayout.LEFT, 1.0f, editorAreaId);
        layout.addStandaloneView("rcpapp.cartographer.MapView",  true, IPageLayout.RIGHT, 0.1f, InstanceView.ID);
        layout.addStandaloneView("rcpapp.cartographer.CallStackView", true, IPageLayout.BOTTOM, 0.9f, MapView.ID);
        layout.setEditorAreaVisible(false);
	}
}
