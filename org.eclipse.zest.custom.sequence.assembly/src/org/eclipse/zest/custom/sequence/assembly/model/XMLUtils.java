package org.eclipse.zest.custom.sequence.assembly.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.saxon.Transform;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.examples.navigator.AssemblyContentProvider;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import cs.uvic.ca.ice.model.CallSite;
import cs.uvic.ca.ice.model.Function;
import cs.uvic.ca.ice.model.Instance;

/**
 * @author jbaldwin
 */
public class XMLUtils {

	/* AVA XML (AXML) Format
	 * 
	 * - The root element is a 'sourcecode' entity that describes the binary
	 * 	this AXML represents
	 * - The file must contain at least one 'functionEntryPoint' entity.  If
	 * 	only one exists then the next deepest element is nested within it.  If
	 * 	more than one exists they are listed individually with the nesting occuring
	 * 	in the last occurrence
	 * - The 'function' entity represent a function defined in the binary
	 * - Zero or more 'call' entities are nested inside the 'function'.  One
	 * 	exists for each function call (e.g. x86's call instruction) made.
	 * - The 'index' attribute used through out is the 'ord' from IDA Pro.  Ultimately
	 * 	this number must either uniquely identify the function or indicate the function
	 * 	is defined outside the scope of this module. 
	 * - The 'externalfile' attribute used on calls is really just the textual name of the 
	 * 	module the callee is defined in.
	 * - All integral values are given in hex without the preceding '0x'
	 * 
	 * Sample file:
	 *  
	 * <sourcecode filename="module/executable/instance name">
	 * 	<functionEntryPoint address="" index="" module="" name="" />
	 * 	<functionEntryPoint address="" index="" module="" name="" />
	 * 	<functionEntryPoint address="" index="" module="" name="" >
	 * 		<function address="" index="" module="" name="" >
	 * 			<call calladdress="" externalfile="" functionaddress="" 
	 * 				index="" 
	 * 				module="" 
	 * 				name="" />
	 * 			<call calladdress="" externalfile="" functionaddress="" 
	 * 				index="" 
	 * 				module="" 
	 * 				name="" />
	 * 			<call calladdress="" externalfile="" functionaddress="" 
	 * 				index="" 
	 * 				module="" 
	 * 				name="" />
	 * 		</function>
	 * 		<function address="" index="" module="" name="" >
	 * 		</function>
	 * 	</functionEntryPoint>
	 * </sourcecode>
	 */
	
	public static boolean dumpInstanceToAXML(Instance ins, File axmlFile) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		Document document = builder.newDocument();
		
		Element el = document.createElement("sourcecode");
		el.setAttribute("filename", ins.getName());

		Element fEl = null;
		Element el2 = null;

		Collection<Function> functions = ins.getFunctions();
		Iterator<Function> func_iter = functions.iterator();
		Function f;

		System.out.println("Processing functions: " + functions.size());
		
		// create functionEntryPoint elements
		while(func_iter.hasNext()) {
			f = func_iter.next();
		
			if(f.getEntryPoint()) {
				System.out.println("Found entry: " + f.getName());
				
				el2 = document.createElement("functionEntryPoint");
				el2.setAttribute("index", f.getIndex().toString());
				el2.setAttribute("address", f.getStart().toString());
				el2.setAttribute("name", f.getName());
				el2.setAttribute("module", f.getModule());
				el.appendChild(el2);
			}
		}

		
		func_iter = functions.iterator();
		while(func_iter.hasNext()) {
			f = func_iter.next();
			
			//start of function element
			fEl = document.createElement("function");
			fEl.setAttribute("index", f.getIndex().toString());
			fEl.setAttribute("address", f.getStart().toString());
			fEl.setAttribute("name", f.getName());
			fEl.setAttribute("module", f.getModule());

			if(el2 != null){
				el2.appendChild(fEl);
			} else {
				el.appendChild(fEl);
			}
				
			//call line
			Collection<CallSite> calls = f.getCalls();
			Iterator<CallSite> call_iter = calls.iterator();
			CallSite call;
			Function target;
			
			while(call_iter.hasNext()) {
				call = call_iter.next();
				target = call.target();
				
				Element el3 = document.createElement("call");
			
				if(target != null) {
					if(target.getIndex() == -1)
						el3.setAttribute("index", "external");
					else
						el3.setAttribute("index", call.target().getIndex().toString());
			
					el3.setAttribute("calladdress", call.from().toString());
					el3.setAttribute("functionaddress", call.callee().toString());
					el3.setAttribute("name", call.target().getName());
					el3.setAttribute("externalfile", call.target().getModule());
					el3.setAttribute("module", f.getModule());

					fEl.appendChild(el3);
				} else {
					System.out.println("call target is null");
				}
			}
		}
		
		// Prepare the DOM document for writing
        Source source = new DOMSource(el);

        if(!axmlFile.exists()){
        	axmlFile.delete();
        }
        
       	try {
			axmlFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
 
        Result result = new StreamResult(axmlFile);

        // Write the DOM document to the file
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = null;
		try {
			transformer = tf.newTransformer();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
    
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			e.printStackTrace();
		}

		return true;
	}
	
	public static File parseTextToXML(File theFile, IFile outputFile, File ofile) {

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			Document document = builder.newDocument();
			Element el = document.createElement("sourcecode");
			el.setAttribute("filename", theFile.getName());
	
			FileReader fr = new FileReader(theFile);
			BufferedReader br = new BufferedReader(fr);
			String line;
			Element fEl = null;
			Element el2 = null;
			
			while((line = br.readLine()) != null){
				//line = line.replaceAll("\t", " ");
				String[] tokens = line.split("\\s");
				
				//process line
				if(line.startsWith(">>")){
					//create functionEntryPoint element
					el2 = document.createElement("functionEntryPoint");
					el2.setAttribute("index", tokens[1].replaceAll(":", ""));
					el2.setAttribute("address", tokens[2]);
					el2.setAttribute("name", tokens[3]);
					el2.setAttribute("module", tokens[4]);
					el.appendChild(el2);
				} else {
					if(line.startsWith(">")){
						//start of function element
						fEl = document.createElement("function");
						fEl.setAttribute("index", tokens[1].replaceAll(":", ""));
						fEl.setAttribute("address", tokens[2]);
						fEl.setAttribute("name", tokens[3]);
						if(tokens.length == 5)
							fEl.setAttribute("module", tokens[4]);
						if(el2 != null){
							el2.appendChild(fEl);
						} else {
							el.appendChild(fEl);
						}
					} else {
						if(line.startsWith("<")){
							//end of function element
							
						} else {
							if(tokens.length > 1){
								//call line
								Element el3 = document.createElement("call");
								
								if(tokens[1].replaceAll(":", "").equals("-1")) {
									el3.setAttribute("index", "external");
								} else {
									el3.setAttribute("index", tokens[1].replaceAll(":", ""));
								}
								//el3.setAttribute("address", tokens[2]);
								el3.setAttribute("calladdress", tokens[2]);
								el3.setAttribute("functionaddress", tokens[3]);
								el3.setAttribute("name", tokens[4]);
								
								if(tokens.length == 6){
									el3.setAttribute("externalfile", tokens[5]);
									el3.setAttribute("module", theFile.getName().replace(".ose", ""));
								} else {
									if(tokens.length == 7){
										el3.setAttribute("externalfile", tokens[5]);
										el3.setAttribute("module", tokens[6]);
									} else {
										el3.setAttribute("module", theFile.getName().replace(".ose", ""));
									}
								}
								
								fEl.appendChild(el3);
							}
						}
					}
				}
			}
			
			// Prepare the DOM document for writing
            Source source = new DOMSource(el);
            File file = null;
            String filename = null;
            
            // Prepare the output file
            if(outputFile == null){
            	filename = theFile.getAbsolutePath();
            } else {
            	filename = ResourcesPlugin.getWorkspace().getRoot().getRawLocation().toString() + outputFile.getFullPath().toString();
            }
            
        	filename = filename.substring(0, filename.indexOf(".ose"));
        	filename = filename.replace(".exe", "");
        	filename = filename.replace("\\", "/");
        	file = new File(filename + "." + AssemblyContentProvider.PROPERTIES_EXT);
  
            if(ofile != null)
            	file = ofile;
            
            if(!file.exists()){
            	file.createNewFile();
            }
     
            Result result = new StreamResult(file);
  
            // Write the DOM document to the file
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
        
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            transformer.transform(source, result);

            return file;
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (TransformerConfigurationException e1) {
			e1.printStackTrace();
		} catch (TransformerException e1) {
			e1.printStackTrace();
		} 
		return null;
	}
	
	public static Document createSequenceFromXML(File xmlFile) {
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
	
    public static File transformXSL(String inFilename, String outFilename, String xslFilename) {
    	
    	Bundle bund = Platform.getBundle("org.eclipse.zest.custom.sequence.assembly");
		String path = bund.getLocation() + "src" + Path.SEPARATOR + "org" + Path.SEPARATOR + "eclipse" + Path.SEPARATOR + "zest" + Path.SEPARATOR + "custom" + Path.SEPARATOR + "sequence" + Path.SEPARATOR + "assembly" + Path.SEPARATOR + "model" + Path.SEPARATOR + xslFilename;
		path = path.substring(path.indexOf("file:") + 5);
    	String[] args = {"-o", outFilename, inFilename, path};
    	
    	try {
			Transform.main(args);
			return new File(outFilename);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//XSLT 1.0
        /*try {
        	Bundle bund = Platform.getBundle("org.eclipse.zest.custom.sequence.assembly");
			String path = bund.getLocation() + "src" + Path.SEPARATOR + "org" + Path.SEPARATOR + "eclipse" + Path.SEPARATOR + "zest" + Path.SEPARATOR + "custom" + Path.SEPARATOR + "sequence" + Path.SEPARATOR + "assembly" + Path.SEPARATOR + "model" + Path.SEPARATOR + xslFilename;
			path = path.substring(path.indexOf("file:") + 5);
		    //File xslFile = new File(path);
		    
            // Create transformer factory
            TransformerFactory factory = TransformerFactory.newInstance();

            // Use the factory to create a template containing the xsl file
            Templates template = factory.newTemplates(new StreamSource(
                new FileInputStream(path)));

            // Use the template to create a transformer
            Transformer xformer = template.newTransformer();

            // Prepare the input and output files
            Source source = new StreamSource(new FileInputStream(inFilename));
            Result result = new StreamResult(new FileOutputStream(outFilename));

            // Apply the xsl file to the source file and write the result to the output file
            xformer.transform(source, result);
            return new File(outFilename);
        } catch (FileNotFoundException e) {
        	e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            // An error occurred in the XSL file
        	e.printStackTrace();
        } catch (TransformerException e) {
            // An error occurred while applying the XSL file
            // Get location of error in input file
            SourceLocator locator = e.getLocator();
            int col = locator.getColumnNumber();
            int line = locator.getLineNumber();
            String publicId = locator.getPublicId();
            String systemId = locator.getSystemId();
            e.printStackTrace();
        }*/
		return null;
    }

    public static File parseTraceToXML(File inputFile, File outputFile) {

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			Document document = builder.newDocument();
			Element el = document.createElement("dynamicTrace");
			el.setAttribute("filename", inputFile.getName());
	
			FileReader fr = new FileReader(inputFile);
			BufferedReader br = new BufferedReader(fr);
			String line;
			Element fEl = null;
			Element el2 = null;
			boolean firstCall = true;
			
			//create functionEntryPoint element
			el2 = document.createElement("functionEntryPoint");
			/*el2.setAttribute("index", tokens[1].replaceAll(":", ""));
			el2.setAttribute("address", tokens[2]);
			el2.setAttribute("name", tokens[3]);
			el2.setAttribute("module", tokens[4]);*/
			el.appendChild(el2);
			Element currEl = el;
			Element tEl = null;
			
			while((line = br.readLine()) != null){
				String[] tokens = line.split("\\s+");

				int index = 0;
				if(tokens.length > 0 && tokens[0].length() == 0){
					//get rid of token
					index = 1;
				}
				//process line
				if(line.startsWith("   CALL TO:") || line.startsWith("   RETURN TO:")){
					if(firstCall){
						
						//call line
						Element el3 = document.createElement("call");
						el3.setAttribute("index", "0");
						//el3.setAttribute("address", tokens[2]);
						el3.setAttribute("calladdress", tokens[index + 10]);
						el3.setAttribute("functionaddress", tokens[index + 7] + " " + tokens[index + 8]);
						el3.setAttribute("name", "NM000038");
						el3.setAttribute("externalfile", "Test Description");
						el.appendChild(el3);
						
						firstCall = false;
						
						//call line
						el3 = document.createElement("call");
						el3.setAttribute("index", "0");
						//el3.setAttribute("address", tokens[2]);
						el3.setAttribute("calladdress", tokens[index + 10]);
						el3.setAttribute("functionaddress", tokens[index + 7] + " " + tokens[index + 8]);
						el3.setAttribute("name", tokens[index + 4]);
						currEl.appendChild(el3);						
					}
					
					if(!tokens[index + 4].equals("NM000233") && !tokens[index + 2].equals("NM000233")){						
						//call line
						Element el3 = document.createElement("call");
						el3.setAttribute("index", "0");
						//el3.setAttribute("address", tokens[2]);
						el3.setAttribute("calladdress", tokens[index + 10]);
						el3.setAttribute("functionaddress", tokens[index + 7] + " " + tokens[index + 8]);
						el3.setAttribute("name", tokens[index + 2]);
						
						if(line.startsWith("   RETURN TO:")){
							el3.setAttribute("isreturn", "true");
						}
						
						currEl.appendChild(el3);
					}
				}/* else if(line.startsWith(" XPB DISPATCH:") || line.startsWith(" THREAD START:")){
					tEl = document.createElement("thread");
					tEl.setAttribute("address", tokens[3]);
					
					el.appendChild(tEl);
					currEl = tEl;
				} else if(line.startsWith(" XPB WAIT:") || line.startsWith(" THREAD FINISH:")){
					if(tEl != null)
						currEl = (Element)(tEl.getParentNode());
				}*/
			}
			
			// Prepare the DOM document for writing
            Source source = new DOMSource(el);
            File file = null;
            String filename = null;
            
            // Prepare the output file
            if(outputFile == null){
            	filename = inputFile.getAbsolutePath();
            } else {
            	filename = ResourcesPlugin.getWorkspace().getRoot().getRawLocation().toString() + outputFile.getAbsolutePath().toString();
            }
            
//        	filename = filename.substring(0, filename.indexOf(".ose"));
//        	filename = filename.replace(".exe", "");
//        	filename = filename.replace("\\", "/");
//        	file = new File(filename + "." + AssemblyContentProvider.PROPERTIES_EXT);
            file = outputFile;
            
            if(!file.exists()){
            	file.createNewFile();
            }
     
            Result result = new StreamResult(file);
  
            // Write the DOM document to the file
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
        
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            transformer.transform(source, result);

            return file;
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (TransformerConfigurationException e1) {
			e1.printStackTrace();
		} catch (TransformerException e1) {
			e1.printStackTrace();
		} 
		return null;
	}
}
