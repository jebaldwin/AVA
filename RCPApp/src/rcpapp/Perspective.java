package rcpapp;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/*
 * @author jbaldwin
 */
public class Perspective implements IPerspectiveFactory {
    /** The standard perspecive used in the application. */
    public static final String PERSPECTIVE_ID = "RCPApp" + ".perspectives.defaultPerspective";
 
    /** Left folder's id. */
    public static final String FI_LEFT = PERSPECTIVE_ID + ".leftFolder";
    /** Top folder's id. */
    public static final String FI_TOP = PERSPECTIVE_ID + ".topFolder";
    /** Bottom folder's id. */
    public static final String FI_BOTTOM = PERSPECTIVE_ID + ".bottomFolder";
    /** Right folder's id. */
    public static final String FI_RIGHT = PERSPECTIVE_ID + ".rightFolder";
    
    private IPageLayout layout;
    
    /**
     * We replace the editor area with three application folders that can be used for placing views.
     * 
     * @param the layout
     */
    public void createInitialLayout( IPageLayout layout ) {
        this.layout = layout; 
        String editorAreaId = layout.getEditorArea();
        //layout.setEditorAreaVisible( false );
        layout.setFixed( false );
        
        layout.addStandaloneView("org.eclipse.ui.examples.navigator.view",  true, IPageLayout.LEFT, 0.3f, editorAreaId);
        
        IFolderLayout flayout = layout.createFolder( FI_LEFT, IPageLayout.BOTTOM, 0.75f, editorAreaId );
 
        flayout.addView("org.eclipse.ui.views.ContentOutline");
        flayout.addView("org.eclipse.contribution.visualiser.views.Visualiser");
        flayout.addView("org.eclipse.contribution.visualiser.views.Menu");
     
       /* IFolderLayout glayout = layout.createFolder( FI_RIGHT, IPageLayout.RIGHT, 0.75f, editorAreaId );
        glayout.addView("org.eclipse.zest.custom.sequence.assembly.comments.CommentView");*/
    }
 
    /**
     * @return Returns the layout.
     */
    public IPageLayout getLayout() {
        return layout;
    }
}

