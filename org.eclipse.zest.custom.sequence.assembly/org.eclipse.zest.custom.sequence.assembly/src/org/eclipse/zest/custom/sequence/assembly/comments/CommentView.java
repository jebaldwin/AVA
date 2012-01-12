package org.eclipse.zest.custom.sequence.assembly.comments;


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


public class CommentView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "avadocumentationframe.views.CommentView";

	static Browser left = null;
	static String URLPrefix = "http://avadocumentation.appspot.com/comments.jsp?id=";
	static String CountURLPrefix = "http://avadocumentation.appspot.com/retrieve.jsp?id=";
	static String SubmitURLPrefix = "http://avadocumentation.appspot.com/sign?id=";
	static String id = "";
	public Button button;
	public Text comment;
	public Text user;
	
	public static UMLItem selectedItem;
	
	public CommentView() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		parent.setLayout(gridLayout);		
		
		/*Label label = new Label(parent, SWT.None);
		label.setText("Username:");
		user = new Text(parent, SWT.BORDER);
		
		comment = new Text(parent, SWT.BORDER | SWT.MULTI);
		
		GC gc = new GC(comment);
		FontMetrics fm = gc.getFontMetrics ();
		gc.dispose ();
		int cols = 50;
		int rows = 5;
		int width = cols * fm.getAverageCharWidth();
		int height = rows * fm.getHeight();
		GridData data = new GridData();
		data.widthHint = width;
		data.heightHint = height;
		comment.setLayoutData(data);
		

		button = new Button(parent, SWT.PUSH);
		button.setText("Post Comment");
		
		Listener listener = new Listener() {
		      public void handleEvent(Event event) {
		        if (event.widget == button) {
		            
		        	String text = comment.getText();

		            try {
		            	String message = URLEncoder.encode(text, "UTF-8");
		            	String username = URLEncoder.encode(user.getText(), "UTF-8");
		            	
		                URL url = new URL(SubmitURLPrefix);
		                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		                connection.addRequestProperty("REFERER", URLPrefix + id);
		                connection.addRequestProperty("USER", username);
		                connection.setDoOutput(true);
		                connection.setRequestMethod("POST");

		                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
		                writer.write("content=" + message);
		                writer.close();
		        
		                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
		                   
		                } else {
		                    System.out.println(connection.getResponseMessage());
		                }
		            } catch (MalformedURLException e) {
		                // ...
		            } catch (IOException e) {
		                // ...
		            }
		            
		            left.refresh();
		        } 
		      }
		    };
		button.addListener(SWT.Selection, listener);*/
		    
		GridData gridData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 1;
		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setLayoutData(gridData);
		
		left = new Browser(sashForm, SWT.NONE);
		left.setUrl(URLPrefix);
		
		//redirect browser listener
		LocationListener locationListener = new LocationListener() {
			  private String before;
			  private String after;
			  
		      public void changed(LocationEvent event) {
		            Browser browser = (Browser)event.widget;
		            
		            //determine if there is a new comment
		            after = browser.getUrl();
		            //after = URLEncoder.encode(after);
		            if(before != null){
			            if(before.equals(after)){
			            	//add comment to element in diagram
			            	IEditorPart editor =  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
			            	if(editor instanceof AssemblySequenceEditor){
			            		AssemblySequenceEditor ase = (AssemblySequenceEditor)editor;
			            		ase.changeToCommentIcon();
			            	} else {
			            		DynamicAssemblySequenceEditor ase = (DynamicAssemblySequenceEditor)editor;
			            		ase.changeToCommentIcon();
			            	}
			            }
		            }
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
	
	public static void changeURLID(String url){
		//url = URLEncoder.encode(url);
		//url = url.replaceAll(" ", "%20");
		if(left != null)
			left.setUrl(URLPrefix + url);
		
		id = url;
		//System.out.println(url);
	}
	
	public static int getCommentCount(String url){
		URL yahoo;
		int result = 0;
		
		try {
			//url = URLEncoder.encode(url);
			yahoo = new URL(CountURLPrefix + url);
			BufferedReader in = new BufferedReader(new InputStreamReader(yahoo.openStream()));

			String inputLine;
	
			while ((inputLine = in.readLine()) != null) {
				try {
					Integer res = new Integer(inputLine);
					result = res.intValue();
					return result;
				} catch(NumberFormatException nfe){}
			}
	
			in.close();
		} catch (MalformedURLException me) {
		} catch (IOException e) {
		}		
		
		return result;
	}
}