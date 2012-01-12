package org.eclipse.zest.custom.sequence.assembly.editors;

import java.io.File;
import java.util.LinkedList;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.zest.custom.sequence.assembly.model.XMLUtils;
import org.eclipse.zest.custom.sequence.widgets.Activation;
import org.eclipse.zest.custom.sequence.widgets.Call;
import org.eclipse.zest.custom.sequence.widgets.Lifeline;
import org.eclipse.zest.custom.sequence.widgets.Message;
import org.eclipse.zest.custom.sequence.widgets.UMLItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;


public class ReversedTraceEditor extends TraceEditor {

	protected void loadSaved() {
		String funcName = "";
			
		if(inputFile.getAbsolutePath().contains("$")){
			funcName = inputFile.getAbsolutePath().substring(inputFile.getAbsolutePath().lastIndexOf("$") + 1);
			inputFile = new File(inputFile.getAbsolutePath().substring(0, inputFile.getAbsolutePath().lastIndexOf("$")));
		}
		Document doc = XMLUtils.createSequenceFromXML(inputFile);
		
		Element rootElement = doc.getDocumentElement();
		boolean rootExpanded = new Boolean(rootElement.getAttribute("rootexpanded")).booleanValue();
		
		Element selected = (Element)rootElement.getElementsByTagName("selection").item(0);	
		int selcallindex = Integer.parseInt(selected.getAttribute("callindex"));
		
		//Element root = selected;//(Element)rootElement.getElementsByTagName("root").item(0);
		//int rootcallindex = 0;//Integer.parseInt(root.getAttribute("callindex"));
		Element root = (Element)rootElement.getElementsByTagName("root").item(0);
		int rootcallindex = Integer.parseInt(root.getAttribute("callindex"));
		
		boolean foundSelection = false;
		boolean foundRoot = false;
		LinkedList<Activation> expandCalls = new LinkedList<Activation>();
		
		NodeList list = rootElement.getElementsByTagName("call");
		String[] test = new String[list.getLength()];
		
		int foundIndex = 0;
		
		for (int i = 0; i < list.getLength(); i++) {
			Element el = (Element)list.item(i);
			String name = el.getAttribute("name"); 
			test[i] = name;
			if(name.equals(funcName)){
				if(foundIndex == 0)
					foundIndex = i;
			}
		}
		 
		Activation startOfLoop = null;
		int loopCount = 0;
		Call call = null;
		boolean changeContext = false;
		
		//for (int i = foundIndex; i >= 0; i--) {
		for (int i = 0; i <= foundIndex; i++) {
			Element el = (Element)list.item(i);
			String name = el.getAttribute("name"); 
			boolean addToRoot = new Boolean(el.getAttribute("root")).booleanValue();
			String externalFile = el.getAttribute("externalfile");
			String module = el.getAttribute("module");
			boolean expanded = new Boolean(el.getAttribute("expanded")).booleanValue();
			DynamicCallProxy dcp = new DynamicCallProxy(name, el.getAttribute("calladdress"), externalFile, el.getAttribute("functionaddress"), el.getAttribute("index"), module, addToRoot, expanded);

			if(addToRoot){
				builder.setContext(viewer.getChart().getRootActivation());
			}
			
			if(el.getParentNode().getNodeName().equals("loop")){
				NamedNodeMap map = el.getParentNode().getAttributes();
				int length = new Integer(map.getNamedItem("length").getTextContent()).intValue();
				if(loopCount == 0){
					builder.openGroup("");
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
			
			call = builder.makeCall(name, name);
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
				System.out.println(user);
				System.out.println(Display.getCurrent());
				if(user != null){
				user.setBackground(new Color(Display.getCurrent(), 255, 200, 100));
				}
			} else {
				Lifeline user = builder.setContainer(call.getTarget().getLifeline().getText(), externalFile);
				call.getTarget().getLifeline().setImage(AssemblySequenceLabelProvider.externalimage);
				user.setBackground(new Color(Display.getCurrent(), 255, 200, 100));
			}
			
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
		
		}
		if(rootExpanded){
			viewer.getChart().getRootActivation().setExpanded(true);
		}
		
		
		//set all expanded 
		for (int i = 0; i < expandCalls.size(); i++) {
			Activation act = expandCalls.get(i);
			act.setExpanded(true);
		}
		
		dirty = false;
	}
}
