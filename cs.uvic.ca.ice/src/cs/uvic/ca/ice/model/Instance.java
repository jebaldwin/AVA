package cs.uvic.ca.ice.model;

/* File: Instance.java
 * 
 * Represents a source of data (IDA, Immunity, Olly) connected through a socket. 
 * The Instance class tries to encapsulate pertinent bits of information about the
 * data.
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;

import com.google.gson.Gson;

import cs.uvic.ca.ice.bridge.Message;

public class Instance {
	
	/* Integer id 
	 * 
	 * The process ID of the instnace */
	private Integer id;
	
	/* String name
	 * 
	 * Textual representation of the primary object
	 * open in the instance.
	 */
	private String name;
	
	/* HashMap<Address, Function>
	 * 
	 * Map of addresses to functions.
	 */
	private HashMap<Integer, Function> functions;
	
	/* Socket commSock
	 * 
	 * Socket to the instance for communication. 
	 */
	private Socket commSock;
	
	private final static String AT_FUNCTIONS = "functions";
	private final static String AT_CALLS = "calls";
	
	public Instance(Message m) {
		
		this.id = m.instanceId();
		this.name = m.origin();
		this.commSock = m.socket();
		this.functions = new HashMap<Integer, Function>();
	}
	
	public void update(Message m) {
		//System.out.println("message type: " + m.actionType());
		//System.out.println("data: " + m.data());
		
		Gson gson = new Gson();
		if(m.actionType().equals(AT_FUNCTIONS)) {
			Function f = gson.fromJson(m.data(), Function.class);
			this.functions.put(f.getStart(), f);
		} else if(m.actionType().equals(AT_CALLS)) {
			CallSite cs = gson.fromJson(m.data(), CallSite.class);
			Function callee = this.functions.get(cs.callee());
			Function caller = this.functions.get(cs.to());
			
			if(caller != null)
				cs.setTarget(caller);
			if(callee != null) {
				try {
					callee.insertCallSite(cs);
				} catch (InvalidCallSiteException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public Integer getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public Collection<Function> getFunctions() {
		return this.functions.values();
	}
	
	public void requestCalls(Integer fstart, Integer fend) {
		Message m = new Message("ICE");
		m.setAction("request");
		m.setActionType("calls");
		m.setData(fstart + ":" + fend);
		
		Gson gson = new Gson();
		
		sendRequest(gson.toJson(m));
	}
	
	public void requestFunctions() {
		Message m = new Message("ICE");
		m.setAction("request");
		m.setActionType("functions");
		
		Gson gson = new Gson();
		
		sendRequest(gson.toJson(m));
	}
	
	private void sendRequest(String req) {
		PrintWriter conn;
				
		try {
			System.out.println("sending: (" + req.length() + ") :: " + req);
			conn = new PrintWriter(this.commSock.getOutputStream(), true);
			conn.printf(req);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
