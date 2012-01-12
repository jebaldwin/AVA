package org.eclipse.zest.custom.statediagram.assembly.views;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class NodeModelContentProvider {
	public List<MyConnection> connections;
	public HashMap<String, MyNode> nodes;

	public NodeModelContentProvider(IEditorInput input, GraphViewer viewer) {
		
		//read xml file
		//write each state as a node
		//write a connection after each to the next node
		File inputFile = null;
		
		if (input instanceof FileEditorInput) {
			FileEditorInput fei = (FileEditorInput) input;
			inputFile = new File(fei.getURI());
		} else {
			FileStoreEditorInput fei = (FileStoreEditorInput) input;
			inputFile = new File(fei.getURI());
		}

		Document doc = createFromXML(inputFile);
		Element rootElement = doc.getDocumentElement();
		NodeList states = rootElement.getElementsByTagName("state");
		
		nodes = new HashMap<String, MyNode>();
		connections = new ArrayList<MyConnection>();
		String lastName = "";
		
		for (int i = 0; i < states.getLength(); i++) {
			Element el = (Element)states.item(i);
			MyNode node = new MyNode(el.getAttribute("name"), viewer.getGraphControl());
			
			//fill node with actions
			NodeList actions = el.getElementsByTagName("action");
			ArrayList<MyAction> myActions = new ArrayList<MyAction>();
			for (int j = 0; j < actions.getLength(); j++) {
				Element el2 = (Element)actions.item(j);
				MyAction action = new MyAction(el2.getAttribute("address"), el2.getAttribute("command"), el2.getAttribute("exe"));
				myActions.add(action);
			}
			node.actions = myActions;
			
			nodes.put(el.getAttribute("name"), node);
			
			if(i > 0){
				MyConnection connect = new MyConnection("1", "1", ZestStyles.CONNECTIONS_DIRECTED, nodes.get(lastName), node, viewer.getGraphControl());
				//connections.add(connect);/
			}
			
			lastName = el.getAttribute("name");
		}

		for (MyConnection connection : connections) {
			connection.getSource().getConnectedTo().add(connection.getDestination());
		}
	}

	public HashMap<String, MyNode> getNodes() {
		//return nodes;
		return null;
	}
	
	public MyNode getNode(String name){
		return nodes.get(name);
	}
	
	public static Document createFromXML(File xmlFile) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(xmlFile);
			return document;

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			//e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
