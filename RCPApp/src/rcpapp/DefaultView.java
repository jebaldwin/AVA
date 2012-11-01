package rcpapp;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;

import cs.uvic.ca.ice.model.InstanceMap;

public class DefaultView extends ViewPart implements Observer {
	private Composite parent;	
	
	public DefaultView() {
		InstanceMap im = InstanceMap.getModel();
		im.addObserver(this);
		
		parent = null;
	}

	public void createPartControl(Composite parent) {
		this.parent = parent;
		displayString(this.parent, "Connect a data source");
	}
		
	private static void displayString(Composite parent, String str) {
		System.out.println("Display string: " + str);		
		GridLayout layout = new GridLayout(1, false);
		parent.setLayout(layout);
		
		Label txt = new Label(parent, SWT.CENTER);
		
		txt.setText(str);
		txt.setForeground(new Color(Display.getCurrent(), 0, 0, 255));
		
		FontData[] fD = txt.getFont().getFontData();
		fD[0].setHeight(12);
		txt.setFont(new Font(Display.getCurrent(),fD[0]));
		
		parent.layout();
	}
	
	public void setFocus() {
	}

	public void update(Observable arg0, Object arg1) {
		Display display = Display.getDefault();
		display.asyncExec(new DisplayUpdater(this.parent));
	}

	private class DisplayUpdater implements Runnable {
		private Composite parent;
		
		public DisplayUpdater(Composite parent) {
			this.parent = parent;
		}		
		
		public void run() {
			// Clear screen before updating
			DefaultView.displayString(this.parent, "Now select a tool");
		}
	}
}