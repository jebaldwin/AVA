package cs.uvic.ca.ice.model;

import java.util.Collection;
import java.util.Iterator;

public class CallSite {
	private Integer callee;			/* starting address of calling function */
	private Integer from;			/* address call occurred at */
	private Integer to;				/* address being called */
	private Function call_target;	/* function being called */
	private Integer instance_id;
	
	public CallSite(Integer from, Integer to, Function tar) {
		this.from = from;
		this.to = to;
		this.call_target = tar;
	}
	
	public Integer getInstanceId() {
		return this.instance_id;
	}
	
	public void setInstanceId(Integer id) {
		this.instance_id = id;
	}
	
	public Integer callee() {
		return this.callee;
	}
	
	public Integer from() {
		return this.from;
	}
	
	public Integer to() {
		return this.to;
	}
	
	public Function target() {
		if(this.call_target != null)
			return this.call_target;
		
		/* look up the function since we dont' know it yet */
		Instance ins = InstanceMap.getModel().getInstanceById(this.instance_id);
		Function f = ins.getFunctionByAddress(this.callee);
		
		this.call_target = f;
		return this.call_target;
	}
	
	public void setTarget(Function f) {
		this.call_target = f;
	}
}
