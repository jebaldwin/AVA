package rcpapp.cartographer;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import cs.uvic.ca.ice.model.Function;

public class CFGWindow extends ApplicationWindow {
	private Function func;
	
	public CFGWindow(Function f) {
		super(null);
		
		this.func = f;
		System.out.println("created CFGWindow");
	}
	
    protected Control createContents(Composite parent) {
        this.getShell().setText("Test Window");

        parent.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
        return parent;
    }
}
