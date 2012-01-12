package org.eclipse.zest.custom.statediagram.assembly.views;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.zest.core.viewers.EntityConnectionData;

public class ZestLabelProvider extends LabelProvider {
	@Override
	public String getText(Object element) {
		if (element instanceof MyNode){
			MyNode myNode = (MyNode) element;
			return myNode.name;
		}
		if (element instanceof MyConnection){
			MyConnection myConnection = (MyConnection) element;
			return myConnection.getLabel();
		}
		
		if (element instanceof EntityConnectionData){
			return "";
		}
		throw new RuntimeException("Wrong type: " + element.getClass().toString() );
	}
}
