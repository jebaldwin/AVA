package org.eclipse.zest.custom.sequence.assembly.editors;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Set;

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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.examples.navigator.actions.CollapseAllAction;
import org.eclipse.ui.examples.navigator.actions.ExpandAllAction;
import org.eclipse.ui.examples.navigator.actions.FocusInAction;
import org.eclipse.ui.examples.navigator.actions.FocusUpAction;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.zest.custom.sequence.assembly.Activator;
import org.eclipse.zest.custom.sequence.assembly.Startup;
import org.eclipse.zest.custom.sequence.assembly.model.XMLUtils;
import org.eclipse.zest.custom.sequence.assembly.preferences.PreferenceConstants;
import org.eclipse.zest.custom.sequence.widgets.Activation;
import org.eclipse.zest.custom.sequence.widgets.Lifeline;
import org.eclipse.zest.custom.sequence.widgets.Message;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

/**
 * @author jbaldwin
 */
public class TraceAssemblySequenceEditor extends AssemblySequenceEditor {
	
	/**
	 * Listener for adjusting the breadcrumb for when the root changes.
	 * 
	 * @author jbaldwin
	 */

	private final class SequenceViewerListener implements ISequenceViewerListener {
		public void elementCollapsed(SequenceViewerEvent event) {
			dirty = true;
			NodeProxy element = (NodeProxy) event.getElement();
			expandedList.put(element.externalFile + ":" + element.targetName, new Boolean(false));
			String sync = store.getString(PreferenceConstants.P_GENERAL);

			if (sync.equals(PreferenceConstants.P_GEN_ALL)) {
				// Object element = viewer.elementAt(event.x, event.y);
				// UMLItem[] items = viewer.getChart().getSelection();

				if (element instanceof NodeProxy) {
					// if(items[0] instanceof Activation){
					// function address
					Long position = Long.parseLong(element.functionaddress, 16);
					Startup.send(((NodeProxy) element).module, "updateCursor " + Long.toString(position));// +
																											// " "
																											// +
																											// ((NodeProxy)
																											// element).module);
				}
			}
		}

		public void elementExpanded(SequenceViewerEvent event) {
			// TODO make sure not in expand all action before doing any of these
			// things
			String val = store.getString(PreferenceConstants.P_STATIC);
			String sync = store.getString(PreferenceConstants.P_GENERAL);
			dirty = true;
			NodeProxy element = (NodeProxy) event.getElement();
			updateExpanded();

			// need to get the activation element

			if (val.equals(PreferenceConstants.P_STATIC_RET)) {
				if (Startup.disassemblerIF.idaOpen) {
					// waiting = true;
					NodeProxy node = (NodeProxy) event.getElement();

					// need to get the activation element
					UMLItem[] items = event.getViewer().getChart().getItems();
					for (int i = 0; i < items.length; i++) {
						UMLItem item = items[i];
						if (item instanceof Activation) {
							currentAct = (Activation) item;
							if (node.targetName.equals(currentAct.getText())) {

								try {
									// need to make sure nobody else is waiting
									// for
									// an answer from another diagram
									Startup.available.acquire();

									Activator.getDefault().tactiveeditor = (TraceAssemblySequenceEditor) editor;

									// need to retrieve calls from IDAPro
									Long position = Long.parseLong(node.functionaddress, 16);
									Startup.send(((NodeProxy) element).module, "retrieve " + Long.toString(position));// +
																														// " "
																														// +
																														// ((NodeProxy)
																														// element).module);

									break;
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}

				// while (waiting) {
				// System.out.println("waiting");

				// }
			}

			if (sync.equals(PreferenceConstants.P_GEN_ALL)) {

				// Object element = viewer.elementAt(event.x, event.y);
				// UMLItem[] items = viewer.getChart().getSelection();

				if (element instanceof NodeProxy) {
					// if(items[0] instanceof Activation){
					// function address
					try {
						if (element.externalFile.length() == 0) {
							Long position = Long.parseLong(element.functionaddress, 16);
							Startup.send(((NodeProxy) element).module, "updateCursor " + Long.toString(position));// +
																													// " "
																													// +
																													// ((NodeProxy)
																													// element).module);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			expandedList.put(element.externalFile + ":" + element.targetName, new Boolean(true));
		}

		public void groupCollapsed(SequenceViewerGroupEvent event) {
			dirty = true;
		}

		public void groupExpanded(SequenceViewerGroupEvent event) {
			dirty = true;
		}

		public void rootChanged(SequenceViewerRootEvent event) {
			if (breadcrumb != null) {
				dirty = true;
				Object a = event.getSequenceViewer().getRootActivation();
				if (breadcrumb != null && a != breadcrumb.getInput()) {
					breadcrumb.setInput(a);
				}
			}

			String sync = store.getString(PreferenceConstants.P_GENERAL);
			if (sync.equals(PreferenceConstants.P_GEN_ALL)) {
				NodeProxy element = (NodeProxy) event.getSequenceViewer().getRootActivation();
				// Object element = viewer.elementAt(event.x, event.y);
				// UMLItem[] items = viewer.getChart().getSelection();

				if (element instanceof NodeProxy) {
					// if(items[0] instanceof Activation){
					// function address
					Long position = Long.parseLong(element.functionaddress, 16);
					Startup.send(element.module, "updateCursor " + Long.toString(position));// +
																							// " "
																							// +
																							// element.module);
				}
			}
		}
	}

	public void receiveMessage(String messageStr) {
		dirty = true;
		message = messageStr;
		modifyMessages();
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
				if (viewer != null && o instanceof NodeProxy) {
					viewer.setRootActivation(o);
				} else {
					if (viewer != null && o instanceof Activation) {
						viewer.getChart().setRootActivation((Activation) o);
					}
				}
			}
		}
	}

	private class NavigateToCodeListener extends MouseAdapter {
		@Override
		public void mouseDoubleClick(MouseEvent e) {
			Object element = viewer.elementAt(e.x, e.y);
			UMLItem[] items = viewer.getChart().getSelection();

			if (element instanceof NodeProxy) {
				if (items[0] instanceof Activation) {
					// function address
					Long position = Long.parseLong(((NodeProxy) element).functionaddress, 16);
					Startup.send(((NodeProxy) element).module, "updateCursor " + Long.toString(position));// +
																											// " "
																											// +
																											// ((NodeProxy)
																											// element).module);
					// disassemblerIF.send("updateCursor " +
					// Long.toString(position));
				} else {
					// call address
					Long position = Long.parseLong(((NodeProxy) element).calladdress, 16);
					Startup.send(((NodeProxy) element).module, "updateCursor " + Long.toString(position));// +
																											// " "
																											// +
																											// ((NodeProxy)
																											// element).module);
					// disassemblerIF.send("updateCursor " +
					// Long.toString(position));
				}
			}
		}

		public void mouseDown(MouseEvent e) {
			String sync = store.getString(PreferenceConstants.P_GENERAL);

			if (sync.equals("always")) {

				Object element = viewer.elementAt(e.x, e.y);
				UMLItem[] items = viewer.getChart().getSelection();

				if (element instanceof NodeProxy) {
					Long position = Long.parseLong(((NodeProxy) element).functionaddress, 16);
					Startup.send(((NodeProxy) element).module, "updateCursor " + Long.toString(position));// +
																											// " "
																											// +
																											// ((NodeProxy)
																											// element).module);
				}
			}
		}
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
			/*
			 * DynamicNodeProxy dnp = functionList.get("User"); Element fnode =
			 * null;
			 * 
			 * try { persFile.createNewFile(); Document doc2 =
			 * builder.newDocument(); rootNode2 =
			 * doc2.createElement("sequence"); fnode =
			 * doc2.createElement("function");
			 * 
			 * //this is the first of this diagram fnode.setAttribute("name",
			 * "User"); rootNode2.appendChild(fnode);
			 * 
			 * Element rnode = doc2.createElement("root");
			 * rnode.setAttribute("externalfile", "");
			 * rnode.setAttribute("name", "User"); fnode.appendChild(rnode); }
			 * catch (IOException e1) { // TODO Auto-generated catch block
			 * e1.printStackTrace(); }
			 */

			try {
				Document doc = builder.newDocument();// builder.parse(secFile);
				// Element rootNode = doc.getDocumentElement();
				// Element rootNode = doc.createElement("sourcecode");
				// rootNode.setAttribute("filename", "blah");
				Element rootNode = doc.createElement("dynamicTrace");
				String localPackage = inputFile.getName().replace(".axml", ".exe");
				rootNode.setAttribute("filename", localPackage);

				Activation root = getViewer().getChart().getRootActivation();
				Object obj = root.getData();
				// String externalFile = ((NodeProxy)obj).externalFile;

				Element entryNode = doc.createElement("functionEntryPoint");
				/*
				 * FunctionEntryPointProxy fepp = functionEntryList.get("");
				 * entryNode.setAttribute("index", fepp.index);
				 * entryNode.setAttribute("name", fepp.name);
				 * entryNode.setAttribute("address", fepp.address);
				 * entryNode.setAttribute("module", fepp.module);
				 */
				rootNode.appendChild(entryNode);

				/*
				 * Element fel = doc.createElement("function");
				 * //DynamicNodeProxy dnp = functionList.get("User");
				 * currentNodeProxy = dnp; fel.setAttribute("address",
				 * dnp.address); fel.setAttribute("index", dnp.index);
				 * fel.setAttribute("name", "User");
				 * fel.setAttribute("stereotype", dnp.stereotype);
				 * entryNode.appendChild(fel);
				 * 
				 * if (dnp.expanded) { Element enode =
				 * doc.createElement("expanded");
				 * enode.setAttribute("externalfile", "");
				 * enode.setAttribute("name", dnp.name);
				 * fnode.appendChild(enode); }
				 */

				// from before we had the user initiate the calls
				/*
				 * DynamicNodeProxy dnp = functionList.get(methodToExpand);
				 * currentNodeProxy = dnp; fel.setAttribute("address",
				 * dnp.address); fel.setAttribute("index", dnp.index);
				 * fel.setAttribute("name", methodToExpand);
				 * fel.setAttribute("stereotype", dnp.stereotype);
				 * entryNode.appendChild(fel);
				 */
				NodeProxy dnp;

				UMLItem[] items1 = getViewer().getChart().getItems();
				Element fel2 = null;

				for (int i = 0; i < items1.length; i++) {
					UMLItem item = items1[i];
					if (item instanceof Activation) {
						Activation act = (Activation) item;
						Message[] mess = act.getMessages();

						// obj = act.getData();
						// externalFile = ((NodeProxy)obj).externalFile;
						Element el3;
						/*
						 * if (act.getText().equals("Start")) { dnp =
						 * functionList.get("User"); fel2 = fel; } else {
						 */
						dnp = ascp.actList.get(act.getText());// functionList.get(act.getText());

						if (dnp == null) {
							Set<String> set = renamedList.keySet();
							for (String string : set) {
								if (renamedList.get(string).equals(act.getText())) {
									dnp = ascp.actList.get(string);
								}
							}
						}
						/*
						 * if (dnp.expanded) { Element enode =
						 * doc.createElement("expanded");
						 * enode.setAttribute("externalfile", "");
						 * enode.setAttribute("name", dnp.name);
						 * fnode.appendChild(enode); }
						 */

						boolean found = false;

						// need to check that function doesn't already exist
						NodeList nodes = entryNode.getElementsByTagName("function");
						for (int j = 0; j < nodes.getLength(); j++) {
							Element node = (Element) nodes.item(j);
							String name = node.getAttribute("name");
							if (name.equals(act.getText())) {
								fel2 = node;
								found = true;
							}
						}

						if (!found) {
							fel2 = doc.createElement("function");
							fel2.setAttribute("address", dnp.functionaddress);// dnp.address);
							fel2.setAttribute("index", "0");// dnp.index);
							fel2.setAttribute("stereotype", dnp.stereoType);// dnp.stereotype);
							fel2.setAttribute("externalfile", dnp.externalFile);
							// fel2.setAttribute("expanded", new
							// Boolean(dnp.expanded).toString());

							/*
							 * if(prevCallList.get(act.getText()) == null){
							 * fel2.setAttribute("externalfile", ""); } else {
							 * fel2.setAttribute("externalfile",
							 * prevCallList.get(act.getText())); }
							 */
							fel2.setAttribute("name", act.getText());
							entryNode.appendChild(fel2);
						}
						// }

						for (int j = 0; j < mess.length; j++) {
							Message thisMess = mess[j];
							// could be more than one call to a function, should
							// index by function that calls it + callname
							// DynamicCallProxy dcp =
							// callList.get(act.getLifeline().getText() +
							// thisMess.getText());
							// LifelineProxy ll =
							// ascp.lifelineList.get(act.getLifeline().getText()
							// + thisMess.getText());
							NodeProxy dcp = ascp.messageList.get(act.getLifeline().getText() + thisMess.getText());

							if (dcp == null) {
								Set<String> set = renamedList.keySet();
								for (String string : set) {
									if (renamedList.get(string).equals(thisMess.getText())) {
										dcp = ascp.messageList.get(act.getLifeline().getText() + string);
									}
								}
							}

							el3 = doc.createElement("call");
							// el3.setAttribute("expanded",
							// Boolean.toString(act.isExpanded()));
							el3.setAttribute("calladdress", dcp.calladdress);// dcp.callAddress);
							el3.setAttribute("externalfile", dcp.externalFile);
							el3.setAttribute("functionaddress", dcp.functionaddress);// dcp.functionAddress);
							el3.setAttribute("index", "0");// dcp.index);
							el3.setAttribute("name", thisMess.getText());

							// System.out.println(thisMess.getSource().getText());
							// if(thisMess.getSource().getText().equals("Start")){
							// fel.appendChild(el3);
							// } else {
							fel2.appendChild(el3);
							// }

							// currentNodeProxy = dnp;

						}

						// }

					}
					/*
					 * if (item instanceof Call) { Call call = (Call) items1[i];
					 * // call.getSource(); } if (item instanceof Lifeline) {
					 * Lifeline life = (Lifeline) items1[i]; }
					 */
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
				// npe.printStackTrace();
			}
		} catch (ParserConfigurationException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}

	@Override
	public void doSave(IProgressMonitor arg0) {
		String val = store.getString(PreferenceConstants.P_STATIC);

		if (val.equals("retrieve")) {
			MessageBox messageBox = new MessageBox(Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
			messageBox.setText("Save Resource");
			messageBox.setMessage("Do you want to save as a trace file as well? This will allow you to save this sequence diagram with renamed functions and share with others.");
			int buttonID = messageBox.open();
			switch (buttonID) {
			case SWT.YES:
				display.syncExec(new Runnable() {
					public void run() {
						Shell s = new Shell(display);
						Calendar cal = Calendar.getInstance();
						SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
						String timestamp = sdf.format(cal.getTime());
						s.setSize(400, 400);
						FileDialog fd = new FileDialog(s, SWT.SAVE);
						fd.setText("Save");
						fd.setFilterPath(saveLocation);
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
		}

		// write out persistence to a hidden file
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IPath loc = Path.fromOSString(localPath.toString() + File.separator + inputFile.getName() + ".dat");
		String workPath = ws.getRoot().getLocationURI().toString();
		workPath = workPath.replace("file:/", "");
		workPath = workPath.replace("%20", " ");
		File persFile = new File(workPath + File.separator + loc.toString());
		File secFile = new File(loc.toString());
		File thirdFile = new File(inputFile.getAbsolutePath() + ".dat");

		if (persFile.exists() || secFile.exists() || thirdFile.exists()) {
			if (secFile.exists())
				persFile = secFile;
			if (!persFile.exists())
				persFile = thirdFile;

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;

			try {
				builder = factory.newDocumentBuilder();
				Document doc;

				try {
					doc = builder.parse(persFile);
				} catch (SAXParseException spe) {
					// empty document
					doc = builder.newDocument();
				}
				Element rootNode = doc.getDocumentElement();
				if (rootNode == null) {
					rootNode = doc.createElement("sequence");
				}
				NodeList items = rootNode.getElementsByTagName("function");
				Element method = null;

				for (int i = 0; i < items.getLength(); i++) {
					Element node = (Element) items.item(i);
					if (node.getAttribute("name").equals(methodToExpand)) {
						method = node;
						break;
					}
				}

				Activation root = getViewer().getChart().getRootActivation();
				Activation[] acts = root.getLifeline().getActivations();
				String index = "0";
				for (int i = 0; i < acts.length; i++) {
					if(acts.equals(root)){
						index = Integer.toString(i);
						break;
					}
				}
				Object obj = root.getData();
				String externalFile = ((NodeProxy) obj).externalFile;

				Element fel = doc.createElement("function");
				fel.setAttribute("name", methodToExpand);
				fel.setAttribute("module", ((NodeProxy) obj).module);
				Element el2 = doc.createElement("root");
				el2.setAttribute("externalfile", externalFile);
				el2.setAttribute("name", ((NodeProxy) obj).targetName);
				el2.setAttribute("module", ((NodeProxy) obj).module);
				el2.setAttribute("callindex", index);
				el2.setAttribute("act", ((NodeProxy) obj).act);
				fel.appendChild(el2);

				UMLItem[] items1 = getViewer().getChart().getItems();
				Activation lastAct = null;
				
				for (int i = 0; i < items1.length; i++) {
					UMLItem item = items1[i];
					if (item instanceof Activation) {
						Activation act = (Activation) item;
						obj = act.getData();
						externalFile = ((NodeProxy) obj).externalFile;
						if (act.isExpanded()) {
							Element el3 = doc.createElement("expanded");
							el3.setAttribute("externalfile", externalFile);
							el3.setAttribute("name", act.getData().toString());
							el3.setAttribute("module", ((NodeProxy) obj).module);
							el3.setAttribute("act", ((NodeProxy) obj).act);
							fel.appendChild(el3);
						}
						if(act.isVisible()){
							lastAct = act;
						}
					}
				}
				
				acts = lastAct.getLifeline().getActivations();
				index = "0";
				for (int i = 0; i < acts.length; i++) {
					if(acts.equals(root)){
						index = Integer.toString(i);
						break;
					}
				}

				// items1 = getViewer().getChart().getSelection();
				Element el3 = doc.createElement("selection");
				NodeProxy np = (NodeProxy)lastAct.getData();
				el3.setAttribute("name", np.targetName);
				el3.setAttribute("externalfile", np.externalFile);
				el3.setAttribute("module", np.module);
				el3.setAttribute("callingnode", np.getCallingNode().targetName);
				el3.setAttribute("callindex", index);
				el3.setAttribute("act", ((NodeProxy) obj).act);
				fel.appendChild(el3);

				if (store.getString(PreferenceConstants.P_STATIC).equals(PreferenceConstants.P_STATIC_RET)) {
					if (method != null) {
						NodeList renamed = method.getElementsByTagName("renamed");
						for (int i = 0; i < renamed.getLength(); i++) {
							Node node = renamed.item(i);
							fel.appendChild(node);
						}
					}
				}

				if (method == null) {
					rootNode.appendChild(fel);
				} else {
					rootNode.replaceChild(fel, method);
				}

				// Prepare the DOM document for writing
				Source source = new DOMSource(rootNode);

				Result result = new StreamResult(persFile);
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
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				persFile.createNewFile();
			} catch (IOException e1) {
				try {
					persFile = secFile;
					persFile.createNewFile();
				} catch (IOException e) {
					persFile = thirdFile;
					try {
						thirdFile.createNewFile();
					} catch (IOException e2) {
					}
				}
			}
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			try {
				builder = factory.newDocumentBuilder();
				Document doc = builder.newDocument();

				Activation root = getViewer().getChart().getRootActivation();
				Activation[] acts = root.getLifeline().getActivations();
				String index = "0";
				for (int i = 0; i < acts.length; i++) {
					if(acts.equals(root)){
						index = Integer.toString(i);
						break;
					}
				}
				Object obj = root.getData();
				String externalFile = ((NodeProxy) obj).externalFile;

				Element el = doc.createElement("sequence");
				Element fel = doc.createElement("function");
				fel.setAttribute("name", methodToExpand);
				el.appendChild(fel);
				Element el2 = doc.createElement("root");
				el2.setAttribute("externalfile", externalFile);
				el2.setAttribute("name", root.getText());
				el2.setAttribute("callindex", index);
				el2.setAttribute("act", ((NodeProxy) obj).act);
				fel.appendChild(el2);

				UMLItem[] items = getViewer().getChart().getItems();
				Activation lastAct = null;
				
				for (int i = 0; i < items.length; i++) {
					UMLItem item = items[i];
					if (item instanceof Activation) {
						Activation act = (Activation) item;
						obj = act.getData();
						externalFile = ((NodeProxy) obj).externalFile;
						if (act.isExpanded()) {
							Element el3 = doc.createElement("expanded");
							el3.setAttribute("externalfile", externalFile);
							el3.setAttribute("name", act.getData().toString());
							el3.setAttribute("module", ((NodeProxy) obj).module);
							el3.setAttribute("act", ((NodeProxy) obj).act);
							fel.appendChild(el3);
						}
						if(act.isVisible()){
							lastAct = act;
						}
					}
				}
				
				acts = lastAct.getLifeline().getActivations();
				index = "0";
				for (int i = 0; i < acts.length; i++) {
					if(acts.equals(root)){
						index = Integer.toString(i);
						break;
					}
				}
				Element el3 = doc.createElement("selection");
				NodeProxy np = (NodeProxy)lastAct.getData();
				el3.setAttribute("name", np.targetName);
				el3.setAttribute("externalfile", np.externalFile);
				el3.setAttribute("module", np.module);
				el3.setAttribute("callingnode", np.getCallingNode().targetName);
				el3.setAttribute("callindex", index);
				el3.setAttribute("act", np.act);
				fel.appendChild(el3);

				if (store.getString(PreferenceConstants.P_STATIC).equals(PreferenceConstants.P_STATIC_RET)) {
					Object[] keys = (Object[]) renamedList.keySet().toArray();

					for (int i = 0; i < keys.length; i++) {
						String original = (String) keys[i];
						String newName = renamedList.get(original);
						Element el4 = doc.createElement("renamed");
						el4.setAttribute("original", original);
						el4.setAttribute("new", newName);
						fel.appendChild(el4);
					}
				}

				// Prepare the DOM document for writing
				Source source = new DOMSource(el);

				Result result = new StreamResult(persFile);
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
		// }
	}

	private void updateExpanded() {
		Set<String> set = savedExpandedList.keySet();

		for (String string : set) {
			NodeProxy np = savedExpandedList.get(string);
			getViewer().setExpanded(np, true);
		}
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setInput(input);
		setSite(site);
		firePropertyChange(IWorkbenchPartConstants.PROP_INPUT);
		String tempPath = "";
		Element el = null;

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
			String lPath = tempPath.substring(tempPath.lastIndexOf("$"));
			tempPath = tempPath.substring(0, tempPath.lastIndexOf("$"));
			lPath = lPath.substring(1);
			lPath = lPath.substring(0, lPath.lastIndexOf("/"));
			localPath = Path.fromOSString(lPath);
		}

		String funcAddr = "";
		String module = "";

		if (!tempPath.contains("$")) {
			String newLocation = tempPath;
			inputFile = new File(newLocation);
			document = XMLUtils.createSequenceFromXML(inputFile);
			Element rootElement = document.getDocumentElement();
			el = (Element) rootElement.getElementsByTagName("function").item(0);
			methodToExpand = el.getAttribute("name");
			funcAddr = el.getAttribute("address");
			module = el.getAttribute("module");
		} else {
			methodToExpand = tempPath.substring(tempPath.lastIndexOf('$') + 1);

			// if
			// (store.getString(PreferenceConstants.P_GENERAL).equals(PreferenceConstants.P_GEN_ALL))
			// {
			String newLocation = tempPath.substring(0, tempPath.lastIndexOf('$'));
			inputFile = new File(newLocation);
			document = XMLUtils.createSequenceFromXML(inputFile);
			Element rootElement = document.getDocumentElement();
			NodeList nl = rootElement.getElementsByTagName("function");
			el = null;

			for (int i = 0; i < nl.getLength(); i++) {
				el = (Element) nl.item(i);
				if (el.getAttribute("name").equals(methodToExpand)) {
					break;
				}
			}
			funcAddr = el.getAttribute("address");
			module = el.getAttribute("module");
			// }

			newLocation = tempPath.substring(0, tempPath.lastIndexOf('$'));
			inputFile = new File(newLocation);
		}

		if (store.getString(PreferenceConstants.P_GENERAL).equals(PreferenceConstants.P_GEN_ALL)) {
			// send message to IDAPro to set a breakpoint at selected function
			Long position = Long.parseLong(funcAddr, 16);
			Startup.send(module, "updateCursor " + Long.toString(position));// +
																			// " "
																			// +
																			// module);
		}

		setPartName(methodToExpand);
		addressToExpand = el.getAttribute("address");
	}

	@Override
	public boolean isDirty() {
		return dirty;
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

		ascp = new TraceAssemblySequenceContentProvider(getEditorInput(), methodToExpand, localPath, this);
		breadcrumb = new BreadCrumbViewer(control, SWT.BORDER);

		viewer = new UMLSequenceViewer(control, SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL);
		viewer.setContentProvider(ascp);
		viewer.setLabelProvider(new AssemblySequenceLabelProvider(ascp, viewer));
		viewer.setMessageGrouper(new AssemblyMessageGrouper());

		Document doc = document;
		if (doc == null)
			doc = XMLUtils.createSequenceFromXML(inputFile);

		viewer.setInput(doc);
		viewer.getChart().addMouseListener(new NavigateToCodeListener());
		viewer.getChart().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.addSequenceListener(new SequenceViewerListener());
		viewer.getChart().setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		asbcp = new AssemblySequenceBreadCrumbContentProvider(ascp);
		breadcrumb.setContentProvider(asbcp);
		breadcrumb.setLabelProvider(new AssemblySequenceLabelProvider(ascp, viewer));
		breadcrumb.addSelectionChangedListener(new BreadCrumbSelectionListener());
		breadcrumb.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		MenuManager manager = new MenuManager("ASTSequenceEditor", "#ASTSequenceEditorContext");
		manager.setRemoveAllWhenShown(true);
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
				if (element instanceof NodeProxy) {
					String activationName = ((ILabelProvider) viewer.getLabelProvider()).getText(element);
					focusIn.setFocusElement(element);
					focusIn.setText("Focus On " + activationName);
					manager.add(focusIn);
					expandAll.setText("Expand All Activations Under " + activationName);
					expandAll.setFocusElement(element);
					manager.add(expandAll);
					collapseAll.setText("Collapse All Activations Under " + activationName);
					collapseAll.setFocusElement(element);
					manager.add(collapseAll);

					NodeProxy np = (NodeProxy) element;
					// focus on caller
					if (np.getCallingNode() != null && !viewer.getRootActivation().equals(ascp.rootNode)) {
						manager.add(focusUp);
					}
				} else {
					// String activationName = (String) element;

					// if (act.getText().equals(activationName)) {
					String activationName = ((ILabelProvider) viewer.getLabelProvider()).getText(element);
					focusIn.setFocusElement(act);
					focusIn.setText("Focus On " + activationName);
					manager.add(focusIn);

					expandAll.setText("Expand All Activations Under " + activationName);
					expandAll.setFocusElement(act);
					manager.add(expandAll);

					collapseAll.setText("Collapse All Activations Under " + activationName);
					collapseAll.setFocusElement(act);
					manager.add(collapseAll);

					// focus on caller
					if (!element.equals("Start")) {
						focusUp.setFocusElement(act);
						manager.add(focusUp);
					}

					asbcp.currAct = act;
					// if (np.getCallingNode() != null &&
					// !viewer.getRootActivation().equals(ascp.rootNode))
					// { //
					// manager.add(focusUp);
					// }
				}
				manager.add(cloneAction);
			}
		});
		viewer.getChart().setMenu(contextMenu);
		viewer.getChart().setCloneVisible(false);

		makeActions();

		// load persistence
		loadSaved();
	}

	public void setMethodToExpand(String methodName) {
		methodToExpand = methodName;
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
		getViewer().removeSequenceListener(sequenceListener);
		breadcrumb.removeSelectionChangedListener(breadcrumbListener);
		LinkedList<TraceAssemblySequenceEditor> list = Activator.getDefault().teditors;
		for (int i = 0; i < list.size(); i++) {
			TraceAssemblySequenceEditor ase = list.get(i);
			if (ase.equals(this)) {
				Activator.getDefault().teditors.remove(i);
				break;
			}
		}
		super.dispose();
	}

	/**
	 * @return
	 */
	public UMLSequenceViewer getViewer() {
		return viewer;
	}
	
	public void setRoot(String name, String module, String externalfile, String index){
		UMLItem[] items = getViewer().getChart().getItems();
		for (int i = 0; i < items.length; i++) {
			UMLItem item = items[i];
			if(item instanceof Lifeline){
				Lifeline line = (Lifeline)item;
				if(line.getText().equals(name)){
					Lifeline parent = line.getParent();
					if(parent != null){
						if(parent.getText().equals(externalfile)){
							viewer.getChart().setRootActivation(line.getActivations()[Integer.parseInt(index)]);
							viewer.getChart().getRootActivation().getLifeline().getParent().setBackground(new Color(Display.getCurrent(), 0, 0, 0));
						}
					}
				}
			}
		}
	}
}
