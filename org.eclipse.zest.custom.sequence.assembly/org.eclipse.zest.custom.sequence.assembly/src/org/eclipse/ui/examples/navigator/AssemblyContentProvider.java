/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * Licensed Material - Property of IBM. All rights reserved.
 * US Government Users Restricted Rights - Use, duplication or disclosure
 * restricted by GSA ADP Schedule Contract with IBM Corp.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.navigator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.progress.UIJob;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Provides the properties contained in a *.properties file as children of that
 * file in a Common Navigator.  
 * 
 * @author jbaldwin, jonah
 * @since 3.2 
 */
public class AssemblyContentProvider implements ITreeContentProvider,
		IResourceChangeListener, IResourceDeltaVisitor {
  
	private static final Object[] NO_CHILDREN = new Object[0];

	public static final Object PROPERTIES_EXT = "axml"; //$NON-NLS-1$
	public static final Object DYNAMIC_EXT = "trace"; //$NON-NLS-1$
	
	private final Map cachedModelMap = new HashMap();

	private StructuredViewer viewer;
	
	/**
	 * Create the PropertiesContentProvider instance.
	 * 
	 * Adds the content provider as a resource change listener to track changes on disk.
	 *
	 */
	public AssemblyContentProvider() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);//, IResourceChangeEvent.POST_CHANGE);
	}

	/**
	 * Return the function elements for a *.axml IFile or
	 * NO_CHILDREN for otherwise.
	 */
	public Object[] getChildren(Object parentElement) {  
		NodeList children = null;
		if(parentElement instanceof Document) 
		{
			Document doc = (Document) parentElement;
			Element rootElement = doc.getDocumentElement();
			children = rootElement.getElementsByTagName("section"); //$NON-NLS-1$
			if(children.getLength() == 0){
				children = rootElement.getElementsByTagName("function"); //$NON-NLS-1$
			} else {
				children.item(0).appendChild(doc.createTextNode("D"));
			}
		}
		else if(parentElement instanceof IFile)
		{
			IPath path = ((IFile) parentElement).getRawLocation();
			File file = path.toFile();
			
			if(!file.exists())
			{
				System.err.println("ERROR: File not found."); //$NON-NLS-1$
				return NO_CHILDREN;
			} 
			
			Document doc = createSequenceFromXML(file);
			/*if(doc == null)
				doc = createSequenceFromXML(file); 
			*/
			if(doc != null){
				Element rootElement = doc.getDocumentElement();
				children = rootElement.getElementsByTagName("section"); //$NON-NLS-1$
				if(children.getLength() == 0){
					children = rootElement.getElementsByTagName("function"); //$NON-NLS-1$
				} else {
					children.item(0).appendChild(doc.createTextNode("D"));
				}
			}
			else
				System.err.println("ERROR: Document not created from file."); //$NON-NLS-1$
			
		}else if(parentElement instanceof Element){

		    Element rootElement = (Element) parentElement;
		    children = rootElement.getElementsByTagName("function");
		    
		}else{
			return NO_CHILDREN;
		}

		Object[] childrenObjects = new Object[children.getLength()];

		for(int i = 0; i<children.getLength(); i++)
		{
			childrenObjects[i] = children.item(i);
		}
		return childrenObjects;
	}  

	/**
	 * Load the model from the given file, if possible.  
	 * @param modelFile The IFile which contains the persisted model 
	 */ 
	private synchronized Properties updateModel(IFile modelFile) { 
		
		/*if(PROPERTIES_EXT.equals(modelFile.getFileExtension()) ) {
			Properties model = new Properties();
			if (modelFile.exists()) {
				try {
					model.load(modelFile.getContents()); 
					
					String propertyName; 
					List properties = new ArrayList();
					for(Enumeration names = model.propertyNames(); names.hasMoreElements(); ) {
						propertyName = (String) names.nextElement();
						properties.add(new PropertiesTreeData(propertyName,  model.getProperty(propertyName), modelFile));
					}
					PropertiesTreeData[] propertiesTreeData = (PropertiesTreeData[])
						properties.toArray(new PropertiesTreeData[properties.size()]);
					
					cachedModelMap.put(modelFile, propertiesTreeData);
					return model; 
				} catch (IOException e) {
				} catch (CoreException e) {
				}
			} else {
				cachedModelMap.remove(modelFile);
			}
		}*/
		
		return null; 
	}

	public Object getParent(Object element) {
		if (element instanceof Element) {
			Element data = (Element) element;
			return data.getParentNode();
		} 
		return null;
	}

	public boolean hasChildren(Object element) {		
		if(element instanceof IFile) {
			return PROPERTIES_EXT.equals(((IFile) element).getFileExtension()) || DYNAMIC_EXT.equals(((IFile) element).getFileExtension());
		} else {
			if(element instanceof Element){
				if(((Element) element).getNodeName().equals("section"))
					return true;
			}
		}
		return false;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
		cachedModelMap.clear();
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this); 
	}

	public void inputChanged(Viewer aViewer, Object oldInput, Object newInput) {
		if (oldInput != null && !oldInput.equals(newInput))
			cachedModelMap.clear();
		viewer = (StructuredViewer) aViewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		viewer.refresh();
		/*IResourceDelta delta = event.getDelta();
		try {
			delta.accept(this);
		} catch (CoreException e) { 
			e.printStackTrace();
		} */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
	 */
	public boolean visit(IResourceDelta delta) {

		IResource source = delta.getResource();
		switch (source.getType()) {
		case IResource.ROOT:
		case IResource.PROJECT:
		case IResource.FOLDER:
			return true;
		case IResource.FILE:
			final IFile file = (IFile) source;
			if(file.getFileExtension().equals(PROPERTIES_EXT) || file.getFileExtension().equals(DYNAMIC_EXT)){
				new UIJob("Update Assembly Model in CommonViewer") { //$NON-NLS-1$
					public IStatus runInUIThread(IProgressMonitor monitor) {
						if (viewer != null && !viewer.getControl().isDisposed())
							viewer.refresh(file);
						return Status.OK_STATUS;						
					}
				}.schedule();
			}
			/*if (PROPERTIES_EXT.equals(file.getFileExtension())) {
				//updateModel(file);
				new UIJob("Update Properties Model in CommonViewer") { //$NON-NLS-1$
					public IStatus runInUIThread(IProgressMonitor monitor) {
						if (viewer != null && !viewer.getControl().isDisposed())
							viewer.refresh(file);
						return Status.OK_STATUS;						
					}
				}.schedule();
			}*/
			return false;
		}
		return false;
	} 
	/**
	 * @param xmlFile
	 * @return document
	 */
	public static Document createSequenceFromXML(File xmlFile) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(xmlFile);
			return document;

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
