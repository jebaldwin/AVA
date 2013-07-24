package org.eclipse.ui.MSDN;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.custom.sequence.assembly.editors.AssemblySequenceEditor;
import org.eclipse.zest.custom.sequence.assembly.editors.DynamicAssemblySequenceEditor;
import org.eclipse.zest.custom.sequence.widgets.UMLItem;


public class MSDNView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "avadocumentationframe.views.MSDNView";

	static Browser left = null;
	static String URLPrefix = "https://www.google.com/search?q=";
	static String URLPostfix = "&btnI=I";
	static String id = "";
	public Button button;
	
	public static UMLItem selectedItem;
	
	public MSDNView() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		parent.setLayout(gridLayout);	
		    
		GridData gridData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 1;
		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setLayoutData(gridData);
		
		left = new Browser(sashForm, SWT.NONE);
		left.setUrl("http://msdn.microsoft.com/en-US/");
		left.setUrl(URLPrefix);
		
		//redirect browser listener
		LocationListener locationListener = new LocationListener() {
			  private String before;
			  private String after;
			  
		      public void changed(LocationEvent event) {
		            Browser browser = (Browser)event.widget;
		            
		            //determine if there is a new comment
		            after = browser.getUrl();
		            
		      }
		      
		      public void changing(LocationEvent event) {
		    	  Browser browser = (Browser)event.widget;
		    	  before =  browser.getUrl();
		      }
		      };
		left.addLocationListener(locationListener);
		
		parent.pack();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		//nop
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		//nop
	}
	
	public static void changeURLTerms(String terms){
		//url = URLEncoder.encode(url);
		//url = url.replaceAll(" ", "%20");
		terms = terms.replace("__imp___", "");
		
		if(left != null)
			left.setUrl(URLPrefix + terms + URLPostfix);
		
		//id = url;
		//System.out.println(url);
	}
}