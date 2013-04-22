package cs.uvic.ca.ice.model;

import java.util.Collection;
import java.util.Iterator;

public class CallSite {
	private Long callee;			/* starting address of calling function */
	private Long from;			/* address call occurred at */
	private Long to;				/* address being called */
	private Function call_target;	/* function being called */
	private Long instance_id;
	
	public CallSite(Long from, Long to, Function tar) {
		this.from = from;
		this.to = to;
		this.call_target = tar;
	}
	
	public Long getInstanceId() {
		return this.instance_id;
	}
	
	public void setInstanceId(Long id) {
		this.instance_id = id;
	}
	
	public Long callee() {
		return this.callee;
	}
	
	public Long from() {
		return this.from;
	}
	
	public Long to() {
		return this.to;
	}
	
	public Function target() {
		if(this.call_target != null)
			return this.call_target;
		
		/* look up the function since we dont' know it yet */
		Instance ins = InstanceMap.getModel().getInstanceById(this.instance_id);
		Function f = ins.getFunctionByAddress(this.to);
		
		System.out.println("** target not assigned");
		System.out.println("\tto: " + this.to);
		System.out.println("\tfrom: " + this.from);
		System.out.println("\tcallee: " + this.callee);
		System.out.println("\tfound: " + f);
		if(f != null) {
			System.out.println("\t\tname: " + f.getName());
			System.out.println("\t\tstart: " + f.getStart());
		} else {
			System.out.println("\t\t--> FAILED TO FIND FUNCTION <--");
		}
		
		this.call_target = f;
		return this.call_target;
	}
	
	public void setTarget(Function f) {
		this.call_target = f;
	}
}
