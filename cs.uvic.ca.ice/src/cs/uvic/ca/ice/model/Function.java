package cs.uvic.ca.ice.model;

import java.util.ArrayList;
import java.util.Collection;

public class Function {
	private boolean entryPoint;			/* true if the function is an entry point (exported), false otherwise */
	private String name;				/* name of the function */
	private Integer start;				/* start address of the function */
	private Integer end;				/* end address of the function */
	private Integer size;				/* size (in bytes) of the function */
	private String module;				/* name of the binary containing this function */
	private Integer index;				/* numerical index of this function */
	private String comment;
	private ArrayList<CallSite> calls;	/* list of all calls made by this function */
	
	public Function() {
		this.name = null;
		this.start = null;
		this.end = null;
		this.size = null;
		this.entryPoint = false;
		this.module = null;
		this.comment = null;
		this.index = null;
		this.calls = new ArrayList<CallSite>();
	}
	
	public boolean isExternal() {
		return (this.index == -1 ? true : false);
	}
	
	public void setName(String n) {
		this.name = n;
	}
	
	public String getComment() {
		return this.comment;
	}
	
	public void setComment(String cmt) {
		this.comment = cmt;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setModule(String m) {
		this.module = m;
	}
	
	public String getModule() {
		return this.module;
	}
	
	public void setIndex(Integer i) {
		this.index = i;
	}
	
	public Integer getIndex() {
		return this.index;
	}
	
	public void setEntryPoint(boolean isEntry) {
		this.entryPoint = isEntry;
	}
	
	public boolean getEntryPoint() {
		return this.entryPoint;
	}
	
	public void setStart(Integer addr) {
		this.start = addr;
	}
	
	public Integer getStart() {
		return this.start;
	}
	
	public void setEnd(Integer addr) {
		this.end = addr;
	}
	
	public Integer getEnd() {
		return this.end;
	}
	
	public void setSize(Integer size) {
		this.size = size;
	}
	
	public Integer getSize() {
		return (this.end - this.start);
	}
	
	public boolean makesCalls() {
		if(this.calls == null || this.calls.isEmpty())
			return false;
		
		return true;
	}
	
	public Collection<CallSite> getCalls() {
		return this.calls;
	}
	
	public boolean insertCallSite(CallSite cs) throws InvalidCallSiteException {
		if(this.start == null || this.end == null)
			throw new InvalidCallSiteException();

		if(cs.from() < this.start
			|| cs.from() > this.end)
			throw new InvalidCallSiteException();
		
		boolean ret = this.calls.add(cs);
		
		return ret;
	}
}
