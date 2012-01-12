package org.eclipse.zest.custom.sequence.assembly.editors;

import java.io.File;
import java.util.HashMap;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.zest.custom.sequence.assembly.Activator;
import org.eclipse.zest.custom.sequence.assembly.editors.AssemblySequenceContentProvider.LifelineProxy;
import org.eclipse.zest.custom.sequence.assembly.editors.AssemblySequenceContentProvider.PackageProxy;
import org.eclipse.zest.custom.sequence.assembly.preferences.PreferenceConstants;
import org.eclipse.zest.custom.sequence.widgets.Activation;
import org.eclipse.zest.custom.sequence.widgets.Lifeline;
import org.eclipse.zest.custom.uml.viewers.IStylingSequenceLabelProvider;
import org.eclipse.zest.custom.uml.viewers.UMLSequenceViewer;

/**
 * Provides labels for the sequence viewer.
 * @author jbaldwin
 */

public class AssemblySequenceLabelProvider implements IStylingSequenceLabelProvider, IColorProvider {

	private AssemblySequenceContentProvider ascp;
	private static ImageDescriptor descriptor = Activator.getImageDescriptor("icons" + File.separator + "int_obj.gif");
	public static Image externalimage = descriptor.createImage();
	private static ImageDescriptor descriptor2 = Activator.getImageDescriptor("icons" + File.separator + "public_co.gif");
	public static Image localimage = descriptor2.createImage();
	//private ImageDescriptor commentd = Activator.getImageDescriptor("icons" + File.separator + "public_comment.gif");
	
	private static ImageDescriptor commentdg = Activator.getImageDescriptor("icons" + File.separator + "star-gold-ball-green32.png");
	public static Image localcommentgold = commentdg.createImage();	
	
	private static ImageDescriptor commentdgr = Activator.getImageDescriptor("icons" + File.separator + "star-green-ball-green32.png");
	public static Image localcommentgreen = commentdgr.createImage();	
	
	private static ImageDescriptor commentdr = Activator.getImageDescriptor("icons" + File.separator + "star-red-ball-green32.png");
	public static Image localcommentred = commentdr.createImage();	
	
	//private ImageDescriptor commentd2 = Activator.getImageDescriptor("icons" + File.separator + "external_comment.gif");
	//private Image externalcomment = commentd2.createImage();
	
	private static ImageDescriptor commenteg = Activator.getImageDescriptor("icons" + File.separator + "star-gold-ball-blue32.png");
	public static Image externalcommentgold = commenteg.createImage();	
	
	private static ImageDescriptor commentegr = Activator.getImageDescriptor("icons" + File.separator + "star-green-ball-blue32.png");
	public static Image externalcommentgreen = commentegr.createImage();	
	
	private static ImageDescriptor commenter = Activator.getImageDescriptor("icons" + File.separator + "star-red-ball-blue32.png");
	public static Image externalcommentred = commenter.createImage();	
	
	
	private UMLSequenceViewer viewer;
	private HashMap<String, LifelineProxy> lifelines;
	IPreferenceStore store = Activator.getDefault().getPreferenceStore();
	
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
	

	public AssemblySequenceLabelProvider(AssemblySequenceContentProvider ascp, UMLSequenceViewer viewer) {
		this.ascp = ascp;
		this.viewer = viewer;
	}


	/**
	 * This is the only method that is additional to the standard JFace label providers. 
	 * In this implementation, classes are 
	 */
	public String getStereoType(Object element) {
		lifelines = ascp.lifelineList;
		if(element instanceof String){
			if(element.equals("User")){
				return null;
			}
			LifelineProxy llp = lifelines.get(element);
			
			if(llp != null){
				return llp.sterotype;
			}
		}
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
		if(element instanceof NodeProxy){
			NodeProxy np = (NodeProxy)element;
			if(np.innerloop){
				return new Color(Activator.getDefault().getWorkbench().getDisplay(), 255,99,71);
			}
			return Colors.LIGHT_GREEN.getColor();
		} else {
			if(element instanceof PackageProxy){
				if(((PackageProxy)element).identifier.equals("User")){
					return null;
				}
				return Colors.PACKAGE.getColor();
			}
		}
		return null;
	}

	public Color getForeground(Object element) {
		if(element instanceof NodeProxy){
			return Colors.DARK_BLUE.getColor();
		}
		return null;
	}


	public Image getImage(Object element) {
		
		if(element instanceof String){
			if((element.equals("User") || element.equals("UserEEEEUser"))){
				return null;
			}
			LifelineProxy llp = (LifelineProxy) ascp.lifelineList.get(element);
			if(llp!=null){
				//TODO CA modification
				if(llp.externalFile.equals(llp.module)){
					return localimage;
					//int count = CommentView.getCommentCount(llp.position.toString());
					/*if(store.getString(PreferenceConstants.P_COMMENTS).equals(PreferenceConstants.P_RET_COMMENTS)){
						int count = CommentView.getCommentCount(llp.externalFile + "," + llp.identifier + ":" + llp.address.toString());
						if(count > 0){
							if(count >= 25)
								return localcommentgreen;
							if(count >= 10)
								return localcommentgold;
							
							return localcommentred;
						} else {
							return localimage;
						}
					} else {
						return localimage;
					}*/
				}
				if(llp.externalFile.length() > 0 && !llp.externalFile.equals(ascp.getLocalFile())){
					return externalimage;
					//return externalcomment;
					/*if(store.getString(PreferenceConstants.P_COMMENTS).equals(PreferenceConstants.P_RET_COMMENTS)){
						int count = CommentView.getCommentCount(llp.externalFile + "," + llp.identifier + ":" + llp.address.toString());
						if(count > 0){
							//return externalcomment;
							if(count >= 25)
								return externalcommentgreen;
							if(count >= 10)
								return externalcommentgold;
							
							return externalcommentred;
						} else {
							return externalimage;
						}
					} else {
						return externalimage;
					}*/
				} else {
					return localimage;
					//return localcomment;
					/*if(store.getString(PreferenceConstants.P_COMMENTS).equals(PreferenceConstants.P_RET_COMMENTS)){
						int count = CommentView.getCommentCount(llp.externalFile + "," + llp.identifier + ":" + llp.address.toString());
						if(count > 0){
							if(count >= 25)
								return localcommentgreen;
							if(count >= 10)
								return localcommentgold;
							
							return localcommentred;
						} else {
							return localimage;
						}
					} else {
						return localimage;
					}*/
				}
			} else {
				return localimage;
				//return localcomment;
				/*if(store.getString(PreferenceConstants.P_COMMENTS).equals(PreferenceConstants.P_RET_COMMENTS)){
					int count = CommentView.getCommentCount(llp.externalFile + "," + llp.identifier + ":" + llp.address.toString());
					if(count > 0){
						if(count >= 25)
							return localcommentgreen;
						if(count >= 10)
							return localcommentgold;
						
						return localcommentred;
					} else {
						return localimage;
					}
				} else {
					return localimage;
				}*/
			}
		} else {
			if(element instanceof NodeProxy){
				NodeProxy np = (NodeProxy)element;
				if(((NodeProxy) element).targetName.equals("User")){
					return null;
				}
				if(np.module.equals(np.externalFile)){
					//Breadcrumbs
					//return localimage;
					String address = np.calladdress;
					if(np.calladdress == null)
						address = np.functionaddress;
					
					/*if(store.getString(PreferenceConstants.P_COMMENTS).equals(PreferenceConstants.P_RET_COMMENTS)){
						int count = CommentView.getCommentCount(address.toString());
						if(count > 0){
							if(count >= 25)
								return localcommentgreen;
							if(count >= 10)
								return localcommentgold;
							
							return localcommentred;
						} else {
							return localimage;
						}
					} else {*/
						return localimage;
					//}
				}
				//TODO CA modification
				if(np.externalFile.length() > 0 && !np.externalFile.equals(ascp.getLocalFile())){
					//calls in the diagram
					return externalimage;
					/*String address = np.calladdress;
					if(np.calladdress == null)
						address = np.functionaddress;
					
					if(store.getString(PreferenceConstants.P_COMMENTS).equals(PreferenceConstants.P_RET_COMMENTS)){
						int count = CommentView.getCommentCount(np.externalFile + "," + np.targetName + ":" + np.calladdress + "->" + np.functionaddress);
						if(count > 0){
							//return externalcomment;
							if(count >= 25)
								return externalcommentgreen;
							if(count >= 10)
								return externalcommentgold;
							
							return externalcommentred;
						} else {
							return externalimage;
						}
					} else {
						return externalimage;
					}*/
				} else {
					return localimage;
					//return localcomment;
					/*String address = np.calladdress;
					if(np.calladdress == null)
						address = np.functionaddress;

					if(store.getString(PreferenceConstants.P_COMMENTS).equals(PreferenceConstants.P_RET_COMMENTS)){
						int count = CommentView.getCommentCount(np.externalFile + "," + np.targetName + ":" + np.calladdress + "->" + np.functionaddress);
						if(count > 0){
							if(count >= 25)
								return localcommentgreen;
							if(count >= 10)
								return localcommentgold;
							
							return localcommentred;
						} else {
							return localimage;
						}
					} else {
						return localimage;
					}*/
				}
			} else {
				if(element instanceof Activation){
					Lifeline line = ((Activation)element).getLifeline();
					return line.getImage();
				}
			}
		}
		return null;
	}


	public String getText(Object element) {
		if (element instanceof NodeProxy) {
			return ((NodeProxy) element).targetName;
		}
		
		//external file with the same name, hash value needs to be changed
		if(element instanceof String){
			//EEEE external file
			if(((String) element).contains("EEEE")){
				return ((String)element).substring(0, ((String)element).indexOf("EEEE"));
			}
		}
		
		if(element instanceof Activation){
			return ((Activation)element).getText();
		}
		
		if(element == null){
			return "";
		}
		
		return element.toString();
	}


	public int getLifelineStyle(Object element) {
		/*if (element instanceof IAdaptable) {
			IJavaElement javaElement = (IJavaElement) ((IAdaptable)element).getAdapter(IJavaElement.class);
			if (javaElement instanceof IPackageFragment) {
				return Lifeline.PACKAGE;
			}
		}*/
		if(element instanceof String && (element.equals("User") || element.equals("UserEEEEUser"))){
			//if(element.equals("User")){
			//	viewer.getChart().getRootActivation().getLifeline().setClassStyle(Lifeline.ACTOR);
			//}
			return Lifeline.ACTOR;
		} else {
			if(element instanceof PackageProxy){
				if(((PackageProxy)element).identifier.equals("User")){
					return Lifeline.ACTOR;
				}
			}
		}
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
