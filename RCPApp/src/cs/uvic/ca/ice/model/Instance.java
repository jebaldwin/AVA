package cs.uvic.ca.ice.model;

import cs.uvic.ca.ice.bridge.Message;

public class Instance {
	private Integer id;
	private String name;
	private String data;
	
	public Instance(Message m) {
		this.id = m.instanceId();
		this.name = m.origin();
		this.data = null;
	}
	
	public void update(Message m) {
		
	}
	
	public Integer getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
}
