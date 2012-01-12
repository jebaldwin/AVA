package org.eclipse.zest.custom.sequence.assembly.editors;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.examples.navigator.AssemblyContentProvider;
import org.eclipse.zest.custom.uml.viewers.ISequenceChartContentProvider;
import org.eclipse.zest.custom.uml.viewers.ISequenceContentExtension;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author jbaldwin
 */
public class ReversedAssemblySequenceContentProvider extends AssemblySequenceContentProvider implements ISequenceChartContentProvider, ISequenceContentExtension {

	
	public ReversedAssemblySequenceContentProvider(IEditorInput input, String method, IPath localPath, AssemblySequenceEditor editor) {
		super(input, method, localPath, editor);
	}

	public Object[] getMessages(Object activation) {

		NodeProxy np = (NodeProxy)activation;
		ArrayList<NodeProxy> list = new ArrayList<NodeProxy>();

		System.out.println("get messages for " + np.targetName);
		
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
			if(editor.isTrace || ((np.node.getAttribute("externalfile").equals(np.node.getAttribute("module")) || np.node.getAttribute("externalfile").equals("User")) && np.node.getOwnerDocument().getDocumentURI().equals(doc.getDocumentURI()))){
								
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
					if(function2.getAttribute("name").equals(np.targetName)){
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
					String originalFile = np.node.getOwnerDocument().getDocumentURI();
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
						}		
						
						
						NodeList nl = document.getElementsByTagName("call");
						if(nl != null && nl.getLength() > 0) {
							for(int i = 0 ; i < nl.getLength(); i++) {
								Element call = (Element)nl.item(i);
								
								if(call.getAttribute("name").equals(np.targetName)){
									//need to get function that the call belongs to
									Element function = (Element)call.getParentNode();
									
									NodeProxy np2 = new NodeProxy(function);
									
									//don't include calls to self or external calls of the same name
									if(!np2.targetName.equals(np.targetName)){
										np2.externalFile = newFile.getName().replace("." + AssemblyContentProvider.PROPERTIES_EXT, ".exe");
										
										np2.setCallingNode(np.getCallingNode());
										
										list.add(np2);
										messageList.put(function.getAttribute("name"), np2);
									}
								}
							}
						}
					} else {
						System.out.println("External file does not exist: " + externalFileName);
					}
				}
			} 
		}

		//remove duplicate callbacks
		String firstMessage = "";
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
		}
		return list.toArray();
	}
}