package org.eclipse.zest.custom.sequence.assembly.model;

import org.eclipse.swt.graphics.Image;
import org.eclipse.zest.custom.sequence.assembly.editors.NodeProxy;
import org.w3c.dom.Element;

public class AssemblyActivation{

	public Element node;
	public String targetName;
	private Image image = null;
	public String externalFile;
	
	public AssemblyActivation(Element node){
		this.node = node;
		this.targetName = node.getAttribute("name");
	}
	
	public AssemblyActivation(String targetName){
		this.targetName = targetName;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof NodeProxy)) {
			return false;
		}
		return ((NodeProxy)obj).targetName.equals(this.targetName);
	}
	
	@Override
	public int hashCode() {
		return targetName.hashCode();
	}
	
	public String toString() {
		return targetName;
	}
	
	public Image getImage(){
		return image;
	}
	
	public void setImage(Image image){
		this.image = image;
	}

}
