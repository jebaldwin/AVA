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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.navigator.IDescriptionProvider;
import org.eclipse.zest.custom.sequence.assembly.Activator;
import org.eclipse.zest.custom.sequence.assembly.editors.NodeProxy;

import org.w3c.dom.Element;

/**
 * Provides a label and icon for objects of type {@link Element}. 
 * 
 * @author jbaldwin, jonah
 * @since 3.2 
 */
public class TraceLabelProvider extends LabelProvider implements
		ILabelProvider, IDescriptionProvider {
  
	private ImageDescriptor descriptor = Activator.getImageDescriptor("icons" + File.separator + "int_obj.gif");
	private Image externalimage = descriptor.createImage();
	private ImageDescriptor descriptor2 = Activator.getImageDescriptor("icons" + File.separator + "public_co.gif");
	private Image localimage = descriptor2.createImage();

	public Image getImage(Object element) {
		if (element instanceof Element) {
			String external = ((Element)element).getAttribute("externalfile");
			String module = ((Element)element).getAttribute("module");
			if(external.equals(module))
				return localimage;
			else
				return externalimage;
		}
		if (element instanceof NodeProxy){
			String external = ((NodeProxy)element).externalFile;
			String module = ((NodeProxy)element).module;
			if(external.equals(module))
				return localimage;
			else
				return externalimage;
		}
		if (element instanceof String){
			return localimage;
		}
		return null;
	}

	public String getText(Object element) {
		if (element instanceof Element) {
			if (((Element) element).getTagName()=="call"){
				return ((Element)element).getAttribute("name"); //$NON-NLS-1$
			}
		}  
		if (element instanceof NodeProxy){
			return ((NodeProxy)element).targetName;
		}
		if (element instanceof String){
			return (String) element;
		}
		return null;
	}

	public String getDescription(Object anElement) {
		if (anElement instanceof Element) {
			return ((Element)anElement).getTagName(); 
		}
		return null;
	}
  
}
