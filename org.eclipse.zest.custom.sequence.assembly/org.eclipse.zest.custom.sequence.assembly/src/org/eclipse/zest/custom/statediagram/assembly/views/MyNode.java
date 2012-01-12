package org.eclipse.zest.custom.statediagram.assembly.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphNode;

public class MyNode {

	public String name;
	private GraphNode graphNode;
	private List<MyNode> connections;
	public ArrayList<MyAction> actions;

	public MyNode(String name, Graph graph) {
		this.name = name;
		this.connections = new ArrayList<MyNode>();
		graphNode = new GraphNode(graph, SWT.NONE, name, null);
		graphNode.setBorderColor(new Color(null, 0,0,0));
		graphNode.setBorderWidth(1);
	}

	public List<MyNode> getConnectedTo() {
		return connections;
	}
	
	public GraphNode getNode(){
		return graphNode;
	}
}
