package org.eclipse.zest.custom.sequence.assembly.editors;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
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
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.examples.navigator.actions.CollapseAllAction;
import org.eclipse.ui.examples.navigator.actions.CollapseAllActivationsAction;
import org.eclipse.ui.examples.navigator.actions.ExpandAllAction;
import org.eclipse.ui.examples.navigator.actions.ExpandAllActivationsAction;
import org.eclipse.ui.examples.navigator.actions.ExportImageAction;
import org.eclipse.ui.examples.navigator.actions.FocusInAction;
import org.eclipse.ui.examples.navigator.actions.FocusUpAction;
import org.eclipse.ui.examples.navigator.actions.RemoveFromDiagramAction;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.zest.custom.sequence.assembly.Activator;
import org.eclipse.zest.custom.sequence.assembly.Startup;
import org.eclipse.zest.custom.sequence.assembly.comments.CommentView;
import org.eclipse.zest.custom.sequence.assembly.editors.LoopFinder.LoopRegion;
import org.eclipse.zest.custom.sequence.assembly.preferences.PreferenceConstants;
import org.eclipse.zest.custom.sequence.widgets.Activation;
import org.eclipse.zest.custom.sequence.widgets.Call;
import org.eclipse.zest.custom.sequence.widgets.Lifeline;
import org.eclipse.zest.custom.sequence.widgets.Message;
import org.eclipse.zest.custom.sequence.widgets.MessageGroup;
import org.eclipse.zest.custom.sequence.widgets.Return;
import org.eclipse.zest.custom.sequence.widgets.SequenceChartBuilder;
import org.eclipse.zest.custom.sequence.widgets.UMLItem;
import org.eclipse.zest.custom.sequence.widgets.UMLSequenceChart;
import org.eclipse.zest.custom.uml.viewers.BreadCrumbViewer;
import org.eclipse.zest.custom.uml.viewers.ISequenceViewerListener;
import org.eclipse.zest.custom.uml.viewers.SequenceViewerEvent;
import org.eclipse.zest.custom.uml.viewers.SequenceViewerGroupEvent;
import org.eclipse.zest.custom.uml.viewers.SequenceViewerRootEvent;
import org.eclipse.zest.custom.uml.viewers.UMLSequenceViewer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cs.uvic.ca.idaplugin.comm.DisassemblerSocketComms;

/**
 * @author jbaldwin
 */
public class DynamicAssemblySequenceEditor extends EditorPart {

	protected UMLSequenceViewer viewer;
	private ThumbnailOutlinePage fOutlinePage;
	private Composite control;
	protected BreadCrumbViewer breadcrumb;
	protected String methodToExpand = "";
	protected File inputFile = null;
	private ExportImageAction exportImage;
	private CollapseAllAction collapseAll;
	private CollapseAllActivationsAction collapseAllAction;
	private ExpandAllAction expandAll;
	private RemoveFromDiagramAction remove;
	private ExpandAllActivationsAction expandAllAction;
	private FocusInAction focusIn;
	private FocusUpAction focusUp;
	private Action cloneAction;
	protected BreadCrumbSelectionListener breadcrumbListener = new BreadCrumbSelectionListener();
	protected SequenceViewerListener sequenceListener = new SequenceViewerListener();
	private AssemblySequenceContentProvider ascp;
	private Document document;
	protected IPath localPath;
	protected SequenceChartBuilder builder;
	static final String DATE_FORMAT_NOW = "yyyyMMdd-HHmmss";
	protected HashMap<String, DynamicNodeProxy> functionList = new HashMap<String, DynamicNodeProxy>();
	protected HashMap<String, DynamicCallProxy> callList = new HashMap<String, DynamicCallProxy>();
	protected HashMap<String, FunctionEntryPointProxy> functionEntryList = new HashMap<String, FunctionEntryPointProxy>();
	protected LinkedList<DynamicCallProxy> orderedCallList = new LinkedList<DynamicCallProxy>();
	// private HashMap<String, String> prevCallList = new HashMap<String,
	// String>();
	// Associated communications interface to disassembler
	public static final String DEFAULT_PORT = "-p:";
	private DisassemblerSocketComms disassemblerIF;
	private Thread disassemblerThread = null;
	protected String message;
	public String localFile = "";
	protected DynamicNodeProxy currentNodeProxy;
	protected int index = 0;
	protected static String prefixString = "debug";
	IPreferenceStore store = Activator.getDefault().getPreferenceStore();

	public String saveLocation = "";
	protected boolean dirty = true;
	private boolean firstCall = true;
	Display display = Activator.getDefault().getWorkbench().getDisplay();
	private String currentRoot = "";
	private int loopCount = 0;
	private int loopOffsetCount = 0;

	public SequenceChartBuilder getBuilder() {
		return builder;
	}

	/**
	 * Listener for adjusting the breadcrumb for when the root changes.
	 * 
	 * @author jbaldwin
	 */

	private final class SequenceViewerListener implements ISequenceViewerListener {
		public void elementCollapsed(SequenceViewerEvent event) {
			// change saved info
			dirty = true;
		}

		public void elementExpanded(SequenceViewerEvent event) {
			// change saved info
			dirty = true;
		}

		public void groupCollapsed(SequenceViewerGroupEvent event) {
		}

		public void groupExpanded(SequenceViewerGroupEvent event) {
		}

		public void rootChanged(SequenceViewerRootEvent event) {
			if (breadcrumb != null) {
				Activation a = event.getSequenceViewer().getChart().getRootActivation();
				// TODO fix this when we select User, but fucks stuff up if it
				// happens on load
				if (!a.getLifeline().getText().equals("User")) {
					if (breadcrumb != null && a != breadcrumb.getInput()) {
						// TODO fix this
						breadcrumb.setInput(a);
						breadcrumb.refresh();
					}
				}
			}
			dirty = true;
		}
	}

	/**
	 * Listener for adjusting the root when the breadcrumb is selected.
	 * 
	 * @author Del Myers
	 */

	private final class BreadCrumbSelectionListener implements ISelectionChangedListener {
		public void selectionChanged(SelectionChangedEvent event) {
			if (event.getSelection() instanceof IStructuredSelection) {
				Object o = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (viewer != null && o instanceof Activation) {
					viewer.getChart().setRootActivation((Activation) o);
					breadcrumb.setInput(o);
					breadcrumb.refresh();
				}
			}
		}
	}

	private class NavigateToCodeListener extends MouseAdapter {
		@Override
		public void mouseDoubleClick(MouseEvent e) {
			Object element = viewer.elementAt(e.x, e.y);
			UMLItem[] items = viewer.getChart().getSelection();
			CommentView.selectedItem = items[0];
			
			if (element != null && element.equals("Start")) {
				element = "User";
			}
			if (element == null || element instanceof String) {
				if (items[0] instanceof Activation) {
					DynamicNodeProxy dnp = functionList.get(element);
					// function address
					Long position = Long.parseLong(dnp.address, 16);
					Startup.send(dnp.module, "updateCursor " + Long.toString(position));// +
					// " "
					// +
					// dnp.module);
				} else {
					if(items[0] instanceof MessageGroup){
						MessageGroup mg = (MessageGroup)items[0];
						CommentView.changeURLID("Loop:" + mg.getActivation().getLifeline().getParent().getText() + "," + mg.getActivation().getLifeline().getText());
					} else {
						if(items[0] instanceof Call){
							Call call = (Call) items[0];
							Activation a = call.getSource();
							
							CommentView.changeURLID(a.getLifeline().getParent().getText() + ":" + a.getLifeline().getText() + "->" + call.getTarget().getLifeline().getParent().getText() + ":" + call.getTarget().getLifeline().getText());
							
							/*
							 * Doesn't work, dnp is null
							 * DynamicNodeProxy dnp = functionList.get(a.getLifeline().getText());
							// currName + name
							DynamicCallProxy dcp = callList.get(call.getSource().getLifeline().getText() + element + " " + dnp.module);
							// call address
							Long position = Long.parseLong(dcp.callAddress, 16);
							Startup.send(dnp.module, "updateCursor " + Long.toString(position));*/
							// dnp.module);
							
							
						} else {
							//element is a top level lifeline
							//LifelineProxy llp = (LifelineProxy) ascp.lifelineList.get(element);
							Lifeline line = (Lifeline)items[0];
							CommentView.changeURLID(line.getParent().getText() + ":" + line.getText());
						}
					}
				}
			}
		}
	}

	public void receiveMessage(String messageStr) {
		dirty = true;
		message = messageStr;
		addMessage();
	}

	private void addMessage() {
		if (message.contains("rename")) {
			String[] lines = message.split("\t");
			final String original = lines[1];
			final String newname = lines[2];
			final String module = lines[3];

			/*
			 * if(getPartName().equals(original)){ setPartName(newname);
			 * //ascp.method = newname; }
			 */

			display.asyncExec((new Runnable() {
				public void run() {
					UMLItem[] items = viewer.getChart().getItems();
					Call call = null;

					for (int i = 0; i < items.length; i++) {
						UMLItem item = items[i];
						if (item instanceof Lifeline) {
							Lifeline line = (Lifeline) item;
							Lifeline parent = line.getParent();
							if (parent != null) {
								String thismodule = parent.getText();
								if (line.getText().equals(original) && thismodule.equals(module)) {
									line.setText(newname);
									DynamicNodeProxy dnp = functionList.get(original);
									dnp.name = newname;
									functionList.remove(original);
									functionList.put(newname, dnp);
								}
							}
						} else {
							if (item instanceof Activation) {
								Activation act = (Activation) item;
								Lifeline line = act.getLifeline();
								Lifeline parent = line.getParent();
								if (parent != null) {
									String thismodule = parent.getText();
									if (act.getText().equals(original) && thismodule.equals(module)) {
										act.setText(newname);
									}
								}
							} else {
								if (item instanceof Call) {
									call = (Call) item;
									Activation act = call.getTarget();
									Activation source = call.getSource();
									Lifeline parent = act.getLifeline().getParent();
									String thismodule = parent.getText();
									if (call.getText().equals(original) && thismodule.equals(module)) {
										call.setText(newname);
										DynamicCallProxy dcp = callList.get(source.getLifeline().getText() + original);
										dcp.name = newname;
										callList.remove(source.getLifeline().getText() + original);
										callList.put(source.getLifeline().getText() + newname, dcp);
									}
								}
							}
						}
					}
					viewer.getChart().refresh();
				}
			}));

		} else {
			if (message.contains("innerloop")) {
				String[] tokens = message.split(" ");
				String name = tokens[1];
				String externalfile = tokens[2];
				String module = tokens[3];
				replaceInOrderedCallList(name, externalfile, module);
			} else {

				// Display display =
				// Activator.getDefault().getWorkbench().getDisplay();
				display.syncExec(new Runnable() {
					public void run() {
						message = message.replaceAll("!jeb!", "\n");
						// System.out.println("the new message \n" + message);

						boolean addToRoot = false;
						boolean expanded = store.getString(PreferenceConstants.P_DEBUG).equals(PreferenceConstants.P_DEBUG_STEP);

						message = message.replaceFirst("debug", "");
						if (message.contains("expandcall")) {
							expanded = true;
							message = message.replaceFirst("expandcall", "");
						}
						if (message.contains("addtoroot")) {
							addToRoot = true;
							message = message.replaceFirst("addtoroot", "");
						}

						// dumpFunction(f) should produce
						// > 0: 1001630 sub_1001630
						// -1: 100164A 100111C CharNextW USER32.dll
						// < 0

						String[] lineList = message.split("\r\n|\r|\n");

						// String[] tokens = callLine.split("\\s");
						String[] tokens = message.split("\\s");
						String thisIndex = tokens[1].replace(":", "");

						// TODO send current address for the call address
						String functionAddress = tokens[2];
						String callAddress = tokens[2];

						String name = tokens[3];
						if (name.startsWith("Stack")) {
							name = name.replaceFirst("_", "[");
							name = name.replaceFirst("_", "]");
							System.out.println("name " + name);
						}
						localFile = tokens[4];
						String externalFile = "";

						// > 0: 1001630 sub_1001630 USER32.dll
						if (tokens.length == 6) {
							externalFile = tokens[5].toUpperCase();
							if (externalFile.startsWith("Stack")) {
								externalFile = externalFile.replaceFirst("_", "[");
								externalFile = externalFile.replaceFirst("_", "]");
								System.out.println("externalFile " + externalFile);
							} else {
								externalFile = externalFile.replace("_", ".");
							}
						}

						String currName = "";
						if (addToRoot || firstCall) {
							currName = "User";
							firstCall = false;
						} else {
							if (currentNodeProxy != null) {
								currName = currentNodeProxy.name;
							}
						}

						Activation curr = getBuilder().getCurrentActivation();

						if (curr != null && curr.getLifeline().getText().equals(name)
								&& (curr.getLifeline().getParent().getText().equals(externalFile) || (curr.getLifeline().getParent().getText().equals(localFile) && externalFile.equals("")))) {

							return;
						}

						if (name.length() > 0) {
							
							//build loop array
							String[] test = new String[orderedCallList.size()];
							for (int i = 0; i < orderedCallList.size(); i++) {
								DynamicCallProxy el = (DynamicCallProxy)orderedCallList.get(i);
								String dname = el.name; 
								test[i] = dname;
							}
							
							DynamicCallProxy dcp = new DynamicCallProxy(name, callAddress, externalFile, functionAddress, thisIndex, localFile, addToRoot, expanded);
							callList.put(currName + name, dcp);
							orderedCallList.add(dcp);

							// String name, String address, String index, String
							// stereotype
							currentNodeProxy = new DynamicNodeProxy(name, callAddress, Integer.toString(++index), "", externalFile, expanded, localFile);
							if (functionList.get(name) == null) {
								functionList.put(name, currentNodeProxy);
							}

							System.out.println("adding to diagram: " + name);
							
							Call call = null;
							Activation target = null;
							
							if (addToRoot) {
								// need to make call from the User lifeline
								getBuilder().setContext(viewer.getChart().getRootActivation());
							}

							// TODO check for loops!
							Return ret = getBuilder().makeReturn(name, name);
							if(ret == null){
								call = getBuilder().makeCall(name, name);
								target = call.getTarget();
							} else {
								target = ret.getTarget();
							}
							
								

							if (externalFile.length() == 0 || localFile.toUpperCase().equals(externalFile.toUpperCase())) {
								Lifeline user = builder.setContainer(target.getLifeline().getText(), localFile);
								target.getLifeline().setImage(AssemblySequenceLabelProvider.localimage);
								//user.setBackground(new Color(Display.getCurrent(), 255, 255, 255));
								user.setBackground(new Color(Display.getCurrent(), 255, 200, 100));
							} else {
								Lifeline user = builder.setContainer(target.getLifeline().getText(), externalFile);
								target.getLifeline().setImage(AssemblySequenceLabelProvider.externalimage);
								user.setBackground(new Color(Display.getCurrent(), 255, 200, 100));
							}

							//reset tempname to the correct exe name
							UMLItem[] items = viewer.getChart().getItems();
							if(target.getLifeline().getText().equals("tempname")){
								target.getLifeline().setText(localFile);
							}
							for (int i = 0; i < items.length; i++) {
								UMLItem item = items[i];
								if(item instanceof Call || item instanceof Message){
									if(item.getText().equals("tempname")){
										item.setText(localFile);	
									}
								}
							}
							// user.setClassStyle(Lifeline.PACKAGE);

							// expandCall(expanded, call);

							getBuilder().setContext(target);
							if (expanded) {
								viewer.getChart().reveal(target);
								// FigureCanvas canvas = (FigureCanvas)
								// viewer.getChart().getSequenceControl();
								// canvas.scrollTo(0, 0);
							}
							
							List<LoopRegion> regions = LoopFinder.findLoops(test);
							items = viewer.getChart().getItems();
							
							//check iterations of area, if greater than prefCount, don't put in the diagram
							for (int i = 0; i < regions.size(); i++) {
								
								int start = regions.get(i).offset;
								int end = (regions.get(i).iterations * regions.get(i).length) + regions.get(i).offset;
								
								int calllength = Integer.parseInt(LoopFinder.lengths.get("" + regions.get(i).offset));						
								int realIterations = (end - start)/calllength;
								System.out.println("real iteration " + realIterations);
								
								if(realIterations == Integer.parseInt(store.getString(PreferenceConstants.P_PREF_COUNT))){
									//collapse the calls and delete calls from the list
									Activation firstAct = null;
									String msg = "stopLoop ";
									
									int startCount = 0;
									for (int j = 0; j < items.length; j++) {
										//break call chain
										if(items[j] instanceof Activation){
											Activation act = (Activation) items[j];
											System.out.println(act.getLifeline().getText());
											if(startCount > start  + loopOffsetCount){// + calllength){
											//if(startCount  > start){// + calllength){
												if(firstAct == null){
													firstAct = act;
												}
												Message[] mess = act.getMessages();
												for (int j1 = 0; j1 < mess.length; j1++) {
													act.removeMessage(mess[j1]);
												}
											} 
											startCount++;
										}
									}
									
									System.out.println("start count " + startCount);

									loopOffsetCount += (calllength * (realIterations));
									System.out.println("loop offset count " + loopOffsetCount);
									
									builder.setContext(firstAct);
									builder.openGroup("");
									
									//set loop to true for the calls from offset in the object for the trace file
									int j = start + calllength + 1;
									while(j < orderedCallList.size()){
										orderedCallList.remove(j);
									}
									
									String exFile = "";
									
									for (j = start + 1; j <= start + calllength; j++) {
										DynamicCallProxy d = orderedCallList.get(j);
										d.inLoop = true;
										d.looplength = calllength;
										orderedCallList.remove(j);
										orderedCallList.add(j, d);
										msg += d.functionAddress + " ";
										
										exFile = d.externalFile;
										if(d.externalFile.equals("")){
											exFile = d.module;
										}
										
										//have to remake the calls for the group to show up
										Call mess = builder.makeCall(d.name, d.name);
										
										if (exFile.length() == 0 || localFile.equals(exFile)) {
											Lifeline user = builder.setContainer(mess.getTarget().getLifeline().getText(), localFile);
											mess.getTarget().getLifeline().setImage(AssemblySequenceLabelProvider.localimage);
											user.setBackground(new Color(Display.getCurrent(), 255, 200, 100));
										} else {
											Lifeline user = builder.setContainer(mess.getTarget().getLifeline().getText(), exFile);
											mess.getTarget().getLifeline().setImage(AssemblySequenceLabelProvider.externalimage);
											user.setBackground(new Color(Display.getCurrent(), 255, 200, 100));
										}
										
										builder.setContext(mess.getTarget());
										
										if (d.expanded) {
											viewer.getChart().reveal(mess.getTarget());
										}
									}
									builder.setContext(firstAct);
									builder.closeGroup();
									
									//send message to idapro not to record this loop anymore
									Startup.send(exFile, msg);	
								}
							}
						}
					}

				});
			}
		}
	}

	protected void expandCall(boolean expanded, Call call) {
		if (expanded) {
			if (store.getString(PreferenceConstants.P_DEBUG).equals(PreferenceConstants.P_DEBUG_STEP)) {
				call.getSource().setExpanded(true);
			} else {
				// if (firstLine.contains("expand call")) {

				/*
				 * Call anotherCall = call; while(anotherCall != null){
				 * anotherCall.getSource().setExpanded(true); anotherCall =
				 * anotherCall.getSource().getSourceCall(); }
				 */
				// }
			}
		}
	}

	@Override
	public void doSave(IProgressMonitor arg0) {

		display.syncExec(new Runnable() {
			public void run() {
				Shell s = new Shell(display);
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
				String timestamp = sdf.format(cal.getTime());

				s.setSize(400, 400);
				FileDialog fd = new FileDialog(s, SWT.SAVE);
				fd.setText("Save");
				fd.setFilterPath(Activator.getDefault().dynamicEditor.saveLocation);
				String[] filterExt = { "*.trace" };
				fd.setFilterExtensions(filterExt);
				fd.setFileName(prefixString + "-" + timestamp);
				String selected = fd.open();

				if (selected != null) {
					System.out.println("saving...");
					save(selected);
				}
			}

		});
	}
	
	protected void save(String selected) throws TransformerFactoryConfigurationError {
		// don't save
		// write out persistence to a hidden file
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IPath loc = Path.fromOSString(selected);// Path.fromOSString(saveLocation
		// + prefixString + "-" +
		// timestamp + ".trace");
		File secFile = new File(loc.toString());
		try {
			secFile.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// write out persistence to a hidden file
		// IPath loc2 = Path.fromOSString(localPath.toString() + File.separator
		// + inputFile.getName() + ".dat");
		String path = loc.toString() + ".dat";
		File persFile = new File(path);
		// String workPath = ws.getRoot().getLocationURI().toString();
		// workPath = workPath.replace("file:/", "");
		// workPath = workPath.replace("%20", " ");
		// File persFile = new File(workPath + File.separator +
		// loc2.toString());
		// File secFile = new File(loc2.toString());
		// File thirdFile = new File(inputFile.getAbsolutePath() + ".dat");

		// need to replace persistance file, don't need to save info for other
		// traces
		if (persFile.exists()) {
			persFile.delete();
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		Element rootNode2 = null;

		try {
			builder = factory.newDocumentBuilder();
			DynamicNodeProxy dnp = functionList.get("User");
			if(dnp == null){
				dnp = (DynamicNodeProxy) functionList.values().toArray()[0];
			}
			Element fnode = null;
			Document doc2 = null;

			try {
				persFile.createNewFile();
				doc2 = builder.newDocument();
				rootNode2 = doc2.createElement("sequence");
				fnode = doc2.createElement("function");

				// this is the first of this diagram
				fnode.setAttribute("name", "User");
				fnode.setAttribute("externalfile", "User");
				rootNode2.appendChild(fnode);

				Element rnode = doc2.createElement("root");
				rnode.setAttribute("externalfile", "");
				rnode.setAttribute("name", "User");
				fnode.appendChild(rnode);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				Document doc = builder.newDocument();// builder.parse(secFile);
				// Element rootNode = doc.getDocumentElement();
				// Element rootNode = doc.createElement("sourcecode");
				// rootNode.setAttribute("filename", "blah");
				Element rootNode = doc.createElement("dynamicTrace");
				rootNode.setAttribute("filename", localFile);

				Activation root = getViewer().getChart().getRootActivation();
				Object obj = root.getData();
				// String externalFile = ((NodeProxy)obj).externalFile;

				Element entryNode = doc.createElement("functionEntryPoint");
				FunctionEntryPointProxy fepp = functionEntryList.get("");
				entryNode.setAttribute("index", fepp.index);
				entryNode.setAttribute("name", fepp.name);
				entryNode.setAttribute("address", fepp.address);
				entryNode.setAttribute("module", fepp.module);
				rootNode.appendChild(entryNode);
				Element fel = doc.createElement("function");
				
				if(viewer.getChart().getRootActivation().getText().equals("User")){
					// DynamicNodeProxy dnp = functionList.get("User");
					currentNodeProxy = dnp;
					fel.setAttribute("address", dnp.address);
					fel.setAttribute("index", dnp.index);
					fel.setAttribute("name", "User");
					fel.setAttribute("stereotype", dnp.stereotype);
					fel.setAttribute("externalfile", "User");
					fel.setAttribute("module", dnp.module);
					entryNode.appendChild(fel);
				}
				
				if (dnp.expanded) {
					Element enode = doc2.createElement("expanded");
					enode.setAttribute("externalfile", "");
					enode.setAttribute("name", dnp.name);
					fnode.appendChild(enode);
				}

				// from before we had the user initiate the calls
				/*
				 * DynamicNodeProxy dnp = functionList.get(methodToExpand);
				 * currentNodeProxy = dnp; fel.setAttribute("address",
				 * dnp.address); fel.setAttribute("index", dnp.index);
				 * fel.setAttribute("name", methodToExpand);
				 * fel.setAttribute("stereotype", dnp.stereotype);
				 * entryNode.appendChild(fel);
				 */

				UMLItem[] items1 = getViewer().getChart().getItems();
				//UMLItem[] items1 = getViewer().getChart().getVisibleItems();
				Element fel2 = null;

				for (int i = 0; i < items1.length; i++) {
					UMLItem item = items1[i];
					if (item instanceof Activation && item.getData("todelete") == null) {
						Activation act = (Activation) item;
						Message[] mess = act.getMessages();
						Activation[] acts =  act.getLifeline().getActivations();
							
						for (int j = 0; j < acts.length; j++) {
							if(acts[j] == act){
								break;
							}
						}
						
						// obj = act.getData();
						// externalFile = ((NodeProxy)obj).externalFile;
						Element el3;

						if (act.getText().equals("Start")) {
							dnp = functionList.get("User");
							fel2 = fel;
						} else {
							dnp = functionList.get(act.getText());

							boolean found = false;

							// need to check that function doesn't already exist
							NodeList nodes = entryNode.getElementsByTagName("function");
							for (int j = 0; j < nodes.getLength(); j++) {
								Element node = (Element) nodes.item(j);
								String name = node.getAttribute("name");
								if (name.equals(act.getText()) && node.getAttribute("act").equals(Integer.toString(j))) {
									fel2 = node;
									found = true;
								}
							}

							Activation[] acts2 =  act.getLifeline().getActivations();
							
							int j1;
							for (j1 = 0; j1 < acts2.length; j1++) {
								if(acts2[j1] == act){
									break;
								}
							}
							
							if (!found) {
								fel2 = doc.createElement("function");
								fel2.setAttribute("address", dnp.address);
								fel2.setAttribute("index", dnp.index);
								fel2.setAttribute("stereotype", dnp.stereotype);
								fel2.setAttribute("externalfile", dnp.externalFile);
								fel2.setAttribute("module", dnp.module);
								// fel2.setAttribute("expanded", new
								// Boolean(dnp.expanded).toString());

								/*
								 * if(prevCallList.get(act.getText()) == null){
								 * fel2.setAttribute("externalfile", ""); } else
								 * { fel2.setAttribute("externalfile",
								 * prevCallList.get(act.getText())); }
								 */
								fel2.setAttribute("name", act.getText());
								fel2.setAttribute("act", Integer.toString(j1));
								entryNode.appendChild(fel2);

								if (dnp.expanded) {
									// Element func2 =
									// doc2.createElement("function");
									// func2.setAttribute("externalfile",
									// dnp.externalFile);
									// func2.setAttribute("name", dnp.name);
									Element enode = doc2.createElement("expanded");
									enode.setAttribute("name", dnp.name);
									enode.setAttribute("externalfile", dnp.externalFile);
									fnode.appendChild(enode);
									// func2.appendChild(enode);
									// rootNode2.appendChild(func2);
								}
							}
						}

						for (int j = 0; j < mess.length; j++) {
							Message thisMess = mess[j];
							Activation[] acts2 =  thisMess.getTarget().getLifeline().getActivations();
								
							int j1;
							for (j1 = 0; j1 < acts2.length; j1++) {
								if(acts2[j1] == thisMess.getTarget()){
									break;
								}
							}
							
							// could be more than one call to a function, should
							// index by function that calls it + callname
							DynamicCallProxy dcp = callList.get(act.getLifeline().getText() + thisMess.getText());
							
							if(dcp != null){
								el3 = doc.createElement("call");
								// el3.setAttribute("expanded",
								// Boolean.toString(act.isExpanded()));
								el3.setAttribute("calladdress", dcp.callAddress);
								el3.setAttribute("externalfile", dcp.externalFile);
								el3.setAttribute("functionaddress", dcp.functionAddress);
								el3.setAttribute("module", dcp.module);
								el3.setAttribute("act", Integer.toString(j1));
								el3.setAttribute("index", dcp.index);
								el3.setAttribute("name", thisMess.getText());
	
								// System.out.println(thisMess.getSource().getText());
								// if(thisMess.getSource().getText().equals("Start")){
								// fel.appendChild(el3);
								// } else {
								fel2.appendChild(el3);
								// }
	
								// currentNodeProxy = dnp;
							}

						}

						// }

					}
					if (item instanceof Call) {
						Call call = (Call) items1[i];
						// call.getSource();
					}
					if (item instanceof Lifeline) {
						Lifeline life = (Lifeline) items1[i];
					}
				}

				/*
				 * items1 = getViewer().getChart().getSelection();
				 * 
				 * for (int i = 0; i < items1.length; i++) { UMLItem item =
				 * items1[i]; if (item instanceof Activation) { Activation act =
				 * (Activation)item; obj = act.getData(); //externalFile =
				 * ((NodeProxy)obj).externalFile; Element el3 =
				 * doc.createElement("selection");
				 * //el3.setAttribute("externalfile", externalFile);
				 * el3.setAttribute("name", act.getText());
				 * fel.appendChild(el3); } }
				 */
				
				// Prepare the DOM document for writing
				Source source = new DOMSource(rootNode);

				Result result = new StreamResult(secFile);
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
					e.printStackTrace();
				}

				// Prepare the DOM document for writing
				source = new DOMSource(rootNode2);

				result = new StreamResult(persFile);
				// Write the DOM document to the file

				try {
					transformer = tf.newTransformer();
					transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
					transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
					transformer.setOutputProperty(OutputKeys.INDENT, "yes");
					transformer.transform(source, result);
				} catch (TransformerException e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				IFile newIFile = ws.getRoot().getFileForLocation(loc);
				try {
					newIFile.refreshLocal(IResource.DEPTH_ZERO, null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			} catch (NullPointerException npe) {
				// file is outside of workspace
			}
		} catch (ParserConfigurationException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}

	/*protected void save(String selected) throws TransformerFactoryConfigurationError {
		UMLItem[] items = viewer.getChart().getItems();
		LinkedList<Activation> acts = new LinkedList<Activation>();
		Activation lastAct = null;

		for (int i = 0; i < items.length; i++) {
			UMLItem item = items[i];
			if (item instanceof Activation) {
				Activation call = (Activation) item;
				acts.add(call);
				if (call.isVisible()) {
					lastAct = call;
				}
			}
		}

		// don't save
		// write out persistence to a hidden file
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IPath loc = Path.fromOSString(selected);// Path.fromOSString(saveLocation
		// + prefixString + "-" +
		// timestamp + ".trace");
		File secFile = new File(loc.toString());
		try {
			secFile.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;

		try {
			builder = factory.newDocumentBuilder();

			try {
				Document doc = builder.newDocument();// builder.parse(secFile);
				// Element rootNode = doc.getDocumentElement();
				// Element rootNode = doc.createElement("sourcecode");
				// rootNode.setAttribute("filename", "blah");
				Element rootNode = doc.createElement("dynamicTrace");
				boolean rootExpanded = viewer.getChart().getRootActivation().isExpanded();
				rootNode.setAttribute("rootexpanded", new Boolean(rootExpanded).toString());
				// rootNode.setAttribute("filename", localFile);

				Activation act = viewer.getChart().getRootActivation();
				Activation[] callacts = act.getLifeline().getActivations();
				String index = "0";
				for (int i = 0; i < callacts.length; i++) {
					if (callacts.equals(act)) {
						index = Integer.toString(i);
						break;
					}
				}
				Element el2 = doc.createElement("root");
				el2.setAttribute("externalfile", act.getLifeline().getParent().getText());
				el2.setAttribute("module", act.getLifeline().getParent().getText());
				el2.setAttribute("name", act.getLifeline().getText());
				el2.setAttribute("callindex", index);
				rootNode.appendChild(el2);

				callacts = lastAct.getLifeline().getActivations();
				index = "0";
				for (int i = 0; i < callacts.length; i++) {
					if (callacts.equals(lastAct)) {
						index = Integer.toString(i);
						break;
					}
				}
				Element el3 = doc.createElement("selection");
				el3.setAttribute("name", lastAct.getLifeline().getText());
				el3.setAttribute("externalfile", lastAct.getLifeline().getParent().getText());
				el3.setAttribute("module", lastAct.getLifeline().getParent().getText());

				if (lastAct.getSourceCall() != null) {
					el3.setAttribute("callingnode", lastAct.getSourceCall().getSource().getText());
				}

				el3.setAttribute("callindex", index);
				rootNode.appendChild(el3);
				boolean inloop = false;
				Element inloopEl = null;
				
				for (int i = 0; i < orderedCallList.size(); i++) {
					DynamicCallProxy dcp = orderedCallList.get(i);
					Element entryNode = doc.createElement("call");
					entryNode.setAttribute("name", dcp.name);
					entryNode.setAttribute("module", dcp.module);
					if (dcp.externalFile.length() == 0) {
						entryNode.setAttribute("externalfile", dcp.module);
					} else {
						entryNode.setAttribute("externalfile", dcp.externalFile);
					}
					entryNode.setAttribute("functionaddress", dcp.functionAddress);
					entryNode.setAttribute("calladdress", dcp.callAddress);
					entryNode.setAttribute("index", dcp.index);
					entryNode.setAttribute("root", new Boolean(dcp.addToRoot).toString());
					boolean expanded = acts.get(i).isExpanded();
					entryNode.setAttribute("expanded", new Boolean(expanded).toString());
					if (dcp.innerLoop) {
						Element innerloop = doc.createElement("innerloop");
						rootNode.appendChild(innerloop);
						innerloop.appendChild(entryNode);
					} else {
						if(dcp.inLoop){
							if(!inloop){
								inloopEl = doc.createElement("loop");
								inloopEl.setAttribute("length", Integer.toString(dcp.looplength));
								rootNode.appendChild(inloopEl);
							} 
							inloopEl.appendChild(entryNode);
							inloop = true;
						} else {
							inloop = false;
							rootNode.appendChild(entryNode);
						}
					}
				}

				// Prepare the DOM document for writing
				Source source = new DOMSource(rootNode);

				Result result = new StreamResult(secFile);
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
					e.printStackTrace();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				IFile newIFile = ws.getRoot().getFileForLocation(loc);
				try {
					newIFile.refreshLocal(IResource.DEPTH_ZERO, null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			} catch (NullPointerException npe) {
				// file is outside of workspace
			}
		} catch (ParserConfigurationException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}*/

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setInput(input);
		setSite(site);
		firePropertyChange(IWorkbenchPartConstants.PROP_INPUT);
		String tempPath = "";

		if (input instanceof FileEditorInput) {
			FileEditorInput fei = (FileEditorInput) input;
			tempPath = fei.getFile().getLocation().toPortableString();
			IPath path = ((FileEditorInput) input).getPath();
			String npath = path.toString().substring(0, path.toString().lastIndexOf("/"));
			localPath = Path.fromOSString(npath);
		} else {
			FileStoreEditorInput fei = (FileStoreEditorInput) input;
			tempPath = fei.getURI().toString();
			tempPath = tempPath.replace("file:/", "");
			tempPath = tempPath.replace("%20", " ");
			// String lPath = tempPath.substring(tempPath.lastIndexOf("$"));
			// tempPath = tempPath.substring(0, tempPath.lastIndexOf("$"));
			// lPath = lPath.substring(1);
			String lPath = tempPath.substring(0, tempPath.lastIndexOf("/"));
			localPath = Path.fromOSString(lPath);
		}

		inputFile = new File(tempPath);

		// directory to place saved file in
		// inputFile = new File(localPath.toOSString());

		// if(!tempPath.contains("$")){
		// String newLocation = tempPath;
		/*
		 * inputFile = new File(newLocation); document =
		 * XMLUtils.createSequenceFromXML(inputFile); Element rootElement =
		 * document.getDocumentElement(); methodToExpand =
		 * ((Element)rootElement.
		 * getElementsByTagName("function").item(0)).getAttribute("name");
		 */
		// methodToExpand = "test";
		// }else{
		// methodToExpand = tempPath.substring(tempPath.lastIndexOf('$') + 1);
		// String newLocation = tempPath.substring(0,
		// tempPath.lastIndexOf('$'));
		// inputFile = new File(newLocation);
		// }
		// setPartName("Test");
		// setPartName(methodToExpand);

	}

	public void setPartName(String name, Element firstFunction) {
		String rootName = "";
		String rootModule = "";
		
		// need to add sterotype user, and then call to this function
		if(firstFunction == null){
			rootName = "User";
		} else {
			rootName = firstFunction.getAttribute("name");
			rootModule = firstFunction.getAttribute("module");			
		}
		
		super.setPartName(name);
		this.callList.clear();
		this.functionList.clear();
		this.functionEntryList.clear();

		FunctionEntryPointProxy fepp = new FunctionEntryPointProxy("", "", "0", "");
		functionEntryList.put("", fepp);

		// String name, String address, String index, String stereotype
		currentNodeProxy = new DynamicNodeProxy(rootName, "", Integer.toString(++index), null, rootName, false, "");
		functionList.put(rootName, currentNodeProxy);
		methodToExpand = rootName;
		builder = new SequenceChartBuilder(viewer.getChart(), methodToExpand);
		
		if(rootName.equals("User")){
			viewer.getChart().getRootActivation().getLifeline().setClassStyle(Lifeline.ACTOR);
			Lifeline user = builder.setContainer(rootName, "");
			user.setText(rootName);
			user.setClassStyle(Lifeline.ACTOR);
		} else {
			//set package correctly as well
			viewer.getChart().getRootActivation().getLifeline().setImage(AssemblySequenceLabelProvider.localimage);
			Lifeline user = builder.setContainer(rootName, rootModule);
		}
		
		builder.turnOnRedraw();
		
		// > 0: 1001630 sub_1001630
		/*receiveMessage("debugexpandcall> -1:	FFFFFF	tempname	uphclean.exe");
		receiveMessage("debug> -1:	7C80E9DF	CreateMutexA	uphclean.exe	kernel32_dll");
		receiveMessage("debug> -1:	FFFFFF	tempname	uphclean.exe");
		receiveMessage("debug> -1:	7C802530	WaitForSingleObject	uphclean.exe	kernel32_dll");
		receiveMessage("debug> -1:	FFFFFF	tempname	uphclean.exe");
		receiveMessage("debug> -1:	381A9D	loc_381A9D	uphclean.exe	uphclean.exe");
		receiveMessage("debug> -1:	FFFFFF	tempname	uphclean.exe");
		receiveMessage("debug> -1:	7C8286EE	CopyFileA	uphclean.exe	kernel32_dll");
		receiveMessage("debug> -1:	FFFFFF	tempname	uphclean.exe");*/
		  
		
		/*receiveMessage("debugexpandcall> 0:	1001630	start calc.exe");
		  receiveMessage("debugexpandcall> 0:	1001	sub_1001s calc.exe");
		  receiveMessage("debugexpandcall> 0:	11	call1 calc.exe");
		  
		  receiveMessage("debugexpandcall> 0:	22	call2 calc.exe");
		  receiveMessage("debugexpandcall> 0:	33	call3 calc.exe");
		  receiveMessage("debugexpandcall> 0:	11	call1 calc.exe");
		  receiveMessage("debugexpandcall> 0:	22	call2 calc.exe");
		  receiveMessage("debugexpandcall> 0:	33	call3 calc.exe");
		  receiveMessage("debugexpandcall> 0:	11	call1 calc.exe");
		  receiveMessage("debugexpandcall> 0:	22	call2 calc.exe");
		  receiveMessage("debugexpandcall> 0:	33	call3 calc.exe");
		  receiveMessage("debugexpandcall> 0:	11	call1 calc.exe");
		  receiveMessage("debugexpandcall> 0:	22	call2 calc.exe");
		  receiveMessage("debugexpandcall> 0:	33	call3 calc.exe");
		  receiveMessage("debugexpandcall> 0:	11	call1 calc.exe");
		  

		  receiveMessage("debugexpandcall> 0:	66	call6 calc.exe");
		  receiveMessage("debugexpandcall> 0:	77	call7 calc.exe");
		  receiveMessage("debugexpandcall> 0:	88	call8 calc.exe");		  
		  receiveMessage("debugexpandcall> 0:	22	call9 calc.exe");
		  

		  receiveMessage("debugexpandcall> 0:	33	callj calc.exe");
		  receiveMessage("debugexpandcall> 0:	11	callb calc.exe");
		  receiveMessage("debugexpandcall> 0:	22	call9 calc.exe");
		  receiveMessage("debugexpandcall> 0:	33	callj calc.exe");
		  receiveMessage("debugexpandcall> 0:	11	callb calc.exe");
		  receiveMessage("debugexpandcall> 0:	22	call9 calc.exe");
		  receiveMessage("debugexpandcall> 0:	33	callj calc.exe");
		  receiveMessage("debugexpandcall> 0:	11	callb calc.exe");	  
		  receiveMessage("debugexpandcall> 0:	22	call9 calc.exe");
		  receiveMessage("debugexpandcall> 0:	33	callj calc.exe");
		  receiveMessage("debugexpandcall> 0:	11	callb calc.exe");
		  receiveMessage("debugexpandcall> 0:	22	call9 calc.exe");*/
		  
		//  receiveMessage("innerloop sub_1001s calc.exe calc.exe");
		  
		 // receiveMessage("debug> 0:	1001630	sub_1001630 calc.exe");
		 // receiveMessage("debug> 0:	1001	sub_1001s calc.exe");
		  
		 /* receiveMessage("debugaddtoroot> 0:	1001630	sub_1001630 calc.exe");
		  receiveMessage("debug\nadd to root\n> 0:	1001630	sub_jb \n-1:	100164A	100111C	stuff	stuff.dll \n< 0");
		  receiveMessage("debug> 0:	1001630	sub_1001633 calc.exe USER32.dll");
		  receiveMessage("debug> 0:	1001631	sub_1001634 calc.exe");
		  receiveMessage("debug> 0:	1001632	sub_1001635 calc.exe");
		  receiveMessage("debug> 0:	1001633	sub_1001636 calc.exe");*/
		 
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}
	
	public void setDirty(boolean dirty){
		this.dirty = dirty;
		firePropertyChange(PROP_DIRTY);
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		control = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 3;
		control.setLayout(layout);

		if(store.getString(PreferenceConstants.P_COMMENTS).equals(PreferenceConstants.P_RET_COMMENTS)){
			display.syncExec(
				  new Runnable() {
					    public void run(){
						    ProgressMonitorDialog dialog = new ProgressMonitorDialog(Startup.display.getActiveShell());
						    
							try {
								dialog.run(true, false, new IRunnableWithProgress(){
								    public void run(IProgressMonitor monitor) {
								        monitor.beginTask("Retrieving comment information from the server. This may take a few moments.", 300000);
								        monitor.done();
								    }
								});
							} catch (InvocationTargetException e) {
							} catch (InterruptedException e) {
							}
					    }
				  });
		}
		
		breadcrumb = new BreadCrumbViewer(control, SWT.BORDER);

		viewer = new UMLSequenceViewer(control, SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL);
		viewer.getChart().addMouseListener(new NavigateToCodeListener());
		viewer.getChart().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.addSequenceListener(new SequenceViewerListener());
		viewer.getChart().setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		MenuManager manager = new MenuManager("ASTSequenceEditor", "#ASTSequenceEditorContext");
		manager.setRemoveAllWhenShown(true);
		manager.removeAll();
		Menu contextMenu = manager.createContextMenu(viewer.getChart());
		manager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				Point location = Display.getCurrent().getCursorLocation();
				location = viewer.getChart().toControl(location);
				Object element = viewer.elementAt(location.x, location.y);
				Widget widget = viewer.getChart().getItemAt(location.x, location.y);
				Activation act = null;
				if (widget instanceof Activation) {
					act = (Activation) widget;
				}
				// if (element instanceof String) {
				// UMLItem[] items = viewer.getChart().getItems();
				// for (int i = 0; i < items.length; i++) {
				// if (items[i] instanceof Activation) {
				// Activation act = (Activation) items[i];
				String activationName = (String) element;

				if (act != null && act.getText().equals(activationName)) {
					if (!activationName.equals("Start")) {
						focusIn.setFocusElement(act);
						focusIn.setText("Focus On " + activationName);
						manager.add(focusIn);
					}

					/*
					 * expandAll.setText("Expand All Activations Under " +
					 * activationName); expandAll.setFocusElement(act);
					 * manager.add(expandAll);
					 */
					expandAllAction.setText("Expand All Activations Under " + activationName);
					expandAllAction.setFocusActivation(act);
					manager.add(expandAllAction);

					/*
					 * collapseAll.setText("Collapse All Activations Under " +
					 * activationName); collapseAll.setFocusElement(act);
					 * manager.add(collapseAll);
					 */
					collapseAllAction.setText("Collapse All Activations Under " + activationName);
					collapseAllAction.setFocusActivation(act);
					manager.add(collapseAllAction);

					// focus on caller
					if (!activationName.equals("Start")) {
						focusUp.setFocusElement(act);
						manager.add(focusUp);
						
						if(!(inputFile.getName().contains(".trace"))){
							remove.setText("Remove Everything Before " + activationName);
							remove.setFocusElement(act);
							manager.add(remove);
						}
					}
					
					// if (np.getCallingNode() != null &&
					// !viewer.getRootActivation().equals(ascp.rootNode))
					// { //
					// manager.add(focusUp);
					// }
				}
				// }

				// }
				// }
				manager.add(cloneAction);
				// }
				
				exportImage = new ExportImageAction(viewer);
				ImageDescriptor descriptor = Activator.getImageDescriptor("icons/image_obj.gif");
				exportImage.setText("Save Diagram to Image File");
				exportImage.setImageDescriptor(descriptor);
				manager.add(exportImage);
			}
		});
		viewer.getChart().setMenu(contextMenu);
		viewer.getChart().setCloneVisible(false);

		breadcrumb.setContentProvider(new DynamicSequenceBreadCrumbContentProvider(viewer));
		breadcrumb.setLabelProvider(new DynamicSequenceLabelProvider(viewer));
		breadcrumb.addSelectionChangedListener(new BreadCrumbSelectionListener());
		breadcrumb.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		makeActions();

	}

	private void makeActions() {
		focusIn = new FocusInAction(viewer);
		ImageDescriptor descriptor = Activator.getImageDescriptor("icons/in.gif");
		focusIn.setImageDescriptor(descriptor);

		focusUp = new FocusUpAction(viewer);
		focusUp.setText("Focus On Caller");
		descriptor = Activator.getImageDescriptor("icons/up.gif");
		focusUp.setImageDescriptor(descriptor);

		cloneAction = new Action("Toggle Clone Pane", IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				UMLSequenceChart chart = getViewer().getChart();
				if (!chart.isCloneVisible()) {
					setText("Hide Clone Pane");
				} else {
					setText("Show Clone Pane");
				}
				chart.setCloneVisible(!chart.isCloneVisible());
				setChecked(chart.isCloneVisible());
			}
		};
		getViewer().getChart().setCloneVisible(false);
		cloneAction.setChecked(!getViewer().getChart().isCloneVisible());
		if (getViewer().getChart().isCloneVisible()) {
			cloneAction.setText("Hide Clone Pane");
		} else {
			cloneAction.setText("Show Clone Pane");
		}

		/*
		 * collapseAll = new CollapseAllAction(viewer); descriptor =
		 * Activator.getImageDescriptor("icons/collapseAll.gif");
		 * collapseAll.setImageDescriptor(descriptor);
		 */
		collapseAllAction = new CollapseAllActivationsAction(viewer);
		descriptor = Activator.getImageDescriptor("icons/collapseAll.gif");
		collapseAllAction.setImageDescriptor(descriptor);

		/*
		 * expandAll = new ExpandAllAction(viewer); descriptor =
		 * Activator.getImageDescriptor("icons/expandAll.gif");
		 * expandAll.setImageDescriptor(descriptor);
		 */
		expandAllAction = new ExpandAllActivationsAction(viewer);
		descriptor = Activator.getImageDescriptor("icons/expandAll.gif");
		expandAllAction.setImageDescriptor(descriptor);
		
		remove = new RemoveFromDiagramAction(viewer, this);
		descriptor = Activator.getImageDescriptor("icons/delete.gif");
		remove.setImageDescriptor(descriptor);
	}

	public void setMethodToExpand(String name) {
		super.setPartName(name);
		methodToExpand = name;
		builder = new SequenceChartBuilder(viewer.getChart(), methodToExpand);
		builder.turnOnRedraw();
	}

	@Override
	public void setFocus() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if (IContentOutlinePage.class.equals(adapter)) {
			if (fOutlinePage == null) {
				fOutlinePage = new ThumbnailOutlinePage(viewer.getChart());
			}
			return fOutlinePage;
		}
		return super.getAdapter(adapter);
	}

	@Override
	public void dispose() {
		System.out.println("dispose!");
		Activator.getDefault().dynamicEditor = null;
		getViewer().removeSequenceListener(sequenceListener);
		breadcrumb.removeSelectionChangedListener(breadcrumbListener);
		super.dispose();
	}

	/**
	 * @return
	 */
	public UMLSequenceViewer getViewer() {
		return viewer;
	}

	public class FunctionEntryPointProxy {

		public String address;
		public String index;
		public String module;
		public String name;

		public FunctionEntryPointProxy(String name, String address, String index, String module) {
			this.address = address;
			this.index = index;
			this.module = module;
			this.name = name;
		}
	}

	public class DynamicNodeProxy {

		public String address;
		public String name;
		public String index;
		public String stereotype;
		public String externalFile;
		public boolean expanded = false;
		public String module;

		public DynamicNodeProxy(String name, String address, String index, String stereotype, String externalFile, boolean expanded, String module) {
			this.address = address;
			this.name = name;
			this.index = index;
			this.stereotype = stereotype;
			this.externalFile = externalFile;
			this.expanded = expanded;
			this.module = module;
		}
	}

	public class DynamicCallProxy {

		public int looplength;
		public String callAddress;
		public String externalFile;
		public String functionAddress;
		public String index;
		public String name;
		public String module;
		public boolean addToRoot;
		public boolean expanded;
		public boolean innerLoop = false;
		public boolean inLoop = false;
		public String act;
		
		public DynamicCallProxy(String name, String callAddress, String externalFile, String functionAddress, String index, String module, boolean addToRoot, boolean expanded) {
			this.callAddress = callAddress;
			this.externalFile = externalFile;
			this.functionAddress = functionAddress;
			this.index = index;
			this.name = name;
			this.module = module;
			this.addToRoot = addToRoot;
			this.expanded = expanded;
		}

		public DynamicCallProxy(String name, String callAddress, String externalFile, String functionAddress, String index, String module, boolean addToRoot, boolean expanded, boolean innerLoop) {
			this.callAddress = callAddress;
			this.externalFile = externalFile;
			this.functionAddress = functionAddress;
			this.index = index;
			this.name = name;
			this.module = module;
			this.addToRoot = addToRoot;
			this.expanded = expanded;
			this.innerLoop = innerLoop;
		}

		public boolean equals(Object obj) {
			return ((DynamicCallProxy) obj).equals(name) && ((DynamicCallProxy) obj).equals(module);
		}
	}

	private void replaceInOrderedCallList(final String name, final String externalFile, String module) {
		for (int i = 0; i < orderedCallList.size(); i++) {
			DynamicCallProxy dcp = orderedCallList.get(i);
			if (dcp.name.equals(name) && dcp.module.equals(module)) {
				dcp.innerLoop = true;
				orderedCallList.remove(i);
				orderedCallList.add(i, dcp);
			}
		}

		// paint current acts red
		// TODO take module into account
		display.asyncExec((new Runnable() {
			public void run() {
				UMLItem[] items = viewer.getChart().getItems();
				for (int i = 0; i < items.length; i++) {
					UMLItem item = items[i];
					if (item instanceof Activation) {
						Activation act = (Activation) item;
						if (act.getLifeline().getText().equals(name) && act.getLifeline().getParent().getText().equals(externalFile)) {
							act.setBackground(new Color(display, 255, 99, 71));
						}
					}
				}
			}
		}));
	}
	
	public void changeToCommentIcon(){
		//UMLItem item = viewer.getChart().getSelection()[0];
		UMLItem item = CommentView.selectedItem;
		int count = 0;
		
		if(item instanceof Call){
			Call call = ((Call)item);
			Activation a = call.getSource();
			//NodeProxy dcp = ascp.messageList.get(call.getTarget().getLifeline().getText());
			//count = CommentView.getCommentCount("DynamicCall" + call.getSource().getText() + "To" + call.getTarget().getText());
			count = CommentView.getCommentCount(a.getLifeline().getParent().getText() + ":" + a.getLifeline().getText() + "->" + call.getTarget().getLifeline().getParent().getText() + ":" + call.getTarget().getLifeline().getText());
		} else if (item instanceof Lifeline){
			Lifeline line = (Lifeline)item;
			//LifelineProxy dcp = ascp.lifelineList.get(line.getText() + "EEEE" + line.getParent().getText());
			//count = CommentView.getCommentCount("DynamicLifeline" + line.getText());
			count = CommentView.getCommentCount(line.getParent().getText() + ":" + line.getText());
		} else if(item instanceof MessageGroup){
			MessageGroup mg = (MessageGroup)item;
			//count = CommentView.getCommentCount("Loop" + mg.getActivation().getLifeline().getText());
			count = CommentView.getCommentCount("Loop:" + mg.getActivation().getLifeline().getParent().getText() + "," + mg.getActivation().getLifeline().getText());
			if(count == 1){
				mg.setText("1 comment");
			} else {
				mg.setText(count + " comments");
			}
			
			if(count == 0){
				//do nothing?
			} else if(count >= 25){
				mg.setForeground(ColorConstants.green);
			} else if(count >=10){
				mg.setForeground(ColorConstants.orange);
			} else {
				mg.setForeground(ColorConstants.red);
			}
		}
		
		if(count > 0){
			if(count >= 25){
				if(!isImageExternal(item.getImage()))
					item.setImage(AssemblySequenceLabelProvider.localcommentgreen);
				else
					item.setImage(AssemblySequenceLabelProvider.externalcommentgreen);
			} else if(count >= 10){
				if(!isImageExternal(item.getImage()))
					item.setImage(AssemblySequenceLabelProvider.localcommentgold);
				else
					item.setImage(AssemblySequenceLabelProvider.externalcommentgold);
			} else {
				if(!isImageExternal(item.getImage()))
					item.setImage(AssemblySequenceLabelProvider.localcommentred);
				else
					item.setImage(AssemblySequenceLabelProvider.externalcommentred);
			}
		} else {
			if(!isImageExternal(item.getImage()))
				item.setImage(AssemblySequenceLabelProvider.localimage);
			else
				item.setImage(AssemblySequenceLabelProvider.externalimage);
		}
		
		if(!(item instanceof Call)){
			viewer.getChart().markDirty();
		}
	}
	
	private boolean isImageExternal(Image img){
		if(img.equals(AssemblySequenceLabelProvider.externalcommentgreen) 
				|| img.equals(AssemblySequenceLabelProvider.externalcommentgold)
				|| img.equals(AssemblySequenceLabelProvider.externalcommentred) 
				|| img.equals(AssemblySequenceLabelProvider.externalimage)){
			
			return true;
		}
		return false;
	}
}
