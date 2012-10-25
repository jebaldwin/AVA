package org.eclipse.ui.examples.navigator;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.internal.resources.File;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.zest.custom.sequence.assembly.Activator;
import org.eclipse.zest.custom.sequence.assembly.editors.AssemblySequenceEditor;
import org.eclipse.zest.custom.sequence.assembly.editors.NodeProxy;
import org.eclipse.zest.custom.sequence.assembly.editors.TraceEditor;
import org.w3c.dom.Element;

public class ASMNavigator extends CommonNavigator {

	ASMNavigator nav = this;

	public ASMNavigator() {
		super();
		System.out.println("ASMNavigator");
	}

	public void createPartControl(Composite aParent) {
		System.out.println("ASMNavigator createPartControl");
		super.createPartControl(aParent);
		getCommonViewer().addDoubleClickListener(new InternalListener());
	}

	private final class InternalListener implements IDoubleClickListener {

		public void doubleClick(DoubleClickEvent event) {
			CommonViewer view = (CommonViewer) event.getSource();
			String filename = view.getTree().getTopItem().getText();
			TreeSelection treeSel = (TreeSelection) view.getSelection();
			Object firstElement = treeSel.getFirstElement();

			if (firstElement instanceof Element) {
				String funcName = ((Element) firstElement).getAttribute("name"); //$NON-NLS-1$
				String path = ((Element) firstElement).getBaseURI();
				IFile newIFile = null;
				IPath location = null;
				String passPath = "";
				// if(selection.size() == 1) {

				// TreeSelection treeSel = (TreeSelection) selection;
				String stuff = treeSel.getFirstElement().toString();
				if (stuff.indexOf("[module: ") < 0) {
					TreePath[] paths = treeSel.getPaths();
					TreePath sel = paths[0];
					
					for (int i = 0; i < sel.getSegmentCount(); i++) {
						if (sel.getSegment(i).toString().startsWith("L/")) {
							passPath = sel.getSegment(i).toString().replace("L/", "");
							break;
						}
					}
					

					path = path.replace("file:/", "");
					path = path.replace("%20", " ");
					path = path + "$" + funcName;
					location = new Path(path);
				} else {
					TreePath treep = treeSel.getPaths()[0];
					path = ((File) treep.getSegment(1)).getLocation().toPortableString().replace("L/", "");
				}

				IWorkspace ws = ResourcesPlugin.getWorkspace();
				IFile test = ws.getRoot().getFileForLocation(location);

				if (test != null && !test.isLinked()) {
					newIFile = test;

					try {
						AssemblySequenceEditor editor = (AssemblySequenceEditor) IDE.openEditor(Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage(), newIFile,
								"org.eclipse.zest.custom.sequence.assembly.editors.AssemblySequenceEditor");
						Activator.getDefault().editors.add(editor);
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				} else {
					path = path + "$" + passPath;

					IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(path));

					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					try {
						AssemblySequenceEditor editor = (AssemblySequenceEditor) page.openEditor(new FileStoreEditorInput(fileStore),
								"org.eclipse.zest.custom.sequence.assembly.editors.AssemblySequenceEditor");
						Activator.getDefault().editors.add(editor);
					} catch (PartInitException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else {
				if (firstElement instanceof NodeProxy || firstElement instanceof File) {
					String funcName = "User";
					String module = "";
					String externalfile = "";
					
					if(firstElement instanceof NodeProxy){
						// set root editor of that diagram and open diagram
						NodeProxy np = (NodeProxy) firstElement;
						funcName = np.targetName;
						module = np.module;
						externalfile = np.externalFile;
					} else {
						String temp = ((File) treeSel.getPaths()[0].getSegment(1)).getLocation().toPortableString().replace("L/", "");
						if(temp.endsWith(".axml")){
							/*MessageBox messageBox = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_WARNING);
							messageBox.setText("Warning");
							messageBox.setMessage("Cannot view call graph for module, please select a child function.");
							messageBox.open();*/
							return;
						}
					}
					String path = "";
					IPath location = null;

					if (firstElement instanceof File || ((NodeProxy) firstElement).node == null) {
						TreePath treep = treeSel.getPaths()[0];
						path = ((File) treep.getSegment(1)).getLocation().toPortableString().replace("L/", "");
					} else {
						path = ((NodeProxy) firstElement).node.getBaseURI();
						String stuff = treeSel.getFirstElement().toString();

						if (stuff.indexOf("[module: ") < 0) {
							TreePath[] paths = treeSel.getPaths();
							TreePath sel = paths[0];
							String passPath = "";
							for (int i = 0; i < sel.getSegmentCount(); i++) {
								if (sel.getSegment(i).toString().startsWith("L/")) {
									passPath = sel.getSegment(i).toString().replace("L/", "");
									break;
								}
							}

							path = path.replace("file:/", "");
							path = path.replace("%20", " ");
							// path = path + "$" + funcName;

						}
					}
					IFile newIFile = null;

					// if(selection.size() == 1) {

					// TreeSelection treeSel = (TreeSelection) selection;

					location = new Path(path);
					IWorkspace ws = ResourcesPlugin.getWorkspace();
					IFile test = ws.getRoot().getFileForLocation(location);

					if (test != null && !test.isLinked()) {
						newIFile = test;

						if(newIFile.getFullPath().toString().endsWith(".trace")){
							try {
								TraceEditor editor = (TraceEditor) IDE.openEditor(Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage(), newIFile,
										"org.eclipse.zest.custom.sequence.assembly.editors.Trace");
								Activator.getDefault().traceEditors.add(editor);
								editor.setRoot(funcName, module, externalfile, "0");
							} catch (PartInitException e) {
								e.printStackTrace();
							}
						}
					} else {
						// path = path + "$" + passPath;
						if(path.toString().endsWith(".trace")){
							IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(path));
	
							IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
							try {
								TraceEditor editor = (TraceEditor) page.openEditor(new FileStoreEditorInput(fileStore), "org.eclipse.zest.custom.sequence.assembly.editors.Trace");
								Activator.getDefault().traceEditors.add(editor);
								editor.setRoot(funcName, module, externalfile, "0");
							} catch (PartInitException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				} else {
					/*MessageBox messageBox = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_WARNING);
					messageBox.setText("Warning");
					messageBox.setMessage("Cannot view call graph for module, please select a child function.");
					messageBox.open();*/
				}
				// }
				// }
			}
		}
	}
}
