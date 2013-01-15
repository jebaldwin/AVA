package cs.uvic.ca.ice.model;

import java.util.ArrayList;
import java.util.Collection;

public class Instruction {
	private Long address;
	private Long containing;
	private ArrayList<Long> next;

	public Instruction(Long addr) {
		this.address = addr;
		this.next = new ArrayList<Long>();
	}
	
	public Long getAddress() {
		return this.address;
	}
	
	public Collection<Long> getNextAddresses() {
		return this.next;
	}
	
	public void insertNextAddress(Long address) {
		this.next.add(address);
	}

	public void setContaining(Long addr) {
		this.containing = addr;
	}
	
	public Long getContaining() {
		return this.containing;
	}
	
	public void print() {
		System.out.println(this);
		System.out.println("Address: " + this.address);
		System.out.println("Containing: " + this.containing);
		System.out.println("Next: " + this.next.size());
	}
}
