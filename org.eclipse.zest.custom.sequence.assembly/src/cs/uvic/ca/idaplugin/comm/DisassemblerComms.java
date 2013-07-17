package cs.uvic.ca.idaplugin.comm;

import java.util.Observable;

/**
 * Main class for IDA pro communication. 
 * Used for all communication type (socket, jni, ...)
 */
public abstract class DisassemblerComms extends Observable implements Runnable {
	protected static final String OK = "OK";
	protected static final String BYE = "bye";
	
	// Name of the java application. (Used in console messages)
	protected String appName = "";
	
	public abstract void send(String message);

    public abstract void run();

	/**
	 * Subclasses should override if required
	 */
	public abstract void close();
	
    /**
     * Process objects from the client
     * @param object
     * @return
     */
	public String processInput(Object object) {
		if (object != null) {
			String input = (String)object;
			System.out.println(input);
            this.setChanged();
    		this.notifyObservers(input);
		}
		return OK; // ack
	}
}