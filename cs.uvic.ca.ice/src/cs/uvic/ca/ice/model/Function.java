package cs.uvic.ca.ice.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class Function {
	private boolean entryPoint;			/* true if the function is an entry point (exported), false otherwise */
	private String name;				/* name of the function */
	private Long start;				/* start address of the function */
	private Long end;				/* end address of the function */
	private Long size;				/* size (in bytes) of the function */
	private String module;				/* name of the binary containing this function */
	private Long index;				/* numerical index of this function */
	private String comment;
	private ArrayList<CallSite> calls;	/* list of all calls made by this function */
	private HashMap<Long, Instruction> instructions;
	
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
		this.instructions = new HashMap<Long, Instruction>();
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
	
	public void setIndex(Long i) {
		this.index = i;
	}
	
	public Long getIndex() {
		return this.index;
	}
	
	public void setEntryPoint(boolean isEntry) {
		this.entryPoint = isEntry;
	}
	
	public boolean getEntryPoint() {
		return this.entryPoint;
	}
	
	public void setStart(Long addr) {
		this.start = addr;
	}
	
	public Long getStart() {
		return this.start;
	}
	
	public void setEnd(Long addr) {
		this.end = addr;
	}
	
	public Long getEnd() {
		return this.end;
	}
	
	public void setSize(Long size) {
		this.size = size;
	}
	
	public Long getSize() {
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

	public void insertInstruction(Instruction instr) {
		System.out.println("Insert: " + instr);
		this.instructions.put(instr.getAddress(), instr);
	}

	public Instruction getInstruction(Long addr) {
		return this.instructions.get(addr);
	}
	
	public Collection<Instruction> getInstructions() {
		return this.instructions.values();
	}
}
