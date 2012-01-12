package org.eclipse.zest.custom.statediagram.assembly.views;

import org.eclipse.swt.SWT;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.ZestStyles;

public class MyConnection {
	final String id; 
	public String label; 
	public int style;
	public MyNode source;
	public MyNode destination;
	public GraphConnection connection;
	
	public MyConnection(String id, String label, int style, MyNode source, MyNode destination, Graph graph) {
		this.id = id;
		this.label = label;
		this.style = style;
		this.source = source;
		this.destination = destination;
		connection = new GraphConnection(graph, ZestStyles.CONNECTIONS_DIRECTED, source.getNode(), destination.getNode());
		connection.setHighlightColor(graph.getDisplay().getSystemColor(SWT.COLOR_GREEN));
		connection.setLineColor(graph.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		connection.setLineWidth(1);
	}

	public String getLabel() {
		return label;
	}
	
	public MyNode getSource() {
		return source;
	}
	public MyNode getDestination() {
		return destination;
	}
	
}
