package cs.uvic.ca.ice.bridge;

/* This class (Message) represents an incoming message from a connected
 * instance.  The object is created by translating directly from the JSON
 * format.  The JSON format of a message is:
 * 
 *  {
 *  	"instance_id" : <SHA1 hash of identifying feature>
 *  	"origin" : <textual representation of the origin>
 *  	"content" : <JSON string containing message specific content>
 *  }
 */

public class Message {
	private String instance_id;
	private String origin;
	private String content;
	
	public Message(char[] msg_buf) {
		this.origin = new String(msg_buf);
	}

	public String origin() {
		return this.origin;
	}
	
	public String instanceId() {
		return this.instance_id;
	}
}
