package org.eclipse.zest.custom.sequence.assembly.editors;

import java.io.File;
import java.util.HashMap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.zest.custom.sequence.assembly.Activator;
import org.eclipse.zest.custom.sequence.assembly.editors.AssemblySequenceContentProvider.LifelineProxy;
import org.eclipse.zest.custom.sequence.widgets.Activation;
import org.eclipse.zest.custom.sequence.widgets.Lifeline;
import org.eclipse.zest.custom.uml.viewers.IStylingSequenceLabelProvider;
import org.eclipse.zest.custom.uml.viewers.UMLSequenceViewer;

/**
 * Provides labels for the sequence viewer.
 * @author jbaldwin
 */

public class DynamicSequenceLabelProvider implements IStylingSequenceLabelProvider, IColorProvider {

	private AssemblySequenceContentProvider ascp;
	private ImageDescriptor descriptor = Activator.getImageDescriptor("icons" + File.separator + "int_obj.gif");
	private Image externalimage = descriptor.createImage();
	private ImageDescriptor descriptor2 = Activator.getImageDescriptor("icons" + File.separator + "public_co.gif");
	private Image localimage = descriptor2.createImage();
	private UMLSequenceViewer viewer;
	private HashMap<String, LifelineProxy> lifelines;
	
	private WorkbenchLabelProvider provider = new WorkbenchLabelProvider();
	private static enum Colors {
		DARK_GREEN,
		DARK_BLUE,
		DARK_RED,
		GREEN,
		BLUE,
		RED, LIGHT_GREEN, LIGHT_RED, LIGHT_BLUE, LIGHT_YELLOW, PACKAGE;
		Color c;
		Color getColor() {
			switch (this) {
			case DARK_GREEN:
				if (c == null) {
					c = new Color(Display.getCurrent(), 0,100,0);
				}
				return c;

			case DARK_RED:
				if (c == null) {
					c = new Color(Display.getCurrent(), 100, 0,0);
				}
				return c;
			case DARK_BLUE:
				if (c == null) {
					c = new Color(Display.getCurrent(), 0, 0,100);
				}
				return c;
			case GREEN:
				if (c == null) {
					c = new Color(Display.getCurrent(), 70,255,70);
				}
				return c;

			case RED:
				if (c == null) {
					c = new Color(Display.getCurrent(), 255, 70,70);
				}
				return c;
			case BLUE:
				if (c == null) {
					c = new Color(Display.getCurrent(), 70,70,255);
				}
				return c;
			case LIGHT_GREEN:
				if (c == null) {
					c = new Color(Display.getCurrent(), 200,255,200);
				}
				return c;

			case LIGHT_RED:
				if (c == null) {
					c = new Color(Display.getCurrent(), 255, 200,200);
				}
				return c;
			case LIGHT_BLUE:
				if (c == null) {
					c = new Color(Display.getCurrent(), 200,200,255);
				}
				return c;
			case LIGHT_YELLOW:
				if (c == null) {
					c = new Color(Display.getCurrent(),255,255,100);
				}
				return c;
			case PACKAGE:
				if (c == null) {
					c = new Color(Display.getCurrent(), 255, 200, 100);
				}
				return c;
			}
			
		
			
			return null;
		}
		void dispose() {
			if (c != null && !c.isDisposed()) {
				c.dispose();
			}
			c = null;
		}
	}
	

	public DynamicSequenceLabelProvider(UMLSequenceViewer viewer) {
		//this.ascp = ascp;
		this.viewer = viewer;
	}


	/**
	 * This is the only method that is additional to the standard JFace label providers. 
	 * In this implementation, classes are 
	 */
	public String getStereoType(Object element) {
		return null;
	}

	
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	public void dispose() {
		provider.dispose();
		/*Colors.DARK_RED.dispose();
		Colors.DARK_GREEN.dispose();
		Colors.DARK_BLUE.dispose();
		Colors.BLUE.dispose();
		Colors.RED.dispose();
		Colors.GREEN.dispose();
		Colors.PACKAGE.dispose();*/
	}

	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return true;
	}

	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	public Color getBackground(Object element) {
		System.out.println("package " + element.getClass());
		return Colors.PACKAGE.getColor();
		//return null;
	}

	public Color getForeground(Object element) {
		//return Colors.PACKAGE.getColor();
		return null;
	}


	public Image getImage(Object element) {
		if(element instanceof Lifeline){
			Lifeline line = (Lifeline)element;
			Image img = line.getImage();
			if(img == null)
				return localimage;
			else return img;
		} else if(element instanceof Activation){
			Lifeline line = ((Activation)element).getLifeline();
			Image img = line.getImage();
			if(img == null)
				return localimage;
			else return img;
		}
		return null;
	}


	public String getText(Object element) {
		if(element instanceof Lifeline){
			return ((Lifeline)element).getText();
		}
		if(element instanceof String){
			return (String)element;
		}
		if(element instanceof Activation){
			return ((Activation)element).getLifeline().getText();
		}
		if(element == null){
			return "User";
		}
		return element.toString();
	}


	public int getLifelineStyle(Object element) {
		return -1;
	}


	public int getMessageLineStyle(Object element) {
		return -1;
	}


	public int getMessageSourceStyle(Object element) {
		return -1;
	}


	public int getMessageTargetStyle(Object element) {
		return -1;
	}

}
