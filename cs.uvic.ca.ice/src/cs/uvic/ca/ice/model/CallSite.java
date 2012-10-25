package cs.uvic.ca.ice.model;

public class CallSite {
	private Integer callee;			/* starting address of calling function */
	private Integer from;			/* address call occurred at */
	private Integer to;				/* address being called */
	private Function call_target;	/* function being called */
	
	public CallSite(Integer from, Integer to, Function tar) {
		this.from = from;
		this.to = to;
		this.call_target = tar;
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
		return this.call_target;
	}
	
	public void setTarget(Function f) {
		this.call_target = f;
	}
}
