package org.eclipse.ui.examples.navigator.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.zest.custom.sequence.widgets.Activation;
import org.eclipse.zest.custom.sequence.widgets.Call;
import org.eclipse.zest.custom.sequence.widgets.Message;
import org.eclipse.zest.custom.uml.viewers.UMLSequenceViewer;

public class ExpandAllActivationsAction extends Action {

	private Activation activation;
	private UMLSequenceViewer viewer;
	
	public ExpandAllActivationsAction(UMLSequenceViewer viewer) {
		this.viewer = viewer;
	}

	@Override
	public void run() {
		if (activation == null) {
			return;
		}
		//final ProgressMonitorDialog dialog = new ProgressMonitorDialog(null);//viewer.getChart().getShell());//activation.getChart().getShell());
		//dialog.getShell().setText("Expanding Activations");
			
		//try {
			//dialog.run(false, true, new IRunnableWithProgress() {
			viewer.getChart().getDisplay().asyncExec(new Runnable(){
				/*public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					LinkedList<Activation> activations = new LinkedList<Activation>();
					monitor.beginTask("Expanding Activations", IProgressMonitor.UNKNOWN);
					int total = 0;
					activations.add(activation);
					try {
						activation.getChart().setRedraw(false);
						while (activations.size() > 0) {
							Activation a = activations.remove();
							if (!a.isDisposed()) {
								monitor.subTask(a.getText());
								a.setExpanded(true);
								total++;
								if (total > 1000) {
									if (!MessageDialog.openQuestion(
													dialog.getShell(),
													"Large Sequence Diagram",
													"You are trying to open a" +
													" large sequence diagram," +
													" which may degrade system" +
													" performance. Are you sure" +
													" you want to continue?")) {
										throw new InterruptedException();
									} else {
										total = 0;
									}
								}
								if (monitor.isCanceled()) {
									throw new InterruptedException();
								}
								for (Message m : a.getMessages()) {
									if (m instanceof Call && !m.isDisposed()) {
										activations.add(m.getTarget());
									}
								}
							}
							activation.getDisplay().readAndDispatch();
						}
					} finally {
						activation.getChart().setRedraw(true);
					}
				}*/

				public void run() {
					NullProgressMonitor monitor = new NullProgressMonitor();
					LinkedList<Activation> activations = new LinkedList<Activation>();
					monitor.beginTask("Expanding Activations", IProgressMonitor.UNKNOWN);
					int total = 0;
					activations.add(activation);
					try {
						activation.getChart().setRedraw(false);
						while (activations.size() > 0) {
							Activation a = activations.remove();
							if (!a.isDisposed()) {
								monitor.subTask(a.getText());
								a.setExpanded(true);
								total++;
								if (total > 1000) {
									if (!MessageDialog.openQuestion(
													viewer.getChart().getShell(),
													"Large Sequence Diagram",
													"You are trying to open a" +
													" large sequence diagram," +
													" which may degrade system" +
													" performance. Are you sure" +
													" you want to continue?")) {
										throw new InterruptedException();
									} else {
										total = 0;
									}
								}
								if (monitor.isCanceled()) {
									throw new InterruptedException();
								}
								for (Message m : a.getMessages()) {
									if (m instanceof Call && !m.isDisposed()) {
										activations.add(m.getTarget());
									}
								}
							}
							activation.getDisplay().readAndDispatch();
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						activation.getChart().setRedraw(true);
					}
				}

			});
		//} catch (InvocationTargetException e) {
		//} catch (InterruptedException e) {
		//}
	}

	public void setFocusActivation(Activation a) {
		this.activation = a;
	}

}
