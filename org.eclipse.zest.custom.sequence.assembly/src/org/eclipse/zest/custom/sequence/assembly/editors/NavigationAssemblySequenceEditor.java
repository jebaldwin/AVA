package org.eclipse.zest.custom.sequence.assembly.editors;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.zest.custom.sequence.assembly.Activator;
import org.eclipse.zest.custom.sequence.assembly.Startup;
import org.eclipse.zest.custom.sequence.assembly.editors.DynamicAssemblySequenceEditor.DynamicCallProxy;
import org.eclipse.zest.custom.sequence.assembly.preferences.PreferenceConstants;
import org.eclipse.zest.custom.sequence.widgets.Activation;
import org.eclipse.zest.custom.sequence.widgets.Call;
import org.eclipse.zest.custom.sequence.widgets.Lifeline;
import org.eclipse.zest.custom.sequence.widgets.SequenceChartBuilder;

public class NavigationAssemblySequenceEditor extends DynamicAssemblySequenceEditor {

    protected static String prefixString = "history";
    
	public void dispose() {
		System.out.println("dispose!");
		Activator.getDefault().navEditor = null;
		getViewer().removeSequenceListener(sequenceListener);
		breadcrumb.removeSelectionChangedListener(breadcrumbListener);
		//super.dispose();
	}
	
	@Override
	public void doSave(IProgressMonitor arg0) {
		
		display.syncExec(
			  new Runnable() {
				    public void run(){
					    Shell s = new Shell(display);
						Calendar cal = Calendar.getInstance();
					    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
					    String timestamp =  sdf.format(cal.getTime());
					    
					    s.setSize(400, 400);
						FileDialog fd = new FileDialog(s, SWT.SAVE);
				        fd.setText("Save");
				        fd.setFilterPath(Activator.getDefault().navEditor.saveLocation);
				        String[] filterExt = { "*.trace" };
				        fd.setFilterExtensions(filterExt);
				        fd.setFileName(prefixString + "-" + timestamp);
				        String selected = fd.open();
				        
				        if(selected != null){
				        	save(selected);
				        }
				    }
			  });
	}
	
	public void setPartName(String name){
		//need to add sterotype user, and then call to this function
		//super.setPartName(name);
		this.callList.clear();
		this.functionList.clear();
		this.functionEntryList.clear();
		
		FunctionEntryPointProxy fepp = new FunctionEntryPointProxy("", "", "0", "");
		functionEntryList.put("", fepp);
		
		//String name, String address, String index, String stereotype
		currentNodeProxy = new DynamicNodeProxy("User", "", Integer.toString(++index), "", "", true, "");
		functionList.put("User", currentNodeProxy);
		methodToExpand = "User";
		builder = new SequenceChartBuilder(viewer.getChart(), methodToExpand);
		//viewer.getChart().getRootActivation().getLifeline().setStereotype("actor");
		//viewer.getChart().getRootActivation().getLifeline().setImage(localimage); 
		viewer.getChart().getRootActivation().getLifeline().setClassStyle(Lifeline.ACTOR);
		Lifeline user = builder.setContainer(viewer.getChart().getRootActivation().getLifeline().getText(), "");
		user.setClassStyle(Lifeline.ACTOR);
		
		if(name != null && name.length() > 0){
			String input = this.inputFile.getName().replace(".naxml", "");
			DynamicCallProxy dcp = new DynamicCallProxy(name, "", input, "", "", input, false, true);
	    	String currName = "";
	    	if(currentNodeProxy != null){
	    		currName = currentNodeProxy.name;
	    	}
	    	callList.put(currName + name, dcp);
	    	
	    	//String name, String address, String index, String stereotype
			currentNodeProxy = new DynamicNodeProxy(name, "", Integer.toString(++index), "", "", true, "");
			functionList.put(name, currentNodeProxy);
			methodToExpand = name;
	
	    	Call call = getBuilder().makeCall(name, name);
	    	Activation[] acts = call.getTarget().getLifeline().getActivations();
	    	dcp.act = Integer.toString(acts.length);
			call.getSource().setExpanded(true);
			
			Lifeline pack = getBuilder().setContainer(call.getTarget().getLifeline().getText(), input);
			pack.setBackground(new Color(Display.getCurrent(), 255, 200, 100));
			call.getTarget().getLifeline().setImage(AssemblySequenceLabelProvider.localimage);
			getBuilder().setContext(call.getTarget());
			
			orderedCallList.add(dcp);
		}
		//getBuilder().setContext(call.getTarget());		

//		builder = new SequenceChartBuilder(viewer.getChart(), methodToExpand);
		builder.turnOnRedraw();
		//> 0:	1001630	sub_1001630
		//receiveMessage("debug\n> 0:	1001630	sub_1001630");
		//receiveMessage("debug\n> 0:	1001630	sub_1001s");
		//receiveMessage("debug\n> 0:	1001630	sub_1001630");
		//receiveMessage("debug\nadd to root\n> 0:	1001630	sub_jb \n-1:	100164A	100111C	stuff	stuff.dll \n< 0");
		//receiveMessage("debug\n> 0:	1001630	sub_1001630 USER32.dll");
	}
	
	protected void expandCall(String firstLine, Call call) {
		call.getSource().setExpanded(true);
	}
}
