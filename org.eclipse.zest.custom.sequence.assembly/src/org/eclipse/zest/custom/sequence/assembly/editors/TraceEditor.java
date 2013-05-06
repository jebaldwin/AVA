package org.eclipse.zest.custom.sequence.assembly.editors;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.zest.custom.sequence.assembly.Activator;
import org.eclipse.zest.custom.sequence.assembly.model.XMLUtils;
import org.eclipse.zest.custom.sequence.assembly.preferences.PreferenceConstants;
import org.eclipse.zest.custom.sequence.widgets.Activation;
import org.eclipse.zest.custom.sequence.widgets.Call;
import org.eclipse.zest.custom.sequence.widgets.Lifeline;
import org.eclipse.zest.custom.sequence.widgets.Message;
import org.eclipse.zest.custom.sequence.widgets.MessageGroup;
import org.eclipse.zest.custom.sequence.widgets.UMLItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;


public class TraceEditor extends DynamicAssemblySequenceEditor {

	
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		Activator.getDefault().traceEditors.add(this);
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
	}
	
	private void addMessage() {

	}
	
	public void createPartControl(Composite parent){
		super.createPartControl(parent);
		Document doc = XMLUtils.createSequenceFromXML(inputFile);
		Element rootElement = doc.getDocumentElement();
		Element firstFunction = (Element)rootElement.getElementsByTagName("function").item(0);
		setPartName(inputFile.getName(), firstFunction);
		loadSaved();
	}
	
	@Override
	public void doSave(IProgressMonitor arg0) {
		inputFile.delete();
		try {
			inputFile.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		save(inputFile.getAbsolutePath());
	}
	
	protected void loadSaved() {
		Document doc = XMLUtils.createSequenceFromXML(inputFile);
		Element rootElement = doc.getDocumentElement();
		boolean rootExpanded = new Boolean(rootElement.getAttribute("rootexpanded")).booleanValue();
		
		Element selected = (Element)rootElement.getElementsByTagName("selection").item(0);	
		int selcallindex = 0;
		
		if(selected != null)
			selcallindex = Integer.parseInt(selected.getAttribute("callindex"));
		
		Element root = (Element)rootElement.getElementsByTagName("root").item(0);
		int rootcallindex = 0;
		
		if(root != null)
			rootcallindex = Integer.parseInt(root.getAttribute("callindex"));
	
		boolean foundSelection = false;
		boolean foundRoot = false;
		LinkedList<Activation> expandCalls = new LinkedList<Activation>();
		
		NodeList list = rootElement.getElementsByTagName("call");
		/*String[] test = new String[list.getLength()];
		for (int i = 0; i < list.getLength(); i++) {
			Element el = (Element)list.item(i);
			String name = el.getAttribute("name"); 
			test[i] = name;
		}*/
		 
		Activation startOfLoop = null;
		int loopCount = 0;
		Call call = null;
		boolean changeContext = false;
		
		int threadCount = 0;

		for (int i = 0; i < list.getLength(); i++) {
			Element el = (Element)list.item(i);
			String name = el.getAttribute("name"); 
			boolean addToRoot = new Boolean(el.getAttribute("root")).booleanValue();
			String externalFile = el.getAttribute("externalfile");
			String module = el.getAttribute("module");
			boolean expanded = new Boolean(el.getAttribute("expanded")).booleanValue();
			DynamicCallProxy dcp = new DynamicCallProxy(name, el.getAttribute("calladdress"), externalFile, el.getAttribute("functionaddress"), el.getAttribute("index"), module, addToRoot, expanded);
			
			String isReturn = el.getAttribute("isreturn");
			
			if(addToRoot){
				builder.setContext(viewer.getChart().getRootActivation());
			}
			
			if(el.getParentNode().getNodeName().equals("loop")){
				NamedNodeMap map = el.getParentNode().getAttributes();
				int length = new Integer(map.getNamedItem("length").getTextContent()).intValue();
				if(loopCount == 0){
					MessageGroup mg = builder.openGroup("");
					//CommentView.changeURLID(Integer.toString(mg.hashCode()));
					//int count = CommentView.getCommentCount("Loop" + mg.getActivation().getLifeline().getText());
					/*int count = CommentView.getCommentCount("Loop:" + mg.getActivation().getLifeline().getParent().getText() + "," + mg.getActivation().getLifeline().getText());
					if(count == 1){
						mg.setText(count + " comment");
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
					}*/
										
					startOfLoop = call.getTarget();
				} else {
					if(loopCount == length - 1){
						//builder.setContext(startOfLoop);
						//builder.closeGroup();
						changeContext = true;
					}
				}
				loopCount ++;
			} else {				
				loopCount = 0;
			} 
			
			if(el.getParentNode().getNodeName().equals("thread")){
				//call = builder.makeCall(":Thread", ":Thread");
				call = builder.makeCall(":Thread"+threadCount, ":Thread"+threadCount);
			//	Message[] messages = call.getSource().getMessages();
			//	messages[messages.length - 1].setText("new");
				builder.setContext(call.getTarget());
				threadCount++;
			}
			
			call = builder.makeCall(name, name);
					
			if(isReturn.equals("true")){
				call.setLineStyle(SWT.LINE_DASH);
				//call.getSource().getLifeline().setTooltipText("Sample Description");
				//call.getSource().getLifeline().getParent().setText("This is a test description");
			}
			
			if(!changeContext){
				builder.setContext(call.getTarget());
			} else {
				builder.setContext(startOfLoop);
				builder.closeGroup();
				changeContext = false;
			}
			
			if(el.getParentNode().getNodeName().equals("innerloop")){
				call.getTarget().setBackground(new Color(display, 255, 99, 71));
			}
			
			if (externalFile.length() == 0 || module.equals(externalFile)) {
				Lifeline user = builder.setContainer(call.getTarget().getLifeline().getText(), module);
				if(user == null){
					builder.setContainer(externalFile.toLowerCase(), module.toLowerCase());
				}
				call.getTarget().getLifeline().setImage(AssemblySequenceLabelProvider.localimage);
				call.setImage(AssemblySequenceLabelProvider.localimage);
				if(user != null){
					user.setBackground(new Color(Display.getCurrent(), 255, 200, 100));
				}
				//check if there are comment threads
				if(store.getString(PreferenceConstants.P_COMMENTS).equals(PreferenceConstants.P_RET_COMMENTS)){
					//int count = CommentView.getCommentCount("Dynamic" + "Call" + call.getSource().getText() + "To" + call.getTarget().getText());
					Activation a = call.getSource();
					/*int count = CommentView.getCommentCount(a.getLifeline().getParent().getText() + ":" + a.getLifeline().getText() + "->" + call.getTarget().getLifeline().getParent().getText() + ":" + call.getTarget().getLifeline().getText());
					
					if(count > 0){
						if(count >= 25)
							call.setImage(AssemblySequenceLabelProvider.localcommentgreen);
						else if(count >= 10)
							call.setImage(AssemblySequenceLabelProvider.localcommentgold);
						else
							call.setImage(AssemblySequenceLabelProvider.localcommentred);
					}*/
					
					//count = CommentView.getCommentCount("Dynamic" + "Lifeline" + call.getTarget().getLifeline().getText());
					Lifeline line = call.getTarget().getLifeline();
					/*count = CommentView.getCommentCount(line.getParent().getText() + ":" + line.getText());

					if(count > 0){
						if(count >= 25)
							line.setImage(AssemblySequenceLabelProvider.localcommentgreen);
						else if(count >= 10)
							line.setImage(AssemblySequenceLabelProvider.localcommentgold);
						else
							line.setImage(AssemblySequenceLabelProvider.localcommentred);
					}*/
				}
			} else {
				Lifeline user = builder.setContainer(call.getTarget().getLifeline().getText(), externalFile);
				call.getTarget().getLifeline().setImage(AssemblySequenceLabelProvider.externalimage);
				call.setImage(AssemblySequenceLabelProvider.externalimage);
				user.setBackground(new Color(Display.getCurrent(), 255, 200, 100));
				
				//check if there are comment threads
				if(store.getString(PreferenceConstants.P_COMMENTS).equals(PreferenceConstants.P_RET_COMMENTS)){
					//int count = CommentView.getCommentCount("Dynamic" + "Call" + call.getSource().getText() + "To" + call.getTarget().getText());
					Activation a = call.getSource();
					/*int count = CommentView.getCommentCount(a.getLifeline().getParent().getText() + ":" + a.getLifeline().getText() + "->" + call.getTarget().getLifeline().getParent().getText() + ":" + call.getTarget().getLifeline().getText());
					
					if(count > 0){
						if(count >= 25)
							call.setImage(AssemblySequenceLabelProvider.externalcommentgreen);
						else if(count >= 10)
							call.setImage(AssemblySequenceLabelProvider.externalcommentgold);
						else
							call.setImage(AssemblySequenceLabelProvider.externalcommentred);
					}
					*/
					//count = CommentView.getCommentCount("Dynamic" + "Lifeline" + call.getTarget().getLifeline().getText());
					Lifeline line = call.getTarget().getLifeline();
					/*count = CommentView.getCommentCount(line.getParent().getText() + ":" + line.getText());
					
					if(count > 0){
						if(count >= 25)
							line.setImage(AssemblySequenceLabelProvider.externalcommentgreen);
						else if(count >= 10)
							line.setImage(AssemblySequenceLabelProvider.externalcommentgold);
						else
							line.setImage(AssemblySequenceLabelProvider.externalcommentred);
					}*/
				}
			}
			//viewer.getChart().markDirty();
			
			//reset tempname to the correct exe name
			UMLItem[] items = viewer.getChart().getItems();
			if(call.getTarget().getLifeline().getText().equals("tempname")){
				call.getTarget().getLifeline().setText(externalFile.toLowerCase());
			}
			for (int i1 = 0; i1 < items.length; i1++) {
				UMLItem item = items[i1];
				if(item instanceof Call || item instanceof Message){
					if(item.getText().equals("tempname")){
						item.setText(externalFile.toLowerCase());	
					}
				}
			}
			orderedCallList.add(dcp);
			
			if(expanded){
				//call.getSource().setExpanded(true);
				expandCalls.add(call.getSource());
			} else {
				//call.getSource().setExpanded(false);
			}
			
			//check if it's the root
			Activation act = call.getTarget();
			if(act != null){
				if(!foundRoot && root != null){
					if(act.getLifeline().getText().equals(root.getAttribute("name"))
							&& act.getLifeline().getParent().getText().equals(root.getAttribute("externalfile"))
									&& act.getLifeline().getParent().getText().equals(root.getAttribute("module"))){
						Activation[] acts = act.getLifeline().getActivations();
						if(acts.length == rootcallindex + 1){
							viewer.getChart().setRootActivation(act);
							foundRoot = true;
						}
					}
				}
				
				if(!foundSelection && selected != null){
					if(act.getLifeline().getText().equals(selected.getAttribute("name"))
							&& act.getLifeline().getParent().getText().equals(selected.getAttribute("externalfile"))
									&& act.getLifeline().getParent().getText().equals(selected.getAttribute("module"))){
						Activation source = call.getSource();
						if(source.getLifeline().getText().equals(selected.getAttribute("callingnode"))){
							Activation[] acts = act.getLifeline().getActivations();
							if(acts.length == selcallindex + 1){
								//viewer.getChart().reveal(act);
								foundSelection = true;
							}
						}
					}
				}
			}
		}
		if(rootExpanded){
			viewer.getChart().getRootActivation().setExpanded(true);
		}
		
		//set all expanded and see if it's faster
		for (int i = 0; i < expandCalls.size(); i++) {
			Activation act = expandCalls.get(i);
			act.setExpanded(true);
		}
		
		dirty = false;
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
	
	public void dispose() {
		System.out.println("dispose!");
		Activator.getDefault().traceEditors.remove(this);
		getViewer().removeSequenceListener(sequenceListener);
		breadcrumb.removeSelectionChangedListener(breadcrumbListener);
		//super.dispose();
	}
}
