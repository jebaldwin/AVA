package rcpapp.rails;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/*
 * @author jbaldwin
 */
public class Perspective implements IPerspectiveFactory {
    /** The standard perspecive used in the application. */
    public static final String PERSPECTIVE_ID = "RCPApp" + ".perspectives.railsPerspective";
 
    public static final String FI_LEFT    = PERSPECTIVE_ID + ".leftFolder";
    public static final String FI_TOP     = PERSPECTIVE_ID + ".topFolder";
    public static final String FI_BOTTOM  = PERSPECTIVE_ID + ".bottomFolder";
    public static final String FI_RIGHT   = PERSPECTIVE_ID + ".rightFolder";
    
    private IPageLayout layout;
    
    public void createInitialLayout( IPageLayout layout ) {
        this.layout = layout; 
        String editorAreaId = layout.getEditorArea();
        layout.setFixed( false );
        
        layout.addStandaloneView("RCPApp.rails.dataSource",  true, IPageLayout.LEFT, 0.3f, editorAreaId);
        layout.addStandaloneView("org.eclipse.ui.views.ContentOutline", true, IPageLayout.BOTTOM, 0.3f, editorAreaId);
        layout.setEditor
    }
 
    /**
     * @return Returns the layout.
     */
    public IPageLayout getLayout() {
        return layout;
    }
}
