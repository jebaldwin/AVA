package cs.uvic.ca.ice.model;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import cs.uvic.ca.ice.bridge.CommCenter;
import cs.uvic.ca.ice.bridge.Message;

public class InstanceMap extends Observable implements Observer {
	private static final InstanceMap iceMap = new InstanceMap();
	
	private ConcurrentHashMap<Integer, Instance> imap;
	private CommCenter comms;
	
	private InstanceMap() {
		this.imap = new ConcurrentHashMap<Integer, Instance>(10);
		this.comms = CommCenter.getCommCenter();
		
		System.out.println("ctor imap: " + this.imap);
		System.out.println("ctor size: " + this.imap.size());
		
		this.comms.addObserver(this);
	}

	public static InstanceMap getModel() {
		return iceMap;
	}
	
	public Instance getInstanceById(Integer id) {
		return this.imap.get(id);
	}
	
	public void update(Observable o, Object arg) {
		ConcurrentLinkedQueue<Message> msgQ = (ConcurrentLinkedQueue<Message>)arg;
		Message m = null;
		
		m = msgQ.peek();
		
		System.out.println("Observable o: " + o);
		System.out.println("this.imap: " + this.imap);
		
		System.out.println("Message: " + m);
		System.out.println("Message instanceId:" + m.instanceId());
		
		Instance ins = null;
		if(this.imap.containsKey(m.instanceId()) == true) {
			// Key exists
			System.out.println("Key exists");
			ins = this.imap.get(m.instanceId());
			ins.update(m);
		} else {
			// Key does not exist
			System.out.println("Creating key");
			ins = new Instance(m);
			this.imap.put(m.instanceId(), ins);
		}

		setChanged();
		notifyObservers(ins);
	}
}
