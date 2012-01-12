/**
 * 
 */
package org.eclipse.zest.custom.sequence.assembly.editors;

import org.w3c.dom.Element;

public class NodeProxy{
	public Element node;
	public String targetName;
	public String functionaddress = "";
	public String calladdress;
	public String index = "";
	public String callindex = "";
	public boolean localFunction = true;
	public String externalFile = "";
	public String stereoType = "";
	private NodeProxy callingNode = null;
	public String module = "";
	public String baseURI = "";
	public String act = "";
	public boolean innerloop = false;
	public int loopOffset = -1;
	public int loopLength = -1;
	
	public NodeProxy(Element node){
		this.node = node;
		this.targetName = node.getAttribute("name");
		this.stereoType = node.getAttribute("stereotype");
		this.functionaddress = node.getAttribute("address");
		this.externalFile = node.getAttribute("externalfile");
		this.callindex = node.getAttribute("callindex");
		this.index = node.getAttribute("index");
		this.module = node.getAttribute("module");

		if(node.getParentNode() != null && node.getParentNode().getNodeName().equals("innerloop")){
			innerloop = true;
		}
		
		if(node.getParentNode() != null && node.getParentNode().getNodeName().equals("loop")){
			loopOffset = Integer.parseInt(((Element)node.getParentNode()).getAttribute("offset"));
			loopLength = Integer.parseInt(((Element)node.getParentNode()).getAttribute("length"));
		}
		
		if(node.getAttribute("act").length() > 0){
			this.act = node.getAttribute("act");
		}
		
		if(this.module.equals("")){
			//then use name of the file
			String temp = node.getOwnerDocument().getDocumentElement().getAttribute("filename");
			this.module = temp.replace(".ose", ".exe");
		}
		if(this.externalFile.equals("")){
			this.externalFile = module;
		}
		if(this.functionaddress.length() == 0){
			this.functionaddress = node.getAttribute("functionaddress");
			this.calladdress = node.getAttribute("calladdress");
		}
	}
	
	public NodeProxy(String targetName){
		this.targetName = targetName;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof NodeProxy)) {
			return false;
		}
		return ((NodeProxy)obj).act.equals(this.act) && ((NodeProxy)obj).targetName.equals(this.targetName) && ((NodeProxy)obj).externalFile.equals(this.externalFile) && ((NodeProxy)obj).module.equals(this.module);
	}
	
	@Override
	public int hashCode() {
		return (targetName + act).hashCode();
	}
	
	public String toString() {
		return targetName;
	}
	
	public NodeProxy getCallingNode(){
		return callingNode;
	}
	
	public void setCallingNode(NodeProxy np){
		this.callingNode = np;
	}
}