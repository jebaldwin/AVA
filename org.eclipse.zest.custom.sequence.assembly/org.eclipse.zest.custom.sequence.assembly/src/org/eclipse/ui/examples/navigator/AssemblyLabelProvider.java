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

import org.w3c.dom.Element;

/**
 * Provides a label and icon for objects of type {@link Element}. 
 * 
 * @author jbaldwin, jonah
 * @since 3.2 
 */
public class AssemblyLabelProvider extends LabelProvider implements
		ILabelProvider, IDescriptionProvider {
  
	ImageDescriptor descriptor = Activator.getImageDescriptor("icons" + File.separator + "public_co.gif"); //$NON-NLS-1$
	Image image = descriptor.createImage();

	public Image getImage(Object element) {
		if (element instanceof Element) {
			return image;
		}
		return null;
	}

	public String getText(Object element) {
		if (element instanceof Element) {
			if (((Element) element).getTagName()=="function") //$NON-NLS-1$ 
			{
				return ((Element)element).getAttribute("name"); //$NON-NLS-1$
			}
			if (((Element) element).getTagName()=="section"){
				return ((Element)element).getAttribute("name"); //$NON-NLS-1$
			}
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
