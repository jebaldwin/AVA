package cs.uvic.ca.ice.bridge;

import java.net.Socket;

/* This class (Message) represents an incoming message from a connected
 * instance.  The object is created by translating directly from the JSON
 * format.  The JSON format of a message is:
 * 
 *  {
 *  	"instance_id" : <Process ID of sender>
 *  	"origin" : <textual representation of the origin>
 *  	"action" : <An action describing the type of message>
 *  	"data" : <JSON string containing message specific content>
 *  	"type" : <type of action>
 *  }
 */

public class Message {
	private Long instance_id;
	private String origin;
	private String action;
	private String data;
	private Socket commSock;
	private String actionType;
	
	public Message(char[] msg_buf) {
		this.origin = new String(msg_buf);
	}
	
	public Message(String string) {
		this.origin = string;
	}

	public String origin() {
		return this.origin;
	}
	
	public Long instanceId() {
		return this.instance_id;
	}

	public String data() {
		return this.data;
	}
	
	public void setData(String d) {
		this.data = d;
	}
	
	public void setSocket(Socket socket) {
		this.commSock = socket;
	}
	
	public Socket socket() {
		return this.commSock;
	}
	
	public void setAction(String act) {
		this.action = act;
	}
	
	public String actionType() {
		return this.actionType;
	}
	
	public void setActionType(String at) {
		this.actionType = at;
	}

	public void print() {
		System.out.println("\nMessage: " + this);
		System.out.println("\tinstance_id: " + this.instance_id);
		System.out.println("\torigin: " + this.origin);
		System.out.println("\taction: " + this.action);
		System.out.println("\taction type: " + this.actionType);
		System.out.println("\tdata: " + this.data);
		System.out.println("\tsocket: " + this.commSock + "\n");
	}
}
