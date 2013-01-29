package cs.uvic.ca.ice.model;

import java.util.ArrayList;
import java.util.Collection;

public class Instruction {
	/* Address of this instruction
	 */
	private Long address;

	/* Addressing of first instruction in containing function.
	 */
	private Long containing;

	/* The "flow" of this instruction.
	 * 0 - Normal (fall through)
	 * 1 - Jump
	 * 2 - Call
	 */
	private Integer flowType;

	/* List of instructions that can be
	 * reached from here.
	 */
	private ArrayList<Long> next;

	public Instruction(Long addr) {
		this.address = addr;
		this.next = new ArrayList<Long>();
	}
	
	public String toString() {
		Long x = new Long(0);
		return ("0x" + x.toHexString(this.address));
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
		System.out.println("Flow: " + this.flowType);
		System.out.println("Next: " + this.next.size());
	}

	public boolean isCall() {
		return (this.flowType == 2 ? true : false);
	}
}
