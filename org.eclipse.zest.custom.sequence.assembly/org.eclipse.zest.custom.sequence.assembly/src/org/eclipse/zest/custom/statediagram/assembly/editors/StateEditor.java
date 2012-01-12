package org.eclipse.zest.custom.statediagram.assembly.editors;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphItem;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.custom.sequence.assembly.Activator;
import org.eclipse.zest.custom.sequence.assembly.Startup;
import org.eclipse.zest.custom.statediagram.assembly.views.ConnectionDialog;
import org.eclipse.zest.custom.statediagram.assembly.views.MyAction;
import org.eclipse.zest.custom.statediagram.assembly.views.MyConnection;
import org.eclipse.zest.custom.statediagram.assembly.views.MyNode;
import org.eclipse.zest.custom.statediagram.assembly.views.NodeModelContentProvider;
import org.eclipse.zest.custom.statediagram.assembly.views.RecordDialog;
import org.eclipse.zest.custom.statediagram.assembly.views.ZestLabelProvider;
import org.eclipse.zest.custom.statediagram.assembly.views.ZestNodeContentProvider;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class StateEditor extends EditorPart {

	public GraphViewer viewer;
	
	private Action runAction;
	private Action delAction;
	private Action renameAction;
	private Action connectAction;
	private Action recordAction;
	private boolean recording = false;
	
	private MyNode selectedNode = null;
	private GraphConnection selectedConn = null;
	private MyNode currentStateNode = null;
	private NodeModelContentProvider model;
	
	private Graph graph;
	private int layout = 1;
	private String IDAExec;
	private String stateName;
	
	public StateEditor() {
		super();
	}

	public void dispose() {
		super.dispose();
	}

	public void doSave(IProgressMonitor arg0) {
		System.out.println("saving...");
		
		//save the graphs and actions in order of connections
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();
			Element root = document.createElement("states");
			
			List nodes = graph.getNodes();
			for (int i = 0; i < nodes.size(); i++) {
				GraphNode node = (GraphNode) nodes.get(i);
				
				Element state = document.createElement("state");
				state.setAttribute("name", node.getText());
				MyNode mnode = model.nodes.get(node.getText());
				
				for (int j = 0; j < mnode.actions.size(); j++) {
					MyAction act = mnode.actions.get(j);
					Element actNode = document.createElement("action");
					actNode.setAttribute("command", act.command);
					actNode.setAttribute("address", act.address);
					state.appendChild(actNode);
				}
				root.appendChild(state);
			}
			
			document.appendChild(root);

			
			// Prepare the DOM document for writing
			Source source = new DOMSource(root);

			File testFile = new File("test.state");

			File inputFile = null;
			if (this.getEditorInput() instanceof FileEditorInput) {
				FileEditorInput fei = (FileEditorInput) this.getEditorInput();
				inputFile = new File(fei.getURI());
			} else {
				FileStoreEditorInput fei = (FileStoreEditorInput) this.getEditorInput();
				inputFile = new File(fei.getURI());
			}
			
			Result result = new StreamResult(inputFile);
			
			// Write the DOM document to the file
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer;
			try {
				transformer = tf.newTransformer();
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.transform(source, result);
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

	public void doSaveAs() {	
	}

	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setInput(input);
		setSite(site);
	}

	public boolean isDirty() {
		return true;
	}

	public boolean isSaveAsAllowed() {
		return false;
	}
			
	public void createPartControl(Composite parent) {
		viewer = new GraphViewer(parent, SWT.BORDER);
		graph = viewer.getGraphControl();
		viewer.setContentProvider(new ZestNodeContentProvider());
		viewer.setLabelProvider(new ZestLabelProvider());
		model = new NodeModelContentProvider(this.getEditorInput(), viewer);
		viewer.setInput(model.getNodes());
		LayoutAlgorithm layout = setLayout();
		viewer.setLayoutAlgorithm(layout, true);
		viewer.applyLayout();
		
		parent.addListener (SWT.Resize,  new Listener () {
		    public void handleEvent (Event e) {
		      viewer.applyLayout();
		    }
		  });
		
		graph.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				List selected = graph.getSelection();
				if(selected.size() == 0){
					selectedNode = null;
					return;
				}
				
				for (int i = 0; i < selected.size(); i++) {
					if(selected.get(i) instanceof GraphNode){
						GraphNode gNode = (GraphNode)selected.get(i);
						MyNode tempNode = model.getNode(gNode.getText());
						selectedConn = null;
						
						if(selectedNode != null && !selectedNode.equals(tempNode)){
							gNode.unhighlight();
							selectedNode = tempNode;
							graph.setSelection(new GraphItem[]{gNode});
						} else {
							selectedNode = tempNode;
						}
					} else if(selected.get(i) instanceof GraphConnection) {
						GraphConnection gNode = (GraphConnection)selected.get(i);
						selectedNode = null;
						
						if(selectedConn != null && !selectedConn.equals(gNode)){
							gNode.unhighlight();
							selectedConn = gNode;
							graph.setSelection(new GraphItem[]{gNode});
						} else {
							selectedConn = gNode;
						}					
						//((GraphConnection)selected.get(i)).unhighlight();
						//System.out.println("graph connection");
						//graph.setSelection(new GraphItem[]{});
					}
				}
			}
		});
		
		makeActions();
		contributeToActionBars();
		
		Activator.stateeditors.put(this.getEditorInput().getName(), this);
	}

	private LayoutAlgorithm setLayout() {
		LayoutAlgorithm layout;
		// layout = new
		// SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
		layout = new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
		// layout = new
		// GridLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
		// layout = new
		// HorizontalTreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
		// layout = new
		// RadialLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
		return layout;

	}
	
	@Override
	public void setFocus() {
	}
	
	private void contributeToActionBars() {
		IActionBars bars = getEditorSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(recordAction);
		manager.add(runAction);
		manager.add(renameAction);
		manager.add(delAction);
		manager.add(connectAction);
	}
	
	private void makeActions() {
		recordAction = new Action() {
			public void run() {
				System.out.println("recordAction executed");
				
				try {
					//toggle icon
					if(!recording){
						
						RecordDialog dialog = new RecordDialog(Display.getCurrent().getActiveShell(), model, IDAExec);
						
						if(dialog.open() == Window.OK){
							recordAction.setImageDescriptor(ImageDescriptor.createFromURL(makeIconFileURL("terminatedlaunch_obj.gif")));
							recording = true;
							
							IDAExec = dialog.ida;
							stateName = dialog.name;
							
							//send message to IDAPro to record the session
							Startup.send(IDAExec, "startRecording");
						}
						
					} else {
						recordAction.setImageDescriptor(ImageDescriptor.createFromURL(makeIconFileURL("term_restart.gif")));
						recording = false;
						
						//send message to IDAPro for all the actions and wait for the response
						Startup.send(IDAExec, "stopRecording");
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		};
		recordAction.setText("Record Debugging Actions");
		recordAction.setToolTipText("Record Debugging Actions");
				
		try {
			recordAction.setImageDescriptor(ImageDescriptor.createFromURL(makeIconFileURL("term_restart.gif")));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		runAction = new Action() {
			public void run() {
				System.out.println("Action 1 executed");
				
				if(currentStateNode != null){
					currentStateNode.getNode().setBorderColor(new Color(null, 0, 0, 0));
					currentStateNode.getNode().setBorderWidth(1);
				}
				
				ArrayList<MyAction> actions = selectedNode.actions;
				
				for (int i = 0; i < actions.size(); i++) {
					//send message to IDAPro to execute action
					MyAction action = actions.get(i);
					action.execute();
					
					//set as current state
					currentStateNode = selectedNode;
					currentStateNode.getNode().setBorderColor(new Color(null, 255, 0, 0));
					currentStateNode.getNode().setBorderWidth(3);
					currentStateNode.getNode().unhighlight();
				}
				
				selectedNode = null;
			}
		};
		runAction.setText("Run to Selected State");
		runAction.setToolTipText("Run to Selected State");
		try {
			runAction.setImageDescriptor(ImageDescriptor.createFromURL(makeIconFileURL("start_task.gif")));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		delAction = new Action() {
			public void run() {
				MessageDialog dialog = null;
				
				if(selectedNode != null){
					//ask are you sure?
					dialog = new MessageDialog(
					      null, "Confirm Delete", null, "Are you sure you want to delete " + selectedNode.name + "?",
					      MessageDialog.QUESTION,
					      new String[] {"Yes", "No"},
					      0);
				} else {
					if(selectedConn != null){
						//ask are you sure?
						dialog = new MessageDialog(
							      null, "Confirm Delete", null, "Are you sure you want to delete this connection?",
							      MessageDialog.QUESTION,
							      new String[] {"Yes", "No"},
							      0);
					}
				}
				
				if(dialog.open() == Window.OK){

					/*for (Iterator iterator = model.connections.iterator(); iterator.hasNext();) {
						MyConnection conn = (MyConnection) iterator.next();
						if (conn.getSource() == selectedNode) {
							conn.connection.dispose();
							model.connections.remove(conn);
						}
					}*/
					
					//need to check if selection is a connection or a node
					if(selectedNode != null){
						selectedNode.getNode().dispose();
						model.nodes.remove(selectedNode.name);
						
						graph.applyLayout();
						selectedNode = null;
					} else {
						//selectedConn.getNode().dispose();
						//model.nodes.remove(selectedConn.name);
						System.out.println("deleting connection");
						
						//find MyConnection that contains this connection
						for (int i = 0; i < model.connections.size(); i++) {
							MyConnection conn = model.connections.get(i);
							if(conn.connection == selectedConn){
								System.out.println("found and removed");
								model.connections.remove(i);
								break;
							}
						}
						//model.connections.remove();
						selectedConn.dispose();
						graph.applyLayout();
						selectedConn.unhighlight();
						selectedConn = null;
					}
				}	
			}
		};
		delAction.setText("Delete Selected State");
		delAction.setToolTipText("Delete Selected State");
		try {
			delAction.setImageDescriptor(ImageDescriptor.createFromURL(makeIconFileURL("delete.gif")));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		renameAction = new Action() {
			public void run() {
				System.out.println("renameAction executed");
				
				String name = selectedNode.name;
				InputDialog input = new InputDialog(Display.getCurrent().getActiveShell(), "Rename State", "", name, null);
				
				if(input.open() == Window.OK){
					String newName = input.getValue();
					//selectedNode.name = newName;
					//selectedNode.getNode().setText(newName);
					selectedNode = null;
					
					MyNode node = model.nodes.remove(name);
					node.name = newName;
					node.getNode().setText(newName);
					node.getNode().unhighlight();
					model.nodes.put(newName, node);
				}
			}
		};
		renameAction.setText("Rename Selected State");
		renameAction.setToolTipText("Rename Selected State");
		try {
			renameAction.setImageDescriptor(ImageDescriptor.createFromURL(makeIconFileURL("font.gif")));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		connectAction = new Action() {
			public void run() {
				System.out.println("connectAction executed");
				
				//ConnectionDialog dialog = new ConnectionDialog(Display.getCurrent().getActiveShell(), model, "Connect Two States");
				ConnectionDialog dialog = new ConnectionDialog(Display.getCurrent().getActiveShell(), model);
				
				if(dialog.open() == Window.OK){
					String source = dialog.source;
					String target = dialog.target;
					
					MyNode sourceNode = model.nodes.get(source);
					MyNode targetNode = model.nodes.get(target);
					
					MyConnection connect = new MyConnection("1", "1", ZestStyles.CONNECTIONS_DIRECTED, sourceNode, targetNode, viewer.getGraphControl());
					model.connections.add(connect);
					
					graph.applyLayout();
				}
			}
		};
		connectAction.setText("Connect Two States");
		connectAction.setToolTipText("Connect Two States");
		//try {
			//connectAction.setImageDescriptor(ImageDescriptor.createFromURL(makeIconFileURL("disconnect_co.gif")));
			connectAction.setImageDescriptor(Activator.getImageDescriptor("icons/disconnect_co.gif"));
		//} catch (MalformedURLException e) {
		//	e.printStackTrace();
		//}	
	}
	
	public static URL makeIconFileURL(String name) throws MalformedURLException {
		String pathSuffix= "icons/"; //$NON-NLS-1$ 
		URL iconBaseURL= new URL(Activator.getDefault().getBundle().getEntry("/"), pathSuffix); //$NON-NLS-1$		
		return new URL(iconBaseURL, name);
	}
	
	public void stopRecord(){
		//create state
		MyNode node = new MyNode(stateName, viewer.getGraphControl());
		model.nodes.put(stateName, node);
		
		//add actions
		ArrayList<MyAction> myActions = new ArrayList<MyAction>();
		/*for (int j = 0; j < actions.getLength(); j++) {
			Element el2 = (Element)actions.item(j);
			MyAction action = new MyAction(el2.getAttribute("address"), el2.getAttribute("command"), el2.getAttribute("exe"));
			myActions.add(action);
		}*/
		node.actions = myActions;
	}
	
	public static void receiveMessage(String receivedMessage){
		System.out.println("output actions, yay!");
		if (receivedMessage.startsWith("outputActions")) {

			Display disp = Display.getCurrent();
			final Display disp2 = PlatformUI.getWorkbench().getDisplay();
			Display disp3 = Activator.getDefault().getWorkbench().getDisplay();
			
			//TODO getname from message
			String editorName = "mariposa.state";
			StateEditor editor = Activator.stateeditors.get(editorName);

			editor.stopRecord();
			
		}
	}
}
