package cs.uvic.ca.ice.bridge;

/* This class (Message) represents an incoming message from a connected
 * instance.  The object is created by translating directly from the JSON
 * format.  The JSON format of a message is:
 * 
 *  {
 *  	"instance_id" : <Process ID of sender>
 *  	"origin" : <textual representation of the origin>
 *  	"action" : <An action describing the type of message>
 *  	"data" : <JSON string containing message specific content>
 *  }
 */

public class Message {
	private Integer instance_id;
	private String origin;
	private String action;
	private String data;
	
	public Message(char[] msg_buf) {
		this.origin = new String(msg_buf);
	}

	public String origin() {
		return this.origin;
	}
	
	public Integer instanceId() {
		return this.instance_id;
	}
}
