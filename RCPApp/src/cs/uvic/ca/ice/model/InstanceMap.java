package cs.uvic.ca.ice.model;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import cs.uvic.ca.ice.bridge.CommCenter;
import cs.uvic.ca.ice.bridge.Message;

public class InstanceMap extends Observable implements Observer {
	private static final InstanceMap iceMap = new InstanceMap();
	
	private ConcurrentHashMap<String, Instance> imap;
	private CommCenter comms;
	
	private InstanceMap() {
		this.imap = new ConcurrentHashMap<String, Instance>();
		this.comms = CommCenter.getCommCenter();
		
		this.comms.addObserver(this);
	}

	public static InstanceMap getModel() {
		return iceMap;
	}
	
	public void update(Observable o, Object arg) {
		ConcurrentLinkedQueue<Message> msgQ = (ConcurrentLinkedQueue<Message>)arg;
		Message m = null;
		
		m = msgQ.peek();
		
		Instance ins = null;
		if(this.imap.containsKey(m.instanceId())) {
			// Instance already exists, update it
			ins = new Instance(m);
			this.imap.put(m.instanceId(), ins);
		} else {
			// Instance does not exist, create it
			ins = this.imap.get(m.instanceId());
			ins.update(m);
		}

		setChanged();
		notifyObservers(ins);
	}
}
