package rcpapp.rails;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import cs.uvic.ca.ice.bridge.Message;

public class Perspective implements IPerspectiveFactory, Observer {
    public static final String ID = "RCPApp.rails_perspective";
 
    public static final String FI_LEFT    = ID + ".leftFolder";
    public static final String FI_TOP     = ID + ".topFolder";
    public static final String FI_BOTTOM  = ID + ".bottomFolder";
    public static final String FI_RIGHT   = ID + ".rightFolder";
    
    private IPageLayout layout;
    
    public void createInitialLayout( IPageLayout layout ) {
        this.layout = layout; 
        String editorAreaId = layout.getEditorArea();
        layout.setFixed(false);
        	
        layout.addStandaloneView("rcpapp.rails.DataSource",  true, IPageLayout.LEFT, 0.3f, editorAreaId);
        layout.setEditorAreaVisible(true);
    }
 
    /**
     * @return Returns the layout.
     */
    public IPageLayout getLayout() {
        return layout;
    }
    
    public void update(Observable obs, Object arg) {
    	ConcurrentLinkedQueue<Message> msgQ = (ConcurrentLinkedQueue<Message>)arg;
    	System.out.println("Rails: " + msgQ.peek());
    }
}

