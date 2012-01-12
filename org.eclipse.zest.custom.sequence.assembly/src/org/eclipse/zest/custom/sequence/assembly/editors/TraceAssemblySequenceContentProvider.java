package org.eclipse.zest.custom.sequence.assembly.editors;

import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.examples.navigator.AssemblyContentProvider;
import org.eclipse.zest.custom.sequence.widgets.MessageGroup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author jbaldwin
 */
public class TraceAssemblySequenceContentProvider extends AssemblySequenceContentProvider {

	public TraceAssemblySequenceEditor editor;
	
/*	public TraceAssemblySequenceContentProvider(IEditorInput input, String method, IPath localPath, TraceAssemblySequenceEditor editor){
		this.input = input;
		this.method = method;
		this.localPath = localPath;
		this.editor = editor;
	}*/
	
	public TraceAssemblySequenceContentProvider(IEditorInput input, String method, IPath localPath, TraceAssemblySequenceEditor editor) {
		super(input, method, localPath, editor);
		this.editor = editor;
	}
	
	public Object getLifeline(Object activation) {
		NodeProxy np = (NodeProxy)activation;

		actList.put(np.targetName, np);
		Element node = (Element)np.node;
		String callName = np.targetName;
		LifelineProxy llp = new LifelineProxy(callName, np.stereoType, np.module);
		//llp.sterotype = np.stereoType;
		
		if(node.getAttribute("externalfile").length() > 0){
			llp.externalFile = node.getAttribute("externalfile");
			
			//key must be for external file and different for hashmap
			callName += "EEEE" + node.getAttribute("externalfile");
		} else { 
			if(np.externalFile.length() > 0){
				llp.externalFile = np.externalFile;
				
				//key must be for external file and different for hashmap
				callName += "EEEE" + np.externalFile;
			} 
		}
		lifelineList.put(callName, llp);
		return callName;
	}
	
	public String getLocalFile(){
		return localFile;
	}
	
	public Object[] getMessages(Object activation) {
		NodeProxy np = (NodeProxy)activation;
		/*if(np.loopOffset > -1){
			System.out.println(editor.getViewer().getRootActivation());
			System.out.println(np.loopOffset);
			System.out.println(np.loopLength);
			MessageGroup mg = new MessageGroup(editor.getViewer().getChart());
			mg.setRange(editor.getViewer().getChart().getRootActivation(), np.loopOffset, np.loopLength);
			editor.getViewer().refresh();
		}*/
		ArrayList<NodeProxy> list = new ArrayList<NodeProxy>();

		if(np.node == null){
			return list.toArray();
		}

		if(np.node.getNodeName().equals("functionEntryPoint")){
			//get each function element
			NodeList nl = np.node.getElementsByTagName("function");
			
			if(nl != null && nl.getLength() > 0) {
				
				for(int i = 0 ; i < nl.getLength(); i++) {
					Element function = (Element)nl.item(i);
					NodeProxy np2 = new NodeProxy(function);
					if(function.getAttribute("name") != null){
						list.add(np2);
						messageList.put(function.getAttribute("name"), np2);
					}
				}
			}
		} else {
			if(this.editor.isTrace || ((np.node.getAttribute("externalfile").equals(np.node.getAttribute("module")) || np.node.getAttribute("externalfile").equals("User")) && np.node.getOwnerDocument().getDocumentURI().equals(doc.getDocumentURI()))){
				//get each function element
				NodeList nl1 = elTemp.getElementsByTagName("functionEntryPoint");	
				NodeList nl2;

				if(isCAPackage){
					//CA
					//TODO CA Modification
					nl2 = elTemp.getElementsByTagName("function");
				} else {
					Element function1 = (Element)nl1.item(0);
					
					if(function1 == null){
						nl2 = elTemp.getElementsByTagName("function");
					} else {
						nl2 = function1.getElementsByTagName("function");
					}
				}
				
				Element function2;
				Element funcNode = null;
				String stereotype = "";
				
				for(int i = 0; i < nl2.getLength(); i++){
					function2 = (Element) nl2.item(i);
					if(function2.getAttribute("name").equals(np.targetName) && function2.getAttribute("act").equals(np.act)){
						funcNode = function2;
						break;
					}
				}
				
				if(funcNode != null){
					
					NodeList nl = funcNode.getElementsByTagName("call");
					if(nl != null && nl.getLength() > 0) {

						for(int i = 0 ; i < nl.getLength(); i++) {
							Element function = (Element)nl.item(i);
							NodeProxy np2 = new NodeProxy(function);
							//if(!np2.targetName.equals(method)){
								np2.setCallingNode(np);
							//}
		
							if(function.getAttribute("externalfile") != null){
								np2.localFunction = false;
								np2.externalFile = function.getAttribute("externalfile").replace("." + AssemblyContentProvider.PROPERTIES_EXT, ".exe");
							}
							
							//had to set stereotype of this lifeline before we add the call, so we have to find it
							//TODO add stereotypes to call, does this ever happen but at the beginning anyways?
							/*for(int j = 0; j < nl2.getLength(); j++){
								function2 = (Element) nl2.item(j);
								if(function2.getAttribute("name").equals(function.getAttribute("name"))){
									funcNode = function2;
									np2.stereoType = funcNode.getAttribute("stereotype");
									break;
								}
							}*/
							
							if(function.getAttribute("name") != null){

									Boolean allowMessages = editor.expandedList.get(np2.externalFile + ":" + np2.targetName);
									if(allowMessages != null && allowMessages.booleanValue() == true){
									} else {
										list.add(np2);
										messageList.put(np.targetName + function.getAttribute("name"), np2);
									}
							}
						}
					}	
				}
			} else {
				NodeList nl1 = elTemp.getElementsByTagName("functionEntryPoint");	

				if(isCAPackage){ 
					//CA
					//TODO CA Modification
					Element function2;
					Element funcNode = null;
					NodeList nl2 = elTemp.getElementsByTagName("function");
					for(int i = 0; i < nl2.getLength(); i++){
						function2 = (Element) nl2.item(i);
						if(function2.getAttribute("name").equals(np.targetName) && function2.getAttribute("section").equals(np.node.getAttribute("externalfile"))){
							funcNode = function2;
							break;
						}
					}
					
					if(funcNode != null){
						
						NodeList nl = funcNode.getElementsByTagName("call");
						if(nl != null && nl.getLength() > 0) {
							for(int i = 0 ; i < nl.getLength(); i++) {
								Element function = (Element)nl.item(i);
								NodeProxy np2 = new NodeProxy(function);
								//if(!np2.targetName.equals(method)){
									np2.setCallingNode(np);
								//}
			
								if(function.getAttribute("externalfile") != null){
									np2.localFunction = false;
									np2.externalFile = function.getAttribute("externalfile").replace("." + AssemblyContentProvider.PROPERTIES_EXT, ".exe");
								}
								
								if(function.getAttribute("name") != null){
									Boolean allowMessages = editor.expandedList.get(np2.externalFile + ":" + np2.targetName);
									if(allowMessages != null && allowMessages.booleanValue() == true){
									} else {
										list.add(np2);
										messageList.put(np.targetName + function.getAttribute("name"), np2);
									}
									//list.add(np2);
									//messageList.put(function.getAttribute("name"), np2);
								}
							}
						}	
					}
				} else {
					//need to load external file
					/*String originalFile = np.node.getOwnerDocument().getDocumentURI();
					originalFile = originalFile.replaceAll("%20", " ");
					String newFileName = "";
					String externalFileName = np.node.getAttribute("externalfile") + ".axml";
					if(np.node.getAttribute("externalfile").length() == 0){
						externalFileName = originalFile.substring(originalFile.lastIndexOf("/") + 1);
					}
					
					if(np.node.getAttribute("externalfile").length() > 0){
						newFileName = originalFile.substring(0, originalFile.lastIndexOf('/')) + '/' + np.node.getAttribute("externalfile") + "." + AssemblyContentProvider.PROPERTIES_EXT;
					} else {
						newFileName = originalFile;
					}
					File newFile = new File(newFileName.replaceAll("file:", ""));
					String orig = newFile.getParent();
					
					if(!localPath.toOSString().equals(orig) || !newFile.exists()){
						IWorkspace ws = ResourcesPlugin.getWorkspace();
						IPath location = Path.fromOSString(localPath.toOSString() + File.separator + externalFileName);
	
						IFile res = ws.getRoot().getFileForLocation(location);
						
						if(res != null){
						//if(res.isLinked()){
							String path = res.getLocationURI().toString();
							path = path.replace("file:/", "");
							path = path.replace("%20", " ");
							newFile = new File(path);
						//}	
						} else {
							String path = originalFile;
							path = path.replace("file:/", "");
							path = path.substring(0, path.lastIndexOf("/")) + "/" + externalFileName;
							newFile = new File(path);
						}
					}
					
					if(newFile.exists() && !editor.isTrace){
						
						Document document = null;
						
						if(externalFiles.containsKey(newFile.getAbsolutePath())){		
							document = externalFiles.get(newFile.getAbsolutePath());		
						} else {
							DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
							DocumentBuilder builder;
							
							try {
								builder = factory.newDocumentBuilder();
								document = builder.parse(newFile);
								externalFiles.put(newFile.getAbsolutePath(), document);
							} catch (SAXException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();	
							} catch (ParserConfigurationException e) {
								e.printStackTrace();
							}
						}		*/
						
						//NodeList nl = document.getElementsByTagName("function");
						NodeList nl = elTemp.getElementsByTagName("function");
						
						if(nl != null && nl.getLength() > 0) {
							
							for(int i = 0 ; i < nl.getLength(); i++) {
								Element function = (Element)nl.item(i);

								if(function.getAttribute("name").equals(np.targetName) && function.getAttribute("act").equals(np.act)){
									NodeProxy np2 = new NodeProxy(function);
									//np2.externalFile = newFile.getName().replace("." + AssemblyContentProvider.PROPERTIES_EXT, ".exe");
									np2.setCallingNode(np.getCallingNode());
	
									NodeList nl2 = function.getElementsByTagName("call");
									if(nl2 != null && nl2.getLength() > 0) {
										
										for(int j = 0 ; j < nl2.getLength(); j++) {
											Element function2 = (Element)nl2.item(j);
											NodeProxy np3 = new NodeProxy(function2);
											np3.localFunction = false;
											
											if(function2.getAttribute("externalfile").length() > 0){
												np3.externalFile = function2.getAttribute("externalfile");
												np3.setCallingNode(np2);
											} else {
												//np3.externalFile = newFile.getName().replace("." + AssemblyContentProvider.PROPERTIES_EXT, ".exe");
												np3.setCallingNode(np2);
											}
											
											if(function2.getAttribute("name") != null){
												//list.add(np3);
												//messageList.put(function2.getAttribute("name"), np3);
												Boolean allowMessages = editor.expandedList.get(np2.externalFile + ":" + np2.targetName);
												if(allowMessages != null && allowMessages.booleanValue() == true){
												} else {
													list.add(np3);
													messageList.put(function2.getAttribute("name"), np3);
												}
											}
										}
									}	
									break;
								}
							}
						}		
					//} else {
					//	System.out.println("External file does not exist: " + externalFileName);
					//}
				}
			} 
		}

		//remove duplicate callbacks
		/*String firstMessage = "";
		for (int i = 0; i < list.size(); i++) {
			NodeProxy node = list.get(i);
			if(i == 0){
				firstMessage = list.get(0).targetName;
			} else {
				if(!list.get(i).targetName.equals(firstMessage)){
					break;
				} 
				if(i == list.size()-1){
					//all messages were the same, need to make only one
					NodeProxy np2 = list.get(0);
					list = new ArrayList<NodeProxy>();
					list.add(np2);
				}
			}
		}*/
		return list.toArray();
	}
	
	public Object getTarget(Object message) {
		return message;
	}
	
	public boolean isCall(Object message) {
		if(message != null){
			NodeProxy np = (NodeProxy)message;
			Boolean allowMessages = editor.expandedList.get(np.externalFile + ":" + np.targetName);
			if(allowMessages != null && allowMessages.booleanValue() == true){
				//return false;
			}
		}
		return true;
	}
	
	public Object[] getElements(Object input) {
	
		if(input instanceof Document){
			Document doc = (Document)input;
			this.doc = doc;
			
			//get the root element
			Element rootElement = doc.getDocumentElement();
			elTemp = rootElement;
			Element function = null;
			
			NodeList nl = rootElement.getElementsByTagName("function");
			function = (Element)nl.item(0);
			/*for(int i = 0; i < nl.getLength(); i++){
				Element temp = (Element)nl.item(i);
				if(temp.getAttribute("name").equals(method)){
					function = temp;
					break;
				}
			}*/
			
			//get each function element
			//if(rootElement.getElementsByTagName("functionEntryPoint").getLength() > 0){
			//	NodeList nl1 = rootElement.getElementsByTagName("functionEntryPoint");;
				
				if(rootElement.getNodeName().equals("softwarePackage")){
					isCAPackage = true;
				}
				
				if(isCAPackage){
					//CA
					//TODO CA modification
					localFile = function.getAttribute("section");
				} else {
					//localFile = function.getAttribute("module");
					localFile = rootElement.getAttribute("filename").replace(".ose", "");
				}
			//} else {
			//	localFile = rootElement.getAttribute("filename").replace(".ose", "");
			//}

			rootNode = new NodeProxy(function);


			return new Object[] {rootNode};
		}
		return new Object[]{};
	}
	
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
	
	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		// TODO Auto-generated method stub
		
	}
	
	public Object getContainingGroup(Object lifelineOrGroup) {
		if(lifelineOrGroup instanceof String){
			LifelineProxy llp = lifelineList.get(lifelineOrGroup);
			PackageProxy ppp;
			
			if(llp.externalFile.length() > 0){
				ppp = new PackageProxy(llp.externalFile);
			} else {
				ppp = new PackageProxy(localFile);
			}
			return ppp;
		}
		return null;
	}
	
	public boolean hasContainingGroup(Object lifelineOrGroup) {
		if(lifelineOrGroup instanceof String){
			return true;
		}
			
		if(lifelineOrGroup instanceof String){
			LifelineProxy llp = lifelineList.get(lifelineOrGroup);
			if(llp!=null){
				if(llp.externalFile.length() > 0){
					return true;
				}
			}
		}
		return false;
	}
}