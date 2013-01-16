package cs.uvic.ca.ice.model;

import java.util.Collection;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.gson.Gson;

import cs.uvic.ca.ice.bridge.CommCenter;
import cs.uvic.ca.ice.bridge.Message;

public class InstanceMap extends Observable implements Observer {
	private static final InstanceMap iceMap = new InstanceMap();
	
	private boolean isReady;
	
	private ConcurrentHashMap<Long, Instance> imap;
	private CommCenter comms;
	
	private InstanceMap() {
		this.imap = new ConcurrentHashMap<Long, Instance>(10);
		this.comms = CommCenter.getCommCenter();
		this.isReady = false;
		
		//System.out.println("ctor imap: " + this.imap);
		//System.out.println("ctor size: " + this.imap.size());
		
		this.comms.addObserver(this);
	}

	public static InstanceMap getModel() {
		return iceMap;
	}
	
	public Object[] getArrayOfInstances() {
		if(this.imap.isEmpty())
			return null;
		
		Object[] instanceArray = this.imap.values().toArray();
		
		return instanceArray;
	}
	
	public Instance getInstanceById(Long instance_id) {
		return this.imap.get(instance_id);
	}
	
	public Instance getFirstInstance() {
		if(this.imap.isEmpty())
			return null;
		
		Instance ins = this.imap.elements().nextElement();
		System.out.println("first instance: " + ins);
		return ins;
	}
	
	public void update(Observable o, Object arg) {
		ConcurrentLinkedQueue<Message> msgQ = (ConcurrentLinkedQueue<Message>)arg;
		Message m = null;
		Instance ins = null;
		
		m = msgQ.poll();

		if(m.actionType() != null && m.actionType().equals("sync")) {
			System.out.println("--- received sync message ---");
			ins = this.imap.get(m.instanceId());
			this.isReady = true;
			setChanged();
			notifyObservers(ins);
		}
		
		if(m.action() != null && m.action().equals("sync") &&
			m.actionType() != null && m.actionType().equals("instructions"))
		{
			ins = this.imap.get(m.instanceId());
			Long addr = new Long(m.data());
			Function f = ins.getFunctionByAddress(addr);
			
			setChanged();
			notifyObservers(f);
		}
		
		if(this.imap.containsKey(m.instanceId()) == true) {
			// Key exists
			ins = this.imap.get(m.instanceId());
			ins.update(m);
		} else {
			// Key does not exist
			ins = new Instance(m);
			this.imap.put(m.instanceId(), ins);
			ins.requestFunctions();
		}
	}

	public boolean ready() {
		return this.isReady;
	}

	public Collection<Instance> getInstances() {
		return this.imap.values();
	}
}
