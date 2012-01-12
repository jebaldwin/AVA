package org.eclipse.zest.custom.statediagram.assembly.views;

import org.eclipse.zest.custom.sequence.assembly.Startup;

public class MyAction {

	public String address;
	public String command;
	public String exeID;
	
	public MyAction(String address, String command, String exeID) {
		this.address = address;
		this.command = command;
		this.exeID = exeID;
	}
	
	public void execute(){
		System.out.println("execute actions");
		System.out.println(command);
		System.out.println(address);
		
		//send message to idapro
		Startup.send(exeID, "executeAction " + command + " " + address);
	}
}
